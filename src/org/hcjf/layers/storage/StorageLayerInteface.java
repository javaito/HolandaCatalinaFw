package org.hcjf.layers.storage;

import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.Query;

import java.io.Closeable;
import java.util.List;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public interface StorageLayerInteface extends LayerInterface {

    public Storable begin(Object o);

    public void commit();

    public Object create();

    public Object update();

    public Object delete();

    public List<Object> read(Query query);

}
