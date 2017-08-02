/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package rest;

import config.ApplicationConfig;
import entities.Project;
import entities.ProjectRight;
import entities.User;
import entities.query.FlexQuery;
import java.io.UnsupportedEncodingException;
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
    
    @POST
    public Response create(@Context MessageContext jaxrsContext, Project entity) {
        AuthToken token = Authentication.validate(jaxrsContext);
        // réservé aux admin & superadmins
        RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        
        Response res = super.insert(entity);
        new java.io.File(ApplicationConfig.PROJECTS_LOCATION + '/' + entity.getId().toString()).mkdirs();
        return res;
    }
    
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
        return super.edit(entity);
    }
    
    @DELETE
    @Path("{id}")
    public Response remove(@Context MessageContext jaxrsContext, @PathParam("id") Long id) {
        AuthToken token = Authentication.validate(jaxrsContext);
        // droits:
        try { // supprimer le projet
            RightsChecker.getInstance(em).validate(token, User.Roles.USER, id, ProjectRight.Rights.DELETEPROJECT);
        }
        catch(WebApplicationException e) { // ou admins
            RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        }
        Project project = em.find(Project.class, id);
        if(project == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        // suppression logique
        project.setActive(false);       
        return Response.status(Response.Status.NO_CONTENT).build();
    }
    
    @GET
    @Path("{id}")
    public Response find(@PathParam("id") Long id) {
        return super.find(id);
    }
    
    @GET
    @Path("query/{whereParams}")
    public Response findAll(@Context MessageContext jaxrsContext, @PathParam("whereParams") String whereParams) {       
        AuthToken token = Authentication.validate(jaxrsContext);
        User user = em.find(User.class, token.getUserId());
        
        FlexQuery<Project> projectsQuery = user.isAdmin() ?
                new FlexQuery<>(Project.PROJECTLIST_FOR_ADMIN) : new FlexQuery<>(Project.PROJECTLIST_FOR_USER);
        try {
            projectsQuery.setParameters(Base64Url.decode(whereParams), "default");
        } catch (UnsupportedEncodingException ex) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        projectsQuery.prepareQuery(em);
        
        if(! user.isAdmin()) {
            projectsQuery.setParameter("userId", user.getId());
            projectsQuery.setParameter("right", ProjectRight.Rights.VIEWPROJECT);
        }
        
        return this.buildResponseList(() -> {
            return projectsQuery.execute().getList();
        });
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
