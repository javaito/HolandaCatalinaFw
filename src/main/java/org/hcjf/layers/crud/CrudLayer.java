package org.hcjf.layers.crud;

import org.hcjf.layers.Layer;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Query;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * This layer implements the {@link CrudLayerInterface}
 * @author javaito
 */
public abstract class CrudLayer<O extends Object> extends Layer implements
        CrudLayerInterface<O>, CreateLayerInterface<O>, ReadLayerInterface<O>,
        ReadRowsLayerInterface, UpdateLayerInterface<O>, DeleteLayerInterface<O> {

    private Class<O> resourceType;

    public CrudLayer(String implName) {
        super(implName);
    }

    public CrudLayer() {
    }

    /**
     * This method return the resource class of the layer.
     * @return Resource class.
     */
    public synchronized final Class<O> getResourceType() {
        if(resourceType == null) {
            Class currentClass = getClass();
            Type genericSuperClass = currentClass.getGenericSuperclass();
            while (currentClass != Object.class &&
                    !(genericSuperClass instanceof ParameterizedType)) {
                currentClass = currentClass.getSuperclass();
                genericSuperClass = currentClass.getGenericSuperclass();
            }

            if (genericSuperClass instanceof ParameterizedType) {

                Type actualType = ((ParameterizedType) genericSuperClass).
                        getActualTypeArguments()[0];
                if (actualType instanceof ParameterizedTypeImpl) {
                    resourceType = (Class<O>) ((ParameterizedTypeImpl) actualType).getRawType();
                } else {
                    resourceType = (Class<O>) actualType;
                }
            } else {
                throw new IllegalArgumentException();
            }
        }

        return resourceType;
    }

    /**
     * This method implements the creation of the resource.
     *
     * @param object     Object to represents an instance of the resource.
     * @param parameters Some parameters that could be necessaries
     *                   in order to create an instance of the resource.
     * @return The instance of the resource.
     */
    @Override
    public O create(O object, Map<String, Object> parameters) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the creation of the resource.
     * @param object Object to represents an instance of the resource.
     * @return The instance of the resource.
     */
    @Override
    public O create(O object) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the read operation to find an instance of
     * the resource using only it's id.
     *
     * @param id Id to found the instance.
     * @return Return the instance founded or null if the instance is not found.
     */
    @Override
    public O read(Object id) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the update of the resource.
     *
     * @param object     Instance of the resource that gonna be updated.
     *                   This instance must have an id to identify the updatable data.
     * @param parameters Some parameters that could be necessaries
     *                   in order to update an instance of the resource.
     * @return The instance updated.
     */
    @Override
    public O update(O object, Map<String, Object> parameters) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the update of the resource.
     * @param object Instance of the resource that gonna be updated.
     *               This instance must have an id to identify the updatable data.
     * @return The instance updated.
     */
    @Override
    public O update(O object) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the delete operation over the resource.
     *
     * @param id Id of the instance that gonna be deleted.
     * @return Instance of the resource that was deleted.
     */
    @Override
    public O delete(Object id) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the read operation without filters.
     * @return List with all the instances of the resource.
     */
    public Collection<O> read()   {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the read operation using the filters
     * specified in the query.
     * @param query Query.
     * @return Return the list with the instances founded.
     */
    public Collection<O> read(Query query)  {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the read operation using the filters
     * specified in the query.
     * @param query Query.
     * @param parameters Parameters to evaluate query.
     * @return Return the list with the instances founded.
     */
    public Collection<O> read(Query query, Object... parameters)  {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the read operation using the filters
     * specified in the query.
     * @param queryId Id of the query.
     * @return Return the list with the instances founded.
     */
    public Collection<O> read(Query.QueryId queryId)  {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the read operation using the filters
     * specified in the query.
     * @param queryId Id of the query.
     * @param parameters Parameters to evaluate query.
     * @return Return the list with the instances founded.
     */
    public Collection<O> read(Query.QueryId queryId, Object... parameters)  {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the read operation using the filters
     * specified in the query and return a collection of maps.
     * @param query Query to read data.
     * @return Return the list with the instances founded.
     */
    public Collection<JoinableMap> readRows(Query query)  {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the read operation using the filters
     * specified in the query and return a collection of maps.
     * @param query Query to read data.
     * @param parameters Parameters to evaluate query.
     * @return Return the list with the instances founded.
     */
    public Collection<JoinableMap> readRows(Query query, Object... parameters)  {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the read operation using the filters
     * specified in the query and return a collection of maps.
     * @param queryId Id of the query.
     * @return Return the list with the instances founded.
     */
    public Collection<JoinableMap> readRows(Query.QueryId queryId)  {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the read operation using the filters
     * specified in the query and return a collection of maps.
     * @param queryId Id of the query.
     * @param parameters Parameters to evaluate query.
     * @return Return the list with the instances founded.
     */
    public Collection<JoinableMap> readRows(Query.QueryId queryId, Object... parameters)  {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the update operation over a add of the instances,
     * this instances are selected using the query like a filter.
     *
     * @param queryId    Id of the query.
     * @param parameters Values that contains the information to update the
     *                   instances.
     * @return Return the instances updated.
     */
    @Override
    public Collection<O> update(Query.QueryId queryId, Map<String, Object> parameters) {
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
    @Override
    public Collection<O> update(Query query, Map<String, Object> parameters) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the delete operation over a add of the instances.
     * This instances are selected using the query like a filter.
     *
     * @param queryId Id of the query.
     * @return Return the instances deleted.
     */
    @Override
    public Collection<O> delete(Query.QueryId queryId) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the delete operation over a add of the instances.
     * This instances are selected using the query like a filter.
     * @param query Query to select the instances to delete.
     * @return Return the instances deleted.
     */
    @Override
    public Collection<O> delete(Query query) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the read operation over the add of query created.
     *
     * @param queryId Id of the query.
     * @return Return the instance of the resource's query.
     */
    @Override
    public Query readQuery(Query.QueryId queryId) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the create operation of the resource's query.
     *
     * @param query Query instance.
     * @param parameters Some parameters that could be necessaries
     *                   in order to create an instance of the resource's query.
     * @return Instance of the resource's query.
     */
    @Override
    public Query createQuery(Query query, Map<String, Object> parameters) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the update operation of the resource's query.
     *
     * @param query      Instance of the resource's query that gonna be updated.
     *                   This instance must have an id to identify the updatable data.
     * @param parameters Some parameters that could be necessaries
     *                   in order to update an instance of the resource's query.
     * @return Updated instance of the resource's query.
     */
    @Override
    public Query updateQuery(Query query, Map<String, Object> parameters) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method implements the delete operation of the resource's query.
     *
     * @param queryId Id of the query.
     * @return Deleted instance of the resource's query.
     */
    @Override
    public Query deleteQuery(Query.QueryId queryId) {
        throw new UnsupportedOperationException();
    }
}
