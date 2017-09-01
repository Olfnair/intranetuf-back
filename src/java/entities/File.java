/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import entities.query.FlexQuerySpecification;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
    @NamedQuery(name="File.getProject", query="SELECT f.project FROM File f WHERE f.id = :fileId"),
    @NamedQuery(name="File.getAvailableByProject", query="SELECT f.id FROM File f WHERE f.project.id = :projectId AND (f.version.status = :versionStatus OR f.author.id = :userId)"),
    @NamedQuery(name="File.getForCheckersByProject", query="SELECT f.id FROM File f INNER JOIN f.version.workflowChecks as checks WHERE f.project.id = :projectId AND f.version.id = checks.version.id AND checks.user.id = :userId AND checks.status = :checkStatus")
})
public class File extends entities.Entity {
    private static final long serialVersionUID = 1L;
    
    public final static FlexQuerySpecification<File> LIST_BY_PROJECT; // utilisé pour afficher la liste des fichiers contenus dans un projet
    public final static FlexQuerySpecification<File> LIST_BY_USER; // utilisé pour lister les fichiers par utilisateur
    
    static {
        LIST_BY_PROJECT = new FlexQuerySpecification<>("SELECT f FROM File f WHERE f.active = true :where: :orderby:", "f", File.class);
        LIST_BY_PROJECT.addWhereSpec("project.id", "projectId", "=", "AND", Long.class);
        LIST_BY_PROJECT.addWhereSpec("version.filename", "versionFilename", "LIKE", "AND", String.class);
        LIST_BY_PROJECT.addWhereSpec("version.num", "versionNum", "=", "AND", Long.class);
        LIST_BY_PROJECT.addWhereSpec("author", "authorName", "LIKE", "AND", String.class);
        LIST_BY_PROJECT.addWhereClauseReplacer("author", "concat(f.author.firstname, ' ', f.author.name)");
        LIST_BY_PROJECT.addWhereSpec("id", "filesIds", "IN", "AND", List.class);
        LIST_BY_PROJECT.addOrderBySpec("version.filename");
        LIST_BY_PROJECT.addOrderBySpec("version.num");
        LIST_BY_PROJECT.addOrderBySpec("version.date_upload");
        LIST_BY_PROJECT.addOrderBySpec("author");
        LIST_BY_PROJECT.addOrderByClauseReplacer("author", "f.author.name, f.author.firstname");
        LIST_BY_PROJECT.addOrderBySpec("version.status");
        LIST_BY_PROJECT.addDefaultOrderByClause("version.filename", "ASC");
        
        LIST_BY_USER = new FlexQuerySpecification<>("SELECT f FROM File f JOIN FETCH f.project WHERE f.active = true :where: :orderby:", "f", File.class);
        LIST_BY_USER.addWhereSpec("author.id", "authorId", "=", "AND", Long.class);
        LIST_BY_USER.addWhereSpec("project.name", "projectName", "LIKE", "AND", String.class);
        LIST_BY_USER.addWhereSpec("version.filename", "versionFilename", "LIKE", "AND", String.class);
        LIST_BY_USER.addWhereSpec("version.num", "versionNum", "=", "AND", Long.class);
        LIST_BY_USER.addOrderBySpec("version.filename");
        LIST_BY_USER.addOrderBySpec("version.num");
        LIST_BY_USER.addOrderBySpec("version.date_upload");
        LIST_BY_USER.addDefaultOrderByClause("version.filename", "ASC");
    }
    
    private boolean active = true;
    
    @OneToOne(fetch=FetchType.EAGER, cascade={CascadeType.ALL})
    private Version version;
    
    @ManyToOne(fetch=FetchType.EAGER)
    private User author;
    
    @ManyToOne(fetch=FetchType.LAZY)
    private Project project;
    
    @OneToMany(mappedBy="file", fetch=FetchType.LAZY)
    private List<Version> versions = new ArrayList<>();
    
    @OneToMany(mappedBy="file", fetch=FetchType.LAZY)
    private List<Log> logs = new ArrayList<>();

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
    public String toString() {
        return "entities.File[ id=" + getId() + " ]";
    }
    
}
