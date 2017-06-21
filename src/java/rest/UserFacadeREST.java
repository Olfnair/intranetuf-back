/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import entities.User;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import mail.MailSender;
import mail.SendMailThread;
import rest.security.AuthToken;

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
    public Response create(User entity) {
        // TODO : check que c'est bien l'admin qui fait la requête
        Response res = super.insert(entity);
        // on signe le token avec la clé pour les activations. Important pour éviter de générer un token qui pourrait aussi servir pour tout le reste
        AuthToken token = AuthenticationEndpoint.issueToken(entity, 3 * 24 * 60 * 60, AuthToken.ACTIVATION_KEY); // token valable pendant 3 jours
        String url = "";
        String text = "Vous venez de créer un compte sur IntranetUF. Cliquez sur ce lien pour l'activer et choisir votre mot de passe : ";
        try {
            url = "http://localhost:4200/activate/?token=" + URLEncoder.encode(token.toJsonString(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(UserFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
        new SendMailThread(new MailSender(entity.getEmail(), "Activer votre compte", text + url)).start();
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
        //new SendMailThread(new MailSender("chalet.florian@gmail.com", "test", "le message de test")).start();
        
        return super.buildResponseList(() -> {
            javax.persistence.Query usersQuery = em.createNamedQuery("User.ListAllComplete");
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

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
