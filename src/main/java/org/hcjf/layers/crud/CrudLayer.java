package org.hcjf.layers.crud;

import org.hcjf.layers.Layer;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Query;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * @author javaito
 */
public abstract class CrudLayer<O extends Object> extends Layer implements
        CreateLayerInterface<O>, ReadLayerInterface<O>, ReadRowsLayerInterface,
        UpdateLayerInterface<O>, DeleteLayerInterface<O> {

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
                if (actualType instanceof ParameterizedType) {
                    resourceType = (Class<O>) ((ParameterizedType) actualType).getRawType();
                } else {
                    resourceType = (Class<O>) actualType;
                }
            } else {
                throw new IllegalArgumentException();
            }
        }

        return resourceType;
    }

    protected final References getReferences(Map<String, Object> parameters) {
        return (References) parameters.get(References.class.getName());
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

}
