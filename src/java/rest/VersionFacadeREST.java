/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import entities.Date;
import entities.Project;
import files.Config;
import files.Upload;
import java.io.InputStream;
import entities.File;
import entities.Version;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import rest.security.AuthToken;
import rest.security.Authentification;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response create(
            @Context MessageContext jaxrsContext,
            @Multipart("entity") Version entity,
            @Multipart("file") InputStream uploadedInputStream,
            @Multipart("file") Attachment attachment) {       
        AuthToken token = Authentification.validate(jaxrsContext);
        // TODO : vérifier que le token est bien celui de l'auteur ou de l'admin
        try {
            File file = em.find(File.class, entity.getFile().getId());  
            entity.setFile(file);
            entity.setNum(file.getVersion().getNum() + 1);
            entity.setDate_upload(Date.now());
            em.persist(entity);
            file.setVersion(entity);
            em.merge(file);
            Project project = em.find(Project.class, file.getProject().getId());
            new Upload(uploadedInputStream, Config.combineNameWithId(project.getName(), project.getId()), Config.combineNameWithId(entity.getFilename(), entity.getId())).run();
            return Response.status(201).build();
        }
        catch(Exception e) {
            throw new WebApplicationException(Response.status(500).build());
        }
    }

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response edit(@PathParam("id") Long id, Version entity) {
        entity.setId(id);
        return super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") Long id) {
        return super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response find(@PathParam("id") Long id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public Response countREST() {
        return super.count();
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
