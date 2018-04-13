package org.hcjf.layers.crud;

import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Query;
import org.hcjf.utils.Introspection;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * Interface that contains all the methods that must be implemented for
 * all the crud layer implementations.
 * @author javaito
 */
public interface CrudLayerInterface<O extends Object> extends LayerInterface {

    /**
     * This method return the resource class of the layer.
     * @return Resource class.
     */
    public Class<O> getResourceType();

    /**
     * This method implements the creation of the resource.
     * @param object Object to represents an instance of the resource.
     * @param parameters Some parameters that could be necessaries
     *                   in order to create an instance of the resource.
     * @return The instance of the resource.
     */
    public O create(O object, Map<String, Object> parameters);

    /**
     * This method implements the creation of the resource.
     * @param object Object to represents an instance of the resource.
     * @return The instance of the resource.
     */
    public O create(O object);

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

    /**
     * This method implements the update of the resource.
     * @param object Instance of the resource that gonna be updated.
     *               This instance must have an id to identify the updatable data.
     * @param parameters Some parameters that could be necessaries
     *                   in order to update an instance of the resource.
     * @return The instance updated.
     */
    public O update(O object, Map<String, Object> parameters);

    /**
     * This method implements the update of the resource.
     * @param object Instance of the resource that gonna be updated.
     *               This instance must have an id to identify the updatable data.
     * @return The instance updated.
     */
    public O update(O object);

    /**
     * This method implements the update operation over a add of the instances,
     * this instances are selected using the query like a filter.
     * @param queryId Id of the query.
     * @param parameters Values that contains the information to update the
     *                   instances.
     * @return Return the instances updated.
     */
    public Collection<O> update(Query.QueryId queryId, Map<String, Object> parameters);

    /**
     * This method implements the update operation over a add of the instances,
     * this instances are selected using the query like a filter.
     * @param query Query to select the instance to update.
     * @param parameters Values that contains the information to update the
     *                   instances.
     * @return Return the instances updated.
     */
    public Collection<O> update(Query query, Map<String, Object> parameters);

    /**
     * This method implements the delete operation over the resource.
     * @param id Id of the instance that gonna be deleted.
     * @return Instance of the resource that was deleted.
     */
    public O delete(Object id);

    /**
     * This method implements the delete operation over a add of the instances.
     * This instances are selected using the query like a filter.
     * @param queryId Id of the query.
     * @return Return the instances deleted.
     */
    public Collection<O> delete(Query.QueryId queryId);

    /**
     * This method implements the delete operation over a add of the instances.
     * This instances are selected using the query like a filter.
     * @param query Query to select the instances to delete.
     * @return Return the instances deleted.
     */
    public Collection<O> delete(Query query);

    /**
     * This method implements the read operation over the add of query created.
     * @param queryId Id of the query.
     * @return Return the instance of the resource's query.
     */
    public Query readQuery(Query.QueryId queryId);

    /**
     * This method implements the create operation of the resource's query.
     * @param query Query instance.
     * @param parameters Some parameters that could be necessaries
     *                   in order to create an instance of the resource's query.
     * @return Instance of the resource's query.
     */
    public Query createQuery(Query query, Map<String, Object> parameters);

    /**
     * This method implements the update operation of the resource's query.
     * @param query Instance of the resource's query that gonna be updated.
     *              This instance must have an id to identify the updatable data.
     * @param parameters Some parameters that could be necessaries
     *                   in order to update an instance of the resource's query.
     * @return Updated instance of the resource's query.
     */
    public Query updateQuery(Query query, Map<String, Object> parameters);

    /**
     * This method implements the delete operation of the resource's query.
     * @param queryId Id of the query.
     * @return Deleted instance of the resource's query.
     */
    public Query deleteQuery(Query.QueryId queryId);

    /**
     * Return all the crud method of the interface indexed by the statements into the {@link CrudMethodStatement}
     * @return Crud invokers.+
     */
    default Map<String, CrudInvoker> getInvokers() {
        return Introspection.getInvokers(CrudLayerInterface.class, CrudInvokerFilter.instance);
    }

    /**
     * Crud invoker implementation.
     */
    public static class CrudInvoker extends Introspection.Invoker {

        public CrudInvoker(Class implementationClass, Method method) {
            super(implementationClass, method);
        }

    }

    /**
     * Crud invoker filter.
     */
    public static class CrudInvokerFilter implements Introspection.InvokerFilter<CrudInvoker> {

        private static final String CREATE = "create";
        private static final String READ = "read";
        private static final String UPDATE = "update";
        private static final String DELETE = "delete";
        private static final String READ_ROWS = "readRows";
        private static final String CREATE_QUERY = "createQuery";
        private static final String READ_QUERY = "readQuery";
        private static final String UPDATE_QUERY = "updateQuery";
        private static final String DELETE_QUERY = "deleteQuery";

        static final CrudInvokerFilter instance;

        static {
            instance = new CrudInvokerFilter();
        }

        private CrudInvokerFilter() {
        }

        /**
         * Creates one invoker for each public crud method.
         * @param method Declared method.
         * @return Crud invoker entry.
         */
        @Override
        public Introspection.InvokerEntry<CrudInvoker> filter(Method method) {
            Introspection.InvokerEntry<CrudInvoker> result = null;

            String statement = null;
            if(method.getName().equals(CREATE)) {
                if(method.getParameterTypes().length > 1) {
                    statement = CrudMethodStatement.CREATE_OBJECT_MAP.toString();
                } else {
                    statement = CrudMethodStatement.CREATE_OBJECT.toString();
                }
            } else if(method.getName().equals(READ)) {
                if(method.getParameterTypes().length == 0) {
                    statement = CrudMethodStatement.READ.toString();
                } else if(method.getParameterTypes().length == 1) {
                    if(Query.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        statement = CrudMethodStatement.READ_QUERY.toString();
                    } else {
                        statement = CrudMethodStatement.READ_QUERY$ID.toString();
                    }
                } else if(method.getParameterTypes().length == 2) {
                    if(Query.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        statement = CrudMethodStatement.READ_QUERY_OBJECTS.toString();
                    } else {
                        statement = CrudMethodStatement.READ_QUERY$ID_OBJECTS.toString();
                    }
                }
            } else if(method.getName().equals(READ_ROWS)) {
                if(method.getParameterTypes().length == 1) {
                    if(Query.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        statement = CrudMethodStatement.READ_ROWS_QUERY.toString();
                    } else {
                        statement = CrudMethodStatement.READ_ROWS_QUERY$ID.toString();
                    }
                } else if(method.getParameterTypes().length == 2) {
                    if(Query.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        statement = CrudMethodStatement.READ_ROWS_QUERY_OBJECTS.toString();
                    } else {
                        statement = CrudMethodStatement.READ_ROWS_QUERY$ID_OBJECTS.toString();
                    }
                }
            }else if(method.getName().equals(UPDATE)) {
                if(method.getParameterTypes().length == 1) {
                    statement = CrudMethodStatement.UPDATE_OBJECT.toString();
                } else if(method.getParameterTypes().length == 2) {
                    if(Query.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        statement = CrudMethodStatement.UPDATE_QUERY_MAP.toString();
                    } else if(Query.QueryId.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        statement = CrudMethodStatement.UPDATE_QUERY$ID_MAP.toString();
                    } else {
                        statement = CrudMethodStatement.UPDATE_OBJECT_MAP.toString();
                    }
                }
            } else if(method.getName().equals(DELETE)) {
                if(Query.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    statement = CrudMethodStatement.DELETE_QUERY.toString();
                } else if(Query.QueryId.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    statement = CrudMethodStatement.DELETE_QUERY$ID.toString();
                } else {
                    statement = CrudMethodStatement.DELETE_ID.toString();
                }
            } else if(method.getName().equals(CREATE_QUERY)) {
                statement = CrudMethodStatement.CREATE$QUERY_QUERY_MAP.toString();
            } else if(method.getName().equals(READ_QUERY)) {
                statement = CrudMethodStatement.READ$QUERY_QUERY$ID.toString();
            } else if(method.getName().equals(UPDATE_QUERY)) {
                statement = CrudMethodStatement.UPDATE$QUERY_QUERY_MAP.toString();
            } else if(method.getName().equals(DELETE_QUERY)) {
                statement = CrudMethodStatement.DELETE$QUERY_QUERY$ID.toString();
            }

            if(statement != null) {
                result = new Introspection.InvokerEntry<>(statement,
                        new CrudInvoker(CrudLayerInterface.class, method), method.toString());
            }

            return result;
        }

    }

    /**
     * Enum with all the crud interface statements.
     */
    public enum CrudMethodStatement {

        CREATE_OBJECT,

        CREATE_OBJECT_MAP,

        READ_ID,

        READ,

        READ_QUERY,

        READ_QUERY_OBJECTS,

        READ_QUERY$ID,

        READ_QUERY$ID_OBJECTS,

        READ_ROWS_QUERY,

        READ_ROWS_QUERY_OBJECTS,

        READ_ROWS_QUERY$ID,

        READ_ROWS_QUERY$ID_OBJECTS,

        UPDATE_OBJECT_MAP,

        UPDATE_OBJECT,

        UPDATE_QUERY$ID_MAP,

        UPDATE_QUERY_MAP,

        DELETE_ID,

        DELETE_QUERY$ID,

        DELETE_QUERY,

        READ$QUERY_QUERY$ID,

        CREATE$QUERY_QUERY_MAP,

        UPDATE$QUERY_QUERY_MAP,

        DELETE$QUERY_QUERY$ID,

    }
}
