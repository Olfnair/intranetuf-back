/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package rest;

import entities.Date;
import entities.File;
import entities.Project;
import entities.User;
import entities.Version;
import files.Config;
import files.Upload;
import java.io.InputStream;
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
import rest.security.Authentification;

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
        // TODO : vérifier droits
        // TODO : check extension
        // TODO : check file_size et décider d'un max
        AuthToken token = Authentification.validate(jaxrsContext);
        try {
            User author = em.find(User.class, token.getUserId());
            Version version = entity.getVersion();
            version.setFile(entity);
            version.setDate_upload(Date.now());
            entity.setAuthor(author);
            em.persist(entity);
            Project project = em.find(Project.class, entity.getProject().getId());
            new Upload(uploadedInputStream, Config.combineNameWithId(project.getName(), project.getId()), Config.combineNameWithId(version.getFilename(), version.getId())).run();
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
    public Response remove(@PathParam("id") Long id) {
        return super.remove(id);
    }
    
    @GET
    @Path("{id}")
    public Response find(@PathParam("id") Long id) {
        return super.find(id);
    }
    
    @GET
    @Path("/project/{id}")
    public Response findByProject(@PathParam("id") Long id) {
        return super.buildResponseList(() -> {
            javax.persistence.Query authQuery = em.createNamedQuery("File.byProject");
            authQuery.setParameter("projectId", id);
            return authQuery.getResultList();
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
