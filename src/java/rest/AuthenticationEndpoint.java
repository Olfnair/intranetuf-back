package rest;

import dao.DAOLog;
import entities.User;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
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
import entities.Log;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import org.apache.cxf.jaxrs.ext.MessageContext;
import rest.security.PasswordHasher;
import rest.security.RightsChecker;

@Stateless
@Path("auth")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces(MediaType.TEXT_PLAIN)
public class AuthenticationEndpoint {
    @PersistenceContext(unitName = "IUFPU")
    private EntityManager em;
    
    public AuthenticationEndpoint() {
    }
    
    /**
     * Endpoint qui authentifie un utilisateur sur base de ses credentials (login, mot de passe) et lui renvoie un token
     * signé pour ses requêtes suivantes afin de pouvoir s'authentifier rapidement.
     * @param credentials entité qui contient le login et le mot de passe de l'utilisateur à authentifier
     * @return Token d'authentification pour l'utilisateur
     */
    @POST
    public Response authenticateUser(Credentials credentials) {       
        AuthToken token = authenticate(credentials);
        new DAOLog(em).log(new User(token.getUserId()), Log.Type.AUTH_USER, "'" + credentials.getLogin() + "' s'est connecté");
        return Response.ok(token.toJsonString()).build();
    }
    
    /**
     * Endpoint qui permet à un superadmin de récupérer un token qui lui permet d'utiliser le compte d'un autre utilisateur.
     * @param jaxrsContext Contexte de la requête, utilisé pour l'authentification
     * @param login Le login du compte qu'on veut utiliser
     * @return Token qui permet de se faire passer pour l'utisateur correspondant au login.
     */
    @GET
    @Path("adminLoginAs/{login}")
    public Response authenticateAsUser(@Context MessageContext jaxrsContext, @PathParam("login") String login) {       
        AuthToken token = Authentication.validate(jaxrsContext);
        User superadmin = RightsChecker.getInstance(em).validate(token, User.Roles.SUPERADMIN);
        
        TypedQuery<User> userQuery = em.createNamedQuery("User.getByLogin", User.class);
        userQuery.setParameter("login", login);
        User user;
        try {
            user = userQuery.getSingleResult();
        }
        catch(Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if(user.hasRole(User.Roles.SUPERADMIN)) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        AuthToken userToken = Authentication.issueToken(user.getId(), user.getRole(), 60 * 60, AuthToken.AUTH_KEY);
        new DAOLog(em).log(superadmin, Log.Type.AUTH_AS, "'" + superadmin.getLogin() + "' s'est connecté en tant que '" + login + "'", user);
        return Response.ok(userToken.toJsonString()).build();
    }
    
    private AuthToken authenticate(Credentials credentials) {
        TypedQuery<User> authQuery = em.createNamedQuery("User.getByloginWithCredentialsForAuth", User.class);
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
