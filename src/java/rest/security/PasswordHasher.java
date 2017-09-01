/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest.security;

import config.ApplicationConfig;
import config.ConfigFile;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 *
 * @author Florian
 */
public class PasswordHasher {
    private final static byte PEPER[];
    private final static int ITERATION;
    
    private final static int OUTPUT_LEN;
    private final static int SALT_LEN;
    
    static {
        ConfigFile config = new ConfigFile(ApplicationConfig.KEYS_LOCATION + '/' + "hashpass.xml");
        // valeurs de secours
        int iteration = 102417;
        int output_len = 64;
        int salt_len = 32;
        String peper = "pLKvnBOx1V5qIxtblHUbdGQpTyifH0/Qbefppez0Kcg=";
        
        try {
            iteration = Integer.parseInt(config.read("iteration", Integer.toString(iteration)), 10);
            peper = config.read("peper");
            output_len = Integer.parseInt(config.read("output_len", Integer.toString(output_len)), 10);
            salt_len = Integer.parseInt(config.read("salt_len", Integer.toString(salt_len)), 10);
            if(peper == null) {
                throw new Exception("Error in hashpass.xml");
            }
        } catch (Exception ex) {
            Logger.getLogger(ApplicationConfig.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            PEPER = Base64.getDecoder().decode(peper);
            ITERATION = iteration;
            OUTPUT_LEN = output_len;
            SALT_LEN = salt_len;
        }
    }
    
    private int iterations = ITERATION;
    private String base64Salt;
    private String password;
    
    public PasswordHasher(String password) {
        this(password, genBase64Salt(), ITERATION);
    }
    
    public PasswordHasher(String password, String base64Salt) {
        this(password, base64Salt, ITERATION);
    }
    
    public PasswordHasher(String password, String base64Salt, int iterations) {
        this.password = password;
        this.base64Salt = base64Salt;
        this.iterations = iterations;
    }
    
    public String hash() {
        byte salt[] = Base64.getDecoder().decode(base64Salt);
        for(int i = 0; i < salt.length && i < PEPER.length; ++i) {
            salt[i] ^= PEPER[i];
        }
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, OUTPUT_LEN * 8);
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
    
    public static String genBase64Salt() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[SALT_LEN];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
