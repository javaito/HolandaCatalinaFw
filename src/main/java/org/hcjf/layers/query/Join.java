package org.hcjf.layers.query;

/**
 * This class represent a join operation
 * @author javaito
 *
 */
public class Join extends EvaluatorCollection implements Comparable<Join> {

    private final Query.QueryResource resource;
    private final JoinType type;

    public Join(Query query, String resourceName, JoinType type) {
        super(query);
        this.resource = new Query.QueryResource(resourceName);
        this.type = type;
    }

    /**
     * Return the resource of the join.
     * @return Join's resource
     */
    public Query.QueryResource getResource() {
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
