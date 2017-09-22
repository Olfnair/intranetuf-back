/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@NamedQueries({
    @NamedQuery(name="ProjectRight.GetByUserAndProject", query="SELECT pr FROM ProjectRight pr WHERE pr.user.id = :userId AND pr.project.id = :projectId"),
    @NamedQuery(name="ProjectRight.ListForUserAndProjects", query="SELECT pr FROM ProjectRight pr WHERE pr.user.id = :entityId AND pr.project.id IN(:entitiesIds)"),
    @NamedQuery(name="ProjectRight.ListForProjectAndUsers", query="SELECT pr FROM ProjectRight pr WHERE pr.project.id = :entityId AND pr.user.id IN(:entitiesIds)")
})
public class ProjectRight extends entities.Entity {
    private static final long serialVersionUID = 1L;
    
    public final static class Rights {
        // droits sur le projet :
        public static final int VIEWPROJECT     =  1; // voir le projet et ses fichiers
        public static final int EDITPROJECT     =  2; // editer le projet (changer le nom)
        public static final int DELETEPROJECT   =  4; // supprimer le projet
        public static final int ADDFILES        =  8; // ajouter des fichiers au projet
        public static final int DELETEFILES     = 16; // supprimer des fichiers du projet
        public static final int CONTROLFILE     = 32; // controler des fichiers du projet
        public static final int VALIDATEFILE    = 64; // valider les fichiers du projet
        // admin = tous les droits sur tous les projets
        // combiner les droits : 1 + 8 = 9 => voir projet et ajouter fichiers
        // Autrement dit, chaque bit de 'rights' correspond à un droit qui est actif ou pas (0 ou 1)
        // il reste des bits inutilisés pour ajouter des droits si nécessaire
    }
    
    private int rights = 0;
    
    @ManyToOne(fetch=FetchType.EAGER)
    private Project project;
    
    @ManyToOne(fetch=FetchType.EAGER)
    private User user;
    
    public ProjectRight() {}
    
    public ProjectRight(Long id) {
        super(id);
    }
    
    public ProjectRight(User user, Project project) {
        this.user = user;
        this.project = project;
    }

    public int getRights() {
        return rights;
    }

    public void setRights(int rights) {
        this.rights = rights;
    }
    
    public boolean hasRight(int right) {
        return (this.rights & right) > 0 || right == 0;
    }
    
    public void setRight(int right) {
        this.rights |= right;
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
    public String toString() {
        return "entities.ProjectRight[ id=" + getId() + " ]";
    }
    
}
