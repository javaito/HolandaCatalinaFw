package org.hcjf.layers.crud;

import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.query.Query;

import java.util.Collection;
import java.util.Map;

/**
 * @author javaito
 */
public interface UpdateLayerInterface<O extends Object> extends LayerInterface {

    /**
     * This method return the resource class of the layer.
     * @return Resource class.
     */
    default Class<O> getResourceType() {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the update of the resource.
     * @param object Instance of the resource that gonna be updated.
     *               This instance must have an id to identify the updatable data.
     * @param parameters Some parameters that could be necessaries
     *                   in order to update an instance of the resource.
     * @return The instance updated.
     */
    default O update(O object, Map<String, Object> parameters) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the update of the resource.
     * @param object Instance of the resource that gonna be updated.
     *               This instance must have an id to identify the updatable data.
     * @return The instance updated.
     */
    default O update(O object) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the update operation over a add of the instances,
     * this instances are selected using the query like a filter.
     * @param queryId Id of the query.
     * @param parameters Values that contains the information to update the
     *                   instances.
     * @return Return the instances updated.
     */
    default Collection<O> update(Query.QueryId queryId, Map<String, Object> parameters) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the update operation over a add of the instances,
     * this instances are selected using the query like a filter.
     * @param query Query to select the instance to update.
     * @param parameters Values that contains the information to update the
     *                   instances.
     * @return Return the instances updated.
     */
    default Collection<O> update(Query query, Map<String, Object> parameters) {
        throw new UnsupportedOperationException();
    }

}
