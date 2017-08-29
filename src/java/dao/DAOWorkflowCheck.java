/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entities.WorkflowCheck;
import javax.persistence.EntityManager;

/**
 *
 * @author Florian
 */
public class DAOWorkflowCheck {
    private WorkflowCheck check = null;
    private EntityManager em = null;
    
    public DAOWorkflowCheck(WorkflowCheck check) {
        this.check = check;
    }
    
    public DAOWorkflowCheck(WorkflowCheck check, EntityManager em) {
        this.check = check;
        this.em = em;
    }
    
    public void sendMailToInformUser() {
        
    }
}
