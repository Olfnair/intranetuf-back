/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package files;

import config.ApplicationConfig;
import java.io.IOException;
import javax.ws.rs.core.Response;

/**
 *
 * @author Florian
 */
public class Download {
    private final String filepath;
    private final String outputName;
    
    public Download(String outputName, String folder, String file) {
        this.outputName = outputName;
        this.filepath = ApplicationConfig.PROJECTS_LOCATION + '/' +  folder + '/' + file;
    }
    
    public Response run () throws IOException {
        java.io.File fileDownload = new java.io.File(filepath);
        if(! fileDownload.exists()) {
            throw new IOException("file not found");
        }
        Response.ResponseBuilder response = Response.ok((Object) fileDownload);
        response.header("Content-Disposition", "attachment; filename=\"" + outputName + '"');
        response.header("Content-Length", "" + fileDownload.length());
        return response.build();
    }    
}
