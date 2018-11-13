package org.hcjf.layers.crud;

import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.query.Query;
import org.hcjf.layers.query.Queryable;

import java.util.Collection;

/**
 * @author javaito
 */
public interface DeleteLayerInterface<O extends Object> extends LayerInterface {

    /**
     * This method implements the delete operation over the resource.
     * @param id Id of the instance that gonna be deleted.
     * @return Instance of the resource that was deleted.
     */
    default O delete(Object id) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the delete operation over a add of the instances.
     * This instances are selected using the query like a match.
     * @param queryable Instance that contains all the information to evaluate a query.
     * @return Return the instances deleted.
     */
    default Collection<O> delete(Queryable queryable) {
        throw new UnsupportedOperationException();
    }

}
