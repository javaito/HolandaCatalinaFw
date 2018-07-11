package org.hcjf.layers.query;

/**
 * This class represent a join operation
 * @author javaito
 *
 */
public class Join extends EvaluatorCollection implements Comparable<Join> {

    private final Query.QueryResource resource;
    private Equals joinCondition;
    private final JoinType type;

    public Join(Query query, String resourceName, JoinType type) {
        super(query);
        this.resource = new Query.QueryResource(resourceName);
        this.type = type;
    }

    /**
     * Override this method to complete the join condition with the first
     * evaluator of the collection.
     * @param evaluator Evaluator added.
     */
    @Override
    protected boolean onAddEvaluator(Evaluator evaluator) {
        boolean result = true;
        if(joinCondition == null) {
            result = false;
            //The first time that call this method.
            if(evaluator instanceof Equals) {
                if(((Equals)evaluator).getLeftValue() instanceof Query.QueryField &&
                        ((Equals)evaluator).getRightValue() instanceof Query.QueryField) {
                    joinCondition = (Equals) evaluator;
                } else {
                    throw new IllegalArgumentException("The join condition must be between two resource fields.");
                }
            } else {
                throw new IllegalArgumentException("The first evaluator must be the join condition (Equals evaluator).");
            }
        }
        return result;
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
        return (Query.QueryField) joinCondition.getLeftValue();
    }

    /**
     * Return the right field of the join operation.
     * @return Right field.
     */
    public Query.QueryField getRightField() {
        return (Query.QueryField) joinCondition.getRightValue();
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
