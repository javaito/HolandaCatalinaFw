package org.hcjf.layers.query;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Javier Quiroga.
 * @email javier.quiroga@sitrack.com
 */
public final class Group {

    private final Set<Query.QueryField> fieldSet;

    public Group() {
        fieldSet = new TreeSet<>();
    }

    /**
     * Return a unmodifiable set with the fields.
     * @return Fields set.
     */
    public Set<Query.QueryField> getFieldSet() {
        return Collections.unmodifiableSet(fieldSet);
    }

    /**
     * Add a new field into the group.
     * @param field Query field.
     */
    public void addField(Query.QueryField field) {
        fieldSet.add(field);
    }

}
