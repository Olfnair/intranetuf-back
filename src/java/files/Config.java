/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package files;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Florian
 */
public class Config {
    public final static String PROJECTS_LOCATION;
    public final static String PROPERTIES_LOCATION;
    public final static String KEYS_LOCATION;
    
    static {
        ConfigFile configFile = new ConfigFile("global.properties", true);
        String projects = null;
        String properties = null;
        String keys = null;
        try {
            projects = configFile.read("projects");
            properties = configFile.read("config");
            keys = configFile.read("keys");
            if(projects == null || properties == null) {
                throw new Exception("Error in global.properties");
            }
        } catch (IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            PROJECTS_LOCATION = projects;
            PROPERTIES_LOCATION = properties;
            KEYS_LOCATION = keys;
        }
    }
    
    private static String nameFormat(String name) {
        return name.replaceAll("\\s", "_"); // remplace tous les caractères d'espacement par '_'
    }
    
    public static String combineNameWithId(String name, Long id) {
        name = nameFormat(name);
        int extIndex = name.lastIndexOf('.');
        if(extIndex == -1) {
            extIndex = name.length();
        }
        String file = name.substring(0, extIndex);
        String ext = name.substring(extIndex);
        return file + '_' + id.toString() + ext;
    }
}
