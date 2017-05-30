/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;  
import java.io.FileOutputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

/**
 *
 * @author Florian
 */

@Path("upload")
public class UploadFacade {
    
    /*@POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    //@Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response uploadFile(@Multipart("note") String note,
            @Multipart("upfile") Attachment attachment) throws IOException {
        
        String filename = attachment.getContentDisposition().getParameter("filename");
        
        java.nio.file.Path path = Paths.get("C:/Users/Florian/data/" + filename);
        Files.deleteIfExists(path);
        InputStream in = attachment.getObject(InputStream.class);
        
        Files.copy(in, path);
        Response.ResponseBuilder response = Response.status(201);
        AbstractFacade.HEADERS.keySet().forEach((key) -> {
            response.header(key, AbstractFacade.HEADERS.get(key));
        });
        return response.build();
    }*/
    
    /*@POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    //@Consumes(MediaType.MULTIPART_FORM_DATA)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response uploadFile(@FormParam("filename") String filename,
            /*@Multipart("upfile") Attachment attachment*//*InputStream in) throws IOException {
        
        //String filename = attachment.getContentDisposition().getParameter("filename");
        
        java.nio.file.Path path = Paths.get("C:/Users/Florian/data/" + filename);
        Files.deleteIfExists(path);
        //InputStream in = attachment.getObject(InputStream.class);
        
        Files.copy(in, path);
        Response.ResponseBuilder response = Response.status(201);
        AbstractFacade.HEADERS.keySet().forEach((key) -> {
            response.header(key, AbstractFacade.HEADERS.get(key));
        });
        return response.build();
    }*/
    
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response uploadFile(
            @Multipart("file") InputStream uploadedInputStream,
            @Multipart("file") Attachment attachment) {
        
        String filename = attachment.getContentDisposition().getParameter("filename");
        String fileLocation = "C:/Users/Florian/data/" + filename;
        //saving file
        byte[] bytes = new byte[1024 * 1024];
        int read;
        FileOutputStream out;
        try {
            out = new FileOutputStream(new File(fileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            return Response.status(500).build();
        }
        
        return Response.status(201).build();
    }  
}
