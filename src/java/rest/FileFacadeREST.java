/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package rest;

import entities.File;
import entities.Project;
import entities.ProjectRight;
import entities.User;
import entities.Version;
import entities.query.ParamsParser;
import files.Upload;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.cxf.jaxrs.ext.MessageContext;
import rest.security.AuthToken;
import rest.security.Authentication;
import rest.security.RightsChecker;
import utils.UrlBase64;

/**
 *
 * @author Florian
 */
@Stateless
@Path("entities.file")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class FileFacadeREST extends AbstractFacade<File> {
    
    @PersistenceContext(unitName = "IUFPU")
    private EntityManager em;
    
    public FileFacadeREST() {
        super(File.class);
    }
    
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response create(
            @Context MessageContext jaxrsContext,
            @Multipart("entity") File entity,
            @Multipart("file") InputStream uploadedInputStream,
            @Multipart("file") Attachment attachment) {
        // TODO : check extension
        // TODO : check file_size et décider d'un max
        AuthToken token = Authentication.validate(jaxrsContext);
        Project project = em.find(Project.class, entity.getProject().getId());
        
        User author;
        // check droits
        try { // seulement si l'utilisateur peut ajouter des fichiers à ce project
            author = RightsChecker.getInstance(em).validate(token, User.Roles.USER, project.getId(), ProjectRight.Rights.ADDFILES);
        }
        catch(WebApplicationException e) { // ou qu'il est admin
            author = RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        }
        
        try {
            Version version = entity.getVersion();
            version.setFile(entity);
            version.setDate_upload(Instant.now().getEpochSecond());
            version.initWorkflowChecks();
            entity.setAuthor(author);
            em.persist(entity);
            new Upload(uploadedInputStream, project.getId().toString(), version.getId().toString()).run();
            return Response.status(201).build();
        }
        catch(Exception e) {
            throw new WebApplicationException(Response.status(500).build());
        }
    }
    
    @PUT
    @Path("{id}")
    public Response edit(@PathParam("id") Long id, File entity) {
        return super.edit(entity);
    }
    
    @DELETE
    @Path("{id}")
    public Response edit(@Context MessageContext jaxrsContext, @PathParam("id") Long id) {
        AuthToken token = Authentication.validate(jaxrsContext);
        
        javax.persistence.Query projectQuery = em.createNamedQuery("File.getProject");
        projectQuery.setParameter("fileId", id);
        List<Project> projectResult = projectQuery.getResultList();
        if(projectResult == null || projectResult.size() < 1) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        Project project = projectResult.get(0);
        
        // vérifie que l'utilisateur a le droit de supprimer un fichier ou est admin
        try {
            RightsChecker.getInstance(em).validate(token, User.Roles.USER, project.getId(), ProjectRight.Rights.DELETEFILES);
        }
        catch(WebApplicationException e) {
            RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        }
        
        File file = em.find(File.class, id);
        file.setActive(false);
        return Response.status(Response.Status.OK).build();
    }
    
    @GET
    @Path("{id}")
    public Response find(@PathParam("id") Long id) {
        return super.find(id);
    }
    
    @GET
    @Path("/project/{id}/{whereParams}/{orderbyParams}")
    public Response findByProject(@Context MessageContext jaxrsContext, @PathParam("id") Long id,
            @PathParam("whereParams") String whereParams, @PathParam("orderbyParams") String orderbyParams) {
        AuthToken token = Authentication.validate(jaxrsContext);
        
        // droits
        try { // voir le projet
            RightsChecker.getInstance(em).validate(token, User.Roles.USER, id, ProjectRight.Rights.VIEWPROJECT);
        }
        catch(WebApplicationException e) { // ou role admin
            RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        }
        
        HashMap<String, String> whereMap;
        HashMap<String, String> orderbyMap;
        
        try {
            whereMap = new ParamsParser(UrlBase64.decode(whereParams, "UTF-8")).parse();
            orderbyMap = new ParamsParser(UrlBase64.decode(orderbyParams, "UTF-8")).parse();
        } catch (UnsupportedEncodingException ex) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        whereMap.keySet().forEach((String col) -> {
            File.LIST_BY_PROJECT.addWhereCol(col, whereMap.get(col));
        });
        
        orderbyMap.keySet().forEach((String col) -> {
            File.LIST_BY_PROJECT.addOrderByCol(col, orderbyMap.get(col));
        });
        
        File.LIST_BY_PROJECT.addWhereCol("project.id", id.toString());
        
        javax.persistence.Query filesQuery = File.LIST_BY_PROJECT.getQuery(em);
        
        //filesQuery.setParameter(File.LIST_BY_PROJECT.getParamName("project.id"), id);
        
        /*whereMap.keySet().forEach((String col) -> {
            filesQuery.setParameter(File.LIST_BY_PROJECT.getParamName(col), '%' + whereMap.get(col) + '%');
        });*/
        
        return super.buildResponseList(() -> {
            /*File.LIST_BY_PROJECT.addWhereCol("project.id");
            File.LIST_BY_PROJECT.addOrderByCol("version.filename");
            javax.persistence.Query filesQuery = File.LIST_BY_PROJECT.buildQuery(em);
            filesQuery.setParameter(File.LIST_BY_PROJECT.getParamName("project.id"), id);*/
            return filesQuery.getResultList();
        });
    }
    
    @GET
    @Override
    public Response findAll() {
        return super.findAll();
    }
    
    @GET
    @Path("{from}/{to}")
    public Response findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }
    
    @GET
    @Path("count")
    public Response countREST() {
        return super.count();
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
