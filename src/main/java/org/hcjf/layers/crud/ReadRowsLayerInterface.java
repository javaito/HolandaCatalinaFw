package org.hcjf.layers.crud;

import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Queryable;

import java.util.Collection;

/**
 * @author javaito
 */
public interface ReadRowsLayerInterface extends LayerInterface {

    /**
     * This method implements the read operation using the filters
     * specified in the query and return a collection of maps.
     * @param queryable Instance that contains all the information to evaluate a query.
     * @return Return the list with the instances founded.
     */
    default Collection<JoinableMap> readRows(Queryable queryable) {
        throw new UnsupportedOperationException();
    }

}
