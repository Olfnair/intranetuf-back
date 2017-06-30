/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Florian
 */

// un peu quick & dirty, améliorer si besoin
// faire attention à fournir des entrées correctes dans le constructeur (d'où le quick & dirty..)
// pas de vrai contrôle d'erreur
public class FlexQuery {     
    // Select u From User u WHERE u.password = :password AND u.login = :login AND u.active = true AND u.pending = false :where: :orderby:  

    // colonne, operateur : tout ce qui n'est pas dans le hashmap sera refusé.
    private final HashMap<String, String> whereColsOperators  = new HashMap();
    private final HashMap<String, String> whereColsLinkers = new HashMap(); // indique s'il faut employer OR ou AND pour ajouter la colonne à la requête.
    private final HashMap<String, String> whereColsParameterNames = new HashMap();
    
    // spec order by
    private final HashMap<String, Boolean> orderByColsSpec  = new HashMap();
    
    private final List<String> whereCols = new ArrayList();
    private final List<String> orderByCols = new ArrayList();
    
    private final String baseQuery;
    private final String entityName;
    
    private boolean sortAsc = true;
    
    private void clear() {
        whereCols.clear();
        orderByCols.clear();
    }
    
    public FlexQuery(String baseQuery, String entityName) {
        this.baseQuery = baseQuery;
        this.entityName = entityName;
    }
    
    public void addWhereSpec(String column, String paramName, String operator, String link) {
        this.whereColsOperators.put(column, operator);
        this.whereColsLinkers.put(column, link);
        this.whereColsParameterNames.put(column, paramName);
    }
    
    public void addOrderBySpec(String column) {
        this.orderByColsSpec.put(column, Boolean.TRUE);
    }
    
    public void addWhereCol(String col) {
        whereCols.add(col);
    }
    
    public void addOrderByCol(String col) {
        orderByCols.add(col);
    }
    
    public String getParamName(String col) {
        return whereColsParameterNames.get(col);
    }
    
    public void setSortOrder(boolean asc) {
        this.sortAsc = asc;
    }
    
    public Query buildQuery(EntityManager em) {
        return this.buildQuery(em, false);
    }
    
    // Query setFirstResult and setMaxResult pour le limit
    public Query buildQuery(EntityManager em, boolean count) {
        final String whereStr = ":where:";
        final String orderByStr = ":orderby:";
        StringBuilder orderByBuilder = new StringBuilder(100);
        StringBuilder whereBuilder = new StringBuilder(100);
        this.orderByCols.forEach((col) -> {
            if(! count && this.orderByColsSpec.containsKey(col)) {
                if(orderByBuilder.length() == 0) {
                    orderByBuilder.append("order by ");
                }
                else {
                    orderByBuilder.append(',').append(col);
                }
                orderByBuilder.append(this.entityName).append('.').append(col);
                if(this.sortAsc) {
                    orderByBuilder.append(" asc");
                }
                else {
                    orderByBuilder.append(" desc");
                }
            }
        });
        this.whereCols.forEach((col) -> {
            if(this.whereColsOperators.containsKey(col)) {
                whereBuilder.append(' ').append(this.whereColsLinkers.get(col))
                        .append(' ').append(this.entityName).append('.').append(col)
                        .append(' ').append(this.whereColsOperators.get(col))
                        .append(" :").append(this.whereColsParameterNames.get(col)); // id du param;
            }
        });
        StringBuilder lowerCaseQueryBuilder = new StringBuilder(255);
        StringBuilder queryBuilder = new StringBuilder(255);
        lowerCaseQueryBuilder.append(this.baseQuery.toLowerCase()); // tout en minuscle pour simplifier les recherches. (les entités doivent garder leurs majuscules et on ne peut donc pas juste se contenter de mettre la requête en minuscules)
        queryBuilder.append(this.baseQuery);
        int whereIndex = lowerCaseQueryBuilder.indexOf(whereStr);
        if(whereIndex >= 0) {
            lowerCaseQueryBuilder.replace(whereIndex, whereIndex + whereStr.length(), whereBuilder.toString());
            queryBuilder.replace(whereIndex, whereIndex + whereStr.length(), whereBuilder.toString());
        }
        int orderByIndex = lowerCaseQueryBuilder.indexOf(orderByStr);
        if(orderByIndex >= 0) {
            lowerCaseQueryBuilder.replace(orderByIndex, orderByIndex + orderByStr.length(), orderByBuilder.toString());
            queryBuilder.replace(orderByIndex, orderByIndex + orderByStr.length(), orderByBuilder.toString());
        }
        if(count) {
            // on va remplacer tout ce qui est entre le SELECT et le FROM par un COUNT
            final String select = "select";
            final String from = "from";
            
            int selectIndex = lowerCaseQueryBuilder.indexOf(select);
            int fromIndex = lowerCaseQueryBuilder.indexOf(from);
            StringBuilder countBuilder = new StringBuilder();
            countBuilder.append(' ').append("count(").append(this.entityName).append(") ");
            if(selectIndex >= 0 && fromIndex >= 0) {
                lowerCaseQueryBuilder.replace(selectIndex + select.length(), fromIndex, countBuilder.toString());
                queryBuilder.replace(selectIndex + select.length(), fromIndex, countBuilder.toString());
            }
        }
        this.clear(); // reset des paramètres pour les prochaines requêtes
        return em.createQuery(queryBuilder.toString());
    }
}
