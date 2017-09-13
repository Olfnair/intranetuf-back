/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import dao.DAOVersion;
import dao.DAOWorkflowCheck;
import entities.File;
import entities.Project;
import entities.ProjectRight;
import entities.User;
import entities.Version;
import entities.WorkflowCheck;
import entities.query.FlexQuery;
import entities.query.FlexQueryResult;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
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
import utils.Base64Url;

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
        TypedQuery<File> filesQuery = em.createNamedQuery("File.byVersion", File.class);
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
    
    /**
     * Endpoint utilisé pour envoyer un mail de rappel à un contrôleur/valideur qui n'a pas encore vérifié un fichier
     * @param jaxrsContext contexte utilisé pour l'authentification
     * @param id id du WorkflowCheck pour lequel il faut envoyer un mail à la personne qui en est chargé
     * @return Statut HTTP 201 en cas de succès
     */
    @POST
    @Path("sendReminder/{id}")
    public Response sendReminder(@Context MessageContext jaxrsContext, @PathParam("id") Long id) {
        AuthToken token = Authentication.validate(jaxrsContext);
        User user = RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        
        TypedQuery<WorkflowCheck> checkQuery = em.createNamedQuery("WorkflowCheck.getWithUserAndVersion", WorkflowCheck.class);
        checkQuery.setParameter("wfcId", id);
        new DAOWorkflowCheck(checkQuery.getResultList().get(0), em).sendMail(true); // envoi d'un mail de rappel
        return Response.status(Response.Status.CREATED).build();
    }
    
    /**
     * Endpoint utilisé pour créer des WorkflowCheck
     * @param jaxrsContext contexte utilisé pour l'authentification
     * @param entities Liste des checks à créer
     * @return Statut HTTP 201 en cas de succès
     */
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
    
    /**
     * Endpoint utilisé pour récupérer les contrôles/validations liés à certaines versions (fichiers) par statut et utilisateur
     * (Utilisé pour savoir s'il y a un contrôle ou validation à effectuer sur un fichier par l'utilisateur courant dans le front)
     * @param jaxrsContext contexte utilisé pour l'authentification
     * @param userId id de l'utilisateur
     * @param status statut du check
     * @param restLongVersionIds Liste des id's des versions concernées
     * @return Liste des WorkflowChecks correspondants aux paramètres
     */
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
        
        List<Long> versionIds = new ArrayList<>(restLongVersionIds.size());
        restLongVersionIds.forEach((restLongId) -> {
            versionIds.add(restLongId.getValue());
        });
        
        return super.buildResponseList(() -> {
            TypedQuery<WorkflowCheck> wfcQuery = em.createNamedQuery("WorkflowCheck.getByStatusUserVersions", WorkflowCheck.class);
            wfcQuery.setParameter("userId", userId);
            wfcQuery.setParameter("status", status);
            wfcQuery.setParameter("versionIds", versionIds);
            return wfcQuery.getResultList();
        });
    }

    /**
     * Endpoint utilisé pour modifier un WorkflowCheck
     * @param jaxrsContext contexte utilisé pour l'authentification
     * @param id id du WorkflowCheck à modifier
     * @param entity WorkflowCheck modifié
     * @return Statut HTTP 200 en cas de succès
     */
    @PUT
    @Path("{id}")
    public Response edit(@Context MessageContext jaxrsContext, @PathParam("id") Long id, WorkflowCheck entity) {
        AuthToken token = Authentication.validate(jaxrsContext);
        
        TypedQuery<WorkflowCheck> wfcQuery = em.createNamedQuery("WorkflowCheck.getWithUser", WorkflowCheck.class);
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
            TypedQuery<Version> versionQuery = em.createNamedQuery("Version.getWithChecks", Version.class);
            versionQuery.setParameter("versionId", check.getVersion().getId());
            List<Version> result = versionQuery.getResultList();
            version = result.get(0);
        }
        catch(Exception e) {
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
        new DAOVersion(version, em).updateStatus(check);
        
        return Response.status(Response.Status.OK).build();
    }
    
    /**
     * Enpoint utilisé pour faire une recherche sur les WorkflowChecks d'un utilisateur
     * @param jaxrsContext contexte utilisé pour l'authentification
     * @param id id de l'utilisateur
     * @param whereParams paramètres WHERE de la recherche
     * @param orderbyParams paramètres ORDER BY de la recherche
     * @param index index à partir duqle on veut récupérer les résultats
     * @param limit nombre max de résultats à récupérer
     * @return Liste des WorkflowChecks correspondants à la recherche
     */
    @GET
    @Path("/user/{id}/query/{whereParams}/{orderbyParams}/{index}/{limit}")
    public Response findByUser(
            @Context MessageContext jaxrsContext, @PathParam("id") Long id,
            @PathParam("whereParams") String whereParams, @PathParam("orderbyParams") String orderbyParams,
            @PathParam("index") Integer index, @PathParam("limit") Integer limit
    ) {
        AuthToken token = Authentication.validate(jaxrsContext);
        
        if(id != token.getUserId()) {
            // garde : on ne peut lister que ses propres contrôles
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        
        FlexQuery<WorkflowCheck> wfcQuery = new FlexQuery<>(WorkflowCheck.LIST_BY_USER);
        try {
            wfcQuery.setParameters(
                    Base64Url.decode(whereParams) + "col: 'user.id', param: '" + id + "'",
                    Base64Url.decode(orderbyParams),
                    index, limit
            );
        } catch (UnsupportedEncodingException ex) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        wfcQuery.prepareCountQuery(em);
        
        FlexQueryResult<WorkflowCheck> wfcResults = wfcQuery.execute();
        return Response.ok(wfcResults).build();
    }
    
    /**
     * Endpoint utilisé pour récupérer les WorkflowChecks correspondants à une version (d'un fichier)
     * @param jaxrsContext contexte utilisé pour l'authentification
     * @param versionId id de la version
     * @return WorkflowChecks demandés
     */
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
            TypedQuery<WorkflowCheck> checksQuery = em.createNamedQuery("WorkflowCheck.getByVersion", WorkflowCheck.class);
            checksQuery.setParameter("versionId", versionId);
            return checksQuery.getResultList();
        });
    }
      
    /**
     * Enpoint qui permet de récupérer les WorkflowChecks pour la dernière version d'un fichier
     * @param jaxrsContext contexte utilisé pour l'authentification
     * @param fileId - id du fichier
     * @return Liste des WorkflowChecks correspondants à la dernière version du fichier donné en paramètre
     */
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

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
