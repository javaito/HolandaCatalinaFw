package org.hcjf.layers.query.model;

import org.hcjf.layers.query.Query;
import org.hcjf.utils.Strings;

/**
 * Represents any kind of resource.
 */
public class QueryResource implements Comparable<QueryResource>, QueryComponent {

    public static final QueryResource ANY = new QueryResource(Strings.ALL);

    private String resourceName;

    public QueryResource(String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * Set the name of the resource.
     * @param resourceName Name of the resource
     */
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * Return the resource name.
     * @return Resource name.
     */
    public String getResourceName() {
        return resourceName;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if(obj instanceof QueryResource) {
            result = resourceName.equals(((QueryResource)obj).getResourceName());
        }
        return result;
    }

    @Override
    public boolean isUnderlying() {
        return false;
    }

    @Override
    public int compareTo(QueryResource o) {
        return resourceName.compareTo(o.getResourceName());
    }

    @Override
    public String toString() {
        return getResourceName();
    }

}
