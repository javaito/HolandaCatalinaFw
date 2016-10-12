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
    private boolean updateContent;
    private String updateContentSource;
    private List<String> contentColumnNames;
    private List<ComponentParameter> parameters;


    public ViewComponent(String name){
        this.parameters = new ArrayList<>();
        this.contentColumnNames = new ArrayList<>();
        this.name = name;
        this.enabled = true;
        this.updateContent = false;
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

    public boolean updateContent() {
        return updateContent;
    }

    public void setUpdateContent(boolean updateContent) {
        this.updateContent = updateContent;
    }

    public String getUpdateContentSource() {
        return updateContentSource;
    }

    public void setUpdateContentSource(String updateContentSource) {
        this.updateContentSource = updateContentSource;
    }

    public void addContentColumnName(String columnName){
        this.contentColumnNames.add(columnName);
    }

    public List<String> getContentColumnNames(){
        return contentColumnNames;
    }

    public void addParameter(ComponentParameter parameter){
        this.parameters.add(parameter);
    }

    public List<ComponentParameter> getParameters(){
        return this.parameters;
    }
}
