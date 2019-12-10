package org.hcjf.cloud.impl.messages;

import org.hcjf.io.net.messages.MessageCollection;

import java.util.UUID;

public class ServiceDefinitionMessage extends MessageCollection {

    private UUID serviceId;
    private String serviceName;
    private Boolean broadcasting;
    private Boolean eventListener;

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Boolean getBroadcasting() {
        return broadcasting;
    }

    public void setBroadcasting(Boolean broadcasting) {
        this.broadcasting = broadcasting;
    }

    public Boolean getEventListener() {
        return eventListener;
    }

    public void setEventListener(Boolean eventListener) {
        this.eventListener = eventListener;
    }
}
