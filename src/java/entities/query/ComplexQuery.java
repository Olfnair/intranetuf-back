/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities.query;

import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Florian
 */

// un peu quick & dirty, améliorer si besoin
public class ComplexQuery {     
    // Select u From User u WHERE u.password = :password AND u.login = :login AND u.active = true AND u.pending = false :where: :orderby:  

    // colonne, operateur : tout ce qui n'est pas dans le hashmap sera refusé.
    private final HashMap<String, String> columnsOperator;
    private final HashMap<String, String> columnsLinker; // indique s'il faut employer OR ou AND pour ajouter la colonne à la requête.
    
    private final String baseQuery;
    private final String entityName;
    
    
    public ComplexQuery(String baseQuery, String entityName) {
        this.baseQuery = baseQuery.toLowerCase(); // on mets tout en minuscule pour 
        this.columnsOperator = new HashMap();
        this.columnsLinker = new HashMap();
        this.entityName = entityName;
    }
    
    void addColumnSpec(String column, String operator, String link) {
        this.columnsOperator.put(column, operator);
        this.columnsLinker.put(column, link);
    }

    Query buildQuery(EntityManager em, List<String> whereCols, List<String> orderCols) {
        return this.buildQuery(em, whereCols, orderCols, false);
    }
    
    // Query setFirstResult and setMaxResult pour le limit
    Query buildQuery(EntityManager em, List<String> whereCols, List<String> orderCols, boolean count) {
        final String whereStr = ":where:";
        final String orderByStr = ":orderby:";
        StringBuilder orderByBuilder = new StringBuilder(100);
        StringBuilder whereBuilder = new StringBuilder(100);
        orderCols.forEach((col) -> {
            if(columnsOperator.containsKey(col)) {
                if(orderByBuilder.length() == 0) {
                    orderByBuilder.append("order by ").append(col);
                }
                else {
                    orderByBuilder.append(',').append(col);
                }
            }
        });
        whereCols.forEach((col) -> {
            if(columnsOperator.containsKey(col)) {
                whereBuilder.append(' ').append(columnsLinker.get(col))
                        .append(' ').append(this.entityName).append('.').append(col)
                        .append(' ').append(columnsOperator.get(col))
                        .append(" :").append(col);
            }
        });
        StringBuilder queryBuilder = new StringBuilder(255);
        queryBuilder.append(baseQuery);
        int whereIndex = queryBuilder.indexOf(whereStr);
        if(whereIndex >= 0) {
            queryBuilder.replace(whereIndex, whereIndex + whereStr.length(), whereBuilder.toString());
        }
        int orderByIndex = queryBuilder.indexOf(orderByStr);
        if(orderByIndex >= 0) {
            queryBuilder.replace(orderByIndex, orderByIndex + orderByStr.length(), orderByBuilder.toString());
        }
        if(count) {
            // on va remplacer tout ce qui est entre le SELECT et le FROM par un COUNT
            final String select = "select";
            final String from = "from";
            
            int selectIndex = queryBuilder.indexOf(select);
            int fromIndex = queryBuilder.indexOf(from);
            StringBuilder countBuilder = new StringBuilder();
            countBuilder.append(' ').append("count(").append(this.entityName).append(") ");
            if(selectIndex >= 0 && fromIndex >= 0) {
                queryBuilder.replace(selectIndex + select.length(), fromIndex, countBuilder.toString());
            }
        }
        return em.createQuery(queryBuilder.toString());
    }
}
