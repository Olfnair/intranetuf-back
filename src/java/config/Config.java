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
public class Config {
    private final ConfigFile configFile;
    
    public Config(String filepath) {
        configFile = new ConfigFile(filepath);
    }

    public final ConfigFile getConfigFile() {
        return configFile;
    }
}
