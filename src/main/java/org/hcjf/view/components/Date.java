package org.hcjf.view.components;

import org.hcjf.view.ViewComponent;

/**
 * @author Andrés Medina
 * @email armedina@gmail.com
 */
public class Date extends ViewComponent {

    private int defaultDay;
    private int defaultMonth;
    private int defaultYear;

    public Date(String text) {
        super(text);
    }

    public int getDefaultYear() {
        return defaultYear;
    }

    public void setDefaultYear(int defaultYear) {
        this.defaultYear = defaultYear;
    }

    public int getDefaultMonth() {
        return defaultMonth;
    }

    public void setDefaultMonth(int defaultMonth) {
        this.defaultMonth = defaultMonth;
    }

    public int getDefaultDay() {
        return defaultDay;
    }

    public void setDefaultDay(int defaultDay) {
        this.defaultDay = defaultDay;
    }
}
