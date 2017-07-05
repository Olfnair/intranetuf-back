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
public class RestLong {
    private long value;
    
    public RestLong() {
    }
    
    public RestLong(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
    
}
