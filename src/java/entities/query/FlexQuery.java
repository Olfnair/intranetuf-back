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
import javax.persistence.TypedQuery;

/**
 *
 * @author Florian
 * @param <T>
 * 
 * Exemple d'utilisation :
 * Select u From User u WHERE u.password = :password AND u.login = :login AND u.active = true AND u.pending = false :where: :orderby:
 */
public class FlexQuery<T> {
    protected interface TypeCaster {
        public Object cast(String value);
    }
    
    protected interface OperatorAdapter {
        public Object adapt(String value);
    }
    
    protected final static String WHERE_SELECTOR = ":where:";
    protected final static String ORDERBY_SELECTOR = ":orderby:";
    
    protected final static boolean WHERE = true;
    protected final static boolean ORDERBY = false;
    
    @SuppressWarnings("rawtypes")
    protected final static HashMap<Class, TypeCaster> CAST_MAP;
    protected final static HashMap<String, OperatorAdapter> OPERATOR_ADAPT_MAP;
    
    static {
        CAST_MAP = new HashMap<>();
        CAST_MAP.put(Long.class, (String value) -> {
            return Long.valueOf(value);
        });
        CAST_MAP.put(long.class, (String value) -> {
            return Long.valueOf(value);
        });
        CAST_MAP.put(Integer.class, (String value) -> {
            return Integer.valueOf(value);
        });
        CAST_MAP.put(int.class, (String value) -> {
            return Integer.valueOf(value);
        });
        CAST_MAP.put(Boolean.class, (String value) -> {
            return Boolean.valueOf(value);
        });
        CAST_MAP.put(boolean.class, (String value) -> {
            return Boolean.valueOf(value);
        });
        // ajouter les types nécessaires...
        
        // entrer les operateurs en lowercase :
        OPERATOR_ADAPT_MAP = new HashMap<>();
        OPERATOR_ADAPT_MAP.put("like", (String value) -> {
            return '%' + value + '%';
        });
        // ajouter les opérateurs particuliers utilisés...
    }
    
    private final FlexQuerySpecification<T> specification;
    
    private final Class<T> resultClass;
    
    private final HashMap<String, Object> whereCols = new HashMap<>();
    private final HashMap<String, String> orderByCols = new HashMap<>();
    
    private TypedQuery<T> query = null;
    private TypedQuery<Long> countQuery = null;
    
    private Integer index = 0;
    private Integer limit = 0;
    
    // variables de travail :
    private boolean count = false;
    private StringBuilder queryBuilder = null;
    private StringBuilder searchBuilder = null;
    private EntityManager em = null;
    
    // reset pour une prochaine requête éventuelle
    protected void clear() {
        whereCols.clear();
        orderByCols.clear();
        index = 0;
        limit = 0;
        query = null;
        countQuery = null;
        em = null;
        count = false;
    }
    
    public FlexQuery(FlexQuerySpecification<T> specification) {
        this.specification = specification;
        this.resultClass = specification.getResultClass();
    }
    
    public void setParameters(String whereParams, String orderByParams) {
        setParameters(whereParams, orderByParams, 0, 0);
    }
    
    public void setParameters(String whereParams, String orderByParams, Integer index) {
        setParameters(whereParams, orderByParams, index, 0);
    }
    
    public void setParameters(String whereParams, String orderByParams, Integer index, Integer limit) {
        HashMap<String, String> whereMap = new ParamsParser(whereParams).parse();
        HashMap<String, String> orderbyMap = new ParamsParser(orderByParams).parse();
        
        clear();
        
        whereMap.keySet().forEach((String col) -> {
            addWhereCol(col, whereMap.get(col));
        });
        
        orderbyMap.keySet().forEach((String col) -> {
            addOrderByCol(col, orderbyMap.get(col));
        });
                
        setPaginationParams(index, limit);
    }
    
    // ajoute une une colonne dans le WHERE
    public boolean addWhereCol(String col, String param) {
        if(! specification.whereColInSpec(col)) { return false; }
        
        String operator = specification.getWhereColsOperators().get(col);
        @SuppressWarnings("rawtypes")
        Class classType = specification.getWhereColsParameterClass().get(col);
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
        if(! specification.orderByColInSpec(col)) { return false; }
        
        orderByCols.put(col, param);
        return true;
    }
    
    // renvoie le nom d'un paramètre pour une colonne donnée
    public String getParamName(String col) {
        return specification.getWhereColsParameterNames().get(col);
    }
    
    public void setPaginationParams(Integer index, Integer limit) {
        this.index = index;
        this.limit = limit;
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
        Query q = getQuery(count);
        if(q == null) {
            // TODO : lancer exception ?
            return;
        }      
        q.setParameter(name, value);
    }
    
    public FlexQueryResult<T> execute() {
        Long totalCount = -1L;
        
        // total count
        if(count) {
            List<Long> countResult = countQuery.getResultList();
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
        return new FlexQueryResult<>(results, (totalCount < 0L) ? results.size() : totalCount);
    }
    
    protected Query getQuery(boolean cq) {
        return cq ? countQuery : query;
    }
    
    protected void checkForDefaultParams(boolean where) {
        if(where ? ! whereCols.isEmpty() : ! orderByCols.isEmpty()) {
            // rien à faire
            return;
        }
        HashMap<String, String> defaultCols = where ? specification.getDefaultWhereCols()
                : specification.getDefaultOrderByCols();
        defaultCols.keySet().forEach((col) -> {
            if(where) {
                addWhereCol(col, defaultCols.get(col));
            }
            else {
                addOrderByCol(col, defaultCols.get(col));
            }           
        });
    }
    
    // construit la clause WHERE
    protected void buildWhere(StringBuilder whereBuilder) {
        checkForDefaultParams(WHERE);
        whereCols.keySet().forEach((col) -> {
            if(whereBuilder.length() == 0 && this.searchBuilder.indexOf(" where ") < 0) {
                // il n'y a pas de clause where dans la requête de base : on l'ajoute
                whereBuilder.append("where ");
            }
            else {
                whereBuilder.append(' ').append(specification.getWhereColsLinkers().get(col)).append(' ');
            }
            if(specification.getWhereColsReplacers().containsKey(col)) {
                String replacer = specification.getWhereColsReplacers().get(col);
                whereBuilder.append(replacer);
            }
            else {
                whereBuilder.append(specification.getEntityName()).append('.').append(col);
            }
            whereBuilder.append(' ').append(specification.getWhereColsOperators().get(col)).append(" :")
                    .append(specification.getWhereColsParameterNames().get(col)); // id du param;
        });
    }
    
    protected boolean buildReplacerOrderBy(String column, StringBuilder orderByBuilder) {
        if(! specification.getOrderByColsReplacers().containsKey(column)) { return false; }
        
        String sortOrder = getSortOrder(column);
        StringTokenizer tokens = new StringTokenizer(specification.getOrderByColsReplacers().get(column), ",;/");
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
        checkForDefaultParams(ORDERBY);
        orderByCols.keySet().forEach((col) -> {
            if(orderByBuilder.length() == 0) {
                orderByBuilder.append("order by ");
            }
            else {
                orderByBuilder.append(',');
            }
            if(! buildReplacerOrderBy(col, orderByBuilder)) {
                orderByBuilder.append(specification.getEntityName()).append('.').append(col).append(' ').append(getSortOrder(col));
            }
        });
    }
     
    // bind les params de la clause WHERE en fonction de ce qui a été donné en paramètre
    protected void bindParams() {
        whereCols.keySet().forEach((col) -> {
            String paramName = getParamName(col);
            Object value = whereCols.get(col);
            setParameter(paramName, value);
        });
    }
    
    // remplace ce qu'il faut dans la requête en cours de construction pour faire un count si besoin
    protected void prepareForCount() {
        if(! count) { return; }
        // on va remplacer tout ce qui est entre le SELECT et le FROM par un COUNT
        final String select = "select";
        final String from = "from";
        
        int selectIndex = searchBuilder.indexOf(select);
        int fromIndex = searchBuilder.indexOf(from);
        StringBuilder countBuilder = new StringBuilder();
        countBuilder.append(' ').append("count(").append(specification.getEntityName()).append(") ");
        if(selectIndex >= 0 && fromIndex >= 0) {
            searchBuilder.replace(selectIndex + select.length(), fromIndex, countBuilder.toString());
            queryBuilder.replace(selectIndex + select.length(), fromIndex, countBuilder.toString());
        }
    }
    
    // insère une clause (WHERE ou ORDER BY) dans la requête en cours de construction
    protected void insertClause(String selector, StringBuilder clauseBuilder) {
        int i = searchBuilder.indexOf(selector);
        if(i < 0) { return; }
        searchBuilder.replace(i, i + selector.length(), clauseBuilder.toString());
        queryBuilder.replace(i, i + selector.length(), clauseBuilder.toString());
    }
    
    protected void buildQuery(EntityManager em) {
        StringBuilder whereBuilder = new StringBuilder(100);
        StringBuilder orderByBuilder = new StringBuilder(100);
        queryBuilder = new StringBuilder(255);
        searchBuilder = new StringBuilder(255);
        
        this.em = em;
        
        // tout en minuscle pour simplifier les recherches.
        // (les entités doivent garder leurs majuscules et on ne peut donc pas juste se contenter
        //  de mettre la requête en minuscules)
        searchBuilder.append(specification.getBaseQuery().toLowerCase());
        queryBuilder.append(specification.getBaseQuery());
        
        // construit les clauses WHERE et ORDER BY en fonction des paramètres ajoutés avant l'appel
        buildWhere(whereBuilder);
        buildOrderBy(orderByBuilder);
        
        // on insère les clauses WHERE et ORDER BY qu'on vient de construire
        insertClause(WHERE_SELECTOR, whereBuilder);
        insertClause(ORDERBY_SELECTOR, orderByBuilder);
        
        // n'agira que si c'est un count
        prepareForCount();
        
        instanciateQuery();
        bindParams();
        setLimits();
    }
    
    // Query setFirstResult and setMaxResult pour le limit
    protected void instanciateQuery() {     
        if(count) {
            countQuery = em.createQuery(queryBuilder.toString(), Long.class);
        }
        else {
            query = em.createQuery(queryBuilder.toString(), resultClass);
        }
    }
    
    protected void setLimits() {
        if(count) { return; }
        if(index > 0) {
            query.setFirstResult(index);
        }
        if(limit > 0) {
            query.setMaxResults(limit);
        }
    }
}
