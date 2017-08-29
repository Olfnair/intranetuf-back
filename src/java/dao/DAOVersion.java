/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entities.Version;
import entities.WorkflowCheck;
import java.time.Instant;
import javax.persistence.EntityManager;

/**
 *
 * @author Florian
 */
public class DAOVersion {
    private Version version = null;
    private EntityManager em = null;
    
    public DAOVersion(Version version) {
        this.version = version;
    }
    
    public DAOVersion(Version version, EntityManager em) {
        this.version = version;
        this.em = em;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }
    
    public void initWorkflowChecks() {  
        for(WorkflowCheck check : version.getWorkflowChecks()) {
            check.setVersion(version);
            if(check.getType() == WorkflowCheck.Types.CONTROL && check.getOrder_num() == 0) {
                check.setStatus(WorkflowCheck.Status.TO_CHECK);
                check.setDate_init(Instant.now().getEpochSecond());
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
        updateCheck.setDate_checked(Instant.now().getEpochSecond());
        
        if(updateCheck.getStatus() == WorkflowCheck.Status.CHECK_KO) {
            version.setStatus(Version.Status.REFUSED);
            // fichier refusé, tout s'arrête là..
            return;
        }
        
        // sinon, vérifier si il existe encore un check du même type à TO_CHECK avec le même numéro d'ordre
        for(WorkflowCheck check : version.getWorkflowChecks()) {
            if(check.getType() == type && check.getOrder_num() == order_num && check.getStatus() == WorkflowCheck.Status.TO_CHECK) {
                // si oui, fini
                return;
            }
        }
           
        // si non, regarder s'il existe des checks d'ordre order_num + 1 du même type et les mettre à TO_CHECK
        updated = false;
        for(WorkflowCheck check : version.getWorkflowChecks()) {
            if(check.getType() == type && check.getOrder_num() == order_num + 1) {
                check.setStatus(WorkflowCheck.Status.TO_CHECK);
                check.setDate_init(Instant.now().getEpochSecond());
                updated = true;
            }
        }
        
        // si non et que le type == CONTROL, le fichier est contrôlé
        if(! updated && type == WorkflowCheck.Types.CONTROL) {
            version.setStatus(Version.Status.CONTROLLED);
            // préparer les validations :
            for(WorkflowCheck check : version.getWorkflowChecks()) {
                if(check.getType() == type + 1 && check.getOrder_num() == 0) {
                    check.setStatus(WorkflowCheck.Status.TO_CHECK);
                    check.setDate_init(Instant.now().getEpochSecond());
                    updated = true;
                }
            }
        }

        // sinon si le type == VALIDATION, le fichier est validé
        else if(! updated && type == WorkflowCheck.Types.VALIDATION) {
            version.setStatus(Version.Status.VALIDATED);
        }      
    }
}
