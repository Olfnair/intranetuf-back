/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entities.Entity;
import entities.Log;
import entities.User;
import java.time.Instant;
import javax.persistence.EntityManager;

/**
 *
 * @author Florian
 */
public class DAOLog {
    private final EntityManager em;
    
    public DAOLog(EntityManager em) {
        this.em = em;
    }
    
    public void log(User author, String type, String message, Entity... entities) {
        Log log = new Log();
        log.setAuthor(author);
        log.setLogdate(Instant.now().getEpochSecond());
        log.setType(type);
        log.setMessage(message);
        for(Entity entity : entities) {
            if(entity != null) {
                log.setEntity(entity);
            }
        }
        em.persist(log);
    }
}
