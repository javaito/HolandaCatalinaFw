package org.hcjf.layers.storage;

import org.hcjf.layers.Query;

import java.io.Closeable;
import java.util.List;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class StorageSession<O extends Object> implements Closeable {

    private final Storable storable;

    public StorageSession(Storable storable) {
        this.storable = storable;
    }

    public final Storable getStorable() {
        return storable;
    }

    public abstract List<O> read(Query query);

}
