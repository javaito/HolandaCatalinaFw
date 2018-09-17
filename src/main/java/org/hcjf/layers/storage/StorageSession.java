package org.hcjf.layers.storage;

import org.hcjf.layers.query.Queryable;
import org.hcjf.layers.storage.actions.Delete;
import org.hcjf.layers.storage.actions.Insert;
import org.hcjf.layers.storage.actions.Select;
import org.hcjf.layers.storage.actions.Update;

import java.io.Closeable;

/**
 * This class is the base class to implements a storage session over
 * some particular technology. These implementations are the bonds
 * between the storage interface and the particular technologies.
 * @author javaito
 *
 */
public abstract class StorageSession implements Closeable {

    private final String implName;

    public StorageSession(String implName) {
        this.implName = implName;
    }

    /**
     * Returns the implementation name.
     * @return Implementation name.
     */
    public final String getImplName() {
        return implName;
    }

    /**
     * This method must return a select operation implementation, this implementation
     * depends of the storage session technology.
     * @param queryable Object with all the information to create the particular query implementation
     *              over the storage technology.
     * @return Return the select action.
     */
    public Select select(Queryable queryable) {
        throw new UnsupportedOperationException("Unsupported select action for " + implName + " implementation");
    }

    /**
     * This method must return a insert operation implementation, this implementation
     * depends of the storage session technology.
     * @param object Object to initialize the instance of insert operation.
     * @return Return the insert operation.
     */
    public Insert insert(Object object) {
        throw new UnsupportedOperationException("Unsupported insert action for " + implName + " implementation");
    }

    /**
     * This method must return a update operation implementation, this implementation
     * depends of the storage session technology.
     * @param instance Object that contains all the attributes to update.
     * @param queryable Query to select the instance to update.
     * @return Return the update operation.
     */
    public Update update(Object instance, Queryable queryable) {
        throw new UnsupportedOperationException("Unsupported update action for " + implName + " implementation");
    }

    /**
     * This method must return a delete operation implementation, this implementation
     * depends of the storage session technology.
     * @param queryable Query to select the instances to delete.
     * @return Return the delete operation.
     */
    public Delete delete(Queryable queryable) {
        throw new UnsupportedOperationException("Unsupported delete action for " + implName + " implementation");
    }

}
