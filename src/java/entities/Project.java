/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import entities.query.ComplexQuery;
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
    public static final ComplexQuery LIST_ALL_OTHER_PROJECTS;
    
    static {
        LIST_ALL_OTHER_PROJECTS = new ComplexQuery("SELECT p FROM Project p WHERE p.id NOT IN(:fetchedIds) :where: :orderby:", "p");
        LIST_ALL_OTHER_PROJECTS.addWhereSpec("name", "name", "LIKE", "AND");
        LIST_ALL_OTHER_PROJECTS.addOrderBySpec("name");
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
    private List<ProjectRight> projectRights = new ArrayList();

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
