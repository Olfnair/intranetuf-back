/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Florian
 */
@Entity
@XmlRootElement
@NamedQuery(name="File.byProject", query="SELECT f FROM File f WHERE f.project.id = :projectId")
public class File implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private boolean active = true;
    
    @OneToOne(fetch=FetchType.EAGER, cascade={CascadeType.PERSIST, CascadeType.REMOVE})
    private Version version;
    
    @ManyToOne(fetch=FetchType.EAGER)
    private User author;
    
    @ManyToOne(fetch=FetchType.LAZY)
    private Project project;
    
    @OneToMany(mappedBy="file", fetch=FetchType.LAZY)
    private Collection<Version> versions;
    
    @OneToMany(mappedBy="file", fetch=FetchType.LAZY)
    private Collection<Log> logs;
    
    @OneToMany(mappedBy="file", fetch=FetchType.LAZY)
    private Collection<FileAction> fileActions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Collection<Version> getVersions() {
        return versions;
    }

    public void setVersions(Collection<Version> versions) {
        this.versions = versions;
    }
    
    public Collection<Log> getLogs() {
        return logs;
    }

    public void setLogs(Collection<Log> logs) {
        this.logs = logs;
    }

    public Collection<FileAction> getFileActions() {
        return fileActions;
    }

    public void setFileActions(Collection<FileAction> fileActions) {
        this.fileActions = fileActions;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof File)) {
            return false;
        }
        File other = (File) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.File[ id=" + id + " ]";
    }
    
}
