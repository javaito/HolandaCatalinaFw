package org.hcjf.layers.storage.actions;

import org.hcjf.layers.query.Query;
import org.hcjf.layers.storage.StorageAccessException;
import org.hcjf.layers.storage.StorageLayer;
import org.hcjf.layers.storage.StorageSession;
import org.hcjf.log.Log;
import org.hcjf.utils.Introspection;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is the base class for all the possible operations
 * over data storage session.
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class StorageAction<S extends StorageSession> {

    private final S session;
    private String resourceName;
    private final Map<String, StorageValue> values;
    private Query query;
    private final Map<Class<? extends Annotation>, Annotation> environmentAnnotations;
    private Class resultType;

    public StorageAction(S session) {
        this.session = session;
        this.values = new HashMap<>();
        this.environmentAnnotations = new HashMap<>();
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
     *
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
     *
     * @param object
     */
    public final void add(Object object) {
        //TODO: set the environment annotations
        for(Introspection.Getter getter : Introspection.getGetters(object.getClass()).values()) {
            try {
                add(getter.getResourceName(), new FieldStorageValue(getter.invoke(object), getter.getAnnotationsMap()));
            } catch(Exception ex) {
                Log.w(StorageLayer.STORAGE_LOG_TAG, "Invoke getter method fail: $s", ex, getter.getResourceName());
            }
        }
        onAdd(object);
    }

    /**
     *
     * @param object
     */
    protected void onAdd(Object object) {}

    /**
     *
     * @param fieldName
     * @param value
     */
    public final void add(String fieldName, Object value) {
        if(fieldName == null || fieldName.length() == 0) {
            throw new IllegalArgumentException("The field name can't be null or empty string");
        }

        values.put(fieldName, new StorageValue(value));
    }

    /**
     *
     * @param fieldName
     * @param value
     */
    private void add(String fieldName, StorageValue value) {
        values.put(fieldName, value);
    }

    /**
     *
     * @return
     */
    protected final Query getQuery() {
        return query;
    }

    /**
     *
     * @param query
     */
    public final void setQuery(Query query) {
        this.query = query;
    }

    /**
     *
     * @param annotationClass
     * @return
     */
    public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return environmentAnnotations.containsKey(annotationClass);
    }

    /**
     *
     * @param annotationClass
     * @param <A>
     * @return
     */
    public final <A extends Annotation> A getAnnotation(Class<? extends A> annotationClass) {
        A result = null;
        if(environmentAnnotations.containsKey(annotationClass)) {
            result = (A) environmentAnnotations.get(annotationClass);
        }
        return result;
    }

    /**
     *
     * @return
     */
    public abstract <R extends ResultSet> R execute() throws StorageAccessException;

    /**
     *
     */
    protected class StorageValue {

        private final Object value;

        private StorageValue(Object value) {
            this.value = value;
        }

        /**
         *
         * @return
         */
        public final Object getValue() {
            return value;
        }
    }

    /**
     *
     */
    protected class FieldStorageValue extends StorageValue {

        private final Map<Class<? extends Annotation>, Annotation> accessorAnnotation;

        public FieldStorageValue(Object value, Map<Class<? extends Annotation>, Annotation> accessorAnnotation) {
            super(value);
            this.accessorAnnotation = accessorAnnotation;
        }

        /**
         *
         * @param annotationClass
         * @return
         */
        public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            return accessorAnnotation.containsKey(annotationClass);
        }

        /**
         *
         * @param annotationClass
         * @param <A>
         * @return
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
