/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Florian
 */
@Entity
@XmlRootElement
public class ProjectRight implements Serializable {
    // droits sur le projet :
    //  1 : voir le projet et ses fichiers
    //  2 : editer le projet (changer le nom)
    //  4 : supprimer le projet
    //  8 : ajouter des fichiers au projet
    // 16 : supprimer des fichiers du projet
    // 32 : controler des fichiers du projet
    // 64 : valider les fichiers du projet
    // admin = tous les droits sur tous les projets
    public static int VIEWPROJECT = 1;
    public static int EDITPROJECT = 2;
    public static int DELETEPROJECT = 4;
    public static int ADDFILES = 8;
    public static int DELETEFILES = 16;
    public static int CONTROLFILE = 32;
    public static int VALIDATEFILE = 64;
    

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private int rights = 0;
    
    @ManyToOne(fetch=FetchType.EAGER)
    private Project project;
    
    @ManyToOne(fetch=FetchType.EAGER)
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRights() {
        return rights;
    }

    public void setRights(int rights) {
        this.rights = rights;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
        if (!(object instanceof ProjectRight)) {
            return false;
        }
        ProjectRight other = (ProjectRight) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.ProjectRight[ id=" + id + " ]";
    }
    
}
