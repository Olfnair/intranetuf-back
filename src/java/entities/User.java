/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import entities.query.FlexQuerySpecification;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
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
    @NamedQuery(name="User.getWithCredentials", query="SELECT u FROM User u JOIN FETCH u.credentials WHERE u.id = :userId"),
    @NamedQuery(name="User.getByLogin", query="SELECT u FROM User u WHERE u.login = :login"),
    @NamedQuery(name="User.getByloginWithCredentialsForAuth", query="SELECT u FROM User u JOIN FETCH u.credentials WHERE u.login = :login AND u.active = true AND u.pending = false"),
    @NamedQuery(name="User.getByRightOnProject", query="SELECT pr.user FROM ProjectRight pr WHERE pr.project.id = :projectId AND pr.user.id <> :userId AND MOD(pr.rights/:right, 2) >= 1"),
    @NamedQuery(name="User.ActivateMany", query="UPDATE User u SET u.active = :active WHERE u.id IN(:userIds)")
})
public class User extends entities.Entity {
    private static final long serialVersionUID = 1L;
    
    public final static class Roles {
        public final static int USER = 0;
        public final static int ADMIN = 1;
        public final static int SUPERADMIN = 2;
    }
    
    public final static FlexQuerySpecification<User> LIST_FOR_RIGHTS;
    
    static {        
        LIST_FOR_RIGHTS = new FlexQuerySpecification<>("SELECT u FROM User u JOIN FETCH u.login :where: :orderby:", "u", User.class);
        LIST_FOR_RIGHTS.addWhereSpec("login", "name", "LIKE", "AND", String.class);
        LIST_FOR_RIGHTS.addOrderBySpec("login");
        LIST_FOR_RIGHTS.addOrderBySpec("active");
        LIST_FOR_RIGHTS.addDefaultOrderByClause("login", "ASC");
    }
    
    private Long role = 0L;
    
    @NotNull
    //@Pattern(regexp = "[a-zA-Z]+", message = "{invalid.name}")
    private String name;
    
    @NotNull
    //@Pattern(regexp = "[a-zA-Z]+", message = "{invalid.firstname}")
    private String firstname;
    
    @Basic(fetch=FetchType.LAZY)
    @NotNull
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
        
    @OneToOne(fetch=FetchType.LAZY, cascade={CascadeType.ALL})
    Credentials credentials;
        
    private boolean active = true;
    
    private boolean pending = true;
    
    @OneToMany(mappedBy = "user", fetch=FetchType.LAZY)
    private List<Log> logs = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", fetch=FetchType.LAZY)
    private List<WorkflowCheck> fileActions = new ArrayList<>();
    
    @OneToMany(mappedBy="author", fetch=FetchType.LAZY)
    private List<File> files = new ArrayList<>();
    
    @OneToMany(mappedBy="user", fetch=FetchType.LAZY)
    private List<ProjectRight> projectRights = new ArrayList<>();

    public Long getRole() {
        return role;
    }

    public void setRole(Long role) {
        this.role = role;
    }
    
    public boolean hasRole(long role) {
        return (this.role & role) > 0 || role == Roles.USER;
    }
    
    public boolean isAdmin() {
        return hasRole(Roles.ADMIN) || hasRole(Roles.SUPERADMIN);
    }
    
    public boolean isSuperAdmin() {
        return hasRole(Roles.SUPERADMIN);
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
    public List<WorkflowCheck> getFileActions() {
        return fileActions;
    }

    public void setFileActions(List<WorkflowCheck> fileActions) {
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
    public String toString() {
        return "entities.User[ id=" + getId() + " ]";
    }
    
}
