/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package rest;

import dao.DAOLog;
import entities.Entity;
import entities.Log;
import entities.Project;
import entities.ProjectRight;
import entities.User;
import entities.query.FlexQuery;
import entities.query.FlexQueryResult;
import entities.query.FlexQuerySpecification;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.ext.MessageContext;
import rest.security.AuthToken;
import rest.security.Authentication;
import rest.security.RightsChecker;
import utils.Base64Url;

/**
 *
 * @author Florian
 */
@Stateless
@Path("entities.projectright")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class ProjectRightFacadeREST extends AbstractFacade<ProjectRight> {
    
    @PersistenceContext(unitName = "IUFPU")
    private EntityManager em;
    
    public ProjectRightFacadeREST() {
        super(ProjectRight.class);
    }
    
    private void save(User user, ProjectRight right) {
        DAOLog logger = new DAOLog(em);
        if(right.getId() == null) {
            em.persist(right);
            logger.log(user, Log.Type.CREATE_RIGHTS, "Droits : " + right.getRights(), right.getProject(), right.getUser());
        }
        else if(right.getRights() == 0) {
            em.remove(em.merge(right));
            logger.log(user, Log.Type.DELETE_RIGHTS, "Droits : " + right.getRights(), right.getProject(), right.getUser());
        }
        else {
            em.merge(right);
            logger.log(user, Log.Type.EDIT_RIGHTS, "Droits : " + right.getRights(), right.getProject(), right.getUser());
        }
    }
    
    /**
     * Endpoint utilsé pour ajouter ou éditer des droits sur un projet
     * @param jaxrsContext contexte utilisé pour l'authentification
     * @param projectId id du projet concerné par les droits
     * @param rights Liste d'entités ProjectRights à créer/modifier pour ce projet
     * @return Statut HTTP 201 en cas de succès
     */
    @PUT
    @Path("project/{id}")
    public Response createOrEditForProject(@Context MessageContext jaxrsContext, @PathParam("id") Long projectId, List<ProjectRight> rights) {
        AuthToken token = Authentication.validate(jaxrsContext);
        User user = RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        
        Project project = em.find(Project.class, projectId);
        if(project == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        List<Long> usersIds = new ArrayList<>(rights.size());
        HashMap<Long, ProjectRight> mapUserIdToRight = new HashMap<>();
        rights.forEach((right) -> {
            if(right.getProject().getId().longValue() == projectId.longValue()) {
                usersIds.add(right.getUser().getId());
                mapUserIdToRight.put(right.getUser().getId(), right);
            }
        });
        
        List<ProjectRight> existingRights = new ArrayList<>();
        if(usersIds.size() > 0) {
            TypedQuery<ProjectRight> rightsQuery = em.createNamedQuery("ProjectRight.ListForProjectAndUsers", ProjectRight.class);
            rightsQuery.setParameter("entityId", project.getId());
            rightsQuery.setParameter("entitiesIds", usersIds);
            existingRights = rightsQuery.getResultList();
        }
        
        existingRights.forEach((existingRight) -> {
            if(mapUserIdToRight.containsKey(existingRight.getUser().getId())) {
                ProjectRight right = mapUserIdToRight.get(existingRight.getUser().getId());
                right.setRights(right.getRights() | existingRight.getRights());
                right.setId(existingRight.getId());
                mapUserIdToRight.put(existingRight.getUser().getId(), right);
            }
        });
        
        mapUserIdToRight.keySet().forEach((userId) -> {
            save(user, mapUserIdToRight.get(userId));
        });
        
        return Response.status(Response.Status.CREATED).build();
    }
    
    /**
     * Utilisé pour ajouter ou modifier des ProjectRights
     * @param jaxrsContext - contexte utilisé pour l'authentification
     * @param entities - Liste des ProjectRights à ajouter/modifier
     * @return Statut HTTP 201 en cas de succès
     */
    @PUT
    public Response createOrEdit(@Context MessageContext jaxrsContext, List<ProjectRight> entities) {
        AuthToken token = Authentication.validate(jaxrsContext);
        User user = RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        
        entities.forEach((right) -> {
            save(user, right);
        });
        return Response.status(Response.Status.CREATED).build();
    }
    
    /**
     * Endpoint utilisé pour récupérer les ProjectRights pour un projet pour l'utilisateur correspondant au token fourni
     * @param jaxrsContext contexte utilsé pour l'authentification
     * @param projectId id du projet dont on veut récupérer les ProjectRights
     * @return Liste des ProjectRights demandés
     */
    @GET
    @Path("project/{projectId}")
    public Response findForUserByProject(@Context MessageContext jaxrsContext, @PathParam("projectId") Long projectId) {
        AuthToken token = Authentication.validate(jaxrsContext);
        // pas de droits à vérifier, on récupère automatiquement les droits pour l'user qui les demande après authentification
        
        return this.buildResponseList(() -> {
            TypedQuery<ProjectRight> rightsQuery = em.createNamedQuery("ProjectRight.GetByUserAndProject", ProjectRight.class);
            rightsQuery.setParameter("userId", token.getUserId());
            rightsQuery.setParameter("projectId", projectId);
            return rightsQuery.getResultList();
        });
    }
    
    public <T extends Entity, E extends Entity> Response getRights(
            FlexQuerySpecification<T> specification, String rightsQueryName,
            E searchForEntity, MessageContext jaxrsContext,
            String whereParams, String orderbyParams,
            Integer index, Integer limit
    ) {
        // droits : uniquement les admins (ou super)
        AuthToken token = Authentication.validate(jaxrsContext);
        RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        
        if(searchForEntity == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        // selection des projets demandés :
        FlexQuery<T> queryEntities = new FlexQuery<>(specification);
        try {
            queryEntities.setParameters(
                    Base64Url.decode(whereParams),
                    Base64Url.decode(orderbyParams),
                    index, limit
            );
        } catch (UnsupportedEncodingException ex) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        queryEntities.prepareCountQuery(em);
        FlexQueryResult<T> flexQueryResultProjects = queryEntities.execute();
        if(flexQueryResultProjects == null) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        List<T> entities = flexQueryResultProjects.getList();
        
        if(entities == null) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        List<Long> entitiesIds = new ArrayList<>(entities.size());
        entities.forEach((entity) -> {
            entitiesIds.add(entity.getId());
        });
        
        // selection des droits existants éventuels sur ces projets
        TypedQuery<ProjectRight> queryRights = em.createNamedQuery(rightsQueryName, ProjectRight.class);
        queryRights.setParameter("entityId", searchForEntity.getId());
        queryRights.setParameter("entitiesIds", entitiesIds);
        List<ProjectRight> existingRights = queryRights.getResultList();
        if(existingRights == null) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        // on map id de l'entité vers droits pour les droits existants récupérés
        HashMap<Long, ProjectRight> rightsMap = new HashMap<>();
        existingRights.forEach((right) -> {
            if(searchForEntity instanceof User) {
                rightsMap.put(right.getProject().getId(), right);
            }
            else {
                rightsMap.put(right.getUser().getId(), right);
            }
        });
        
        List<ProjectRight> outputRights = new ArrayList<>(entities.size());
        if(searchForEntity instanceof User) {
            // on construit l'output
            entities.forEach((entity) -> {
                if(rightsMap.containsKey(entity.getId())) {
                    // on récupère le droit existant dans le mapping
                    outputRights.add(rightsMap.get(entity.getId()));
                }
                else {
                    // on crée un objet droit pour le front
                    outputRights.add(new ProjectRight((User)searchForEntity, (Project)entity));
                }
            });
        }
        else if(searchForEntity instanceof Project) {
            // on construit l'output
            entities.forEach((entity) -> {
                if(rightsMap.containsKey(entity.getId())) {
                    // on récupère le droit existant dans le mapping
                    outputRights.add(rightsMap.get(entity.getId()));
                }
                else {
                    // on crée un objet droit pour le front
                    outputRights.add(new ProjectRight((User)entity, (Project)searchForEntity));
                }
            });
        }
        else {
            throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
        }
        
        FlexQueryResult<ProjectRight> queryResult = new FlexQueryResult<>(outputRights, flexQueryResultProjects.getTotalCount());
        
        // sortie
        return Response.ok(queryResult).build();
    }
    
    /**
     * Endpoint utilisé pour faire une recherche sur les droits par utilisateur
     * @param jaxrsContext - contexte utilisé pour l'authentification
     * @param userId - id de l'utilisateur dont on veut récupérer les droits
     * @param whereParams - paramètres WHERE de la recherche
     * @param orderbyParams - paramètres ORDER BY
     * @param index - index à partir duquel récupérer les résultats
     * @param limit - nombre max de résultats à récupérer
     * @return Liste des ProjectRights correspondants à la recherche
     */
    @GET
    @Path("user/{userId}/{whereParams}/{orderbyParams}/{index}/{limit}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getRightsForUser(@Context MessageContext jaxrsContext, @PathParam("userId") Long userId,
            @PathParam("whereParams") String whereParams, @PathParam("orderbyParams") String orderbyParams,
            @PathParam("index") Integer index, @PathParam("limit") Integer limit) {
        return this.getRights(
                Project.LIST_FOR_RIGHTS,
                "ProjectRight.ListForUserAndProjects",
                em.find(User.class, userId),
                jaxrsContext, whereParams, orderbyParams, index, limit
        );
    }
    
    /**
     * Endpoint utilisé pour faire une recherche sur les droits par projet
     * @param jaxrsContext - contexte utilisé pour l'authentification
     * @param projectId - id du projet dont on veut récuprérer les droits
     * @param whereParams - paramètres WHERE de la recherche
     * @param orderbyParams - paramètres ORDER BY
     * @param index - index à partir duquel récupérer les résultats
     * @param limit - nombre max de résultats à récupérer
     * @return Liste des ProjectRights correspondants à la recherche
     */
    @GET
    @Path("project/{projectId}/{whereParams}/{orderbyParams}/{index}/{limit}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getRightsForProject(@Context MessageContext jaxrsContext, @PathParam("projectId") Long projectId,
            @PathParam("whereParams") String whereParams, @PathParam("orderbyParams") String orderbyParams,
            @PathParam("index") Integer index, @PathParam("limit") Integer limit) {
        return this.getRights(
                User.LIST_FOR_RIGHTS,
                "ProjectRight.ListForProjectAndUsers",
                em.find(Project.class, projectId),
                jaxrsContext, whereParams, orderbyParams, index, limit
        );
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
