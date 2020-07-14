package org.hcjf.layers.query.evaluators;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.query.Queryable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * @author javaito
 *
 */
public class In extends FieldEvaluator {

    public In(Object leftValue, Object rightValue) {
        super(leftValue, rightValue);
    }

    @Override
    public boolean evaluate(Object object, Queryable.DataSource dataSource, Queryable.Consumer consumer) {
        boolean result = false;

        try {
            Object leftValue = getProcessedLeftValue(object, dataSource, consumer);
            Object rightValue = getProcessedRightValue(object, dataSource, consumer);

            Collection collection = null;
            if(Map.class.isAssignableFrom(rightValue.getClass())) {
                collection = ((Map)rightValue).keySet();
            } else if(Collection.class.isAssignableFrom(rightValue.getClass())) {
                collection = ((Collection)rightValue);
            } else if(rightValue.getClass().isArray()) {
                collection = Arrays.asList((Object[])rightValue);
            }

            if(collection == null) {
                if(leftValue instanceof Number) {
                    result = numberEquals((Number) leftValue, rightValue);
                } else {
                    result = leftValue.equals(rightValue);
                }
            } else {
                if (leftValue instanceof Number) {
                    for (Object collectionItem : collection) {
                        result = numberEquals((Number) leftValue, collectionItem);
                        if (result) {
                            break;
                        }
                    }
                } else {
                    result = collection.contains(leftValue);
                }
            }
        } catch (Exception ex) {
            throw new HCJFRuntimeException("In evaluator fail", ex);
        }

        return result;
    }
    
}
