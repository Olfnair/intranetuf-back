/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author Florian
 */
@javax.ws.rs.ApplicationPath("rest")
public class ApplicationConfig extends Application {
    public final static String PROJECTS_LOCATION = "C:\\IUF_data\\";
    
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(rest.AuthenticationEndpoint.class);
        resources.add(rest.ControlFacadeREST.class);
        resources.add(rest.DateFacadeREST.class);
        resources.add(rest.DownloadFacade.class);
        resources.add(rest.FileFacadeREST.class);
        resources.add(rest.LogFacadeREST.class);
        resources.add(rest.ProjectFacadeREST.class);
        resources.add(rest.RoleFacadeREST.class);
        resources.add(rest.UploadFacade.class);
        resources.add(rest.UserFacadeREST.class);
        resources.add(rest.ValidationFacadeREST.class);
        resources.add(rest.VersionFacadeREST.class);
    }
    
}
