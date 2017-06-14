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
    public Collection<JoinableMap> readRows(Query query);

    /**
     * This method implements the read operation using the filters
     * specified in the query and return a collection of maps.
     * @param query Query to read data.
     * @param parameters Parameters to evaluate query.
     * @return Return the list with the instances founded.
     */
    public Collection<JoinableMap> readRows(Query query, Object... parameters);

    /**
     * This method implements the read operation using the filters
     * specified in the query and return a collection of maps.
     * @param queryId Id of the query.
     * @return Return the list with the instances founded.
     */
    public Collection<JoinableMap> readRows(Query.QueryId queryId);

    /**
     * This method implements the read operation using the filters
     * specified in the query and return a collection of maps.
     * @param queryId Id of the query.
     * @param parameters Parameters to evaluate query.
     * @return Return the list with the instances founded.
     */
    public Collection<JoinableMap> readRows(Query.QueryId queryId, Object... parameters);

}
