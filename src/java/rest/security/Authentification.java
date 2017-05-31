/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package rest.security;

import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.ext.MessageContext;

/**
 *
 * @author Florian
 */
public class Authentification {   
    public static AuthToken validate(MessageContext jaxrsContext) {
        // Get the HTTP Authorization header from the request
        List<String> authorizationHeaderList = jaxrsContext.getHttpHeaders().getRequestHeader(HttpHeaders.AUTHORIZATION);
        
        try {
            if(authorizationHeaderList == null) {
                throw new Exception("Authorization header must be provided");
            }
            String authorizationHeader = authorizationHeaderList.get(0);
            // Check if the HTTP Authorization header is present and formatted correctly
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                throw new Exception("Authorization header must be provided");
            }
            
            // Extract the token from the HTTP Authorization header
            String token = authorizationHeader.substring("Bearer".length()).trim();
            
            // Validate the token
            validateToken(token);
            
        } catch (Exception e) {
            /*ResponseBuilder builder = null;
            builder = Response.status(Response.Status.UNAUTHORIZED);*/
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            /*requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED).build());*/
        }
        return new AuthToken(0, 0, 0);
    }
    
    private static void validateToken(String token) throws Exception {
        // TODO : validation plus "poussee"
        if(! token.equals("token"))
            throw new Exception("bad token");
    }
}
