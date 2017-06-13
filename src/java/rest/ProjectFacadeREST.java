/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package rest;

import entities.Project;
import java.io.File;
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
import rest.security.Authentification;

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
        new java.io.File(
            ApplicationConfig.PROJECTS_LOCATION +
            entity.getName().replaceAll("\\s", "_") + // remplace tous les caractères d'espacement par '_'
            '_' + entity.getId().toString()
        ).mkdirs();
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
        //AuthToken token = Authentification.validate(jaxrsContext);
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
