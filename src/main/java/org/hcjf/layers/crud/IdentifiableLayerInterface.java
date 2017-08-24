package org.hcjf.layers.crud;

import org.hcjf.utils.NamedUuid;

import java.util.UUID;

/**
 * This interface identify the layers with identifiable resources.
 * @author javaito
 */
public interface IdentifiableLayerInterface<O extends Object> extends ReadLayerInterface<O> {

    /**
     * Reads an instance of the resource from the data source using a uuid type 5 instance
     * @param uuid UUID instance.
     * @return Returns the resource instance.
     */
    default O read(UUID uuid) {
        return read((Object)uuid);
    }

    /**
     * Creates a named uuid type 5 with the implementation name of the layer.
     * @return New instance of the uuid.
     */
    default UUID createId() {
        return NamedUuid.create(getImplName()).getId();
    }

}
