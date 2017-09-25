/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import entities.query.FlexQuerySpecification;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Florian
 */
@Entity
@XmlRootElement
@NamedQueries({
    @NamedQuery(name="WorkflowCheck.getByVersion", query="SELECT wfc FROM WorkflowCheck wfc JOIN FETCH wfc.user WHERE wfc.version.id = :versionId ORDER BY wfc.type ASC, wfc.order_num ASC"),
    @NamedQuery(name="WorkflowCheck.getByStatusUserVersions", query="SELECT wfc FROM WorkflowCheck wfc JOIN FETCH wfc.version WHERE wfc.user.id = :userId AND wfc.status = :status AND wfc.version.id IN(:versionIds)"),
    @NamedQuery(name="WorkflowCheck.getWithUserAndProject", query="SELECT wfc FROM WorkflowCheck wfc JOIN FETCH wfc.user JOIN FETCH wfc.project WHERE wfc.id = :wfcId"),
    @NamedQuery(name="WorkflowCheck.getWithUserAndProjectAndVersion", query="SELECT wfc FROM WorkflowCheck wfc JOIN FETCH wfc.user JOIN FETCH wfc.project JOIN FETCH wfc.version WHERE wfc.id = :wfcId"),
    @NamedQuery(name="WorkflowCheck.cancelByVersion", query="UPDATE WorkflowCheck wfc SET wfc.status = -1 WHERE wfc.version.id = :versionId AND (wfc.status = 0 OR wfc.status = 1)")
})
public class WorkflowCheck extends entities.Entity {
    private static final long serialVersionUID = 1L;
    
    // la flemme de faire des enums...
    public final static class Type {
        public final static int CONTROL = 0;
        public final static int VALIDATION = 1;
    }
    
    public final static class Status {
        public final static int CANCELLED = -1;
        public final static int WAITING = 0;
        public final static int TO_CHECK = 1;
        public final static int CHECK_OK = 2;
        public final static int CHECK_KO = 3;
    }
    
    public static final FlexQuerySpecification<WorkflowCheck> LIST_BY_USER;
    
    static {
        LIST_BY_USER = new FlexQuerySpecification<>("SELECT wfc FROM WorkflowCheck wfc JOIN FETCH wfc.version "
                + "JOIN FETCH wfc.project :where: :orderby:", "wfc", WorkflowCheck.class);
        LIST_BY_USER.addWhereSpec("user.id", "userId", "=", "AND", Long.class);
        LIST_BY_USER.addWhereSpec("version.filename", "versionFilename", "LIKE", "AND", String.class);
        LIST_BY_USER.addWhereSpec("version.num", "versionNum", "=", "AND", Long.class);
        LIST_BY_USER.addWhereSpec("version.status", "versionNum", "=", "AND", Long.class);
        LIST_BY_USER.addWhereSpec("project.name", "projectName", "LIKE", "AND", String.class);
        LIST_BY_USER.addWhereSpec("status", "status", "=", "AND", Long.class);
        LIST_BY_USER.addWhereSpec("status_greater_than", "statusGt", ">", "AND", Long.class);
        LIST_BY_USER.addWhereClauseReplacer("status_greater_than", "wfc.status");
        LIST_BY_USER.addWhereSpec("type", "type", "=", "AND", Long.class);
        LIST_BY_USER.addOrderBySpec("version.filename");
        LIST_BY_USER.addOrderBySpec("version.num");
        LIST_BY_USER.addOrderBySpec("version.status");
        LIST_BY_USER.addOrderBySpec("version.date_upload");
        LIST_BY_USER.addOrderBySpec("project.name");
        LIST_BY_USER.addOrderBySpec("date_init");
        LIST_BY_USER.addOrderBySpec("date_checked");
        LIST_BY_USER.addDefaultOrderByClause("version.filename", "ASC");
    }
    
    private Integer status = 0;
    
    private Long date_init;
    
    private Long date_checked;
    
    private String comment;
    
    private Integer order_num = 0;
    
    @NotNull
    private Integer type;
    
    @ManyToOne(fetch=FetchType.LAZY)
    private Version version;
    
    @ManyToOne(fetch=FetchType.LAZY)
    private User user;
    
    @ManyToOne(fetch=FetchType.LAZY)
    private Project project;
    
    public WorkflowCheck() {}
    
    public WorkflowCheck(Long id) {
        super(id);
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Long getDate_init() {
        return date_init;
    }

    public void setDate_init(Long date_init) {
        this.date_init = date_init;
    }

    public Long getDate_checked() {
        return date_checked;
    }

    public void setDate_checked(Long date_checked) {
        this.date_checked = date_checked;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getOrder_num() {
        return order_num;
    }

    public void setOrder_num(Integer order_num) {
        this.order_num = order_num;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Version getVersion() {
        return version;
    }
    
    public void setVersion(Version version) {
        this.version = version;
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
    
    @Override
    public String toString() {
        return "entities.FileAction[ id=" + getId() + " ]";
    }
    
}
