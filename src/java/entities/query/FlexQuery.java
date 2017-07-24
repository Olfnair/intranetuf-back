/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package entities.query;

import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Florian
 * @param <T>
 */

// Exemple d'utilisation :
// Select u From User u WHERE u.password = :password AND u.login = :login AND u.active = true AND u.pending = false :where: :orderby:
public class FlexQuery<T> {
    protected interface TypeCaster {
        public Object cast(String value);
    }
    
    protected interface OperatorAdapter {
        public Object adapt(String value);
    }
    
    protected final static String WHERE_SELECTOR = ":where:";
    protected final static String ORDERBY_SELECTOR = ":orderby:";
    
    protected final static HashMap<Class, TypeCaster> CAST_MAP;
    protected final static HashMap<String, OperatorAdapter> OPERATOR_ADAPT_MAP;
    
    static {
        CAST_MAP = new HashMap();
        CAST_MAP.put(Long.class, (String value) -> {
            return Long.parseLong(value);
        });
        CAST_MAP.put(long.class, (String value) -> {
            return Long.parseLong(value);
        });
        CAST_MAP.put(Integer.class, (String value) -> {
            return Integer.parseInt(value);
        });
        CAST_MAP.put(int.class, (String value) -> {
            return Integer.parseInt(value);
        });
        // ajouter les types nécessaires...
        
        // enter les operateurs en lowercase :
        OPERATOR_ADAPT_MAP = new HashMap();
        OPERATOR_ADAPT_MAP.put("like", (String value) -> {
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
    
    private final HashMap<String, Object> whereCols = new HashMap();
    private final HashMap<String, String> orderByCols = new HashMap();
    
    // permet de remplacer n'importe quelle colonne du where par autre chose (une fonction plus complexe par exemple...)
    private final HashMap<String, String> whereColsReplacers = new HashMap();
    
    // semblable pour order by (on peut spécifier une liste de colonnes à la place)
    private final HashMap<String, String> orderByColsReplacers = new HashMap();
    
    private final String baseQuery;
    private final String entityName;
    private Query query = null;
    
    private Integer index = 0;
    private Integer limit = 0;
    private boolean paginate = false;
    
    // variables de travail :
    private boolean count = false;
    private StringBuilder queryBuilder = null;
    private StringBuilder searchBuilder = null;
    private EntityManager em = null;
    
    // reset pour une prochaine requête éventuelle
    protected void clear() {
        whereCols.clear();
        orderByCols.clear();
        paginate = false;
        query = null;
        em = null;
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
    public boolean addWhereCol(String col, String param) {
        if(! whereColInSpec(col)) { return false; }
        
        String operator = whereColsOperators.get(col);
        Class classType = whereColsParameterClass.get(col);
        // suppression des espaces pas nécessaires dans param
        param = param.trim().replaceAll("[\n\t ]+", " ");
        Object value = param;
        if(OPERATOR_ADAPT_MAP.containsKey(operator.toLowerCase())) {
            // adaptation de la valeur pour certains opérateurs (LIKE, ...)
            OperatorAdapter operatorAdapter = OPERATOR_ADAPT_MAP.get(operator.toLowerCase());
            try {
                value = operatorAdapter.adapt(param);
            } catch (Exception e) {
                // en cas d'erreur, on ignore simplement cet ajout
                return false;
            }
        }
        if(CAST_MAP.containsKey(classType)) {
            // cast de la valeur pour certains types (Long, Integer, ..)
            TypeCaster typeCaster = CAST_MAP.get(classType);
            try {
                value = typeCaster.cast(param);
            } catch(Exception e) {
                // en cas d'erreur, on ignore simplement cet ajout
                return false;
            }
        }
        whereCols.put(col, value);
        return true;
    }
    
    // ajoute une colonne order by à la requete
    public boolean addOrderByCol(String col, String param) {
        if(! orderByColInSpec(col)) { return false; }
        
        orderByCols.put(col, param);
        return true;
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
    
    // renvoie le nom d'un paramètre pour une colonne donnée
    public String getParamName(String col) {
        return whereColsParameterNames.get(col);
    }
    
    public void setPaginationParams(Integer index, Integer limit) {
        this.index = index;
        this.limit = limit;
        this.paginate = true;
    }
    
    // data
    public void prepareQuery(EntityManager em) {
        count = false;
        buildQuery(em);
    }
    
    // count
    public void prepareCountQuery(EntityManager em) {
        count = true;
        buildQuery(em);
    }
    
    public String getSortOrder(String column) {
        String order = orderByCols.get(column);
        if(order.equals("DESC") || order.equals("desc")) {
            return "desc";
        }
        return "asc";
    }
    
    public void setParameter(String name, Object value) {
        if(query == null) {
            // TODO : lancer exception ?
            return;
        }      
        query.setParameter(name, value);
    }
    
    public FlexQueryResult<T> execute() {
        Long totalCount = -1L;
        
        // total count
        if(count) {
            List<Long> countResult = query.getResultList();
            if(countResult == null || countResult.size() < 1) {
                // exception ?
                clear();
                return null;
            }
            totalCount = countResult.get(0);
            count = false;
            buildQuery(em);
        }
        
        // filtered results
        List<T> results = query.getResultList();
        if(results == null) {
            // exception ?
            clear();
            return null;
        }
        
        // reset des paramètres pour les requêtes suivantes éventuelles
        clear();
        
        // retour
        return new FlexQueryResult<>(results, (totalCount < 0) ? results.size() : totalCount);
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
    
    protected boolean buildReplacerOrderBy(String column, StringBuilder orderByBuilder) {
        if(! orderByColsReplacers.containsKey(column)) { return false; }
        
        String sortOrder = getSortOrder(column);
        StringTokenizer tokens = new StringTokenizer(orderByColsReplacers.get(column), ",;/");
        boolean comma = false;
        while(tokens.hasMoreElements()) {
            if(comma) {
                orderByBuilder.append(',');
            }
            orderByBuilder.append(tokens.nextElement()).append(' ').append(sortOrder);
            comma = true;
        }
        return true;
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
            if(! buildReplacerOrderBy(col, orderByBuilder)) {
                orderByBuilder.append(entityName).append('.').append(col).append(' ').append(getSortOrder(col));
            }
        });
    }
     
    // bind les params de la clause WHERE en fonction de ce qui a été donné en paramètre
    protected void bindParams(Query query) {
        whereCols.keySet().forEach((col) -> {
            String paramName = getParamName(col);
            Object value = whereCols.get(col);
            setParameter(paramName, value);
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
        int i = searchBuilder.indexOf(selector);
        if(i < 0) { return ; }
        searchBuilder.replace(i, i + selector.length(), clauseBuilder.toString());
        queryBuilder.replace(i, i + selector.length(), clauseBuilder.toString());
    }
    
    protected void buildQuery(EntityManager em) {
        StringBuilder whereBuilder = new StringBuilder(100);
        StringBuilder orderByBuilder = new StringBuilder(100);
        queryBuilder = new StringBuilder(255);
        searchBuilder = new StringBuilder(255);
        
        this.em = em;
        
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
        
        instanciateQuery();
    }
    
    // Query setFirstResult and setMaxResult pour le limit
    protected void instanciateQuery() {
        query = em.createQuery(queryBuilder.toString());
        bindParams(query);
        if(paginate && ! count) {
            query.setFirstResult(index); 
            query.setMaxResults(limit);
        }
    }
}
