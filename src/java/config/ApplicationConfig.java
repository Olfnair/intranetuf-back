/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Application;

/**
 *
 * @author Florian
 */
@javax.ws.rs.ApplicationPath("rest")
public class ApplicationConfig extends Application {
    public final static String PROJECTS_LOCATION;
    public final static String PROPERTIES_LOCATION;
    public final static String KEYS_LOCATION;
    public final static String FRONTEND_URL;
    
    public final static AuthConfig AUTH_CONFIG;
    public final static MailConfig MAIL_CONFIG;
    public final static PBDKF2Config PBDKF2_CONFIG;
    public final static UploadConfig UPLOAD_CONFIG;
    
    static {
        ConfigFile configFile = new ConfigFile("global.xml", true);
        String projects = null;
        String properties = null;
        String keys = null;
        String frontEndURL = null;
        try {
            projects = configFile.read("projects");
            properties = configFile.read("config");
            keys = configFile.read("keys");
            frontEndURL = configFile.read("frontEndURL");
            if(projects == null || properties == null || keys == null || frontEndURL == null) {
                throw new Exception("Error in global.xml");
            }
        } catch (Exception ex) {
            Logger.getLogger(ApplicationConfig.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            PROJECTS_LOCATION = projects;
            PROPERTIES_LOCATION = properties;
            KEYS_LOCATION = keys;
            FRONTEND_URL = frontEndURL;
        }
        
        AUTH_CONFIG = new AuthConfig(KEYS_LOCATION + '/' + "keys.xml");
        MAIL_CONFIG = new MailConfig(PROPERTIES_LOCATION + '/' + "mail.xml");
        PBDKF2_CONFIG = new PBDKF2Config(KEYS_LOCATION + '/' + "hashpass.xml");
        UPLOAD_CONFIG = new UploadConfig(PROPERTIES_LOCATION + '/' + "upload.xml");
    }
    
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(rest.AuthenticationEndpoint.class);
        resources.add(rest.DownloadEndpoint.class);
        resources.add(rest.FileFacadeREST.class);
        resources.add(rest.LogFacadeREST.class);
        resources.add(rest.ProjectFacadeREST.class);
        resources.add(rest.ProjectRightFacadeREST.class);
        resources.add(rest.UserFacadeREST.class);
        resources.add(rest.VersionFacadeREST.class);
        resources.add(rest.WorkflowCheckFacadeREST.class);
    }
}
