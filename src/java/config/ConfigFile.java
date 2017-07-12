package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigFile {
    private final Properties prop = new Properties();
    private final String filename;
    private boolean loaded = false;
    private boolean isResource = false;
    
    public ConfigFile(String filename) {
        this.filename = filename;
    }
    
    public ConfigFile(String filename, boolean isResource) {
        this.filename = filename;
        this.isResource = isResource;
    }
    
    public String read(String property) throws IOException {
        return read(property, null);
    }      
    
    public String read(String property, String defaultValue) throws IOException {
        if(! this.loaded) {
            if(! isResource) {
                try (InputStream input = new FileInputStream(filename)) {
                    prop.load(input);
                    input.close();
                }
            }
            else {
                try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(filename)) {
                    prop.load(input);
                    input.close();
                }
            }
            loaded = true;
        }
        if(defaultValue == null) {
            return prop.getProperty(property);
        }
        return prop.getProperty(property, defaultValue);
    }
}