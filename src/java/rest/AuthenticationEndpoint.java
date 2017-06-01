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
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class AuthenticationEndpoint {
    @PersistenceContext(unitName = "IUFPU")
    private EntityManager em;
    
    @POST
    public Response authenticateUser(Credentials credentials) {       
        AuthToken token = authenticate(credentials);
        return Response.ok(/*token.toString()*/"token").build();   
    }
    
    private AuthToken authenticate(Credentials credentials) {
        Query auth = em.createNamedQuery("User.Auth");
        auth.setParameter("login", credentials.getLogin());
        auth.setParameter("pwd_hash", credentials.getPassword());
        List<User> user = auth.getResultList();

        // si on a un et un seul résultat (login est unique) c'est bon, sinon :
        if(user == null || user.isEmpty() || user.size() > 1) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        
        // génération du token en fonction du l'utilisateur
        return issueToken(user.get(0));
    }
    
    private static AuthToken issueToken(User user) {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[Long.SIZE];
        random.nextBytes(bytes);
        return new AuthToken(ByteUtils.bytesToLong(bytes), user.getId(), 0);
    }
}
