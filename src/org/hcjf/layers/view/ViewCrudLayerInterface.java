package org.hcjf.layers.view;

import org.hcjf.layers.LayerInterface;
import org.hcjf.view.ViewComponent;

import java.util.Map;

/**
 * @author Andrés Medina
 * @email armedina@gmail.com
 */
public interface ViewCrudLayerInterface extends LayerInterface {
    public ViewComponent onAction(String action, Map<String, Object> params);
}
