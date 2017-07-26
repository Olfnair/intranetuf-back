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
    @NamedQuery(name="WorkflowCheck.getByUser", query="SELECT wfc FROM WorkflowCheck wfc WHERE wfc.user.id = :userId"),
    @NamedQuery(name="WorkflowCheck.getWithUser", query="SELECT wfc FROM WorkflowCheck wfc JOIN FETCH wfc.user WHERE wfc.id = :wfcId")
})
public class WorkflowCheck implements Serializable {  
    
    // la flemme de faire des enums...
    public final static class Types {
        public final static int CONTROL = 0;
        public final static int VALIDATION = 1;
    }
    
    public final static class Status {
        public final static int WAITING = 0;
        public final static int TO_CHECK = 1;
        public final static int CHECK_OK = 2;
        public final static int CHECK_KO = 3;
    } 
    
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Integer status = 0;
    
    private Long date_init;
    
    private Long date_action;
    
    private String comment;
    
    private Integer order_num = 0;
    
    @NotNull
    private Integer type;
    
    @ManyToOne(fetch=FetchType.LAZY)
    private Version version;
    
    @ManyToOne(fetch=FetchType.LAZY)
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getDate_action() {
        return date_action;
    }

    public void setDate_action(Long date_action) {
        this.date_action = date_action;
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
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof WorkflowCheck)) {
            return false;
        }
        WorkflowCheck other = (WorkflowCheck) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.FileAction[ id=" + id + " ]";
    }
    
}
