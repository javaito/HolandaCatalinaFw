package org.hcjf.layers.query;

import org.hcjf.layers.query.evaluators.EvaluatorCollection;
import org.hcjf.layers.query.model.QueryResource;

/**
 * This class represent a join operation
 * @author javaito
 *
 */
public class Join extends EvaluatorCollection implements Comparable<Join> {

    private final QueryResource resource;
    private final JoinType type;
    private final Boolean outer;
    private Boolean isNestedJoin;

    public Join(Query query, String resourceName, JoinType type) {
        this(query, new QueryResource(resourceName), type, false);
    }

    public Join(Query query, QueryResource resource, JoinType type) {
        this(query, resource, type, false);
    }

    public Join(Query query, String resourceName, JoinType type, boolean outer) {
        this(query, new QueryResource(resourceName), type, outer);
    }

    public Join(Query query, QueryResource resource, JoinType type, boolean outer) {
        super(query);
        this.resource = resource;
        this.type = type;
        this.outer = outer;
    }

    /**
     * Return the resource of the join.
     * @return Join's resource
     */
    public QueryResource getResource() {
        return resource;
    }

    /**
     * Return the resource name to join.
     * @return Resource name.
     */
    public String getResourceName() {
        return resource.getResourceName();
    }

    /**
     * Return the join operation type.
     * @return Join type.
     */
    public JoinType getType() {
        return type;
    }

    /**
     * Verify if the join instance has a outer modifier.
     * @return True if the join has a modifier.
     */
    public Boolean getOuter() {
        return outer;
    }

    /**
     * Return if is nested join
     * @return true if the join is nestedJoin
     * */
    public Boolean isNestedJoin() {
        return isNestedJoin;
    }

    /**
     * Set value to isNestedJoin
     * @param isNestedJoin
     * */
    public void setIsNestedJoin(boolean isNestedJoin){
        this.isNestedJoin = isNestedJoin;
    }

    /**
     * Compare two instances of joins.
     * @param join Other join to compare.
     * @return Return int value that represents the difference between two instances.
     */
    @Override
    public int compareTo(Join join) {
        return getResource().compareTo(join.getResource());
    }

    /**
     * Verify if this instance of join is equals that other object.
     * @param object Object to verify.
     * @return Return true if the join is equals.
     */
    @Override
    public boolean equals(Object object) {
        boolean result = false;
        if(object.getClass().equals(getClass())) {
            Join join = (Join) object;
            result = getResource().equals(join.getResource());
        }
        return result;
    }

    /**
     * Join types.
     */
    public enum JoinType {

        JOIN,

        INNER,

        LEFT,

        RIGHT,

        FULL

    }
}
