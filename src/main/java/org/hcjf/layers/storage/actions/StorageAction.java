package org.hcjf.layers.storage.actions;

import org.hcjf.layers.query.Query;
import org.hcjf.layers.storage.StorageAccessException;
import org.hcjf.layers.storage.StorageLayer;
import org.hcjf.layers.storage.StorageSession;
import org.hcjf.layers.storage.values.StorageValue;
import org.hcjf.log.Log;
import org.hcjf.utils.Introspection;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is the base class for all the possible operations
 * over data storage session.
 * @author javaito
 *
 */
public abstract class StorageAction<S extends StorageSession> {

    private final S session;
    private String resourceName;
    private final Map<String, StorageValue> values;
    private Query query;
    private Class resultType;

    public StorageAction(S session) {
        this.session = session;
        this.values = new HashMap<>();
    }

    /**
     * Return the storage session. This session is the bond between
     * the storage interface and the storage implementation.
     * @return Storage session.
     */
    protected final S getSession() {
        return session;
    }

    /**
     * Returns the name of the storage on which it will run the operation
     * @return Name of the storage.
     */
    protected final String getResourceName() {
        return resourceName;
    }

    /**
     * Returns the name of the resource upon which the action will be executed
     * @param resourceName Resource name.
     */
    public final void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * Return the vales to create the storage operation.
     * @return Values.
     */
    protected Map<String, StorageValue> getValues() {
        return values;
    }

    /**
     * Return the result type that must complete the action with the stored data.
     * @return Result type.
     */
    protected final Class getResultType() {
        return resultType;
    }

    /**
     * Set the result type that must complete the action with the stored data.
     * @param resultType Result type.
     */
    public final void setResultType(Class resultType) {
        this.resultType = resultType;
    }

    /**
     * Add an object like data source for the action.
     * @param object Data source object.
     */
    public final void add(Object object) {
        onAdd(object);
    }

    /**
     * This method will called when a data source object is added.
     * @param object Data source object.
     */
    protected void onAdd(Object object) {}

    /**
     * Add some primitive value to the action.
     * @param fieldName Name of the value.
     * @param value Value.
     */
    public final void add(String fieldName, Object value) {
        if(fieldName == null || fieldName.length() == 0) {
            throw new IllegalArgumentException("The field name can't be null or empty string");
        }

        values.put(fieldName, new StorageValue(value));
    }

    /**
     * Add value to the action.
     * @param fieldName Name of the value.
     * @param value Value.
     */
    protected void add(String fieldName, StorageValue value) {
        values.put(fieldName, value);
    }

    /**
     * Return the query associated to the action.
     * @return Associated query.
     */
    protected final Query getQuery() {
        return query;
    }

    /**
     * Set the associated query to the action.
     * @param query Associated query.
     */
    public final void setQuery(Query query) {
        this.query = query;
    }

    /**
     * This method must be implemented for each action implementation
     * @param params Set of parameters to execute the action.
     * @return Return the storage response for the action.
     */
    public abstract <R extends ResultSet> R execute(Object... params) throws StorageAccessException;

    /**
     * Implementation of the {@link StorageValue} that contains all the information
     * about of the class's member that contains the value.
     */
    protected class FieldStorageValue extends StorageValue {

        private final Map<Class<? extends Annotation>, Annotation> accessorAnnotation;

        public FieldStorageValue(Object value, Map<Class<? extends Annotation>, Annotation> accessorAnnotation) {
            super(value);
            this.accessorAnnotation = accessorAnnotation;
        }

        /**
         * Check if the member value has some class of annotation.
         * @param annotationClass Class of annotation.
         * @return Return true if the member has the annotation and false in otherwise.
         */
        public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            return accessorAnnotation.containsKey(annotationClass);
        }

        /**
         * Return the annotation instance that contains the member.
         * @param annotationClass Annotation class found.
         * @param <A> Annotation class expected.
         * @return Return the instance of the annotation or null if the member doesn't have
         * the annotation.
         */
        public final <A extends Annotation> A getAnnotation(Class<? extends A> annotationClass) {
            A result = null;
            if(accessorAnnotation.containsKey(annotationClass)) {
                result = (A) accessorAnnotation.get(annotationClass);
            }
            return result;
        }
    }
}
