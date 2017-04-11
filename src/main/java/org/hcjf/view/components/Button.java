package org.hcjf.view.components;

import org.hcjf.view.ViewComponent;

/**
 * @author Andrés Medina
 * @email armedina@gmail.com
 */
public class Button extends ViewComponent{
    private String action;

    public Button(String name) {
        super(name);
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
