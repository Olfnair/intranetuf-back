/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package rest.security;

import config.ApplicationConfig;
import config.AuthConfig;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import javax.xml.bind.annotation.XmlRootElement;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Florian
 */
@XmlRootElement
public class AuthToken implements Serializable {   
    public static final int AUTH_KEY = 0;
    public static final int ACTIVATION_KEY = 1;
    
    private static final long serialVersionUID = 1L;
    
    @XmlTransient()
    private final AuthConfig config;
    
    private long nonce;
    private long userId;
    private long roles;
    private long expDate;
    private String signature;
    
    public AuthToken() {
        this(ApplicationConfig.AUTH_CONFIG);
    }
    
    public AuthToken(AuthConfig config) {
        this.config = config;
    }
    
    public AuthToken(String jsonString) {
        this(ApplicationConfig.AUTH_CONFIG, jsonString);
    }
    
    public AuthToken(AuthConfig config, String jsonString) {
        this.config = config;
        extractDataFromJsonString(jsonString);
    }
    
    public AuthToken(long nonce, long userId, long roles, long secValidity) {
        this(ApplicationConfig.AUTH_CONFIG, nonce, userId, roles, secValidity);
    }
    
    public AuthToken(AuthConfig config, long nonce, long userId, long roles, long secValidity) {
        this.config = config;
        this.nonce = nonce;
        this.userId = userId;
        this.roles = roles;
        this.expDate = Instant.now().getEpochSecond() + secValidity;
    }
    
    private void setEmpty() {
        this.signature = "";
        this.nonce = 0;
        this.roles = 0;
        this.userId = 0;
        this.expDate = 0;
    }
    
    public long getNonce() {
        return nonce;
    }
    
    public void setNonce(long nonce) {
        this.nonce = nonce;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public long getRoles() {
        return roles;
    }
    
    public void setRoles(long roles) {
        this.roles = roles;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }

    public long getExpDate() {
        return expDate;
    }

    public void setExpDate(long expDate) {
        this.expDate = expDate;
    }
    private String generateSignature(int key) {
        return generateSignature(key, false);
    }
    
    private String generateSignature(int key, boolean old) {
        String sign;
        String secret = getSecret(key, old);
        if(secret == null) {
            return "";
        }
        try {
            String data = Long.toString(this.nonce) + Long.toString(this.userId) + Long.toString(this.roles) + Long.toString(this.expDate);
            
            Mac HMAC_sha512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes("ISO-8859-1"), "HmacSHA512");
            HMAC_sha512.init(secret_key);
            
            sign = Base64.getEncoder().encodeToString(HMAC_sha512.doFinal(data.getBytes("ISO-8859-1")));
        }
        catch (IllegalStateException | InvalidKeyException | NoSuchAlgorithmException e){
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } catch (UnsupportedEncodingException ex) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return sign;
    }
    
    public void sign(int key) {
        this.signature = this.generateSignature(key);
    }
    
    public boolean checkSign(int key) {
        boolean ret = this.signature.equals(this.generateSignature(key, false)); // clé actuelle
        if(! ret) {
            ret = this.signature.equals(this.generateSignature(key, true)); // ancienne clé
        }
        return ret;
    }
    
    public boolean isExpired() {
        return Instant.now().getEpochSecond() > this.expDate;
    }
    
    private void extractDataFromJsonString(String json) {
        try {
            StringBuilder builder;
            int i = 0;
            
            builder = new StringBuilder(Long.BYTES);
            final String nonceIdentifier = "\"n\":\"";
            int nonceIndex = json.indexOf(nonceIdentifier, i) + nonceIdentifier.length();
            for(i = nonceIndex; json.charAt(i) != '\"' && i < json.length() && i > -1; ++i) {
                builder.append(json.charAt(i));
            }
            this.nonce = Long.parseLong(builder.toString());
            
            builder = new StringBuilder(Long.BYTES);
            final String userIdIdentifier = "\"u\":\"";
            int userIdIndex = json.indexOf(userIdIdentifier, i) + userIdIdentifier.length();
            for(i = userIdIndex; json.charAt(i) != '\"' && i < json.length() && i > -1; ++i) {
                builder.append(json.charAt(i));
            }
            this.userId = Long.parseLong(builder.toString());
            
            builder = new StringBuilder(Long.BYTES);
            final String roleIdIdentifier = "\"r\":\"";
            int roleIdIndex = json.indexOf(roleIdIdentifier, i) + roleIdIdentifier.length();
            for(i = roleIdIndex; json.charAt(i) != '\"' && i < json.length() && i > -1; ++i) {
                builder.append(json.charAt(i));
            }
            this.roles = Long.parseLong(builder.toString());
            
            builder = new StringBuilder(Long.BYTES);
            final String expDateIdentifier = "\"e\":\"";
            int expDateIndex = json.indexOf(expDateIdentifier, i) + expDateIdentifier.length();
            for(i = expDateIndex; json.charAt(i) != '\"' && i < json.length() && i > -1; ++i) {
                builder.append(json.charAt(i));
            }
            this.expDate = Long.parseLong(builder.toString());
            
            builder = new StringBuilder(50); // une taille de 50 devrait être suffisante pour la signature
            final String signatureIdentifier = "\"s\":\"";
            int signatureIndex = json.indexOf(signatureIdentifier, i) + signatureIdentifier.length();
            for(i = signatureIndex; json.charAt(i) != '\"' && i < json.length() && i > -1; ++i) {
                builder.append(json.charAt(i));
            }
            this.signature = builder.toString();
        }
        catch (Exception e) {
            this.setEmpty();
        }
    }
    
    public String toJsonString() {
        // !!! attention à ne pas insérer d'espace !!!
        return "{"
            + "\"n\":\"" + Long.toString(this.nonce) + "\","
            + "\"u\":\"" + Long.toString(this.userId) + "\","
            + "\"r\":\"" + Long.toString(this.roles) + "\","
            + "\"e\":\"" + Long.toString(this.expDate) + "\","
            + "\"s\":\"" + this.signature + "\"" +
        "}";
    }
    
    private String getSecret(int key) {
        return getSecret(key, false);
    }
    
    private String getSecret(int key, boolean old) {
        if(key == ACTIVATION_KEY) {
            return old ? config.getActivationSecretOld() : config.getActivationSecret();
        }
        return old ? config.getAuthSecretOld() : config.getAuthSecret(); // par defaut
    }
}
