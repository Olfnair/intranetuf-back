package files;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigFile {
    private final Properties prop = new Properties();
    private final String filename;
    private boolean loaded = false;
    FileInputStream input;
    
    public ConfigFile(String filename) {
        this.filename = filename;
    }
    
    public String read(String property) throws IOException { 
        if(! this.loaded) {
            input = new FileInputStream(filename);
            prop.load(input);
            input.close();
            loaded = true;
        } 
        return prop.getProperty(property);
    }
}