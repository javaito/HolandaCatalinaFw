package org.hcjf.io.console;

import org.hcjf.utils.bson.BsonParcelable;

import java.util.List;
import java.util.UUID;

/**
 * Server metadata model.
 * @author javaito
 */
public class ServerMetadata implements BsonParcelable {

    private UUID instanceId;
    private String serverName;
    private String serverVersion;
    private String clusterName;
    private Boolean loginRequired;
    private List<String> loginFields;
    private List<String> loginSecretFields;

    public UUID getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(UUID instanceId) {
        this.instanceId = instanceId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Boolean getLoginRequired() {
        return loginRequired;
    }

    public void setLoginRequired(Boolean loginRequired) {
        this.loginRequired = loginRequired;
    }

    public List<String> getLoginFields() {
        return loginFields;
    }

    public void setLoginFields(List<String> loginFields) {
        this.loginFields = loginFields;
    }

    public List<String> getLoginSecretFields() {
        return loginSecretFields;
    }

    public void setLoginSecretFields(List<String> loginSecretFields) {
        this.loginSecretFields = loginSecretFields;
    }
}
