/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Florian
 */
public class AuthConfig extends Config {
    
    protected interface StringGetter {
        public String get();
    }
    
    protected interface StringSetter {
        public void set();
    }
    
    private String authSecret;
    private String authSecretOld;
    private String activationSecret;
    private String activationSecretOld;
    
    private final String filepath;
    
    private ReentrantReadWriteLock instanceLock = new ReentrantReadWriteLock(true);
    
    public AuthConfig(String filepath) {
        super(filepath);
        this.filepath = filepath;
        ConfigFile config = getConfigFile();
        String auth = null;
        String authOld = null;
        String activation = null;
        String activationOld = null;
        try {
            instanceLock.writeLock().lock();
            auth = config.read("auth");
            authOld = config.read("auth_old");
            activation = config.read("activation");
            activationOld = config.read("activation_old");
            if(auth == null || activation == null) {
                throw new Exception("Error in keys.xml");
            }
            else if(auth.equals(activation)) {
                throw new Exception("key for auth is same as activation. Not Recommended : use 2 different keys.");
            }
        } catch (Exception ex) {
            auth = "ifDdJaJ+RcWFPUVSymIjLe5PHc4plksmKFwSfa7KNxQ="; // clé par défaut de secours
            activation = "MRyZ57CFn/h2p4j7co9fUbt18q4nrGj+53nikbvdfHs="; // clé par défaut de secours
            Logger.getLogger(AuthConfig.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            authSecret = auth;
            authSecretOld = authOld;
            activationSecret = activation;
            activationSecretOld = activationOld;
            instanceLock.writeLock().unlock();
        }
    }
    
    public void saveKeys() {
        Properties config = new Properties();
        
        try {
            instanceLock.readLock().lock();
            config.setProperty("auth", authSecret);
            if(authSecretOld != null) {
                config.setProperty("auth_old", authSecretOld);
            }
            
            config.setProperty("activation", activationSecret);
            if(activationSecretOld != null) {
                config.setProperty("activation_old", activationSecretOld);
            }
        } finally {
           instanceLock.readLock().unlock(); 
        }
        
        FileOutputStream filestream;
        try {
            filestream = new FileOutputStream(new java.io.File(filepath));
            config.storeToXML(
                    filestream, "\n"
                            + "Les clés utilisées pour signer les tokens\n"
                            + "- auth: pour les tokens d'authentification\n"
                            + "- activation: pour les tokens d'activation\n",
                    "UTF-8"
            );
        } catch (IOException ex) {
            Logger.getLogger(AuthConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String syncGet(StringGetter stringGetter) {
        try {
            instanceLock.readLock().lock();
            return stringGetter.get();
        } finally {
            instanceLock.readLock().unlock();
        }
    }
    
    private void syncSet(StringSetter stringSetter) {
        try {
            instanceLock.writeLock().lock();
            stringSetter.set();
        } finally {
            instanceLock.writeLock().unlock();
        }
    }

    public String getAuthSecret() {
        return syncGet(() -> {
            return authSecret;
        });
    }

    public void setAuthSecret(String authSecret) {
        syncSet(() -> {
            this.authSecret = authSecret;
        });
    }

    public String getAuthSecretOld() {
        return syncGet(() -> {
            return authSecretOld;
        });
    }

    public void setAuthSecretOld(String authSecretOld) {
        syncSet(() -> {
            this.authSecretOld = authSecretOld;
        });
    }

    public String getActivationSecret() {
        return syncGet(() -> {
            return activationSecret;
        });
    }

    public void setActivationSecret(String activationSecret) {
        syncSet(() -> {
            this.activationSecret = activationSecret;
        });
    }

    public String getActivationSecretOld() {
        return syncGet(() -> {
            return activationSecretOld;
        });
    }

    public void setActivationSecretOld(String activationSecretOld) {
        syncSet(() -> {
            this.activationSecretOld = activationSecretOld;
        });
    }
}
