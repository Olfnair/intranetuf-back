/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import rest.objects.RestLong;

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
    
    @POST
    public Response create(List<WorkflowCheck> entities) {
        entities.forEach((check) -> {
            em.persist(check);
        });
        return Response.status(Response.Status.CREATED).build();
    }
    
    @POST // on passe les id's de versions dans le corps de la requête pour être sur de ne pas être limité par la taille d'un GET
    @Path("{userId}/{status}")
    public Response getByStatusUserVersions(
        @PathParam("userId") Long userId,
        @PathParam("status") Integer status,
        List<RestLong> restLongVersionIds
    ) {       
        // TODO : check token + check user = userId ou user = admin
        
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
    public Response edit(@PathParam("id") Long id, WorkflowCheck entity) {
        entity.setId(id);
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
    @Produces(MediaType.TEXT_PLAIN)
    public Response countREST() {
        return super.count();
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
