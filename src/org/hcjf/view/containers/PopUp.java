package org.hcjf.view.containers;

import org.hcjf.view.ViewComponent;
import org.hcjf.view.ViewComponentContainer;

/**
 * @author Andrés Medina
 * @email armedina@gmail.com
 */
public class PopUp extends ViewComponentContainer {
    private int time;
    private boolean persistent;

    public PopUp(String name) {
        super(name);
        time = 3; //PROPERTIES
        persistent = false; //PROPERTIES
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }
}