package org.hcjf.layers.storage.actions;

import java.util.Collection;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class CollectionResultSet extends ResultSet<Collection<Object>> {

    public CollectionResultSet(Collection<Object> result) {
        super(result.size(), result);
    }

}
