package org.hcjf.layers.crud;

import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Query;

import java.util.Collection;

/**
 * @author javaito
 */
public interface ReadRowsLayerInterface extends LayerInterface {

    /**
     * This method implements the read operation using the filters
     * specified in the query and return a collection of maps.
     * @param query Query to read data.
     * @return Return the list with the instances founded.
     */
    default Collection<JoinableMap> readRows(Query query) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the read operation using the filters
     * specified in the query and return a collection of maps.
     * @param query Query to read data.
     * @param parameters Parameters to evaluate query.
     * @return Return the list with the instances founded.
     */
    default Collection<JoinableMap> readRows(Query query, Object... parameters) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the read operation using the filters
     * specified in the query and return a collection of maps.
     * @param queryId Id of the query.
     * @return Return the list with the instances founded.
     */
    default Collection<JoinableMap> readRows(Query.QueryId queryId) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the read operation using the filters
     * specified in the query and return a collection of maps.
     * @param queryId Id of the query.
     * @param parameters Parameters to evaluate query.
     * @return Return the list with the instances founded.
     */
    default Collection<JoinableMap> readRows(Query.QueryId queryId, Object... parameters) {
        throw new UnsupportedOperationException();
    }

}
