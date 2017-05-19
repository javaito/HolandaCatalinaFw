package org.hcjf.layers.storage;

import org.hcjf.layers.query.Query;
import org.hcjf.layers.storage.actions.*;

import java.io.Closeable;
import java.util.Map;

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
     * This method must return a select operation implementation, this implementation
     * depends of the storage session technology.
     * @param query Object with all the information to create the particular query implementation
     *              over the storage technology.
     * @return Return the select action.
     * @throws StorageAccessException Encapsulates all exceptions of the underlying technology
     */
    public Select select(Query query) throws StorageAccessException {
        throw new UnsupportedOperationException("Unsupported select action for " + implName + " implementation");
    }

    /**
     * This method must return a insert operation implementation, this implementation
     * depends of the storage session technology.
     * @param object Object to initialize the instance of insert operation.
     * @return Return the insert operation.
     * @throws StorageAccessException Encapsulates all exceptions of the underlying technology
     */
    public Insert insert(Object object) throws StorageAccessException {
        throw new UnsupportedOperationException("Unsupported insert action for " + implName + " implementation");
    }

    /**
     * This method must return a insert operation implementation, this implementation
     * depends of the storage session technology.
     * @return Return the insert operation.
     * @throws StorageAccessException Encapsulates all exceptions of the underlying technology
     */
    public Insert insert() throws StorageAccessException {
        throw new UnsupportedOperationException("Unsupported insert action for " + implName + " implementation");
    }

    /**
     * This method must return a update operation implementation, this implementation
     * depends of the storage session technology.
     * @param query Query to filter the update.
     * @param values Values to been updated.
     * @return Return the update operation.
     * @throws StorageAccessException Encapsulates all exceptions of the underlying technology
     */
    public Update update(Query query, Map<String, Object> values) throws StorageAccessException {
        throw new UnsupportedOperationException("Unsupported update action for " + implName + " implementation");
    }

    /**
     * This method must return a update operation implementation, this implementation
     * depends of the storage session technology.
     * @param query Query to filter the update
     * @return Return the update operation.
     * @throws StorageAccessException Storage access exception
     */
    public Update update(Query query) throws StorageAccessException {
        throw new UnsupportedOperationException("Unsupported update action for " + implName + " implementation");
    }

    /**
     * This method must return a update operation implementation, this implementation
     * depends of the storage session technology.
     * @param object Instance that will be updated.
     * @return Return the update operation.
     * @throws StorageAccessException Storage access exception
     */
    public Update update(Object object) throws StorageAccessException {
        throw new UnsupportedOperationException("Unsupported update action for " + implName + " implementation");
    }

    /**
     * This method must return a update operation implementation, this implementation
     * depends of the storage session technology.
     * @param object Instance that will be updated.
     * @param values Values to been updated.
     * @return Return the update operation.
     * @throws StorageAccessException Encapsulates all exceptions of the underlying technology
     */
    public Update update(Object object, Map<String, Object> values) throws StorageAccessException {
        throw new UnsupportedOperationException("Unsupported update action for " + implName + " implementation");
    }

    /**
     * This method must return a update operation implementation, this implementation
     * depends of the storage session technology.
     * @return Return the update operation.
     * @throws StorageAccessException Storage access exception
     */
    public Update update() throws StorageAccessException {
        throw new UnsupportedOperationException("Unsupported update action for " + implName + " implementation");
    }

    /**
     * This method must return a update operation implementation, this implementation
     * depends of the storage session technology.
     * @param values Values to been updated.
     * @return Return the update operation.
     * @throws StorageAccessException Encapsulates all exceptions of the underlying technology
     */
    public Update update(Map<String, Object> values) throws StorageAccessException {
        throw new UnsupportedOperationException("Unsupported update action for " + implName + " implementation");
    }

    /**
     * This method must return a delete operation implementation, this implementation
     * depends of the storage session technology.
     * @return Return the delete operation.
     * @throws StorageAccessException Encapsulates all exceptions of the underlying technology
     */
    public Delete delete() throws StorageAccessException {
        throw new UnsupportedOperationException("Unsupported delete action for " + implName + " implementation");
    }

    /**
     * This method must return a delete operation implementation, this implementation
     * depends of the storage session technology.
     * @param query Query to filter the delete.
     * @return Return the delete operation.
     * @throws StorageAccessException Encapsulates all exceptions of the underlying technology
     */
    public Delete delete(Query query) throws StorageAccessException {
        throw new UnsupportedOperationException("Unsupported delete action for " + implName + " implementation");
    }

    /**
     * This method must return a delete operation implementation, this implementation
     * depends of the storage session technology.
     * @param object Instance that will be deleted.
     * @return Return the delete operation.
     * @throws StorageAccessException Encapsulates all exceptions of the underlying technology
     */
    public Delete delete(Object object) throws StorageAccessException {
        throw new UnsupportedOperationException("Unsupported delete action for " + implName + " implementation");
    }

    /**
     * This method must return a upsert operation implementation, this implementation
     * depends of the storage session technology.
     * @param object Object to initialize the instance of upsert operation.
     * @return Return the upsert operation.
     * @throws StorageAccessException Encapsulates all exceptions of the underlying technology
     */
    public Upsert upsert(Object object) throws StorageAccessException {
        throw new UnsupportedOperationException("Unsupported upsert action for " + implName + " implementation");
    }

    /**
     * This method must return a upsert operation implementation, this implementation
     * depends of the storage session technology.
     * @return Return the upsert operation.
     * @throws StorageAccessException Encapsulates all exceptions of the underlying technology
     */
    public Upsert upsert() throws StorageAccessException {
        throw new UnsupportedOperationException("Unsupported upsert action for " + implName + " implementation");
    }

    /**
     * This method must return a procedure operation implementation, this implementation
     * depends of the storage session technology.
     * @return Return the procedure operation.
     * @throws StorageAccessException Encapsulates all exceptions of the underlying technology
     */
    public Procedure procedure() throws StorageAccessException {
        throw new UnsupportedOperationException("Unsupported procedure action for " + implName + " implementation");
    }

    /**
     * This method normalize the data source name to application mame.
     * @param component Component from data source.
     * @return Return the name that match with the application mame.
     */
    public Query.QueryComponent normalizeDataSourceToApplication(Query.QueryComponent component) {
        return component;
    }

    /**
     * This method normalize the application name to data source name.
     * @param component Component from application.
     * @return Return the name that match with the data source name.
     */
    public Query.QueryComponent normalizeApplicationToDataSource(Query.QueryComponent component) {
        return component;
    }
}
