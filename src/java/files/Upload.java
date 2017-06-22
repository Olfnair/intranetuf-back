/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package files;

import config.ApplicationConfig;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Florian
 */
public class Upload {
    private final InputStream uploadedInputStream;
    private final String filepath;
    
    public Upload(InputStream uploadedInputStream, String folder, String file) {
        this.uploadedInputStream = uploadedInputStream;
        this.filepath = ApplicationConfig.PROJECTS_LOCATION + '/' +  folder + '/' + file;
    }
    
    public void run () throws IOException {
        byte[] bytes = new byte[1024 * 1024];
        int read;
        FileOutputStream out;
        out = new FileOutputStream(new java.io.File(filepath));
        while ((read = uploadedInputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        out.flush();
        out.close();
    }
}
