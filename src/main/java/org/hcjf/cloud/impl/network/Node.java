package org.hcjf.cloud.impl.network;

import java.util.Date;

/**
 * @author javaito
 */
public class Node extends NetworkComponent {

    private String version;
    private String clusterName;
    private String dataCenterName;
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

    public synchronized String getLanId() {
        String result = null;
        if(lanAddress != null && !lanAddress.isEmpty()) {
            result = createNodeHash(lanAddress, lanPort);
        }
        return result;
    }

    public synchronized String getWanId() {
        String result = null;
        if(wanAddress != null && !wanAddress.isEmpty()) {
            result = createNodeHash(wanAddress, wanPort);
        }
        return result;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public enum Status {

        LOST,

        DISCONNECTED,

        CONNECTED,

        CONNECTING

    }
}
