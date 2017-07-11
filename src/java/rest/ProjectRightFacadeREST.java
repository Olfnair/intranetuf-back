/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import entities.Project;
import entities.ProjectRight;
import entities.User;
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
import rest.security.AuthToken;
import rest.security.Authentication;
import rest.security.RightsChecker;

/**
 *
 * @author Florian
 */
@Stateless
@Path("entities.projectright")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class ProjectRightFacadeREST extends AbstractFacade<ProjectRight> {

    @PersistenceContext(unitName = "IUFPU")
    private EntityManager em;

    public ProjectRightFacadeREST() {
        super(ProjectRight.class);
    }

    @POST
    public Response createOrEdit(List<ProjectRight> entities) {
        entities.forEach((right) -> {
            if(right.getId() == null) {
                em.persist(right);
            }
            else if(right.getRights() == 0) {
                em.remove(right);
            }
            else {
                em.merge(right);
            }
        });
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("{id}")
    public Response edit(@PathParam("id") Long id, ProjectRight entity) {
        return super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") Long id) {
        return super.remove(super.find(id));
    }

    /*@GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response find(@PathParam("id") Long id) {
        return super.find(id);
    }*/
    
    @GET
    @Path("{projectId}")
    public Response findForUserByProject(@Context MessageContext jaxrsContext, @PathParam("projectId") Long projectId) {
        AuthToken token = Authentication.validate(jaxrsContext);
        // pas de droits à vérifier, on récupère automatiquement les droits pour l'user qui les demande après authentification
        
        return this.buildResponseList(() -> {
            javax.persistence.Query rightsQuery = em.createNamedQuery("ProjectRight.GetByUserAndProject");
            rightsQuery.setParameter("userId", token.getUserId());
            rightsQuery.setParameter("projectId", projectId);
            return rightsQuery.getResultList();
        });
    }
    
    @GET
    @Path("user/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getRightsForUser(@Context MessageContext jaxrsContext, @PathParam("id") Long id) {
        // droits : uniquement les admins (ou super)
        AuthToken token = Authentication.validate(jaxrsContext);
        RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        
        return super.buildResponseList(() -> {
            User user = em.find(User.class, id);
            if(user == null) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            ProjectRight.LIST_BY_USER.addOrderByCol("project.name");
            javax.persistence.Query rightsQuery = ProjectRight.LIST_BY_USER.buildQuery(em);
            rightsQuery.setParameter("userId", id);
            List<ProjectRight> fetchedRights = rightsQuery.getResultList();
            List<Long> fetchedProjectIds = new ArrayList(100);
            List<ProjectRight> rights = new ArrayList(100);
            
            fetchedRights.forEach((right) -> {
                fetchedProjectIds.add(right.getProject().getId());
                rights.add(right);
            });
            
            Project.LIST_ALL_OTHER_PROJECTS.addOrderByCol("name");
            javax.persistence.Query projectsQuery = Project.LIST_ALL_OTHER_PROJECTS.buildQuery(em);
            projectsQuery.setParameter("fetchedIds", fetchedProjectIds);
            List<Project> projects = projectsQuery.getResultList();
            
            projects.forEach((project) -> {
                rights.add(new ProjectRight(user, project));
            });
            
            return rights;
        });
    }

    @GET
    @Override
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
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
