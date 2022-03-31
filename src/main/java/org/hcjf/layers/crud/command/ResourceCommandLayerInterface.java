package org.hcjf.layers.crud.command;

import org.hcjf.layers.LayerInterface;

import java.util.Map;

/**
 * Represents a single command to be executed when a PUT request is sent to a resource.<br/>
 * It should be associated with a specific resource and carry a single task, returning its corresponding result.
 */
public interface ResourceCommandLayerInterface extends LayerInterface {

    Map<String, Object> execute(Object payload);

}
