package org.hcjf.cloud.impl.network;

import com.google.gson.JsonObject;
import org.hcjf.service.ServiceConsumer;
import org.hcjf.utils.bson.BsonParcelable;

import java.util.Date;
import java.util.UUID;

/**
 * @author javaito
 */
public class Node implements ServiceConsumer, BsonParcelable {

    public static final class Fields {
        public static final String ID = "id";
        public static final String CLUSTER_NAME = "clusterName";
        public static final String DATA_CENTER_NAME = "dataCenterName";
        public static final String LOCAL = "local";
        public static final String NAME = "name";
        public static final String VERSION = "version";
        public static final String STARTUP_DATE = "startupDate";
        public static final String LAN_ADDRESS = "lanAddress";
        public static final String LAN_PORT = "lanPort";
        public static final String WAN_ADDRESS = "wanAddress";
        public static final String WAN_PORT = "wanPort";
    }

    private UUID id;
    private String clusterName;
    private String dataCenterName;
    private String name;
    private String version;
    private Date startupDate;
    private String lanAddress;
    private Integer lanPort;
    private String wanAddress;
    private Integer wanPort;
    private Status status;
    private Long lastStatusUpdate;
    private Integer connectionAttempts;
    private boolean localNode;

    public Node() {
        status = Status.DISCONNECTED;
        lastStatusUpdate = System.currentTimeMillis();
        connectionAttempts = 0;
    }

    public String getLanId() {
        String result = null;
        if(lanAddress != null) {
            result = createNodeHash(lanAddress, lanPort);
        }
        return result;
    }

    public String getWanId() {
        String result = null;
        if(wanAddress != null) {
            result = createNodeHash(wanAddress, wanPort);
        }
        return result;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getDataCenterName() {
        return dataCenterName;
    }

    public void setDataCenterName(String dataCenterName) {
        this.dataCenterName = dataCenterName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getStartupDate() {
        return startupDate;
    }

    public void setStartupDate(Date startupDate) {
        this.startupDate = startupDate;
    }

    public String getLanAddress() {
        return lanAddress;
    }

    public void setLanAddress(String lanAddress) {
        this.lanAddress = lanAddress;
    }

    public Integer getLanPort() {
        return lanPort;
    }

    public void setLanPort(Integer lanPort) {
        this.lanPort = lanPort;
    }

    public String getWanAddress() {
        return wanAddress;
    }

    public void setWanAddress(String wanAddress) {
        this.wanAddress = wanAddress;
    }

    public Integer getWanPort() {
        return wanPort;
    }

    public void setWanPort(Integer wanPort) {
        this.wanPort = wanPort;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.lastStatusUpdate = System.currentTimeMillis();
    }

    public Long getLastStatusUpdate() {
        return lastStatusUpdate;
    }

    public Integer getConnectionAttempts() {
        return connectionAttempts;
    }

    public void setConnectionAttempts(Integer connectionAttempts) {
        this.connectionAttempts = connectionAttempts;
    }

    public boolean isLocalNode() {
        return localNode;
    }

    public void setLocalNode(boolean localNode) {
        this.localNode = localNode;
    }

    public static String createNodeHash(String remoteHost, Integer port) {
        return remoteHost + ":" + port;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(Fields.ID, getId().toString());
        jsonObject.addProperty(Fields.LOCAL, isLocalNode());
        jsonObject.addProperty(Fields.CLUSTER_NAME, getClusterName());
        jsonObject.addProperty(Fields.DATA_CENTER_NAME, getDataCenterName());
        jsonObject.addProperty(Fields.NAME, getName());
        jsonObject.addProperty(Fields.VERSION, getVersion());
        jsonObject.addProperty(Fields.STARTUP_DATE, getStartupDate().toString());
        if(getLanAddress() != null) {
            jsonObject.addProperty(Fields.LAN_ADDRESS, getLanAddress());
            jsonObject.addProperty(Fields.LAN_PORT, getLanPort());
        }
        if(getWanAddress() != null) {
            jsonObject.addProperty(Fields.WAN_ADDRESS, getWanAddress());
            jsonObject.addProperty(Fields.WAN_PORT, getWanPort());
        }
        return jsonObject;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public enum Status {

        LOST,

        DISCONNECTED,

        CONNECTED,

        CONNECTING

    }
}
