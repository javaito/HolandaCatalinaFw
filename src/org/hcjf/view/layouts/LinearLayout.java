package org.hcjf.view.layouts;

/**
 * @author Andrés Medina
 * @email armedina@gmail.com
 */
public class LinearLayout extends Layout {

    private Orientation orientation;

    public LinearLayout(String name) {
        super(name);
        orientation = Orientation.VERTICAL;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public enum Orientation{
        HORIZONTAL,
        VERTICAL
    }
}
