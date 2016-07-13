package org.hcjf.layers.storage;

import java.io.Closeable;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public interface Storable {

    public void create();

    public void update();

    public void delete();

    public void read(Object id);

}
