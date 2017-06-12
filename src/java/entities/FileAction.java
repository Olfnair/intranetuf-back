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

/**
 *
 * @author Florian
 */
@Entity
public class FileAction implements Serializable {

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
    
    private Long order_num = 0L;
    
    @ManyToOne
    private File file;
    
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

    public Long getOrder_num() {
        return order_num;
    }

    public void setOrder_num(Long order_num) {
        this.order_num = order_num;
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
        if (!(object instanceof FileAction)) {
            return false;
        }
        FileAction other = (FileAction) object;
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
