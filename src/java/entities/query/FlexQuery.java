/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package entities.query;

import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Florian
 */

// Exemple d'utilisation :
// Select u From User u WHERE u.password = :password AND u.login = :login AND u.active = true AND u.pending = false :where: :orderby:
public class FlexQuery {
    protected interface TypeCaster {
        public Object cast(String value);
    }
    
    protected interface OperatorAdapter {
        public Object adapt(String value);
    }
    
    protected final static String WHERE_SELECTOR = ":where:";
    protected final static String ORDERBY_SELECTOR = ":orderby:";
    
    protected final static HashMap<Class, TypeCaster> CASTMAP;
    protected final static HashMap<String, OperatorAdapter> OPERATORADAPTMAP;
    
    static {
        CASTMAP = new HashMap();
        CASTMAP.put(Long.class, (String value) -> {
            return Long.parseLong(value);
        });
        CASTMAP.put(long.class, (String value) -> {
            return Long.parseLong(value);
        });
        CASTMAP.put(Integer.class, (String value) -> {
            return Integer.parseInt(value);
        });
        CASTMAP.put(int.class, (String value) -> {
            return Integer.parseInt(value);
        });
        // ajouter les types nécessaires...
        
        OPERATORADAPTMAP = new HashMap();
        OPERATORADAPTMAP.put("like", (String value) -> {
            return '%' + value + '%';
        });
        // ajouter les opérateurs particuliers utilisés...
    }
            
    // spec where colonne, operateur : tout ce qui n'est pas dans le hashmap sera refusé.
    private final HashMap<String, String> whereColsOperators  = new HashMap();
    private final HashMap<String, String> whereColsLinkers = new HashMap(); // indique s'il faut employer OR ou AND pour ajouter la colonne à la requête.
    private final HashMap<String, String> whereColsParameterNames = new HashMap();
    private final HashMap<String, Class> whereColsParameterClass = new HashMap();
    
    // spec order by
    private final HashMap<String, Boolean> orderByColsSpec = new HashMap();
    
    private final HashMap<String, String> whereCols = new HashMap();
    private final HashMap<String, String> orderByCols = new HashMap();
    
    // permet de remplacer n'importe quelle colonne du where par autre chose (une fonction plus complexe par exemple...)
    private final HashMap<String, String> whereColsReplacers = new HashMap();
    
    // semblable pour order by (on peut spécifier une liste de colonnes à la place)
    private final HashMap<String, String> orderByColsReplacers = new HashMap();
    
    private final String baseQuery;
    private final String entityName;
    
    // variables de travail :
    private boolean count = false;
    private StringBuilder queryBuilder = null;
    private StringBuilder searchBuilder = null;
    
    // reset pour une prochaine requête éventuelle
    protected void clear() {
        whereCols.clear();
        orderByCols.clear();
    }
    
    public FlexQuery(String baseQuery, String entityName) {
        this.baseQuery = baseQuery;
        this.entityName = entityName;
    }
    
    // spec pour ce qui est accepté en WHERE
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
    
    // ajoute une une colonne dans le WHERE
    public void addWhereCol(String col, String param) {
        if(! whereColInSpec(col)) { return; }
        whereCols.put(col, param);
    }
    
    // ajoute une colonne order by à la requete
    public void addOrderByCol(String col, String param) {
        if(! orderByColInSpec(col)) { return; }
        orderByCols.put(col, param);
    }
    
    public void addWhereColReplacer(String col, String replacer) {
        if(! whereColInSpec(col)) { return; }
        whereColsReplacers.put(col, replacer);
    }
    
    public void addOrderByColReplacer(String col, String replacer) {
        if(! orderByColInSpec(col)) { return; }
        orderByColsReplacers.put(col, replacer.trim());
    }
    
    // renvoie le nom d'un paramètre pour une colonne donnée
    public String getParamName(String col) {
        return whereColsParameterNames.get(col);
    }
    
    // data
    public Query getQuery(EntityManager em) {
        count = false;
        return buildQuery(em);
    }
    
    // count
    public Query getCountQuery(EntityManager em) {
        count = true;
        return buildQuery(em);
    }
    
    // construit la clause WHERE
    protected void buildWhere(StringBuilder whereBuilder) {
        whereCols.keySet().forEach((col) -> {
            whereBuilder.append(' ').append(whereColsLinkers.get(col)).append(' ');
            if(whereColsReplacers.containsKey(col)) {
                String replacer = whereColsReplacers.get(col);
                whereBuilder.append(replacer);
            }
            else {
                whereBuilder.append(entityName).append('.').append(col);
            }
            whereBuilder.append(' ').append(whereColsOperators.get(col)).append(" :")
                    .append(whereColsParameterNames.get(col)); // id du param;
        });
    }
    
    // construit la clause ORDER BY
    protected void buildOrderBy(StringBuilder orderByBuilder) {
        if(count) { return; }
        orderByCols.keySet().forEach((col) -> {
            if(orderByBuilder.length() == 0) {
                orderByBuilder.append("order by ");
            }
            else {
                orderByBuilder.append(',');
            }
            orderByBuilder.append(entityName).append('.').append(col);
            String order = orderByCols.get(col);
            if(order.equals("DESC") || order.equals("desc")) {
                orderByBuilder.append(" desc");
            }
            else {
                orderByBuilder.append(" asc");
            }
        });
    }
     
    // bind les params de la clause WHERE en fonction de ce qui a été donné en paramètre
    protected void bindParams(Query query) {
        whereCols.keySet().forEach((col) -> {
            String paramName = getParamName(col);
            Object value = whereCols.get(col);
            String operator = whereColsOperators.get(col);
            Class classType = whereColsParameterClass.get(col);
            if(OPERATORADAPTMAP.containsKey(operator.toLowerCase())) {
                // adaptation de la valeur pour certains opérateurs (LIKE, ...)
                OperatorAdapter operatorAdapter = OPERATORADAPTMAP.get(operator.toLowerCase());
                value = operatorAdapter.adapt((String) value);
            }
            if(CASTMAP.containsKey(classType)) {
                // cast de la valeur pour certains types (Long, Integer, ..)
                TypeCaster typeCaster = CASTMAP.get(classType);
                value = typeCaster.cast((String)value);
            }
            query.setParameter(paramName, value);
        });
    }
    
    // remplace ce qu'il faut dans la requête en cours de construction pour faire un count si besoin
    protected StringBuilder prepareForCount() {
        if(! count) { return queryBuilder; }
        // on va remplacer tout ce qui est entre le SELECT et le FROM par un COUNT
        final String select = "select";
        final String from = "from";
        
        int selectIndex = searchBuilder.indexOf(select);
        int fromIndex = searchBuilder.indexOf(from);
        StringBuilder countBuilder = new StringBuilder();
        countBuilder.append(' ').append("count(").append(entityName).append(") ");
        if(selectIndex >= 0 && fromIndex >= 0) {
            searchBuilder.replace(selectIndex + select.length(), fromIndex, countBuilder.toString());
            queryBuilder.replace(selectIndex + select.length(), fromIndex, countBuilder.toString());
        }
        return queryBuilder;
    }
    
    // insère une clause (WHERE ou ORDER BY) dans la requête en cours de construction
    protected void insertClause(String selector, StringBuilder clauseBuilder) {
        int index = searchBuilder.indexOf(selector);
        if(index >= 0) {
            searchBuilder.replace(index, index + selector.length(), clauseBuilder.toString());
            queryBuilder.replace(index, index + selector.length(), clauseBuilder.toString());
        }
    }
    
    // Query setFirstResult and setMaxResult pour le limit
    protected Query buildQuery(EntityManager em) {
        StringBuilder whereBuilder = new StringBuilder(100);
        StringBuilder orderByBuilder = new StringBuilder(100);
        queryBuilder = new StringBuilder(255);
        searchBuilder = new StringBuilder(255);
        
        // construit les clauses WHERE et ORDER BY en fonction des paramètres ajoutés avant l'appel
        buildWhere(whereBuilder);
        buildOrderBy(orderByBuilder);
        
        // tout en minuscle pour simplifier les recherches.
        // (les entités doivent garder leurs majuscules et on ne peut donc pas juste se contenter
        //  de mettre la requête en minuscules)
        searchBuilder.append(baseQuery.toLowerCase());
        queryBuilder.append(baseQuery);
        
        // on insère les clauses WHERE et ORDER BY qu'on vient de construire
        insertClause(WHERE_SELECTOR, whereBuilder);
        insertClause(ORDERBY_SELECTOR, orderByBuilder);
        
        // n'agira que si c'est un count
        prepareForCount();
        
        Query query = em.createQuery(queryBuilder.toString());
        bindParams(query);
        clear(); // reset des paramètres pour les prochaines requêtes
        return query;
    }
}
