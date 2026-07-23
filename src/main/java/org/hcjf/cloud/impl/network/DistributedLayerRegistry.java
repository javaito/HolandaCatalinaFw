package org.hcjf.cloud.impl.network;

import java.util.UUID;

public class DistributedLayerRegistry {

    public static final class Protocol {
        public static final String HCM = "HCM";
        public static final String HTTP = "HTTP";
    }

    private UUID id;
    private String className;
    private String layerName;
    private UUID serviceEndPoint;
    private String regex;
    private String protocol;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public UUID getServiceEndPoint() {
        return serviceEndPoint;
    }

    public void setServiceEndPoint(UUID serviceEndPoint) {
        this.serviceEndPoint = serviceEndPoint;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public static UUID createDistributedLayerId(String className, String layerName) {
        return new UUID(className.hashCode(), layerName.hashCode());
    }
}
