/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import entities.query.FlexQuerySpecification;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
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
    @NamedQuery(name="Version.getProject", query="SELECT v.file.project FROM Version v WHERE v.id = :versionId"),
    @NamedQuery(name="Version.getWithChecks", query="SELECT v FROM Version v JOIN FETCH v.workflowChecks WHERE v.id = :versionId"),
    @NamedQuery(name="Version.getOtherVersions", query="SELECT v FROM Version v WHERE v.file.id = :fileId AND v.id != :versionId")
})
public class Version extends entities.Entity {
    private static final long serialVersionUID = 1L;
    
    public final static class Status {
       public final static int CREATED = 0;
       public final static int CONTROLLED = 1;
       public final static int VALIDATED = 2;
       public final static int REFUSED = 3;
    }
    
    public final static FlexQuerySpecification<Version> LIST_OTHER_VERSIONS;
    
    static {
        LIST_OTHER_VERSIONS = new FlexQuerySpecification<>("SELECT v FROM Version v :where: :orderby:", "v", Version.class);
        LIST_OTHER_VERSIONS.addWhereSpec("file.id", "fileId", "=", "AND", Long.class);
        LIST_OTHER_VERSIONS.addWhereSpec("id", "versionId", "<>", "AND", Long.class);
        LIST_OTHER_VERSIONS.addWhereSpec("filename", "filename", "LIKE", "AND", String.class);
        LIST_OTHER_VERSIONS.addWhereSpec("num", "versionNum", "=", "AND", Long.class);
        LIST_OTHER_VERSIONS.addWhereSpec("status", "status", "=", "AND", Long.class);
        LIST_OTHER_VERSIONS.addOrderBySpec("filename");
        LIST_OTHER_VERSIONS.addOrderBySpec("num");
        LIST_OTHER_VERSIONS.addOrderBySpec("status");
        LIST_OTHER_VERSIONS.addDefaultOrderByClause("num", "DESC");
    }
    
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
    
    @OneToMany(mappedBy = "version", fetch=FetchType.LAZY)
    private List<Log> logs = new ArrayList<>();
    
    public Version() {}
    
    public Version(Long id) {
        super(id);
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

    public List<Log> getLogs() {
        return logs;
    }

    public void setLogs(List<Log> logs) {
        this.logs = logs;
    }
    
    @Override
    public String toString() {
        return "entities.Version[ id=" + getId() + " ]";
    }
    
}
