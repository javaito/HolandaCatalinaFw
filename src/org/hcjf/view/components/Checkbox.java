package org.hcjf.view.components;

import org.hcjf.view.ViewComponent;

/**
 * @author Andrés Medina
 * @email armedina@gmail.com
 */
public class Checkbox extends ViewComponent{
    private boolean checked;

    public Checkbox(String name) {
        super(name);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
