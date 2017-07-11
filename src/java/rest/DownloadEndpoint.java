/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package rest;

import config.ApplicationConfig;
import entities.File;
import entities.Version;
import entities.Project;
import entities.ProjectRight;
import entities.User;
import files.Download;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import rest.security.AuthToken;
import rest.security.Authentication;
import rest.security.RightsChecker;

/**
 *
 * @author Florian
 */
@Path("download")
public class DownloadEndpoint {
    @PersistenceContext(unitName = "IUFPU")
    private EntityManager em;
    
    public DownloadEndpoint() {
    }
    
    @GET
    @Path("{versionId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@QueryParam("token") String jsonToken, @PathParam("versionId") Long versionId) {
        AuthToken token = Authentication.validate(jsonToken);
        
        javax.persistence.Query filesQuery = em.createNamedQuery("File.byVersion");
        filesQuery.setParameter("versionId", versionId);
        List<File> fileList = filesQuery.getResultList();
        if(fileList == null || fileList.size() < 1) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        File file = fileList.get(0);
        Project project = file.getProject();
        if(project == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        // check droits
        // TODO : plus restrictif... Seulement quand le fichier est validé et qu'on peut le voir, sauf pour les auteurs, controleurs, valideurs et admins ou >
        // Actuellement : si on peut voir le projet on peut dl
        try {
            RightsChecker.getInstance(em).validate(token, User.Roles.USER, project.getId(), ProjectRight.Rights.VIEWPROJECT);
        }
        catch(WebApplicationException e) { // les admins peuvent tjrs dl
            RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        }
        
        Version version = em.find(Version.class, file.getVersion().getId());
        if(version == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        try {
            return new Download(version.getFilename(), ApplicationConfig.combineNameWithId(project.getName(), project.getId()),
                    ApplicationConfig.combineNameWithId(version.getFilename(), versionId)).run();
        }
        catch(Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
