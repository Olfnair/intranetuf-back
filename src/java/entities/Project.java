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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Florian
 */
@Entity
@XmlRootElement
public class Project implements Serializable {
    public static final FlexQuerySpecification<Project> PROJECTLIST_FOR_USER;
    public static final FlexQuerySpecification<Project> PROJECTLIST_FOR_ADMIN;
    public static final FlexQuerySpecification<Project> LIST_FOR_RIGHTS;
    
    static {
        // attention à conserver un espace lors des retours à la ligne dans l'écriture des requêtes...
        PROJECTLIST_FOR_USER = new FlexQuerySpecification<>("SELECT p FROM Project p INNER JOIN p.projectRights pr "
                + "WHERE p.active = true AND pr.user.id = :userId AND pr.project.id = p.id "
                + "AND MOD(pr.rights/:right, 2) >= 1 :where: ORDER BY p.name ASC", "p", Project.class);
        PROJECTLIST_FOR_USER.addWhereSpec("name", "projectName", "LIKE", "AND", String.class);
        
        PROJECTLIST_FOR_ADMIN = new FlexQuerySpecification<>("SELECT p FROM Project p :where: :orderby:", "p", Project.class);
        PROJECTLIST_FOR_ADMIN.addWhereSpec("name", "projectName", "LIKE", "AND", String.class);
        PROJECTLIST_FOR_ADMIN.addWhereSpec("active", "projectActive", "=", "AND", boolean.class);
        PROJECTLIST_FOR_ADMIN.addOrderBySpec("name");
        PROJECTLIST_FOR_ADMIN.addOrderBySpec("active");
        PROJECTLIST_FOR_ADMIN.addDefaultOrderByClause("name", "ASC");
        
        LIST_FOR_RIGHTS = new FlexQuerySpecification<>("SELECT p FROM Project p :where: :orderby:", "p", Project.class);
        LIST_FOR_RIGHTS.addWhereSpec("name", "projectName", "LIKE", "AND", String.class);
        LIST_FOR_RIGHTS.addOrderBySpec("name");
        LIST_FOR_RIGHTS.addOrderBySpec("active");
    }

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    private String name;
    
    private boolean active = true;
    
    @OneToMany(mappedBy="project", fetch=FetchType.LAZY)
    private List<File> files = new ArrayList<>();
    
    @OneToMany(mappedBy="project", fetch=FetchType.LAZY)
    private List<ProjectRight> projectRights = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @XmlTransient
    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    @XmlTransient
    public List<ProjectRight> getProjectRights() {
        return projectRights;
    }

    public void setProjectRights(List<ProjectRight> projectRights) {
        this.projectRights = projectRights;
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
        if (!(object instanceof Project)) {
            return false;
        }
        Project other = (Project) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Project[ id=" + id + " ]";
    }
    
}
