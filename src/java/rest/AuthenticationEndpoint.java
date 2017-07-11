package rest;

import entities.User;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path; 
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import rest.security.AuthToken;
import rest.security.Authentication;
import entities.Credentials;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import org.apache.cxf.jaxrs.ext.MessageContext;
import rest.security.PasswordHasher;
import rest.security.RightsChecker;

@Path("auth")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces(MediaType.TEXT_PLAIN)
public class AuthenticationEndpoint {
    @PersistenceContext(unitName = "IUFPU")
    private EntityManager em;
    
    public AuthenticationEndpoint() {
    }
    
    @POST
    public Response authenticateUser(Credentials credentials) {       
        AuthToken token = authenticate(credentials);
        return Response.ok(token.toJsonString()).build();
    }
    
    @GET
    @Path("adminLoginAs/{login}")
    public Response authenticateAsUser(@Context MessageContext jaxrsContext, @PathParam("login") String login) {       
        AuthToken token = Authentication.validate(jaxrsContext);
        User admin = RightsChecker.getInstance(em).validate(token, User.Roles.SUPERADMIN);
        
        Query userQuery = em.createNamedQuery("User.getByLogin");
        userQuery.setParameter("login", login);
        User user;
        try {
            user = (User)userQuery.getSingleResult();
        }
        catch(Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if(user.hasRole(User.Roles.SUPERADMIN)) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        AuthToken userToken = Authentication.issueToken(user.getId(), user.getRole(), 60 * 60, AuthToken.AUTH_KEY);
        return Response.ok(userToken.toJsonString()).build();
    }
    
    private AuthToken authenticate(Credentials credentials) {
        Query authQuery = em.createNamedQuery("User.getByloginWithCredentialsForAuth");
        authQuery.setParameter("login", credentials.getLogin());
        List<User> users = authQuery.getResultList();

        // si on a un et un seul résultat (login est unique) c'est bon, sinon :
        if(users == null || users.isEmpty() || users.size() > 1) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        
        // check pw
        User user = users.get(0);
        PasswordHasher ph = new PasswordHasher(credentials.getPassword(), user.getCredentials().getSalt(), user.getCredentials().getIteration());
        if(! ph.hash().equals(user.getCredentials().getPassword())) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        
        // génération du token en fonction du l'utilisateur
        return Authentication.issueToken(user.getId(), user.getRole(), 60 * 60, AuthToken.AUTH_KEY); // token valable pendant 1h = 60 * 60 = 3600 sec
    }
}
