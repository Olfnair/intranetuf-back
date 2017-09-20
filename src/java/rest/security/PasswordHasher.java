/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest.security;

import config.ApplicationConfig;
import config.PBDKF2Config;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 *
 * @author Florian
 */
public class PasswordHasher {
    private final PBDKF2Config config;
    private int iterations;
    private String base64Salt;
    private String password;
    
    // default Config :
    public PasswordHasher(String password) {
        this(ApplicationConfig.PBDKF2_CONFIG, password, genBase64Salt(ApplicationConfig.PBDKF2_CONFIG.getSaltLen()),
                ApplicationConfig.PBDKF2_CONFIG.getIterations());
    }
    
    public PasswordHasher(String password, String base64Salt) {
        this(ApplicationConfig.PBDKF2_CONFIG, password, base64Salt, ApplicationConfig.PBDKF2_CONFIG.getIterations());
    }
    
    public PasswordHasher(String password, String base64Salt, int iterations) {
        this(ApplicationConfig.PBDKF2_CONFIG, password, base64Salt, iterations);
    }
    
    
    // specify Config :
    public PasswordHasher(PBDKF2Config config, String password) {
        this(config, password, genBase64Salt(config.getSaltLen()), config.getIterations());
    }
    
    public PasswordHasher(PBDKF2Config config, String password, String base64Salt) {
        this(config, password, base64Salt, config.getIterations());
    }
    
    public PasswordHasher(PBDKF2Config config, String password, String base64Salt, int iterations) {
        this.config = config;
        this.password = password;
        this.base64Salt = base64Salt;
        this.iterations = iterations;
        if(iterations < config.getIterations()) {
            this.iterations = config.getIterations();
        }
    }
    
    public String hash() {
        byte salt[] = Base64.getDecoder().decode(base64Salt);
        byte[] peper = config.getPeper();
        for(int i = 0; i < salt.length && i < peper.length; ++i) {
            salt[i] ^= peper[i];
        }
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, config.getOutputLen() * 8);
        try {
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            return Base64.getEncoder().encodeToString(f.generateSecret(spec).getEncoded());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
        }
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public String getBase64Salt() {
        return base64Salt;
    }

    public void setBase64Salt(String base64Salt) {
        this.base64Salt = base64Salt;
    }
    
    private static String genBase64Salt(int len) {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[len];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
