package org.hcjf.layers.crud.command;

import org.hcjf.layers.LayerInterface;

import java.util.Map;

public interface ResourceCommandLayerInterface extends LayerInterface {

    Map<String, Object> execute(Object payload);

}
