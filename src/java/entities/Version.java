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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
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
@NamedQueries({
    @NamedQuery(name="Version.getWithChecks", query="SELECT v FROM Version v JOIN FETCH v.workflowChecks WHERE v.id = :versionId")
})
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
    
    @NotNull
    private Long date_upload;
    
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

    public Long getDate_upload() {
        return date_upload;
    }

    public void setDate_upload(Long date_upload) {
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
            if(check.getType() == WorkflowCheck.Types.CONTROL && check.getOrder_num() == 0) {
                check.setStatus(WorkflowCheck.Status.TO_CHECK);
            }
            else {
                check.setStatus(WorkflowCheck.Status.WAITING);
            }
        }
    }
    
    
    // préconditions :
    // bloquer l'update si updateCheck n'est pas TO_CHECK ou qu'il existe un check TO_CHECK de type inférieur
    // bloquer l'update si la version a déjà le statut REFUSED
    public void updateStatus(WorkflowCheck updateCheck) {
        int type = updateCheck.getType();
        int order_num = updateCheck.getOrder_num();
        boolean updated;
        
        // TODO : envoyer mails, noter dates       
        
        if(updateCheck.getStatus() == WorkflowCheck.Status.CHECK_KO) {
            this.setStatus(Status.REFUSED);
            // fichier refusé, tout s'arrête là..
            return;
        }
        
        // sinon, vérifier si il existe encore un check du même type à TO_CHECK avec le même numéro d'ordre
        for(WorkflowCheck check : this.workflowChecks) {
            if(check.getType() == type && check.getOrder_num() == order_num && check.getStatus() == WorkflowCheck.Status.TO_CHECK) {
                // si oui, fini
                return;
            }
        }
           
        // si non, regarder s'il existe des checks d'ordre order_num + 1 du même type et les mettre à TO_CHECK
        updated = false;
        for(WorkflowCheck check : this.workflowChecks) {
            if(check.getType() == type && check.getOrder_num() == order_num + 1) {
                check.setStatus(WorkflowCheck.Status.TO_CHECK);
                updated = true;
            }
        }
        
        // si non et que le type == CONTROL, le fichier est contrôlé
        if(! updated && type == WorkflowCheck.Types.CONTROL) {
            this.setStatus(Status.CONTROLLED);
            // préparer les validations :
            for(WorkflowCheck check : this.workflowChecks) {
                if(check.getType() == type + 1 && check.getOrder_num() == 0) {
                    check.setStatus(WorkflowCheck.Status.TO_CHECK);
                    updated = true;
                }
            }
        }

        // sinon si le type == VALIDATION, le fichier est validé
        else if(! updated && type == WorkflowCheck.Types.VALIDATION) {
            this.setStatus(Status.VALIDATED);
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
