/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import entities.query.FlexQuerySpecification;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Florian
 */
@Entity
@XmlRootElement
public class Log extends entities.Entity {
    private static final long serialVersionUID = 1L;
    
    public final static class Type {
        public final static String AUTH = "AUTH";
        public final static String AUTH_AS = "AUTH_AS";
        public final static String DOWNLOAD = "DOWNLOAD";
        public final static String CREATE_PROJECT = "CREATE_PROJECT";
        public final static String EDIT_PROJECT = "EDIT_PROJECT";
        public final static String DELETE_PROJECT = "DELETE_PROJECT";
        public final static String ACTIVATE_PROJECT = "ACTIVATE_PROJECT";
        public final static String CREATE_FILE = "CREATE_FILE";
        public final static String EDIT_FILE = "EDIT_FILE";
        public final static String DELETE_FILE = "DELETE_FILE";
        public final static String ACTIVATE_FILE= "ACTIVATE_FILE";
        public final static String CREATE_VERSION = "CREATE_VERSION";
        public final static String EDIT_VERSION = "EDIT_VERSION";
        public final static String DELETE_VERSION = "DELETE_VERSION";
        public final static String ACTIVATE_VERSION = "ACTIVATE_VERSION";
        public final static String CREATE_USER = "CREATE_USER";
        public final static String EDIT_USER = "EDIT_USER";
        public final static String DELETE_USER = "DELETE_USER";
        public final static String ACTIVATE_USER = "ACTIVATE_USER";
        public final static String ACTIVATIONLINK_USER = "ACTIVATIONLINK_USER";
        public final static String NEW_CREDENTIALS = "NEW_CREDENTIALS";
        public final static String CONTROL_OK = "CONTROL_OK";
        public final static String CONTROL_KO = "CONTROL_KO";
        public final static String VALIDATION_OK = "VALIDATION_OK";
        public final static String VALIDATION_KO = "VALIDATION_KO";
        public final static String CREATE_RIGHTS = "CREATE_RIGHTS";
        public final static String WORKFLOWCHECK_REMINDER = "WORKFLOWCHECK_REMINDER";
        public final static String EDIT_RIGHTS = "EDIT_RIGHTS";
        public final static String DELETE_RIGHTS = "DELETE_RIGHTS";
    }
    
    public final static FlexQuerySpecification<Log> LIST_ALL;
    
    static {
        LIST_ALL = new FlexQuerySpecification<>("SELECT log FROM Log log :where: :orderby:", "log", Log.class);
        LIST_ALL.addWhereSpec("message", "message", "LIKE", "AND", String.class);
        LIST_ALL.addWhereSpec("type", "type", "LIKE", "AND", String.class);
        LIST_ALL.addWhereSpec("author.login", "authorLogin", "LIKE", "AND", String.class);
        LIST_ALL.addWhereSpec("user.login", "userLogin", "LIKE", "AND", String.class);
        LIST_ALL.addWhereSpec("project.name", "projectName", "LIKE", "AND", String.class);
        LIST_ALL.addWhereSpec("version.filename", "type", "LIKE", "AND", String.class);
        LIST_ALL.addOrderBySpec("message");
        LIST_ALL.addOrderBySpec("type");
        LIST_ALL.addOrderBySpec("author.login");
        LIST_ALL.addOrderBySpec("user.login");
        LIST_ALL.addOrderBySpec("project.name");
        LIST_ALL.addOrderBySpec("version.filename");
        LIST_ALL.addOrderBySpec("logdate");
        LIST_ALL.addDefaultOrderByClause("logdate", "DESC");
    }
    
    private String type;
    private String message;
    
    @NotNull
    private Long logdate;
    
    @ManyToOne(fetch=FetchType.EAGER)
    private User author;
    
    @ManyToOne(fetch=FetchType.EAGER)
    private User user;
    
    @ManyToOne(fetch=FetchType.EAGER)
    private Project project;
    
    @ManyToOne(fetch=FetchType.EAGER)
    private Version version;
    
    public Log() {}
    
    public Log(Long id) {
        super(id);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getLogdate() {
        return logdate;
    }

    public void setLogdate(Long logdate) {
        this.logdate = logdate;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }
    
    public void setEntity(entities.Entity entity) {
        Class<?> classOfEntity = entity.getClass();
        String methodName = "set" + classOfEntity.getSimpleName();
        try {
            Method method = this.getClass().getMethod(methodName, classOfEntity);
            method.invoke(this, entity);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public String toString() {
        return "entities.Log[ id=" + getId() + " ]";
    }
    
}
