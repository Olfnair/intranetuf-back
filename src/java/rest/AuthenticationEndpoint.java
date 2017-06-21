package rest;

import entities.User;
import java.security.SecureRandom;
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
import rest.security.Credentials;
import utils.ByteUtils;

@Path("/auth")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces(MediaType.TEXT_PLAIN)
public class AuthenticationEndpoint {
    @PersistenceContext(unitName = "IUFPU")
    private EntityManager em;
    
    @POST
    public Response authenticateUser(Credentials credentials) {       
        AuthToken token = authenticate(credentials);
        return Response.ok(token.toJsonString()).build();
    }
    
    private AuthToken authenticate(Credentials credentials) {
        Query authQuery = em.createNamedQuery("User.Auth");
        authQuery.setParameter("login", credentials.getLogin());
        authQuery.setParameter("password", credentials.getPassword());
        List<User> user = authQuery.getResultList();

        // si on a un et un seul résultat (login est unique) c'est bon, sinon :
        if(user == null || user.isEmpty() || user.size() > 1) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        
        // génération du token en fonction du l'utilisateur
        return issueToken(user.get(0), 60 * 60, AuthToken.AUTH_KEY); // token valable pendent 1h = 60 * 60 = 3600 sec
    }
    
    public static AuthToken issueToken(User user, long secValidity, int key) {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[Long.BYTES];
        random.nextBytes(bytes);
        AuthToken token = new AuthToken(ByteUtils.bytesToLong(bytes), user.getId(), 0, secValidity);
        token.sign(key);
        return token;
    }
}
