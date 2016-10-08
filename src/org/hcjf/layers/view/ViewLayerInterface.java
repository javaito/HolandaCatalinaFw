package org.hcjf.layers.view;

import org.hcjf.layers.LayerInterface;
import org.hcjf.view.ViewComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * @mail armedina@gmail.com
 */
public interface ViewLayerInterface extends LayerInterface {

    public ViewComponent onAction(String action, Map<String, Object> params);
}
