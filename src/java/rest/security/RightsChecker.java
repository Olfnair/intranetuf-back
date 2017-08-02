/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package rest.security;

import entities.Project;
import entities.ProjectRight;
import entities.User;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 *
 * @author Florian
 */
public class RightsChecker {
    private EntityManager em;
    
    private static final RightsChecker CHECKER;
    
    static {
        CHECKER = new RightsChecker();
    }
    
    private void setEntityManager(EntityManager em) {
        this.em = em;
    }   
    
    // singleton = impossible à instancier
    private RightsChecker() { }
    
    // récupérer le singleton
    public static final RightsChecker getInstance(EntityManager em) {
        CHECKER.setEntityManager(em);
        return CHECKER;
    }
    
    public User validate(AuthToken token) throws WebApplicationException {
        // le role User.Roles.USER (= 0) étant le minimum
        return this.validate(token, User.Roles.USER, -1L, 0);
    }
    
    public User validate(AuthToken token, int role) throws WebApplicationException {
        return this.validate(token, role, -1L, 0);
    }
    
    public User validate(AuthToken token, int role, Long projectId, int rights) throws WebApplicationException {       
        User user = em.find(User.class, token.getUserId());
        
        if(user == null || ! user.hasRole(role)) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        
        if(projectId >= 0) {
            Project project = em.find(Project.class, projectId);
            if(project == null || ! project.isActive() && ! user.isAdmin()) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }     
            TypedQuery<ProjectRight> rightsQuery = em.createNamedQuery("ProjectRight.GetByUserAndProject", ProjectRight.class);
            rightsQuery.setParameter("userId", user.getId());
            rightsQuery.setParameter("projectId", projectId);
            List<ProjectRight> rightsResult = rightsQuery.getResultList();
            if(rightsResult == null || rightsResult.size() != 1) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }
            if(! rightsResult.get(0).hasRight(rights)) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }
        }
        
        return user;
    }
}
