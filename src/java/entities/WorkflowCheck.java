/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Florian
 */
@Entity
@XmlRootElement
public class WorkflowCheck implements Serializable {  
    public enum CheckType {
        CONTROL (0),
        VALIDATION (1);
        
        private final Integer value;
        
        CheckType(Integer value) {
            this.value = value;
        }
    }

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /*@Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private java.util.Date date_init;*/
    @OneToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
    private Date date_init;
    
    /*@Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private java.util.Date date_action;*/
    @OneToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
    private Date date_action;
    
    private String comment;
    
    private Integer order_num = 0;
    
    private CheckType type;
    
    @ManyToOne
    private Version version;
    
    @ManyToOne
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate_init() {
        return date_init;
    }

    public void setDate_init(Date date_init) {
        this.date_init = date_init;
    }

    public Date getDate_action() {
        return date_action;
    }

    public void setDate_action(Date date_action) {
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

    public CheckType getType() {
        return type;
    }

    public void setType(CheckType type) {
        this.type = type;
    }

    public Version getFile() {
        return version;
    }

    public void setFile(Version version) {
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
