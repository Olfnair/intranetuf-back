/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package rest.security;

import java.security.SecureRandom;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.ext.MessageContext;

/**
 *
 * @author Florian
 */

class ByteUtils {
    public static byte[] longToBytes(long l) {
        byte[] result = new byte[Long.SIZE / Byte.SIZE];
        for (int i = Long.SIZE / Byte.SIZE - 1; i >= 0; --i) {
            result[i] = (byte)(l & 0xFF);
            l >>= Byte.SIZE;
        }
        return result;
    }
    
    public static long bytesToLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < Long.SIZE / Byte.SIZE; ++i) {
            result <<= Byte.SIZE;
            result |= (b[i] & 0xFF);
        }
        return result;
    }
}

public class Authentication {
    
    public static AuthToken issueToken(Long userId, Long roleId, long secValidity, int key) {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[Long.BYTES];
        random.nextBytes(bytes);
        AuthToken token = new AuthToken(ByteUtils.bytesToLong(bytes), userId, roleId, secValidity);
        token.sign(key);
        return token;
    }
    
    // AUTH_KEY par defaut
    public static AuthToken validate(String jsonTokenData) {
        return validate(jsonTokenData, AuthToken.AUTH_KEY);
    }
    
    public static AuthToken validate(String jsonTokenData, int key) {
        if(jsonTokenData == null) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Token not found").build());
        }
        
        AuthToken token = new AuthToken(jsonTokenData);
        
        // Validate the token
        validateToken(token, key);
        return token;
    }
    
    // AUTH_KEY par defaut
    public static AuthToken validate(MessageContext jaxrsContext) {
        return validate(jaxrsContext, AuthToken.AUTH_KEY);
    }
    
    public static AuthToken validate(MessageContext jaxrsContext, int key) {
        // Get the HTTP Authorization header from the request
        List<String> authorizationHeaderList = jaxrsContext.getHttpHeaders().getRequestHeader(HttpHeaders.AUTHORIZATION);
        
        if(authorizationHeaderList == null || authorizationHeaderList.size() < 1) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Authorization header must be provided").build());
        }
        String authorizationHeader = authorizationHeaderList.get(0);
        // Check if the HTTP Authorization header is present and formatted correctly
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Authorization header must be provided").build());
        }
        
        // Extract the token from the HTTP Authorization header
        String jsonTokenData = authorizationHeader.substring("Bearer ".length());//.trim(); -> ne devrait pas être nécessaire : attention à ne pas ajouter d'espaces...
        
        return validate(jsonTokenData, key);
    }
    
    private static void validateToken(AuthToken token, int key) {
        if(! token.checkSign(key)) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build());
        }  
        else if(token.isExpired()) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Expired Token").build());
        }
    }
}
