/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import config.ApplicationConfig;
import entities.Credentials;
import entities.User;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import mail.Mail;
import mail.SendMailThread;
import org.apache.cxf.jaxrs.ext.MessageContext;
import rest.security.AuthToken;
import rest.security.Authentication;
import rest.security.PasswordHasher;
import rest.security.RightsChecker;
import utils.UrlBase64;

/**
 *
 * @author Florian
 */
@Stateless
@Path("entities.user")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class UserFacadeREST extends AbstractFacade<User> {
    
    @PersistenceContext(unitName = "IUFPU")
    private EntityManager em;

    public UserFacadeREST() {
        super(User.class);
    }

    @POST
    public Response create(@Context MessageContext jaxrsContext, User entity) {
        // droits : admin ou super
        AuthToken adminToken = Authentication.validate(jaxrsContext);
        RightsChecker.getInstance(em).validate(adminToken, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        
        entity.setCredentials(new Credentials(entity.getLogin()));
        Response res = super.insert(entity);
        // On signe le token avec la clé pour les activations.
        // Important pour éviter de générer un token qui pourrait aussi servir pour tout le reste
        AuthToken userToken = Authentication.issueToken(entity.getId(), 0L, 3 * 24 * 60 * 60, AuthToken.ACTIVATION_KEY); // token valable pendant 3 jours
        String url = "";
        String text = "Vous venez de créer un compte sur IntranetUF. Cliquez sur ce lien pour l'activer et choisir votre mot de passe : ";
        try {
            url = ApplicationConfig.FRONTEND_URL + "/#/activate/" + UrlBase64.encode(userToken.toJsonString(), "ISO-8859-1");
        } catch (UnsupportedEncodingException ex) {
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{error: \"UnsupportedEncodingException\"}").build());
        }
        new SendMailThread(new Mail(entity.getEmail(), "Activer votre compte", text + url)).start();
        return res;
    }

    @PUT
    @Path("{id}")
    public Response edit(@PathParam("id") Long id, User entity) {
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
        return super.buildResponseList(() -> {
            javax.persistence.Query usersQuery = User.LIST_ALL_COMPLETE.getQuery(em);
            return usersQuery.getResultList();
        });
    }
    
    // utilisé pour récupérer les controleurs et valideurs par projet
    @GET
    @Path("rightOnProject/{projectId}/{right}")
    public Response getByRightOnProject(@Context MessageContext jaxrsContext, @PathParam("projectId") Long projectId, @PathParam("right") Long right) {
        AuthToken token = Authentication.validate(jaxrsContext);
        // pas de check de droit spécifique
        
        return super.buildResponseList(() -> {
            javax.persistence.Query usersQuery = User.LIST_BY_RIGHT_ON_PROJECT.getQuery(em);
            usersQuery.setParameter("projectId", projectId);
            usersQuery.setParameter("userId", token.getUserId());
            usersQuery.setParameter("right", right);
            return usersQuery.getResultList();
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

    @GET
    @Path("activate")
    public Response findUserToActivate(@Context MessageContext jaxrsContext) {
        AuthToken token = Authentication.validate(jaxrsContext, AuthToken.ACTIVATION_KEY);
        User user = em.find(User.class, token.getUserId());
        if(user == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }
        else if(! user.isPending()) {
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("user").build());
        }
        return super.buildResponse(200, user);
    }
    
    @PUT
    @Path("activate/{id}")
    public Response activate(@Context MessageContext jaxrsContext, @PathParam("id") Long id, Credentials entity) {
        AuthToken token = Authentication.validate(jaxrsContext, AuthToken.ACTIVATION_KEY);
        if(token.getUserId() != id) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        // vérifie que le compte n'est pas déjà pending == false
        javax.persistence.Query userQuery = em.createNamedQuery("User.getWithCredentials");
        userQuery.setParameter("userId", id);
        User user;
        try {
            user = (User) userQuery.getSingleResult();
        }
        catch(Exception e) {
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("user").build());
        }
        if(user == null || ! user.isPending()) {
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("user").build());
        }
        // TODO : améliorer le check password
        String password = entity.getPassword();
        if(password == null || password.length() < 8) {
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("password").build());
        }
        
        PasswordHasher ph = new PasswordHasher(password);
        user.getCredentials().setPassword(ph.hash());
        user.getCredentials().setIteration(ph.getIterations());
        user.getCredentials().setSalt(ph.getBase64Salt());
        user.setPending(false);
        return super.buildResponse(200);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
