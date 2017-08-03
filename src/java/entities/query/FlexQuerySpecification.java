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
    private final HashMap<String, String> whereColsOperators  = new HashMap<>();
    private final HashMap<String, String> whereColsLinkers = new HashMap<>(); // indique s'il faut employer OR ou AND pour ajouter la colonne à la requête.
    private final HashMap<String, String> whereColsParameterNames = new HashMap<>();
    @SuppressWarnings("rawtypes")
    private final HashMap<String, Class> whereColsParameterClass = new HashMap<>();
    
    // spec order by
    private final HashMap<String, Boolean> orderByColsSpec = new HashMap<>();
    
    // where et order by par défaut
    private final HashMap<String, String> defaultWhereCols = new HashMap<>();
    private final HashMap<String, String> defaultOrderByCols = new HashMap<>();
    
    // permet de remplacer n'importe quelle colonne du where par autre chose (une fonction plus complexe par exemple...)
    private final HashMap<String, String> whereColsReplacers = new HashMap<>();
    
    // semblable pour order by (on peut spécifier une liste de colonnes à la place)
    private final HashMap<String, String> orderByColsReplacers = new HashMap<>();
    
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
        whereColsOperators.put(column, operator);
        whereColsLinkers.put(column, link);
        whereColsParameterNames.put(column, paramName);
        whereColsParameterClass.put(column, classType);
    }
    
    // spec pour ce qui est accepté en ORDER BY
    public void addOrderBySpec(String column) {
        orderByColsSpec.put(column, Boolean.TRUE);
    }
    
    // est-ce que la colonne est dans la spec ?
    public boolean whereColInSpec(String col) {
        return whereColsOperators.containsKey(col);
    }
    
    // est-ce que la colonne est dans la spec ?
    public boolean orderByColInSpec(String col) {
        return orderByColsSpec.containsKey(col);
    }
    
    public HashMap<String, String> getDefaultWhereCols() {
        return defaultWhereCols;
    }
    
    public HashMap<String, String> getDefaultOrderByCols() {
        return defaultOrderByCols;
    }
    
    public void addDefaultWhereCol(String col, String param) {
        defaultWhereCols.put(col, param);
    }
    
    public void addDefaultOrderByCol(String col, String param) {
        defaultOrderByCols.put(col, param);
    }
    
    public void addWhereColReplacer(String col, String replacer) {
        if(! whereColInSpec(col)) { return; }
        whereColsReplacers.put(col, replacer.trim().replaceAll("[\n\t ]+", " "));
    }
    
    public void addOrderByColReplacer(String col, String replacer) {
        if(! orderByColInSpec(col)) { return; }
        // supprime absolument tous les espaces :
        orderByColsReplacers.put(col, replacer.trim().replaceAll("[\n\t ]", ""));
    }

    public HashMap<String, String> getWhereColsOperators() {
        return whereColsOperators;
    }

    public HashMap<String, String> getWhereColsLinkers() {
        return whereColsLinkers;
    }

    public HashMap<String, String> getWhereColsParameterNames() {
        return whereColsParameterNames;
    }

    @SuppressWarnings("rawtypes")
    public HashMap<String, Class> getWhereColsParameterClass() {
        return whereColsParameterClass;
    }

    public HashMap<String, Boolean> getOrderByColsSpec() {
        return orderByColsSpec;
    }

    public HashMap<String, String> getWhereColsReplacers() {
        return whereColsReplacers;
    }

    public HashMap<String, String> getOrderByColsReplacers() {
        return orderByColsReplacers;
    }
    
    public String getBaseQuery() {
        return baseQuery;
    }

    public String getEntityName() {
        return entityName;
    }
}
