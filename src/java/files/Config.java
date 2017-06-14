/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package files;

import entities.Project;
import entities.Version;

/**
 *
 * @author Florian
 */
public class Config {
    public final static String PROJECTS_LOCATION = "C:/IUF_data";
    
    private static String nameFormat(String name) {
        return name.replaceAll("\\s", "_"); // remplace tous les caractères d'espacement par '_'
    }
    
    public static String getProjectFolder(Project project) {
        return nameFormat(project.getName()) + '_' + project.getId().toString();
    }
    
    public static String getFileName(Version version) {
        String name = nameFormat(version.getFilename());
        int extIndex = name.lastIndexOf('.', 0);
        String file = name.substring(0, extIndex);
        String ext = name.substring(extIndex);
        return file + '_' + version.getId().toString() + ext;
    }
}
