package org.hcjf.layers.query.model;

import org.hcjf.layers.query.Query;
import org.hcjf.utils.Strings;

public abstract class QueryParameter implements Comparable<QueryParameter>, QueryComponent {

    private QueryResource resource;
    private String fieldPath;
    private final String originalValue;
    private final boolean underlying;

    public QueryParameter(Query query, String originalValue, String value) {
        this.originalValue = originalValue.trim();

        String cleanValue = value;
        if(cleanValue.startsWith(Strings.AT)) {
            cleanValue = value.substring(1);
            underlying = true;
        } else {
            underlying = false;
        }
        this.resource = QueryResource.ANY;
        if(cleanValue.contains(Strings.CLASS_SEPARATOR)) {
            boolean resourceNameFounded = false;
            for(QueryResource queryResource : query.getResources()) {
                if(originalValue.startsWith(queryResource.getResourceName() + ".")) {
                    this.resource = queryResource;
                    resourceNameFounded = true;
                    break;
                }
            }

            if(resourceNameFounded) {
                this.fieldPath = cleanValue.substring((resource.getResourceName()).length() + 1);
            } else {
                this.fieldPath = cleanValue;
            }
        } else {
            this.fieldPath = cleanValue;
        }
    }

    /**
     * Returns the resource of this parameter.
     * @return Resource of this parameter.
     */
    public QueryResource getResource() {
        return resource;
    }

    /**
     * Returns the field path.
     * @return Field path.
     */
    public String getFieldPath() {
        return fieldPath;
    }

    /**
     * Returns the original value.
     * @return Original value.
     */
    public String getOriginalValue() {
        return originalValue;
    }

    /**
     * Return the original representation of the field.
     * @return Original representation.
     */
    @Override
    public String toString() {
        return originalValue;
    }

    /**
     * This method returns true if the component is underlying and false in the otherwise.
     * @return Underlying value.
     */
    @Override
    public boolean isUnderlying() {
        return underlying;
    }

    /**
     * Compare the original value of the fields.
     * @param obj Other field.
     * @return True if the fields are equals.
     */
    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }

    /**
     * Compare the string representation of both objects.
     * @param o Other object.
     * @return Magnitude of the difference between both objects.
     */
    @Override
    public int compareTo(QueryParameter o) {
        return toString().compareTo(o.toString());
    }

    /**
     * Verify if the query parameter make reference to the specified resource.
     * @param resource Resource instance to test.
     * @return Returns true if the parameter make reference to the specified resource and false in the otherwise.
     */
    public abstract boolean verifyResource(QueryResource resource);

}
