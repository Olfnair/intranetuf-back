/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package files;

/**
 *
 * @author Florian
 */
public class Config {
    public final static String PROJECTS_LOCATION = "C:/IUF_data";
    
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
