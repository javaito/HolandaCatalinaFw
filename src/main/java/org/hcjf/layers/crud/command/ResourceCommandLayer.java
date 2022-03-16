package org.hcjf.layers.crud.command;

public abstract class ResourceCommandLayer implements ResourceCommandLayerInterface {

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
