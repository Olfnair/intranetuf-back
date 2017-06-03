/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package rest.security;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import javax.xml.bind.annotation.XmlRootElement;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Florian
 */
@XmlRootElement
public class AuthToken implements Serializable {
    private static String SECRET = "la_clef_ultra_secrete"; // TODO : Récupérer depuis un fichier de config
    
    private long nonce;
    private long userId;
    private long roleId;
    private long expDate;
    private String signature;
    
    public AuthToken() {
    }
    
    public AuthToken(String jsonString) {
        extractDataFromJsonString(jsonString);
    }
    
    public AuthToken(long nonce, long userId, long roleId) {
        this.nonce = nonce;
        this.userId = userId;
        this.roleId = roleId;
        this.expDate = Instant.now().getEpochSecond() + 60 * 60; // valable 1h (60 * 60 = 3600s = 1h)
    }
    
    private void setEmpty() {
        this.signature = "";
        this.nonce = 0;
        this.roleId = 0;
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
    
    public long getRoleId() {
        return roleId;
    }
    
    public void setRoleId(long roleId) {
        this.roleId = roleId;
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
    
    private String generateSignature() {
        String sign;
        try {
            String data = Long.toString(this.nonce) + Long.toString(this.userId) + Long.toString(this.roleId);
            
            Mac HMAC_sha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(SECRET.getBytes(), "HmacSHA256");
            HMAC_sha256.init(secret_key);
            
            sign = Base64.encodeBase64String(HMAC_sha256.doFinal(data.getBytes()));
        }
        catch (IllegalStateException | InvalidKeyException | NoSuchAlgorithmException e){
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return sign;
    }
    
    public void sign() {
        this.signature = this.generateSignature();
    }
    
    public boolean checkSign() {
        return this.signature.equals(this.generateSignature());
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
            this.roleId = Long.parseLong(builder.toString());
            
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
        catch (NumberFormatException e) {
            this.setEmpty();
        }
    }
    
    public String toJsonString() {
        // !!! attention à ne pas insérer d'espace !!!
        return "{"
            + "\"n\":\"" + Long.toString(this.nonce) + "\","
            + "\"u\":\"" + Long.toString(this.userId) + "\","
            + "\"r\":\"" + Long.toString(this.roleId) + "\","
            + "\"e\":\"" + Long.toString(this.expDate) + "\","
            + "\"s\":\"" + this.signature + "\"" +
        "}";
    }
    
    public static void updateSecret(String secret) {
        SECRET = secret;
    }
}
