package org.hcjf.layers.query;

import java.util.*;

/**
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

    public String getResourceName() {
        return resourceName;
    }

    public String getLeftField() {
        return leftField;
    }

    public String getRightField() {
        return rightField;
    }

    public JoinType getType() {
        return type;
    }

    public Collection<Map<String,Object>> evaluate(
            String leftField, Collection<Object> leftSource, Query.Consumer leftConsumer,
            String rightField, Query.Consumer rightConsumer){

        Map<Object, List<Object>> leftMap = new HashMap<>();
        Map<Object, List<Object>> rightMap = new HashMap<>();

        Object leftValue;
        for(Object srcLeftObject : leftSource) {
            leftValue = leftConsumer.get(srcLeftObject, leftField);
            if(leftMap.containsKey(leftValue)) {
                leftMap.put(leftValue, new ArrayList<>());
            }
            leftMap.get(leftValue).add(srcLeftObject);
        }



        switch (getType()) {
            case JOIN: case INNER: {
                for(Object srcLeftObject : leftSource) {
                    leftValue = leftConsumer.get(srcLeftObject, leftField);
                    if(leftValue != null) {

                    }
                }
                break;
            }
            case LEFT: {
                break;
            }
            case RIGHT: {
                break;
            }
        }

        return null;
    }

    @Override
    public int compareTo(Join join) {
        return join.getResourceName().compareTo(getResourceName()) +
                join.getLeftField().compareTo(getLeftField()) +
                join.getRightField().compareTo(getRightField());
    }

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

    public enum JoinType {

        JOIN,

        INNER,

        LEFT,

        RIGHT

    }
}
