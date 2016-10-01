package org.hcjf.view.components;

import org.hcjf.view.ViewComponent;

/**
 * @author Andrés Medina
 * @email armedina@gmail.com
 */
public class Button extends ViewComponent{
    private Action action;

    public Button(String name) {
        super(name);
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
