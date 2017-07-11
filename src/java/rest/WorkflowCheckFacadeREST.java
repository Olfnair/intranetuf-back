/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import entities.File;
import entities.Project;
import entities.ProjectRight;
import entities.User;
import entities.Version;
import entities.WorkflowCheck;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.ext.MessageContext;
import rest.objects.RestLong;
import rest.security.AuthToken;
import rest.security.Authentication;
import rest.security.RightsChecker;

/**
 *
 * @author Florian
 */
@Stateless
@Path("entities.workflowcheck")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class WorkflowCheckFacadeREST extends AbstractFacade<WorkflowCheck> {

    @PersistenceContext(unitName = "IUFPU")
    private EntityManager em;

    public WorkflowCheckFacadeREST() {
        super(WorkflowCheck.class);
    }
    
    private Project getProjectFromVersion(Long versionId) {
        javax.persistence.Query filesQuery = em.createNamedQuery("File.byVersion");
        filesQuery.setParameter("versionId", versionId);
        List<File> fileList = filesQuery.getResultList();
        if(fileList == null || fileList.size() < 1) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        File file = fileList.get(0);
        Project project = file.getProject();
        if(project == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return project;
    }
    
    @POST
    public Response create(@Context MessageContext jaxrsContext, List<WorkflowCheck> entities) {
        AuthToken token = Authentication.validate(jaxrsContext);
        
        if(entities == null || entities.size() < 1) {
            throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
        }
        Long versionId = entities.get(0).getVersion().getId();
        // check cohérence données
        for(WorkflowCheck check: entities) {
            if(check.getVersion().getId().longValue() != versionId.longValue()) {
                throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
            }
        }
        
        // récup du projet...
        Project project = this.getProjectFromVersion(versionId);
        
        // droits :
        try { // ajouter des fichiers au projet
            RightsChecker.getInstance(em).validate(token, User.Roles.USER, project.getId(), ProjectRight.Rights.ADDFILES);
        }
        catch(WebApplicationException e) { // ou admin
            RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        }
        
        entities.forEach((check) -> {
            em.persist(check);
        });
        return Response.status(Response.Status.CREATED).build();
    }
    
    @POST // on passe les id's de versions dans le corps de la requête pour être sur de ne pas être limité par la taille d'un GET
    @Path("{userId}/{status}")
    public Response getByStatusUserVersions(
        @Context MessageContext jaxrsContext,
        @PathParam("userId") Long userId,
        @PathParam("status") Integer status,
        List<RestLong> restLongVersionIds
    ) {
        AuthToken token = Authentication.validate(jaxrsContext);
        
        // check user = userId ou user = admin
        if(token.getUserId() != userId) {
            RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        }
        
        List<Long> versionIds = new ArrayList(restLongVersionIds.size());
        restLongVersionIds.forEach((restLongId) -> {
            versionIds.add(restLongId.getValue());
        });
        return super.buildResponseList(() -> {
            javax.persistence.Query wfcQuery = em.createNamedQuery("WorkflowCheck.getByStatusUserVersions");
            wfcQuery.setParameter("userId", userId);
            wfcQuery.setParameter("status", status);
            wfcQuery.setParameter("versionIds", versionIds);
            return wfcQuery.getResultList();
        });
    }

    @PUT
    @Path("{id}")
    public Response edit(@Context MessageContext jaxrsContext, @PathParam("id") Long id, WorkflowCheck entity) {
        AuthToken token = Authentication.validate(jaxrsContext);
        
        javax.persistence.Query wfcQuery = em.createNamedQuery("WorkflowCheck.getWithUser");
        wfcQuery.setParameter("wfcId", id);
        List<WorkflowCheck> wfcResults = wfcQuery.getResultList();
        if(wfcResults == null || wfcResults.size() < 1) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        WorkflowCheck check = wfcResults.get(0);
        
        // droits :
        // check que l'utilisateur est bien celui qui peut modifier ce WorkflowCheck
        if(token.getUserId() != check.getUser().getId()) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        
        int status = entity.getStatus();
        if(status != WorkflowCheck.Status.CHECK_OK && status != WorkflowCheck.Status.CHECK_KO) {
            throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
        }
        
        Version version;
        try {
            javax.persistence.Query versionQuery = em.createNamedQuery("Version.getWithChecks");
            versionQuery.setParameter("versionId", check.getVersion().getId());
            List<Version> result = versionQuery.getResultList();
            version = result.get(0);
        }
        catch(Exception e) {
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        // bloquer l'update si la version a déjà le statut REFUSED
        if(version.getStatus() == Version.Status.REFUSED) {
            throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
        }
        
        // bloquer l'update si updateCheck n'est pas TO_CHECK ou qu'il existe un check TO_CHECK de type inférieur
        // ou un order_num inférieur qui n'est pas CHECK_OK
        for(WorkflowCheck c : version.getWorkflowChecks()) {
            if(c.getId().longValue() == id.longValue() && c.getStatus() != WorkflowCheck.Status.TO_CHECK) {
                throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
            }
            else if(c.getType() < check.getType() && c.getStatus() != WorkflowCheck.Status.CHECK_OK) {
                throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
            }
            else if(c.getType().intValue() == check.getType().intValue() && c.getOrder_num() < check.getOrder_num() && c.getStatus() != WorkflowCheck.Status.CHECK_OK) {
                throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
            }
        }
        
        // persister (on récupère le comment et le status, mais on ne touche pas au reste,
        // question d'intégrité (sinon on pourrait modifier user ou version..)
        check.setComment(entity.getComment());
        check.setStatus(status);
        version.updateStatus(check);
        
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") Long id) {
        return super.remove(id);
    }

    @GET
    @Path("{id}")
    public Response find(@PathParam("id") Long id) {
        return super.find(id);
    }

    @GET
    @Override
    public Response findAll() {
        return super.findAll();
    }
    
    @GET
    @Path("forVersion/{versionId}")
    public Response findforVersion(@Context MessageContext jaxrsContext, @PathParam("versionId") Long versionId) {
        AuthToken token = Authentication.validate(jaxrsContext);      
        Project project = this.getProjectFromVersion(versionId);       
        // droits :
        try {
            RightsChecker.getInstance(em).validate(token, User.Roles.USER, project.getId(), ProjectRight.Rights.VIEWPROJECT);
        }
        catch(WebApplicationException e) {
            RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        }
        return super.buildResponseList(() -> {
            javax.persistence.Query checksQuery = em.createNamedQuery("WorkflowCheck.getByVersion");
            checksQuery.setParameter("versionId", versionId);
            return checksQuery.getResultList();
        });
    }
      
    @GET
    @Path("forLastVersion/{fileId}")
    public Response findforFile(@Context MessageContext jaxrsContext, @PathParam("fileId") Long fileId) {
        AuthToken token = Authentication.validate(jaxrsContext);
        File file = em.find(File.class, fileId);
        if(file == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        // auth et droits vérifiés ici :
        return this.findforVersion(jaxrsContext, file.getVersion().getId());
    }
    
    @GET
    @Path("{from}/{to}")
    public Response findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public Response countREST() {
        return super.count();
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
