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
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Florian
 */
@Entity
@XmlRootElement
@NamedQueries({
    @NamedQuery(name="User.ListAllComplete", query="SELECT u FROM User u JOIN FETCH u.email JOIN FETCH u.login"),
    @NamedQuery(name="User.getWithCredentials", query="SELECT u FROM User u JOIN FETCH u.credentials WHERE u.id = :userId"),
    @NamedQuery(name="User.getByloginWithCredentialsForAuth", query="SELECT u FROM User u JOIN FETCH u.credentials WHERE u.login = :login AND u.active = true AND u.pending = false")
})
public class User implements Serializable {
    
    public final static ComplexQuery LIST_ALL_COMPLETE;
    
    static {
        LIST_ALL_COMPLETE = new ComplexQuery("SELECT u FROM User u JOIN FETCH u.email JOIN FETCH u.login", "u");
        //LIST_ALL_COMPLETE.addColumnSpec("dgfd", "dfgfd", "dfdgd");
    }

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    //@Pattern(regexp = "[a-zA-Z]+", message = "{invalid.name}")
    private String name;
    
    @NotNull
    //@Pattern(regexp = "[a-zA-Z]+", message = "{invalid.firstname}")
    private String firstname;
    
    @Basic(fetch=FetchType.LAZY)
    @NotNull
    
    /*@Pattern(regexp = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\."
        +"[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
        +"(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?",
             message = "{invalid.email}")*/
    // http://emailregex.com/
    @Pattern(regexp = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/="
            + "?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-"
            + "\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")"
            + "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]"
            + "*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.)"
            + "{3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:"
            + "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|"
            + "\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])",
            message = "{invalid.email}")
    private String email;
    
    @Basic(fetch=FetchType.LAZY)
    @Column(unique = true)
    @NotNull
    @Pattern(regexp = "[a-zA-Z0-9]+", message = "{invalid.login}")
    private String login;
        
    @OneToOne(fetch=FetchType.LAZY, cascade={CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE})
    Credentials credentials;
        
    private boolean active = true;
    
    private boolean pending = true;
    
    @OneToMany(mappedBy = "user", fetch=FetchType.LAZY)
    private List<Log> logs = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", fetch=FetchType.LAZY)
    private List<FileAction> fileActions = new ArrayList<>();
    
    @OneToMany(mappedBy="author", fetch=FetchType.LAZY)
    private List<File> files = new ArrayList<>();
    
    @OneToMany(mappedBy="user", fetch=FetchType.LAZY)
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

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }
    
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    @XmlTransient
    public List<Log> getLogs() {
        return logs;
    }

    public void setLogs(List<Log> logs) {
        this.logs = logs;
    }

    @XmlTransient
    public List<FileAction> getFileActions() {
        return fileActions;
    }

    public void setFileActions(List<FileAction> fileActions) {
        this.fileActions = fileActions;
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
        if (!(object instanceof User)) {
            return false;
        }
        User other = (User) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.User[ id=" + id + " ]";
    }
    
}
