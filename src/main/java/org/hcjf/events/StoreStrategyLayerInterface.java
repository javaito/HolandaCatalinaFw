package org.hcjf.events;

import org.hcjf.layers.LayerInterface;

public interface StoreStrategyLayerInterface extends LayerInterface {

    void storeEvent(DistributedEvent event);

    DistributedEvent restoreNext();

}
