/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

/**
 *
 * @author Florian
 * @param <T> Entity class
 */
public abstract class AbstractFacade<T> {
    
    protected interface VoidQuery<T> {
        public void query(T entity);
    }
    
    protected interface Query<T> {
        public T query();
    }
    
    protected interface ListQuery<T> {
        public List<T> query();
    }

    private final Class<T> entityClass;

    public AbstractFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
    }
    
    protected abstract EntityManager getEntityManager();
    
    public Response buildResponse(int status, Object entity) {      
        return Response.status(status).entity(entity).build();
    }
    
    public Response buildResponse(int status) {
        return this.buildResponse(status, null);
    }
    
    public Response buildResponse(VoidQuery<T> q, T entity) {
        try {
            q.query(entity);
        }
        catch(Exception e) {
            return this.buildResponse(400);
        }
        return this.buildResponse(200, entity);
    }
    
    public Response buildResponse(Query<T> q) {
        T entity;
        try {
            entity = q.query();
        }
        catch(Exception e) {
            return this.buildResponse(400);
        }
        return this.buildResponse(200, entity);
    }
    
    public Response buildResponseList(ListQuery<T> q) {
        List<T> entitiesList;
        try {
            entitiesList = q.query();
        }
        catch(Exception e) {
            return this.buildResponse(400);
        }
        return this.buildResponse(200, new GenericEntity<List<T>>(entitiesList){});
    }

    // !!! ne pas surcharger directement cette méthode dans les endpoints rest : tjrs donner un nom différent que celui de la méthode exposée !!!
    public Response insert(T entity) {
        return buildResponse((T ent) -> { 
            getEntityManager().persist(ent);
        }, entity);
    }

    public Response edit(T entity) {
        return buildResponse((T ent) -> { 
            getEntityManager().merge(ent);
        }, entity);
    }

    public Response remove(Object id) {
        T entity = getEntityManager().find(entityClass, id);
        return buildResponse((T ent) -> {
            getEntityManager().remove(getEntityManager().merge(ent));
        }, entity);
    }

    public Response find(Object id) {
        return buildResponse(() -> {
            return getEntityManager().find(entityClass, id);
        });
    }

    public Response findAll() {
        return buildResponseList(() -> {
            CriteriaQuery<T> cq = getEntityManager().getCriteriaBuilder().createQuery(entityClass);
            cq.select(cq.from(entityClass));
            return getEntityManager().createQuery(cq).getResultList();
        });
    }

    public Response findRange(int[] range) {
        return buildResponseList(() -> {
            CriteriaQuery<T> cq = getEntityManager().getCriteriaBuilder().createQuery(entityClass);
            cq.select(cq.from(entityClass));
            TypedQuery<T> q = getEntityManager().createQuery(cq);
            q.setMaxResults(range[1] - range[0] + 1);
            q.setFirstResult(range[0]);
            return q.getResultList();
        });
    }

    public Response count() {
        CriteriaQuery<Long> cq = getEntityManager().getCriteriaBuilder().createQuery(Long.class);
        Root<T> rt = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        TypedQuery<Long> q = getEntityManager().createQuery(cq);
        return buildResponse(200, q.getSingleResult().intValue());
    }
    
}
