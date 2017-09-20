/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package files;

import config.UploadConfig;

/**
 *
 * @author Florian
 */
public class ExtensionChecker {
    
    private static String getExtension(String filename) {
        int index = filename.lastIndexOf(".");
        if(index < 0 || index == filename.length() - 1) {
            return null;
        }
        String ext = filename.substring(index + 1);
        if(ext.isEmpty()) {
            return null;
        }
        return ext;
    }
    
    private final String[] allowedExtensions;
    
    public ExtensionChecker(UploadConfig config) {
        this.allowedExtensions = config.getAllowedExtensions();
    }
    
    public ExtensionChecker(String[] allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }
    
    public ExtensionChecker(String allowedExtensions) {
        this(allowedExtensions.replaceAll("[.\\s]+", "").split(","));
    }
    
    public boolean check(String filename) {
       String ext = getExtension(filename);
       if(ext == null) {
           return false;
       }
       for(String allowedExt : allowedExtensions) {
           if(ext.equals(allowedExt)) {
               return true;
           }
       }
       return false;
    }
}
