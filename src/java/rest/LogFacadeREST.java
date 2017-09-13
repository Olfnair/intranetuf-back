/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import entities.Log;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Florian
 */
@Stateless
@Path("entities.log")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class LogFacadeREST extends AbstractFacade<Log> {

    @PersistenceContext(unitName = "IUFPU")
    private EntityManager em;

    public LogFacadeREST() {
        super(Log.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
