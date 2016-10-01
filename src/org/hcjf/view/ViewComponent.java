package org.hcjf.view;

import org.hcjf.view.parameters.componentParameters.ComponentParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrés Medina
 * @email armedina@gmail.com
 */
public class ViewComponent {

    private String name;
    private boolean enabled;
    private List<ComponentParameter> parameters;


    public ViewComponent(String name){
        this.parameters = new ArrayList<>();
        this.name = name;
        this.enabled = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void addParameter(ComponentParameter parameter){
        this.parameters.add(parameter);
    }

    public List<ComponentParameter> getParameters(){
        return this.parameters;
    }
}
