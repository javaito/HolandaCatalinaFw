package org.hcjf.layers.query;

/**
 * This class represent a join operation
 * @author javaito
 * @email javaito@gmail.com
 */
public class Join implements Comparable<Join> {

    private final String resourceName;
    private final String leftField;
    private final String rightField;
    private final JoinType type;

    public Join(String resourceName, String leftField, String rightField, JoinType type) {
        this.resourceName = resourceName;
        this.leftField = leftField;
        this.rightField = rightField;
        this.type = type;
    }

    /**
     * Return the resource name to join.
     * @return Resource name.
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Return the left field of the join operation.
     * @return Left field.
     */
    public String getLeftField() {
        return leftField;
    }

    /**
     * Return the right field of the join operation.
     * @return Right field.
     */
    public String getRightField() {
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
