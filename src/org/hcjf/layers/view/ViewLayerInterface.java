package org.hcjf.layers.view;

import org.hcjf.layers.LayerInterface;
import org.hcjf.view.ViewComponent;

import java.util.HashMap;

/**
 * @mail armedina@gmail.com
 */
public interface ViewLayerInterface extends LayerInterface {

    public ViewComponent onAction(String action, HashMap<String, Object> params);
}
