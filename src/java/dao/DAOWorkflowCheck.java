/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import config.ApplicationConfig;
import entities.Project;
import entities.User;
import entities.Version;
import entities.WorkflowCheck;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import mail.Mail;
import mail.SendMailThread;

/**
 *
 * @author Florian
 */
public class DAOWorkflowCheck {
    private WorkflowCheck check = null;
    private EntityManager em = null;
    
    public DAOWorkflowCheck(WorkflowCheck check, EntityManager em) {
        this.check = check;
        this.em = em;
    }
    
    public boolean sendMail(boolean reminder) {
        return sendMail(null, reminder);
    }
    
    public boolean sendMail(Version version) {
        return sendMail(version, false);
    }
     
    public boolean sendMail(Version version, boolean reminder) {
        if(check.getStatus() != WorkflowCheck.Status.TO_CHECK) {
            // garde
            return false;
        }
        
        if(version == null) {
            version = check.getVersion();
        }
        
        TypedQuery<Project> projectQuery = em.createQuery(
                "SELECT v.file.project FROM Version v JOIN FETCH v.file JOIN FETCH v.file.project WHERE v.id = :id",
                Project.class
        );
        TypedQuery<User> emailQuery = em.createQuery(
                "SELECT wfc.user FROM WorkflowCheck wfc JOIN FETCH wfc.user JOIN FETCH wfc.user.email WHERE wfc.id = :id",
                User.class
        );
        
        projectQuery.setParameter("id", version.getId());
        emailQuery.setParameter("id", check.getId());
            
        Project project = projectQuery.getResultList().get(0);
        User user = emailQuery.getResultList().get(0);
            
        String subject;
        if(reminder) {
            subject = check.getType() == WorkflowCheck.Type.CONTROL ? "Rappel Contrôle" : "Rappel Validation";
        }
        else {
            subject = check.getType() == WorkflowCheck.Type.CONTROL ? "Nouveau Contrôle" : "Nouvelle Validation";
        }
        String verb = check.getType() == WorkflowCheck.Type.CONTROL ? "contrôler" : "valider";
        String text = "Bonjour " + user.getFirstname() + " " + user.getName() + ",\n\n"
                + "Vous avez été désigné pour " + verb + " le fichier " + version.getFilename()
                + " du projet " + project.getName() +". Rendez vous sur "
                + ApplicationConfig.FRONTEND_URL + " pour le " + verb + ".";
        new SendMailThread(new Mail(user.getEmail(), subject, text)).start();
        return true;
    } 
}
