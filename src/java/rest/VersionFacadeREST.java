/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import config.ApplicationConfig;
import dao.DAOLog;
import dao.DAOVersion;
import entities.Project;
import entities.File;
import entities.Log;
import entities.ProjectRight;
import entities.User;
import entities.Version;
import entities.query.FlexQuery;
import files.ExtensionChecker;
import files.MultiPartManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.apache.cxf.jaxrs.ext.MessageContext;
import rest.security.AuthToken;
import rest.security.Authentication;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import rest.security.RightsChecker;
import utils.Base64Url;

/**
 *
 * @author Florian
 */
@Stateless
@Path("entities.version")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class VersionFacadeREST extends AbstractFacade<Version> {

    @PersistenceContext(unitName = "IUFPU")
    private EntityManager em;

    public VersionFacadeREST() {
        super(Version.class);
    }
    
    @GET
    @Path("{versionId}/other/{fileId}/{whereParams}/{orderbyParams}/{index}/{limit}")
    public Response getOtherVersions(
            @Context MessageContext jaxrsContext,
            @PathParam("versionId") Long versionId,
            @PathParam("fileId") Long fileId,
            @PathParam("whereParams") String whereParams,
            @PathParam("orderbyParams") String orderbyParams,
            @PathParam("index") Integer index,
            @PathParam("limit") Integer limit
    ) {
        AuthToken token = Authentication.validate(jaxrsContext);
        RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        
        FlexQuery<Version> versionsQuery = new FlexQuery<>(Version.LIST_OTHER_VERSIONS);
        try {
            versionsQuery.setParameters(Base64Url.decode(whereParams) + "col: 'file.id', param: '" + fileId + "'col: 'id', param: '" + versionId +"'",
                    Base64Url.decode(orderbyParams),
                    index, limit
            );
        } catch (UnsupportedEncodingException ex) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        versionsQuery.prepareCountQuery(em);
        return Response.ok(versionsQuery.execute()).build();
    }
    
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response create(@Context HttpServletRequest request) {
        try {
            // récupération des paramètres multipart :
            MultiPartManager multipartManager = new MultiPartManager(request);
            Version version = multipartManager.getEntity("entity", Version.class);
            Part uploadedFilePart = multipartManager.get("file");
            
            // vérification de l'extension :
            if(! new ExtensionChecker(ApplicationConfig.UPLOAD_CONFIG).check(version.getFilename())) {
                throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
            }
                    
            // Auth :
            AuthToken token = Authentication.validate(request.getHeader("Authorization"));
            
            File file;
            Project project;
            try {
                file = em.find(File.class, version.getFile().getId());
                project = em.find(Project.class, file.getProject().getId());
            }
            catch(Exception e) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            
            User author;
            // Check droits :
            try { // seulement si l'utilisateur peut ajouter des fichiers à ce project
                author = RightsChecker.getInstance(em).validate(token, User.Roles.USER, project.getId(), ProjectRight.Rights.ADDFILES);
            }
            catch(WebApplicationException e) { // ou qu'il est admin
                author = RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
            }
            
            // vérifie que le token est bien celui de l'auteur ou de l'admin
            if(file.getAuthor().getId().longValue() != author.getId().longValue() && ! author.isAdmin()) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }
            
            version.setFile(file);
            version.setNum(file.getVersion().getNum() + 1);
            version.setDate_upload(Instant.now().getEpochSecond());
            em.persist(version);
            em.flush();
            new DAOVersion(version, em).initWorkflowChecks();
            em.merge(version);
            file.setVersion(version);
            em.merge(file);
            uploadedFilePart.write(ApplicationConfig.PROJECTS_LOCATION + '/' + project.getId().toString() + '/' + version.getId().toString());
            multipartManager.close();
            
            new DAOLog(em).log(author, Log.Type.CREATE_VERSION, "", project, version);   
            return Response.status(Response.Status.CREATED).build();     
        } catch (IOException | ServletException ex) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        } catch (IllegalStateException ex) {
            throw new WebApplicationException(413);
        }
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
