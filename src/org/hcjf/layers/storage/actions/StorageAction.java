package org.hcjf.layers.storage.actions;

import org.hcjf.layers.query.Query;
import org.hcjf.layers.storage.StorageAccessException;
import org.hcjf.layers.storage.StorageLayer;
import org.hcjf.layers.storage.StorageSession;
import org.hcjf.log.Log;
import org.hcjf.utils.Introspection;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is the base class for all the possible operations
 * over data storage session.
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class StorageAction {

    private final StorageSession session;
    private final String storageName;
    private final Map<String, Object> values;
    private Query query;

    public StorageAction(StorageSession session, String storageName) {
        this.session = session;
        this.storageName = storageName;
        this.values = new HashMap<>();
    }

    /**
     * Return the storage session. This session is the bond between
     * the storage interface and the storage implementation.
     * @return Storage session.
     */
    protected StorageSession getSession() {
        return session;
    }

    /**
     * Returns the name of the storage on which it will run the operation
     * @return Name of the storage.
     */
    protected String getStorageName() {
        return storageName;
    }

    /**
     * Return the vales to create the storage operation.
     * @return Values.
     */
    protected Map<String, Object> getValues() {
        return values;
    }

    /**
     *
     * @param object
     */
    public final void add(Object object) {
        for(Introspection.Getter getter : Introspection.getGetters(object.getClass()).values()) {
            try {
                add(getter.getResourceName(), getter.invoke(object));
            } catch(Exception ex) {
                Log.w(StorageLayer.STORAGE_LOG_TAG, "Invoke getter method fail: $s", ex, getter.getResourceName());
            }
        }
    }

    /**
     *
     * @param fieldName
     * @param value
     */
    public final void add(String fieldName, Object value) {
        if(fieldName == null || fieldName.length() == 0) {
            throw new IllegalArgumentException("The field name can't be null or empty string");
        }

        values.put(fieldName, value);
    }

    protected final Query getQuery() {
        return query;
    }

    public final void setQuery(Query query) {
        this.query = query;
    }

    /**
     *
     * @return
     */
    public abstract <R extends ResultSet> R execute() throws StorageAccessException;

}
