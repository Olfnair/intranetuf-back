/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import entities.query.FlexQuery;
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
@NamedQueries({
    // renvoie l'id de l'utilisateur ayant :userId et le droit :right sur :projectId. Utile pour vérifier les droits d'un utilisateur à partir de son ID : retourne userId s'il a le droit, sinon rien
    @NamedQuery(name="ProjectRight.UserHasRight", query="SELECT pr.user FROM ProjectRight pr WHERE pr.user.id = :userId AND pr.project.id = :projectId AND MOD(pr.rights/:right, 2) >= 1"),
    @NamedQuery(name="ProjectRight.GetByUserAndProject", query="SELECT pr FROM ProjectRight pr WHERE pr.user.id = :userId AND pr.project.id = :projectId")
})
public class ProjectRight implements Serializable {
    public final static FlexQuery LIST_BY_USER;
    
    static {
        LIST_BY_USER = new FlexQuery("SELECT pr FROM ProjectRight pr WHERE pr.user.id = :userId :where: :orderby:", "pr");
        LIST_BY_USER.addWhereSpec("project.name", "projectName", "LIKE", "AND", String.class);
        LIST_BY_USER.addOrderBySpec("project.name");
    }
    
    
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
    

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private int rights = 0;
    
    @ManyToOne(fetch=FetchType.EAGER)
    private Project project;
    
    @ManyToOne(fetch=FetchType.EAGER)
    private User user;

    public ProjectRight() {
    }
    
    public ProjectRight(User user, Project project) {
        this.user = user;
        this.project = project;
    }
    
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
