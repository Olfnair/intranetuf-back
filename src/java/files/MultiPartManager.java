/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package files;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

/**
 *
 * @author Florian
 */
public class MultiPartManager {
    private final static String DEFAULT_ENCODING = "UTF-8"; 
    
    private String encoding;
    private HttpServletRequest request;
    
    private HashMap<String, Part> parts = new HashMap<>();
    
    public MultiPartManager(HttpServletRequest request) {
        this(request, DEFAULT_ENCODING);
    }
    
    public MultiPartManager(HttpServletRequest request, String encoding) {
        this.request = request;
        this.encoding = encoding;
    }
    
    private void addPart(String name, Part part) throws IOException {
        if(parts.containsKey(name)) {
            parts.get(name).delete();
        }
        parts.put(name, part);
    }
    
    public <T> T getEntity(String name, Class<T> classOfT) throws IOException, IllegalStateException, ServletException {
        StringBuilder jsonEntityBuilder = new StringBuilder();
        Part entityPart = request.getPart(name);
        addPart(name, entityPart);
        BufferedReader reader = new BufferedReader(new InputStreamReader(entityPart.getInputStream(), encoding));
        char[] buffer = new char[2048];
        for (int length; (length = reader.read(buffer)) > 0;) {
            jsonEntityBuilder.append(buffer, 0, length);
        }
        reader.close();
        
        String jsonEntity = jsonEntityBuilder.toString();
        Gson gson = new Gson();    
        return gson.fromJson(jsonEntity, classOfT);
    }
    
    public Part get(String name) throws IOException, IllegalStateException, ServletException {
        Part part = request.getPart(name);
        addPart(name, part);
        return part;
    }
    
    public void close() {
        parts.forEach((name, part) -> {
            try {
                part.delete();
            } catch (IOException ex) {
                Logger.getLogger(MultiPartManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
}
