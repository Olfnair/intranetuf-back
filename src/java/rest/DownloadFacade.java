/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 *
 * @author Florian
 */
@Path("download")
public class DownloadFacade {
    private static final String FILE_PATH = "C:\\test.mkv";
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@PathParam("id") Long id) {
        java.io.File file = new java.io.File(FILE_PATH);
        ResponseBuilder response = Response.ok((Object) file);
        AbstractFacade.HEADERS.keySet().forEach((key) -> {
            response.header(key, AbstractFacade.HEADERS.get(key));
        });
        response.header("Content-Disposition", "attachment; filename=test.mkv");
        response.header("Content-Length", "" + file.length());
        return response.build();
    }
}
