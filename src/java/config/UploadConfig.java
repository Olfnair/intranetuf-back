/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

/**
 *
 * @author Florian
 */
public class UploadConfig extends Config {
    private final String[] allowedExtensions;   
    
    public UploadConfig(String filepath) {
        super(filepath);
        String extensions = "";
        try {
            ConfigFile config = getConfigFile();
            extensions = config.read("extensions");
        } catch(Exception e) {
            
        } finally {
            allowedExtensions = extensions.replaceAll("[.\\s]+", "").split(",");
        }
    }

    public String[] getAllowedExtensions() {
        return allowedExtensions;
    }
}
