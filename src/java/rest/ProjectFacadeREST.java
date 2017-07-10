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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.ext.MessageContext;
import rest.security.AuthToken;
import rest.security.Authentication;

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
    public Response create(Project entity) {
        Response res = super.insert(entity);
        new java.io.File(ApplicationConfig.PROJECTS_LOCATION + '/' + ApplicationConfig.combineNameWithId(entity.getName(), entity.getId())).mkdirs();
        return res;
    }
    
    @PUT
    @Path("{id}")
    public Response edit(@PathParam("id") Long id, Project entity) {
        return super.edit(entity);
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
    public Response findAll(@Context MessageContext jaxrsContext) {
        AuthToken token = Authentication.validate(jaxrsContext);
        User user = em.find(User.class, token.getUserId());
        
        // si admin : tous les projets
        if(user.hasRole(User.Roles.ADMIN) || user.hasRole(User.Roles.SUPERADMIN)) {
            return super.findAll();
        }
        
        // sinon, uniquement les projets que l'utilisateur peut voir
        return this.buildResponseList(() -> {
            javax.persistence.Query projectsQuery = em.createNamedQuery("Project.ListForUser");
            projectsQuery.setParameter("userId", user.getId());
            projectsQuery.setParameter("right", ProjectRight.Rights.VIEWPROJECT);
            return projectsQuery.getResultList();
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
