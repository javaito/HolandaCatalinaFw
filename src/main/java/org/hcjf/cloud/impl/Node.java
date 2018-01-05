package org.hcjf.cloud.impl;

import org.hcjf.service.ServiceConsumer;
import org.hcjf.utils.bson.BsonParcelable;

import java.util.Date;
import java.util.UUID;

/**
 * @author javaito
 */
public class Node implements ServiceConsumer, BsonParcelable {

    private UUID id;
    private String name;
    private String version;
    private Date startupDate;
    private String lanAddress;
    private Integer lanPort;
    private String wanAddress;
    private Integer wanPort;
    private Status status;

    public Node() {
        status = Status.DISCONNECTED;
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
    }

    public static String createNodeHash(String remoteHost, Integer port) {
        return remoteHost + ":" + port;
    }

    public enum Status {

        DISCONNECTED,

        CONNECTED,

        CONNECTING

    }
}
