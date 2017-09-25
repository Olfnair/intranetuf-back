/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import entities.Log;
import entities.User;
import entities.Version;
import entities.WorkflowCheck;
import entities.query.FlexQuery;
import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
@Path("entities.log")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class LogFacadeREST extends AbstractFacade<Log> {

    @PersistenceContext(unitName = "IUFPU")
    private EntityManager em;

    public LogFacadeREST() {
        super(Log.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    @GET
    @Path("{whereParams}/{orderbyParams}/{index}/{limit}")
    public Response findAll(@Context MessageContext jaxrsContext,
            @PathParam("whereParams") String whereParams, @PathParam("orderbyParams") String orderbyParams,
            @PathParam("index") Integer index, @PathParam("limit") Integer limit) {
        
        AuthToken token = Authentication.validate(jaxrsContext);
        User superadmin = RightsChecker.getInstance(em).validate(token, User.Roles.SUPERADMIN);
        
        FlexQuery<Log> logsQuery = new FlexQuery<>(Log.LIST_ALL);
        try {
            logsQuery.setParameters(
                    Base64Url.decode(whereParams),
                    Base64Url.decode(orderbyParams),
                    index, limit
            );
        } catch (UnsupportedEncodingException ex) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        logsQuery.prepareCountQuery(em);
        return Response.ok(logsQuery.execute()).build();
    }
    
    @GET
    @Path("update")
    public Response update() {
        TypedQuery<Version> listVersions = em.createQuery("Select v FROM Version v", Version.class);
        List<Version> versions = listVersions.getResultList();
        for(Version version : versions) {
            TypedQuery<WorkflowCheck> checksQuery = em.createQuery("Select w FROM WorkflowCheck w WHERE w.version.id = :versionId ORDER BY w.date_checked DESC", WorkflowCheck.class);
            checksQuery.setParameter("versionId", version.getId());
            List<WorkflowCheck> checks = checksQuery.getResultList();
            if(! checks.isEmpty()) {
                WorkflowCheck check = checks.get(0);
                version.setLastCheck((check.getDate_checked() != null) ? checks.get(0) : null);
            }
        }
        return Response.status(Response.Status.OK).build();
    }
    
}
