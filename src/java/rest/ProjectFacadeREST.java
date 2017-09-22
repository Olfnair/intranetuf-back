/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package rest;

import config.ApplicationConfig;
import dao.DAOLog;
import entities.Log;
import entities.Project;
import entities.ProjectRight;
import entities.User;
import entities.query.FlexQuery;
import java.io.UnsupportedEncodingException;
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
import utils.Base64Url;

/**
 *
 * @author Florian
 */
@Stateless
@Path("entities.project")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class ProjectFacadeREST extends AbstractFacade<Project> {
    
    @PersistenceContext(unitName = "IUFPU")
    private EntityManager em;
    
    public ProjectFacadeREST() {
        super(Project.class);
    }
    
    /**
     * Endpoint utilisé pour créer un projet
     * @param jaxrsContext - contexte utilisé pour l'authentification
     * @param entity - Entité contenant les infos du projet quy'on veut créer
     * @return L'entité Project créée
     */
    @POST
    public Response create(@Context MessageContext jaxrsContext, Project entity) {
        AuthToken token = Authentication.validate(jaxrsContext);
        // réservé aux admin & superadmins
        RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        
        Response res = super.insert(entity);
        new java.io.File(ApplicationConfig.PROJECTS_LOCATION + '/' + entity.getId().toString()).mkdirs();
        new DAOLog(em).log(new User(token.getUserId()), Log.Type.CREATE_PROJECT, "", entity);
        return res;
    }
    
    /**
     * Endpoint utilisé pour supprimer/restaurer plusieurs projets d'un coup
     * @param jaxrsContext - contexte utilisé pour l'authentification
     * @param activate - true pour restaurer les projets, false pour supprimer
     * @param restLongProjectIds - liste des Id's des projets à affecter
     * @return Statut HTTP 204 en cas de succès
     */
    @PUT
    @Path("activateMany/{activate}")
    public Response activateMany(
            @Context MessageContext jaxrsContext,
            @PathParam("activate") Integer activate,
            List<RestLong> restLongProjectIds
    ) {
        AuthToken token = Authentication.validate(jaxrsContext);
        User user = RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        
        List<Long> projectIds = new ArrayList<>(restLongProjectIds.size());
        restLongProjectIds.forEach((restLongId) -> {
            projectIds.add(restLongId.getValue());
        });
        
        javax.persistence.Query updateQuery = em.createNamedQuery("Project.ActivateMany");
        updateQuery.setParameter("active", activate > 0);
        updateQuery.setParameter("projectIds", projectIds);
        updateQuery.executeUpdate();
        
        // Logs
        projectIds.forEach((projectId) -> {
            new DAOLog(em).log(user, (activate > 0) ? Log.Type.ACTIVATE_PROJECT : Log.Type.DELETE_PROJECT,
                    "", new Project(projectId));
        });
        
        return Response.status(Response.Status.NO_CONTENT).build();
    }
    
    /**
     * Endpoint qui permet de modifier un projet
     * @param jaxrsContext - contexte utilisé pour l'authentification
     * @param id - id du projet à modifier
     * @param entity - entité contenant les modifications à apporter au projet
     * @return Le projet modifié
     */
    @PUT
    @Path("{id}")
    public Response edit(@Context MessageContext jaxrsContext, @PathParam("id") Long id, Project entity) {
        AuthToken token = Authentication.validate(jaxrsContext);
        User user;
        // droits
        try {
            user = RightsChecker.getInstance(em).validate(token, User.Roles.USER, id, ProjectRight.Rights.EDITPROJECT);
        }
        catch(WebApplicationException e) {
            user = RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        }
        
        Project project = em.find(Project.class, id);
        if(project == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if(! user.isAdmin()) {
            entity.setActive(project.isActive());
        }
        entity.setId(project.getId());
        
        Response res = super.edit(entity);
        new DAOLog(em).log(user, Log.Type.EDIT_PROJECT, "Ancien nom : " + project.getName(), entity);
        return res;
    }
    
    /**
     * Endpoint qui permet de supprimer (logiquement) un projet
     * @param jaxrsContext - contexte utilisé pour l'authentification
     * @param id - id du projet
     * @return Statut HTTP 204 en cas de succès
     */
    @DELETE
    @Path("{id}")
    public Response remove(@Context MessageContext jaxrsContext, @PathParam("id") Long id) {
        AuthToken token = Authentication.validate(jaxrsContext);
        // droits:
        User user;
        try { // supprimer le projet
            user = RightsChecker.getInstance(em).validate(token, User.Roles.USER, id, ProjectRight.Rights.DELETEPROJECT);
        }
        catch(WebApplicationException e) { // ou admins
            user = RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        }
        Project project = em.find(Project.class, id);
        if(project == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        // suppression logique
        project.setActive(false);
        new DAOLog(em).log(user, Log.Type.DELETE_PROJECT, "", project);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
    
    /**
     * Endpoint qui permet de faire une recherche sur les projets
     * @param jaxrsContext - contexte utilisé pour l'authentification
     * @param whereParams - paramètres WHERE
     * @param orderbyParams - paramètres ORDER BY
     * @param index - index à partir duqel on veut récupérer les résultats
     * @param limit - nombre max de résultats à récupérer
     * @return Liste des projets correspondants à la recherche
     */
    @GET
    @Path("query/{whereParams}/{orderbyParams}/{index}/{limit}")
    public Response findAll(@Context MessageContext jaxrsContext,
            @PathParam("whereParams") String whereParams, @PathParam("orderbyParams") String orderbyParams,
            @PathParam("index") Integer index, @PathParam("limit") Integer limit) {       
        AuthToken token = Authentication.validate(jaxrsContext);
        User user = em.find(User.class, token.getUserId());
        
        FlexQuery<Project> projectsQuery = user.isAdmin() ?
                new FlexQuery<>(Project.PROJECTLIST_FOR_ADMIN) : new FlexQuery<>(Project.PROJECTLIST_FOR_USER);
        try {
            projectsQuery.setParameters(
                    Base64Url.decode(whereParams),
                    Base64Url.decode(orderbyParams),
                    index, limit
            );
        } catch (UnsupportedEncodingException ex) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        if(! user.isAdmin()) {
            projectsQuery.setBaseParameter("userId", user.getId());
            projectsQuery.setBaseParameter("right", ProjectRight.Rights.VIEWPROJECT);
        }
        
        projectsQuery.prepareCountQuery(em);
        
        return Response.ok(projectsQuery.execute()).build();
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
