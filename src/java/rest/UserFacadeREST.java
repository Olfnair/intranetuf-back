/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import config.ApplicationConfig;
import entities.Credentials;
import entities.User;
import entities.query.FlexQuery;
import entities.query.FlexQuerySpecification;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import mail.Mail;
import mail.SendMailThread;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.openjpa.persistence.EntityExistsException;
import rest.objects.RestError;
import rest.objects.RestLong;
import rest.security.AuthToken;
import rest.security.Authentication;
import rest.security.PasswordHasher;
import rest.security.RightsChecker;
import utils.Base64Url;

/**
 *
 * @author Florian
 */
@Stateless
@Path("entities.user")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class UserFacadeREST extends AbstractFacade<User> {
    
    @PersistenceContext(unitName = "IUFPU")
    private EntityManager em;
    
    static void sendActivationLink(Long userId, String email, String subject, String text) {
        // On signe le token avec la clé pour les activations.
        // Important pour éviter de générer un token qui pourrait aussi servir pour tout le reste
        AuthToken userToken = Authentication.issueToken(userId, 0L, 3 * 24 * 60 * 60, AuthToken.ACTIVATION_KEY); // token valable pendant 3 jours
        String url;
        try {
            url = ApplicationConfig.FRONTEND_URL + "/#/activate/" + Base64Url.encode(userToken.toJsonString(), "ISO-8859-1");
        } catch (UnsupportedEncodingException ex) {
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{error: \"UnsupportedEncodingException\"}").build());
        }
        new SendMailThread(new Mail(email, "Activer votre compte", text + url)).start();
    }

    public UserFacadeREST() {
        super(User.class);
    }

    @POST
    public Response create(@Context MessageContext jaxrsContext, User entity) {
        // droits : admin ou super
        AuthToken adminToken = Authentication.validate(jaxrsContext);
        RightsChecker.getInstance(em).validate(adminToken, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        
        entity.setCredentials(new Credentials(entity.getLogin()));
        Response res = super.insert(entity);
        Long id;
        try {
            id = entity.getId();
        } catch(EntityExistsException e) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_ACCEPTABLE)
                    .entity(new RestError("login")).build());
        }
        
        // Envoi d'un lien d'activation par mail
        UserFacadeREST.sendActivationLink(
                id,
                entity.getEmail(),
                "Activer votre compte",
                "Vous venez de créer un compte sur IntranetUF. Cliquez sur ce lien pour l'activer et choisir votre mot de passe : "
        );
        
        return res;
    }
    
    @PUT
    public Response editMany(@Context MessageContext jaxrsContext, List<User> entities) {
        AuthToken token = Authentication.validate(jaxrsContext);
        User admin = RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        
        entities.forEach((user) -> {
            User persistedUser = em.find(User.class, user.getId());
            if(! admin.isSuperAdmin() && (persistedUser.hasRole(User.Roles.SUPERADMIN) || user.hasRole(User.Roles.SUPERADMIN))) {
                // un admin simple essaye de modifier un superadmin ou de donner le droit superadmin
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }
            em.merge(user);
        });
        
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @PUT
    @Path("{id}")
    public Response edit(@Context MessageContext jaxrsContext, @PathParam("id") Long id, User entity) {
        AuthToken token = Authentication.validate(jaxrsContext);
        User askingUser = em.find(User.class, token.getUserId());
              
        User user = em.find(User.class, id);
        if(user == null || askingUser == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        // vérifie que l'utilisateur qui fait la demande est bien celui qui est modifié ou admin
        // si l'utilisateur à modifier est superadmin, celui qui modifie doit être superadmin aussi
        if(askingUser.getId().longValue() != user.getId().longValue() && ! askingUser.isAdmin()
                || user.isSuperAdmin() && ! askingUser.isSuperAdmin()) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        
        String login = entity.getLogin();
        if(login != null) {
            // login match le regex ?
            if(! login.matches("[a-zA-Z0-9]+")) {
                // login ne match pas le regex
                throw new WebApplicationException(Response.status(Response.Status.NOT_ACCEPTABLE)
                        .entity(new RestError("login")).build());
            }
                       
            // login unique ?
            TypedQuery<User> usersByloginQuery = em.createNamedQuery("User.getByLogin", User.class);
            usersByloginQuery.setParameter("login", login);
            List<User> usersByLoginResult = usersByloginQuery.getResultList();
            if(usersByLoginResult.size() > 0 && usersByLoginResult.get(0).getId().longValue() != user.getId().longValue()) {
                // login déjà utlisé
                throw new WebApplicationException(Response.status(Response.Status.NOT_ACCEPTABLE)
                        .entity(new RestError("login")).build());
            }
        }
            
        if(! askingUser.isAdmin()) {
            entity.setActive(user.isActive());
            entity.setPending(user.isPending());
        }
        else if(entity.isPending() && (! user.isSuperAdmin() || askingUser.isSuperAdmin())) {
            // Réactivation d'un compte : (reset mot de passe)
            // Envoi d'un lien d'activation par mail
            UserFacadeREST.sendActivationLink(
                    id,
                    entity.getEmail(),
                    "Réinitialisation de votre compte",
                    "Votre compte a été réinitilisé. Cliquez sur ce lien pour le réactiver et choisir un nouveau mot de passe : "
            );
        }
        
        entity.setId(user.getId());
        return super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public Response remove(@Context MessageContext jaxrsContext, @PathParam("id") Long id) {
        AuthToken token = Authentication.validate(jaxrsContext);
        RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        
        User user = em.find(User.class, id);
        if(user == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        user.setActive(false);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
    
    @PUT
    @Path("activateMany/{activate}")
    public Response activateMany(
            @Context MessageContext jaxrsContext,
            @PathParam("activate") Integer activate,
            List<RestLong> restLongProjectIds
    ) {
        AuthToken token = Authentication.validate(jaxrsContext);
        User user = RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        
        List<Long> userIds = new ArrayList<>(restLongProjectIds.size());
        restLongProjectIds.forEach((restLongId) -> {
            long id = restLongId.getValue();
            User persistedUser = em.find(User.class, id);
            if(! persistedUser.isSuperAdmin() || user.isSuperAdmin()) {
                // on ne peut pas modifier un superadmin sans être superadmin
                userIds.add(restLongId.getValue());
            }
        });
        
        javax.persistence.Query updateQuery = em.createNamedQuery("User.ActivateMany");
        updateQuery.setParameter("active", activate > 0);
        updateQuery.setParameter("userIds", userIds);
        updateQuery.executeUpdate();
        
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("{id}")
    public Response find(@Context MessageContext jaxrsContext, @PathParam("id") Long id) {
        AuthToken token = Authentication.validate(jaxrsContext);
        
        User askingUser = em.find(User.class, token.getUserId());
        if(askingUser.getId().longValue() != id.longValue() && ! askingUser.isAdmin()) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        
        return this.buildResponse(() -> {
            TypedQuery<User> userQuery = em.createNamedQuery("User.getWithLoginAndEmail", User.class);
            userQuery.setParameter("userId", id);
            User user = userQuery.getResultList().get(0);
            
            // nécessaire au chargement de l'email et du mot de passe...
            // si on ne le fait pas, si l'entité est dans le cache, on n'aura pas l'email ni le login dans la réponse.
            // apparemment, c'est du à un "bug" d'openjpa : JOIN FETCH ne marche que pour mettre en cache
            String email = user.getEmail();
            String login = user.getLogin();
            // .........................................................
            
            return user;
        });
    }
    
    @GET
    @Path("{id}/role")
    public Response getRole(@Context MessageContext jaxrsContext, @PathParam("id") Long id) {
        try {
            AuthToken token = Authentication.validate(jaxrsContext);
            if(token.getUserId() != id) {
                RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
            }
        } catch(WebApplicationException e) {
            return Response.ok(new RestLong(0)).build();
        }
        User user = em.find(User.class, id);
        if(user == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return Response.ok(new RestLong(user.getRole())).build();
    }
    
    private FlexQuery<User> prepareFindAll(MessageContext jaxrsContext,
            String whereParams, String orderbyParams,
            Integer index, Integer limit, boolean showSuper) {
        
        String showSuperStr = "";
        
        if(! showSuper) {
            showSuperStr = " WHERE MOD(u.role/:role, 2) < 1"; // quand l'utilisateur n'a pas le rôle
        }
        
        FlexQuerySpecification<User> LIST_ALL_COMPLETE = new FlexQuerySpecification<>("SELECT u FROM User u JOIN FETCH u.email JOIN FETCH u.login"
              + showSuperStr  + " :where: :orderby:", "u", User.class);   
        LIST_ALL_COMPLETE.addWhereSpec("name", "name", "LIKE", "AND", String.class);
        LIST_ALL_COMPLETE.addWhereSpec("firstname", "firstname", "LIKE", "AND", String.class);
        LIST_ALL_COMPLETE.addWhereSpec("email", "email", "LIKE", "AND", String.class);
        LIST_ALL_COMPLETE.addWhereSpec("login", "login", "LIKE", "AND", String.class);
        LIST_ALL_COMPLETE.addWhereSpec("id", "ids", "NOT IN", "AND", List.class);
        LIST_ALL_COMPLETE.addOrderBySpec("name");
        LIST_ALL_COMPLETE.addOrderBySpec("firstname");
        LIST_ALL_COMPLETE.addOrderBySpec("email");
        LIST_ALL_COMPLETE.addOrderBySpec("login");
        LIST_ALL_COMPLETE.addOrderBySpec("active");
        LIST_ALL_COMPLETE.addOrderBySpec("pending");
        LIST_ALL_COMPLETE.addDefaultOrderByClause("login", "ASC");
        
        FlexQuery<User> usersQuery = new FlexQuery<>(LIST_ALL_COMPLETE);
        
        try {
            usersQuery.setParameters(
                    Base64Url.decode(whereParams),
                    Base64Url.decode(orderbyParams),
                    index, limit
            );
        } catch (UnsupportedEncodingException ex) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        if(! showSuper) {
           usersQuery.setBaseParameter("role", User.Roles.SUPERADMIN);
        }
        
        return usersQuery;
    }

    @GET
    @Path("{whereParams}/{orderbyParams}/{index}/{limit}")
    public Response findAll(@Context MessageContext jaxrsContext,
            @PathParam("whereParams") String whereParams, @PathParam("orderbyParams") String orderbyParams,
            @PathParam("index") Integer index, @PathParam("limit") Integer limit) {
        
        AuthToken token = Authentication.validate(jaxrsContext);
        User user = RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        
        FlexQuery<User> usersQuery = prepareFindAll(jaxrsContext, whereParams, orderbyParams, index, limit, user.isSuperAdmin());
        
        usersQuery.prepareCountQuery(em);
        return Response.ok(usersQuery.execute()).build();
    }
    
    @POST
    @Path("{whereParams}/{orderbyParams}/{index}/{limit}")
    public Response findAllExcludeIds(@Context MessageContext jaxrsContext,
            @PathParam("whereParams") String whereParams, @PathParam("orderbyParams") String orderbyParams,
            @PathParam("index") Integer index, @PathParam("limit") Integer limit,
            List<RestLong> restLongIds) {
        
        AuthToken token = Authentication.validate(jaxrsContext);
        User user = RightsChecker.getInstance(em).validate(token, User.Roles.ADMIN | User.Roles.SUPERADMIN);
        
        FlexQuery<User> usersQuery = prepareFindAll(jaxrsContext, whereParams, orderbyParams, index, limit, user.isSuperAdmin());
        
        if(restLongIds != null && restLongIds.size() > 0) {
            List<Long> ids = new ArrayList<>(restLongIds.size());
            restLongIds.forEach((id) -> {
                ids.add(id.getValue());
            });
            usersQuery.addWhereClause("id", ids);
        }
        
        usersQuery.prepareCountQuery(em);
        return Response.ok(usersQuery.execute()).build();
    }
    
    // utilisé pour récupérer les contrôleurs et valideurs par projet
    @GET
    @Path("rightOnProject/{projectId}/{right}")
    public Response getByRightOnProject(@Context MessageContext jaxrsContext, @PathParam("projectId") Long projectId, @PathParam("right") Long right) {
        AuthToken token = Authentication.validate(jaxrsContext);
        // pas de check de droit spécifique
        
        return super.buildResponseList(() -> {
            TypedQuery<User> usersQuery = em.createNamedQuery("User.getByRightOnProject", User.class);
            usersQuery.setParameter("projectId", projectId);
            usersQuery.setParameter("userId", token.getUserId());
            usersQuery.setParameter("right", right);
            return usersQuery.getResultList();
        });
    }

    @GET
    @Path("{from}/{to}")
    public Response findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    public Response countREST() {
        return super.count();
    }

    @GET
    @Path("activate")
    public Response findUserToActivate(@Context MessageContext jaxrsContext) {
        AuthToken token = Authentication.validate(jaxrsContext, AuthToken.ACTIVATION_KEY);
        User user = em.find(User.class, token.getUserId());
        if(user == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }
        else if(! user.isPending()) {
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("user").build());
        }
        return super.buildResponse(200, user);
    }
    
    @PUT
    @Path("activate/{id}")
    public Response activate(@Context MessageContext jaxrsContext, @PathParam("id") Long id, Credentials entity) {
        AuthToken token = Authentication.validate(jaxrsContext, AuthToken.ACTIVATION_KEY);
        if(token.getUserId() != id) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        // vérifie que le compte n'est pas déjà pending == false
        TypedQuery<User> userQuery = em.createNamedQuery("User.getWithCredentials", User.class);
        userQuery.setParameter("userId", id);
        User user;
        try {
            user = userQuery.getSingleResult();
        }
        catch(Exception e) {
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("user").build());
        }
        if(user == null || ! user.isPending()) {
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("user").build());
        }
        // TODO : améliorer le check password
        String password = entity.getPassword();
        if(password == null || password.length() < 8) {
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("password").build());
        }
        
        PasswordHasher ph = new PasswordHasher(password);
        user.getCredentials().setPassword(ph.hash());
        user.getCredentials().setIteration(ph.getIterations());
        user.getCredentials().setSalt(ph.getBase64Salt());
        user.setPending(false);
        return super.buildResponse(200);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
