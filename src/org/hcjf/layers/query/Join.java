package org.hcjf.layers.query;

/**
 * This class represent a join operation
 * @author javaito
 * @email javaito@gmail.com
 */
public class Join implements Comparable<Join> {

    private final Query.QueryResource resource;
    private final Query.QueryField leftField;
    private final Query.QueryField rightField;
    private final JoinType type;

    public Join(String resourceName, Query.QueryField leftField, Query.QueryField rightField, JoinType type) {
        this.resource = new Query.QueryResource(resourceName);
        this.leftField = leftField;
        this.rightField = rightField;
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
     * Return the left field of the join operation.
     * @return Left field.
     */
    public Query.QueryField getLeftField() {
        return leftField;
    }

    /**
     * Return the right field of the join operation.
     * @return Right field.
     */
    public Query.QueryField getRightField() {
        return rightField;
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
        return join.getResourceName().compareTo(getResourceName()) +
                join.getLeftField().compareTo(getLeftField()) +
                join.getRightField().compareTo(getRightField());
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
            result = join.getResourceName().equals(getResourceName()) &&
                    join.getLeftField().equals(getLeftField()) &&
                    join.getRightField().equals(getRightField());
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

        RIGHT

    }
}
