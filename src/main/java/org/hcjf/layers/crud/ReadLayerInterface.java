package org.hcjf.layers.crud;

import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.query.Query;

import java.util.Collection;

/**
 * @author javaito
 */
public interface ReadLayerInterface<O extends Object> extends LayerInterface {

    /**
     * This method return the resource class of the layer.
     * @return Resource class.
     */
    public Class<O> getResourceType();

    /**
     * This method implements the read operation to find an instance of
     * the resource using only it's id.
     * @param id Id to found the instance.
     * @return Return the instance founded or null if the instance is not found.
     */
    public O read(Object id);

    /**
     * This method implements the read operation without filters.
     * @return List with all the instances of the resource.
     */
    public Collection<O> read();

    /**
     * This method implements the read operation using the filters
     * specified in the query.
     * @param query Query.
     * @return Return the list with the instances founded.
     */
    public Collection<O> read(Query query);

    /**
     * This method implements the read operation using the filters
     * specified in the query.
     * @param query Query.
     * @param parameters Parameters to evaluate query.
     * @return Return the list with the instances founded.
     */
    public Collection<O> read(Query query, Object... parameters);

    /**
     * This method implements the read operation using the filters
     * specified in the query.
     * @param queryId Id of the query.
     * @return Return the list with the instances founded.
     */
    public Collection<O> read(Query.QueryId queryId);

    /**
     * This method implements the read operation using the filters
     * specified in the query.
     * @param queryId Id of the query.
     * @param parameters Parameters to evaluate query.
     * @return Return the list with the instances founded.
     */
    public Collection<O> read(Query.QueryId queryId, Object... parameters);

}
