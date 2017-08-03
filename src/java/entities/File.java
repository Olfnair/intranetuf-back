/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import entities.query.FlexQuerySpecification;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
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
@NamedQueries({
    @NamedQuery(name="File.byVersion", query="SELECT f FROM File f JOIN FETCH f.project WHERE f.version.id = :versionId"),
    @NamedQuery(name="File.getProject", query="SELECT f.project FROM File f WHERE f.id = :fileId")
})
public class File implements Serializable {
    
    public final static FlexQuerySpecification<File> LIST_BY_PROJECT; // utilisé pour afficher la liste des fichiers contenus dans un projet
    
    static {
        LIST_BY_PROJECT = new FlexQuerySpecification<>("SELECT f FROM File f WHERE f.active = true :where: :orderby:", "f", File.class);
        LIST_BY_PROJECT.addWhereSpec("project.id", "projectId", "=", "AND", Long.class);
        LIST_BY_PROJECT.addWhereSpec("version.filename", "versionFilename", "LIKE", "AND", String.class);
        LIST_BY_PROJECT.addWhereSpec("version.num", "versionNum", "=", "AND", Long.class);
        LIST_BY_PROJECT.addWhereSpec("author", "authorName", "LIKE", "AND", String.class);
        LIST_BY_PROJECT.addWhereClauseReplacer("author", "concat(f.author.firstname, ' ', f.author.name)");
        LIST_BY_PROJECT.addOrderBySpec("version.filename");
        LIST_BY_PROJECT.addOrderBySpec("version.num");
        LIST_BY_PROJECT.addOrderBySpec("version.date_upload");
        LIST_BY_PROJECT.addOrderBySpec("author");
        LIST_BY_PROJECT.addOrderByClauseReplacer("author", "f.author.name, f.author.firstname");
        LIST_BY_PROJECT.addOrderBySpec("version.status");
        LIST_BY_PROJECT.addDefaultOrderByClause("version.filename", "ASC");
    }

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
    private List<Version> versions = new ArrayList<>();
    
    @OneToMany(mappedBy="file", fetch=FetchType.LAZY)
    private List<Log> logs = new ArrayList<>();

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

    public List<Version> getVersions() {
        return versions;
    }

    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }
    
    public List<Log> getLogs() {
        return logs;
    }

    public void setLogs(List<Log> logs) {
        this.logs = logs;
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
