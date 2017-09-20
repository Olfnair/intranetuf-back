/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest.security;

import config.ApplicationConfig;
import config.AuthConfig;
import java.security.SecureRandom;
import java.util.Base64;
import javax.ejb.Schedule;
import javax.ejb.Schedules;
import javax.ejb.Singleton;

/**
 *
 * @author Florian
 */
@Singleton
public class KeyManager {
    private final AuthConfig config;
    
    public static String generateRandomKey() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[32];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
    
    public KeyManager() {
        config = ApplicationConfig.AUTH_CONFIG;
    }    
    
    @Schedules({
        @Schedule(second="0", minute="0", hour="1", dayOfMonth="*")
    })
    public void updateAuthKey() {
        config.setAuthSecretOld(config.getAuthSecret());
        // on s'assure que les clés soient tjrs différentes.
        do {
            config.setAuthSecret(generateRandomKey());
        } while(config.getAuthSecret().equals(config.getActivationSecret()));
        
        config.saveKeys();
    }
    
    @Schedules({
        @Schedule(second="0", minute="5", hour="2", dayOfMonth="*")
    })
    public void removeOldAuthKey() {
        config.setAuthSecretOld(null);
        config.saveKeys();
    }
    
    @Schedules({
        @Schedule(second="0", minute="0", hour="0", dayOfMonth="2, 12, 22")
    })
    public void updateActivationKey() {
        config.setActivationSecretOld(config.getActivationSecret());    
        // on s'assure que les clés soient tjrs différentes.
        do {
            config.setActivationSecret(generateRandomKey());
        } while(config.getActivationSecret().equals(config.getAuthSecret()));
        
        config.saveKeys();
    }
    
    @Schedules({
        @Schedule(second="0", minute="30", hour="0", dayOfMonth="5, 15, 25")
    })
    public void removeOldActivationKey() {
        config.setActivationSecretOld(null);
        config.saveKeys();
    }
}
