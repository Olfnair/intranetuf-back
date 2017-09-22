/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package rest;

import dao.DAOLog;
import entities.Log;
import entities.Version;
import entities.Project;
import entities.ProjectRight;
import entities.User;
import files.Download;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import rest.objects.RestError;
import rest.security.AuthToken;
import rest.security.Authentication;
import rest.security.RightsChecker;

/**
 *
 * @author Florian
 */
@Stateless
@Path("download")
public class DownloadEndpoint {
    @PersistenceContext(unitName = "IUFPU")
    private EntityManager em;
    
    public DownloadEndpoint() {
    }
    
    /**
     * Endpoint qui permet de télécharger un fichier
     * @param jsonToken Token d'authentification
     * @param versionId id de la version (d'un fichier) qu'on veut télécharger
     * @return Le fichier demandé
     */
    @POST
    @Path("{versionId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@FormParam("token") String jsonToken, @PathParam("versionId") Long versionId) {
        AuthToken token;
        try {
            token = Authentication.validate(new String(Base64.getDecoder().decode(jsonToken), "ISO-8859-1"));
        }
        catch(UnsupportedEncodingException e) {
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new RestError("Token Error")).build());
        }
        
        TypedQuery<Project> projectQuery = em.createNamedQuery("Version.getProject", Project.class);
        projectQuery.setParameter("versionId", versionId);
        List<Project> projectList = projectQuery.getResultList();
        if(projectList == null || projectList.size() < 1) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        Project project = projectList.get(0);
        
        // check droits
        // TODO : plus restrictif... Seulement quand le fichier est validé et qu'on peut le voir, sauf pour les auteurs, controleurs, valideurs et admins ou >
        // Actuellement : si on peut voir le projet on peut dl
        try {
            RightsChecker.getInstance(em).validate(token, User.Roles.USER, project.getId(), ProjectRight.Rights.VIEWPROJECT);
        }
        catch(WebApplicationException e) { // les admins peuvent tjrs dl
            RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        }
        
        Version version = em.find(Version.class, versionId);
        if(version == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        boolean error = false;
        try {
            return new Download(version.getFilename(), project.getId().toString(), version.getId().toString()).run();
        }
        catch(Exception e) {
            error = true;
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            if(! error) {
                new DAOLog(em).log(new User(token.getUserId()), Log.Type.DOWNLOAD, "", project, version);
            }
        }
    }
}
