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
        
        if(authorizationHeaderList == null) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Authorization header must be provided").build());
        }
        String authorizationHeader = authorizationHeaderList.get(0);
        // Check if the HTTP Authorization header is present and formatted correctly
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Authorization header must be provided").build());
        }
        
        // Extract the token from the HTTP Authorization header
        String jsonTokenData = authorizationHeader.substring("Bearer ".length());//.trim(); -> ne devrait pas être nécessaire : attention à ne pas ajouter d'accents...
        AuthToken token = new AuthToken(jsonTokenData);
        
        // Validate the token
        validateToken(token);
        return token;
    }
    
    private static void validateToken(AuthToken token) {
        if(! token.checkSign()) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build());
        }  
        if(token.isExpired()) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Expired Token").build());
        }
    }
}
