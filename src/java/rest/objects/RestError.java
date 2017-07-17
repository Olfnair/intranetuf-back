/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package rest.objects;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Florian
 */
@XmlRootElement
public class RestError {
    private String message = "";
    
    public RestError() {
    }
    
    public RestError(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
}
