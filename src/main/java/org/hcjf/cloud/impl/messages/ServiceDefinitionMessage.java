package org.hcjf.cloud.impl.messages;

import org.hcjf.io.net.messages.MessageCollection;

import java.util.UUID;

public class ServiceDefinitionMessage extends MessageCollection {

    private UUID serviceId;
    private String serviceName;

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
}
