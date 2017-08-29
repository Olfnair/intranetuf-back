/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package rest;

import dao.DAOVersion;
import entities.File;
import entities.Project;
import entities.ProjectRight;
import entities.User;
import entities.Version;
import entities.WorkflowCheck;
import entities.query.FlexQuery;
import entities.query.FlexQueryResult;
import files.Upload;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
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
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.cxf.jaxrs.ext.MessageContext;
import rest.objects.RestLong;
import rest.security.AuthToken;
import rest.security.Authentication;
import rest.security.RightsChecker;
import utils.Base64Url;

/**
 *
 * @author Florian
 */
@Stateless
@Path("entities.file")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class FileFacadeREST extends AbstractFacade<File> {
    
    @PersistenceContext(unitName = "IUFPU")
    private EntityManager em;
    
    public FileFacadeREST() {
        super(File.class);
    }
    
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response create(
            @Context MessageContext jaxrsContext,
            @Multipart("entity") File entity,
            @Multipart("file") InputStream uploadedInputStream,
            @Multipart("file") Attachment attachment) {
        // TODO : check extension
        // TODO : check file_size et décider d'un max
        AuthToken token = Authentication.validate(jaxrsContext);
        Project project = em.find(Project.class, entity.getProject().getId());
        
        User author;
        // check droits
        try { // seulement si l'utilisateur peut ajouter des fichiers à ce project
            author = RightsChecker.getInstance(em).validate(token, User.Roles.USER, project.getId(), ProjectRight.Rights.ADDFILES);
        }
        catch(WebApplicationException e) { // ou qu'il est admin
            author = RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        }
        
        try {
            Version version = entity.getVersion();
            version.setFile(entity);
            version.setDate_upload(Instant.now().getEpochSecond());
            new DAOVersion(version, em).initWorkflowChecks();
            entity.setAuthor(author);
            em.persist(entity);
            new Upload(uploadedInputStream, project.getId().toString(), version.getId().toString()).run();
            return Response.status(201).build();
        }
        catch(Exception e) {
            throw new WebApplicationException(Response.status(500).build());
        }
    }
    
    @PUT
    @Path("{id}")
    public Response edit(@PathParam("id") Long id, File entity) {
        return super.edit(entity);
    }
    
    @DELETE
    @Path("{id}")
    public Response edit(@Context MessageContext jaxrsContext, @PathParam("id") Long id) {
        AuthToken token = Authentication.validate(jaxrsContext);
        
        TypedQuery<Project> projectQuery = em.createNamedQuery("File.getProject", Project.class);
        projectQuery.setParameter("fileId", id);
        List<Project> projectResult = projectQuery.getResultList();
        if(projectResult == null || projectResult.size() < 1) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        Project project = projectResult.get(0);
        
        // vérifie que l'utilisateur a le droit de supprimer un fichier ou est admin
        try {
            RightsChecker.getInstance(em).validate(token, User.Roles.USER, project.getId(), ProjectRight.Rights.DELETEFILES);
        }
        catch(WebApplicationException e) {
            RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        }
        
        File file = em.find(File.class, id);
        file.setActive(false);
        return Response.status(Response.Status.OK).build();
    }
    
    @GET
    @Path("{id}")
    public Response find(@PathParam("id") Long id) {
        return super.find(id);
    }
    
    @GET
    @Path("{fileId}/isauthor/{userId}")
    public Response find(
            @Context MessageContext jaxrsContext,
            @PathParam("fileId") Long fileId,
            @PathParam("userId") Long userId
    ) {     
        try {
            AuthToken token = Authentication.validate(jaxrsContext);
            if(token.getUserId() != userId) {
                RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
            }
        } catch(WebApplicationException e) {
            return Response.ok(new RestLong(0)).build();
        }
        User user = em.find(User.class, userId);
        File file = em.find(File.class, fileId);
        if(user == null || file == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        boolean userIsAuthor = (user.getId().longValue() == file.getAuthor().getId().longValue());
        return Response.ok(new RestLong(userIsAuthor ? 1 : 0)).build();
    }
    
    @GET
    @Path("/project/{id}/query/{whereParams}/{orderbyParams}/{index}/{limit}")
    public Response findByProject(@Context MessageContext jaxrsContext, @PathParam("id") Long id,
            @PathParam("whereParams") String whereParams, @PathParam("orderbyParams") String orderbyParams,
            @PathParam("index") Integer index, @PathParam("limit") Integer limit) {
        AuthToken token = Authentication.validate(jaxrsContext);
        
        
        // droits
        User user;
        try { // voir le projet
            user = RightsChecker.getInstance(em).validate(token, User.Roles.USER, id, ProjectRight.Rights.VIEWPROJECT);
        }
        catch(WebApplicationException e) { // ou role admin
            user = RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        }
        
        FlexQuery<File> filesQuery = new FlexQuery<>(File.LIST_BY_PROJECT);
        try {
            filesQuery.setParameters(
                    Base64Url.decode(whereParams) + "col: 'project.id', param: '" + id + "'",
                    Base64Url.decode(orderbyParams),
                    index, limit
            );
        } catch (UnsupportedEncodingException ex) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        if(! user.isAdmin()) {
            TypedQuery<Long> availableIdsQuery = em.createNamedQuery("File.getAvailableByProject", Long.class);
            availableIdsQuery.setParameter("projectId", id);
            availableIdsQuery.setParameter("userId", user.getId());
            availableIdsQuery.setParameter("versionStatus", Version.Status.VALIDATED);
            List<Long> availableIdsList = availableIdsQuery.getResultList();
        
            TypedQuery<Long> toCheckIdsQuery = em.createNamedQuery("File.getForCheckersByProject", Long.class);
            toCheckIdsQuery.setParameter("userId", user.getId());
            toCheckIdsQuery.setParameter("projectId", id);
            toCheckIdsQuery.setParameter("checkStatus", WorkflowCheck.Status.TO_CHECK);
            List<Long> toCheckIdsList = toCheckIdsQuery.getResultList();
        
            List<Long> idsList = new ArrayList<>(availableIdsList.size() + toCheckIdsList.size());
            idsList.addAll(availableIdsList);
            idsList.addAll(toCheckIdsList);
            
            if(! idsList.isEmpty()) {
                filesQuery.addWhereClause("id", idsList);
            }
        }
        
        filesQuery.prepareCountQuery(em);
        
        FlexQueryResult<File> files = filesQuery.execute();
        return Response.ok(files).build();
    }
    
    @GET
    @Override
    public Response findAll() {
        return super.findAll();
    }
    
    @GET
    @Path("{from}/{to}")
    public Response findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }
    
    @GET
    @Path("count")
    public Response countREST() {
        return super.count();
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
