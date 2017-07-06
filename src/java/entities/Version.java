/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Florian
 */
@Entity
@XmlRootElement
public class Version implements Serializable {
    
    public final static class Status {
       public final static int CREATED = 0;
       public final static int CONTROLLED = 1;
       public final static int VALIDATED = 2;
       public final static int REFUSED = 3;
    }

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    private String filename;
    
    @Min(1L)
    private Long num = 1L;
    
    private int status = 0;
    
    @OneToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
    private Date date_upload;
    
    @ManyToOne(fetch=FetchType.LAZY)
    private File file;
    
    @OneToMany(mappedBy="version", fetch=FetchType.LAZY, cascade=CascadeType.PERSIST)
    private List<WorkflowCheck> workflowChecks = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getNum() {
        return num;
    }

    public void setNum(Long num) {
        this.num = num;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getDate_upload() {
        return date_upload;
    }

    public void setDate_upload(Date date_upload) {
        this.date_upload = date_upload;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public List<WorkflowCheck> getWorkflowChecks() {
        return workflowChecks;
    }

    public void setWorkflowChecks(List<WorkflowCheck> workflowChecks) {
        this.workflowChecks = workflowChecks;   
    }
    
    public void initWorkflowChecks() {
        
        for(WorkflowCheck check : this.workflowChecks) {
            check.setVersion(this);
            if(check.getOrder_num() == 0) {
                check.setStatus(WorkflowCheck.Status.TO_CHECK);
            }
            else {
                check.setStatus(WorkflowCheck.Status.WAITING);
            }
        }
    }
    
    public void updateStatus(WorkflowCheck updateCheck) {
        // TODO : bloquer l'update si la version a déjà le statut REFUSED ?
        if(updateCheck.getStatus() == WorkflowCheck.Status.CHECK_KO) {
            this.setStatus(Status.REFUSED);
            return;
        }
        for(WorkflowCheck check : this.workflowChecks) {
            
        }
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
        if (!(object instanceof Version)) {
            return false;
        }
        Version other = (Version) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Version[ id=" + id + " ]";
    }
    
}
