package org.hcjf.layers.crud;

import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.query.Query;
import org.hcjf.layers.query.Queryable;

import java.util.Collection;

/**
 * @author javaito
 */
public interface ReadLayerInterface<O extends Object> extends LayerInterface {

    /**
     * This method implements the read operation to find an instance of
     * the resource using only it's id.
     * @param id Id to found the instance.
     * @return Return the instance founded or null if the instance is not found.
     */
    default O read(Object id) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the read operation without filters.
     * @return List with all the instances of the resource.
     */
    default Collection<O> read() {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the read operation using the filters
     * specified in the query.
     * @param queryable Instance that contains all the information to evaluate a query.
     * @return Return the list with the instances founded.
     */
    default Collection<O> read(Queryable queryable) {
        throw new UnsupportedOperationException();
    }

}
