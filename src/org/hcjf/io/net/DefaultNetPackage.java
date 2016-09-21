package org.hcjf.io.net;

import java.util.Date;
import java.util.UUID;

/**
 * Created by javaito on 09/09/16.
 */
public class DefaultNetPackage extends NetPackage {

    private NetSession session;
    private final String remoteHost;
    private final String remoteAddress;
    private final int remotePort;
    private final int localPort;
    private final byte[] payload;
    private final Date date;
    private final ActionEvent actionEvent;
    private PackageStatus packageStatus;

    public DefaultNetPackage(String remoteHost, String remoteAddress,
                             int remotePort, int localPort, byte[] payload,
                             ActionEvent actionEvent) {
        this.remoteHost = remoteHost;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.localPort = localPort;
        this.payload = payload;
        this.date = new Date();
        this.actionEvent = actionEvent;
        this.packageStatus = PackageStatus.WAITING;
    }

    /**
     * Return the net session of the package.
     * @return Net session.
     */
    public NetSession getSession() {
        return session;
    }

    /**
     * Set the session over the package was created.
     * @param session Net session
     */
    public void setSession(NetSession session) {
        this.session = session;
    }

    /**
     * Return the payload of the package.
     * @return Payload of the package.
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Return the remote ip address.
     * @return Remote ip address.
     */
    public String getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * Return the address of the remote host.
     * @return Address of the remote host.
     */
    public String getRemoteHost() {
        return remoteHost;
    }

    /**
     * Return the port of the remote host.
     * @return Port of the remote host.
     */
    public int getRemotePort() {
        return remotePort;
    }

    /**
     * Return the local port on the connections was stablished
     * @return Local port.
     */
    public int getLocalPort() {
        return localPort;
    }

    /**
     * Return the creation date of the package.
     * @return Creation date of the package.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Return the action event.
     * @return Action event.
     */
    public ActionEvent getActionEvent() {
        return actionEvent;
    }

    /**
     * Return the status of the package.
     * @return Package's status.
     */
    public PackageStatus getPackageStatus() {
        return packageStatus;
    }

    /**
     * Set a new package status.
     * @param packageStatus Package's status
     */
    public void setPackageStatus(PackageStatus packageStatus) {
        this.packageStatus = packageStatus;
    }
}
