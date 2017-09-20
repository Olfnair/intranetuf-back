/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities.query;

import java.util.HashMap;

/**
 *
 * @author Florian
 */
public class FlexQuerySpecification<T> {
    private final Class<T> resultClass;
    
    // spec where colonne, operateur : tout ce qui n'est pas dans le hashmap sera refusé.
    private final HashMap<String, String> whereClausesOperators  = new HashMap<>();
    private final HashMap<String, String> whereClausesLinkers = new HashMap<>(); // indique s'il faut employer OR ou AND pour ajouter la colonne à la requête.
    private final HashMap<String, String> whereClausesParameterNames = new HashMap<>();
    @SuppressWarnings("rawtypes")
    private final HashMap<String, Class> whereClausesParameterClass = new HashMap<>();
    
    // spec order by
    private final HashMap<String, Boolean> orderByClausesSpec = new HashMap<>();
    
    // where et order by par défaut
    private final HashMap<String, String> defaultWhereClauses = new HashMap<>();
    private final HashMap<String, String> defaultOrderByClauses = new HashMap<>();
    
    // permet de remplacer n'importe quelle colonne du where par autre chose (une fonction plus complexe par exemple...)
    private final HashMap<String, String> whereClausesReplacers = new HashMap<>();
    
    // semblable pour order by (on peut spécifier une liste de colonnes à la place)
    private final HashMap<String, String> orderByClausesReplacers = new HashMap<>();
    
    private final String baseQuery;
    private final String entityName;
    
    public FlexQuerySpecification(String baseQuery, String entityName, Class<T> resultClass) {
        this.baseQuery = baseQuery;
        this.entityName = entityName;
        this.resultClass = resultClass;
    }
    
    public Class<T> getResultClass() {
        return this.resultClass;
    }
    
    // spec pour ce qui est accepté en WHERE
    @SuppressWarnings("rawtypes")
    public void addWhereSpec(String column, String paramName, String operator, String link, Class classType) {
        whereClausesOperators.put(column, operator);
        whereClausesLinkers.put(column, link);
        whereClausesParameterNames.put(column, paramName);
        whereClausesParameterClass.put(column, classType);
    }
    
    // spec pour ce qui est accepté en ORDER BY
    public void addOrderBySpec(String column) {
        orderByClausesSpec.put(column, Boolean.TRUE);
    }
    
    // est-ce que la colonne est dans la spec ?
    public boolean whereColumnInSpec(String column) {
        return whereClausesOperators.containsKey(column);
    }
    
    // est-ce que la colonne est dans la spec ?
    public boolean orderByColumnInSpec(String column) {
        return orderByClausesSpec.containsKey(column);
    }
    
    public HashMap<String, String> getDefaultWhereClauses() {
        return defaultWhereClauses;
    }
    
    public HashMap<String, String> getDefaultOrderByClauses() {
        return defaultOrderByClauses;
    }
    
    public void addDefaultWhereClause(String column, String param) {
        defaultWhereClauses.put(column, param);
    }
    
    public void addDefaultOrderByClause(String column, String param) {
        defaultOrderByClauses.put(column, param);
    }
    
    public void addWhereClauseReplacer(String column, String replacer) {
        if(! whereColumnInSpec(column)) { return; }
        whereClausesReplacers.put(column, replacer.trim().replaceAll("\\s+", " "));
    }
    
    public void addOrderByClauseReplacer(String column, String replacer) {
        if(! orderByColumnInSpec(column)) { return; }
        // supprime absolument tous les espaces :
        orderByClausesReplacers.put(column, replacer.replaceAll("\\s+", ""));
    }

    public HashMap<String, String> getWhereClausesOperators() {
        return whereClausesOperators;
    }

    public HashMap<String, String> getWhereClausesLinkers() {
        return whereClausesLinkers;
    }

    public HashMap<String, String> getWhereClausesParameterNames() {
        return whereClausesParameterNames;
    }

    @SuppressWarnings("rawtypes")
    public HashMap<String, Class> getWhereClausesParameterClass() {
        return whereClausesParameterClass;
    }

    public HashMap<String, Boolean> getOrderByClausesSpec() {
        return orderByClausesSpec;
    }

    public HashMap<String, String> getWhereClausesReplacers() {
        return whereClausesReplacers;
    }

    public HashMap<String, String> getOrderByClausesReplacers() {
        return orderByClausesReplacers;
    }
    
    public String getBaseQuery() {
        return baseQuery;
    }

    public String getEntityName() {
        return entityName;
    }
}
