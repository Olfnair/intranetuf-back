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
    private Project project;
    
    @ManyToOne(fetch=FetchType.EAGER)
    private File file;
    
    @ManyToOne(fetch=FetchType.EAGER)
    private Version version;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
        return "entities.Log[ id=" + getId() + " ]";
    }
    
}
