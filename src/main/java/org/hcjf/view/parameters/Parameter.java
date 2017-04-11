package org.hcjf.view.parameters;

/**
 * @author Andrés Medina
 * @email armedina@gmail.com
 */
public class Parameter {
    private String name;
    private String value;

    public Parameter(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
