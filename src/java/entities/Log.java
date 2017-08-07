/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Florian
 */
@Entity
@XmlRootElement
public class Log extends entities.Entity {
    private static final long serialVersionUID = 1L;
    
    private String type;
    private String message;
    
    @NotNull
    private Long logdate;
    
    @ManyToOne(fetch=FetchType.EAGER)
    private User user;
    
    @ManyToOne(fetch=FetchType.EAGER)
    private File file;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getLogdate() {
        return logdate;
    }

    public void setLogdate(Long logdate) {
        this.logdate = logdate;
    }

    @Override
    public String toString() {
        return "entities.Log[ id=" + getId() + " ]";
    }
    
}
