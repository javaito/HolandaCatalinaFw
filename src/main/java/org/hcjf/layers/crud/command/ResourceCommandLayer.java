package org.hcjf.layers.crud.command;

import org.hcjf.layers.Layer;

/**
 * Utility class to simplify implementation of a resource command.<br/>
 * Layer name is built using <code>Resource::command</code>.<br/>
 * Subclasses should call the parent constructor with the associated resource name and override
 * <code>getCommandName</code> to return the command name.
 */
public abstract class ResourceCommandLayer extends Layer implements ResourceCommandLayerInterface {

    private String resourceName;

    public ResourceCommandLayer(String resourceName) {
        this.resourceName = resourceName;
    }

    protected abstract String getCommandName();

    @Override
    public String getImplName() {
        return String.format("%s::%s", resourceName, getCommandName());
    }

}
