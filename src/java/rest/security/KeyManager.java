/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest.security;

import config.ApplicationConfig;
import config.ConfigFile;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import static javax.ejb.LockType.READ;
import static javax.ejb.LockType.WRITE;
import javax.ejb.Schedule;
import javax.ejb.Schedules;
import javax.ejb.Singleton;

/**
 *
 * @author Florian
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class KeyManager {
    
    private static String AUTH_SECRET;
    private static String AUTH_SECRET_OLD;
    private static String ACTIVATION_SECRET;
    private static String ACTIVATION_SECRET_OLD;
    
    static {
        ConfigFile config = new ConfigFile(ApplicationConfig.KEYS_LOCATION + '/' + "keys.xml");
        String auth = null;
        String auth_old = null;
        String activation = null;
        String activation_old = null;
        try {
            auth = config.read("auth");
            auth_old = config.read("auth_old");
            activation = config.read("activation");
            activation_old = config.read("activation_old");
            if(auth == null || activation == null) {
                throw new Exception("Error in keys.xml");
            }
            else if(auth.equals(activation)) {
                throw new Exception("key for auth is same as activation. Not Recommended : use 2 different keys.");
            }
        } catch (Exception ex) {
            auth = "ifDdJaJ+RcWFPUVSymIjLe5PHc4plksmKFwSfa7KNxQ="; // clé par défaut de secours
            activation = "MRyZ57CFn/h2p4j7co9fUbt18q4nrGj+53nikbvdfHs="; // clé par défaut de secours
            Logger.getLogger(ApplicationConfig.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            AUTH_SECRET = auth;
            AUTH_SECRET_OLD = auth_old;
            ACTIVATION_SECRET = activation;
            ACTIVATION_SECRET_OLD = activation_old;
        }
    }

    @Lock(READ)
    public static String getAUTH_SECRET() {
        return AUTH_SECRET;
    }

    @Lock(READ)
    public static String getAUTH_SECRET_OLD() {
        return AUTH_SECRET_OLD;
    }

    @Lock(READ)
    public static String getACTIVATION_SECRET() {
        return ACTIVATION_SECRET;
    }

    @Lock(READ)
    public static String getACTIVATION_SECRET_OLD() {
        return ACTIVATION_SECRET_OLD;
    }
    
    public static String generateRandomKey() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[32];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
    
    public static void saveKeys() {
        Properties config = new Properties();
        
        config.setProperty("auth", AUTH_SECRET);
        if(AUTH_SECRET_OLD != null) {
            config.setProperty("auth_old", AUTH_SECRET_OLD);
        }
        
        config.setProperty("activation", ACTIVATION_SECRET);
        if(ACTIVATION_SECRET_OLD != null) {
            config.setProperty("activation_old", ACTIVATION_SECRET_OLD);
        }
        
        FileOutputStream filestream;
        try {
            filestream = new FileOutputStream(new java.io.File(ApplicationConfig.KEYS_LOCATION + '/' + "keys.xml"));
            config.storeToXML(
                filestream, "\n"
                        + "Les clés utilisées pour signer les tokens\n"
                        + "- auth: pour les tokens d'authentification\n"
                        + "- activation: pour les tokens d'activation\n",
                "UTF-8"
            );
        } catch (IOException ex) {
            Logger.getLogger(KeyManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public KeyManager() {
    }    
    
    @Lock(WRITE)
    @Schedules({
        @Schedule(second="0", minute="0", hour="1", dayOfMonth="*")
    })
    public void updateAuthKey() {
        AUTH_SECRET_OLD = AUTH_SECRET;
        AUTH_SECRET = generateRandomKey(); 
        // on s'assure que les clés soient tjrs différentes.
        do {
            AUTH_SECRET = generateRandomKey();
        } while(AUTH_SECRET.equals(ACTIVATION_SECRET));
        
        saveKeys();
    }
    
    @Lock(WRITE)
    @Schedules({
        @Schedule(second="0", minute="5", hour="2", dayOfMonth="*")
    })
    public void removeOldAuthKey() {
        AUTH_SECRET_OLD = null;
        saveKeys();
    }
    
    @Lock(WRITE)
    @Schedules({
        @Schedule(second="0", minute="0", hour="0", dayOfMonth="2, 12, 22")
    })
    public void updateActivationKey() {
        ACTIVATION_SECRET_OLD = ACTIVATION_SECRET;    
        // on s'assure que les clés soient tjrs différentes.
        do {
            ACTIVATION_SECRET = generateRandomKey();
        } while(ACTIVATION_SECRET.equals(AUTH_SECRET));
        
        saveKeys();
    }
    
    
    @Lock(WRITE)
    @Schedules({
        @Schedule(second="0", minute="30", hour="0", dayOfMonth="5, 15, 25")
    })
    public void removeOldActivationKey() {
        ACTIVATION_SECRET_OLD = null;
        saveKeys();
    }
}
