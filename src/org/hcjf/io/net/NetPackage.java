package org.hcjf.io.net;

import java.util.Date;
import java.util.UUID;

/**
 * This class represents a package of information over IP protocol.
 * @author javaito
 * @email javaito@gmail.com
 */
public class NetPackage {

    private final UUID id;
    private NetSession session;
    private final String remoteHost;
    private final String remoteAddress;
    private final int remotePort;
    private final int localPort;
    private final byte[] payload;
    private final Date date;
    private final ActionEvent actionEvent;
    private PackageStatus packageStatus;

    public NetPackage(String remoteHost, String remoteAddress,
                      int remotePort, int localPort, byte[] payload, ActionEvent actionEvent) {
        this.id = UUID.randomUUID();
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
     * Return the package id.
     * @return Package id.
     */
    public UUID getId() {
        return id;
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

    /**
     * Actioln events.
     */
    public enum ActionEvent {

        CONNECT,

        DISCONNECT,

        READ,

        WRITE,

        STREAMING;

    }

    /**
     * Transaction's status.
     */
    public enum PackageStatus {

        /**
         * This status indicates that the transaction are waiting in the output queue
         * to be processed. This is the initial state for all the transactions.
         */
        WAITING,

        /**
         * This status indicates that the transaction try to be processed over a session
         * that doesn't exist on the server.
         */
        UNKNOWN_SESSION,

        /**
         * This status indicates that the transaction can be processed because the connection
         * of the session is closed.
         */
        CONNECTION_CLOSE,

        /**
         * En caso de que la sesion que se quiere escribir este bloqueada
         * entonces no se puede realizar ninguna transaccion de salida.
         */
        REJECTED_SESSION_LOCK,

        /**
         * This status indicates that the transaction's process fail.
         */
        IO_ERROR,

        /**
         * This status indicates that the transaction finish ok.
         */
        OK

    }
}
