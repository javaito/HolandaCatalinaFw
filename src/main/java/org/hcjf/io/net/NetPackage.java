package org.hcjf.io.net;

import java.util.Date;
import java.util.UUID;

/**
 * This class represents a package of information over IP protocol.
 * @author javaito
 */
public abstract class NetPackage {

    private final UUID id;

    public NetPackage() {
        this.id = UUID.randomUUID();
    }

    /**
     * Return the package id.
     * @return Package id.
     */
    public final UUID getId() {
        return id;
    }

    /**
     * Return the net session of the package.
     * @return Net session.
     */
    public abstract NetSession getSession();

    /**
     * Set the session over the package was created.
     * @param session Net session
     */
    public abstract void setSession(NetSession session);

    /**
     * Return the payload of the package.
     * @return Payload of the package.
     */
    public abstract byte[] getPayload();

    /**
     * Return the remote ip address.
     * @return Remote ip address.
     */
    public abstract String getRemoteAddress();

    /**
     * Return the address of the remote host.
     * @return Address of the remote host.
     */
    public abstract String getRemoteHost();

    /**
     * Return the port of the remote host.
     * @return Port of the remote host.
     */
    public abstract int getRemotePort();

    /**
     * Return the local port on the connections was stablished
     * @return Local port.
     */
    public abstract int getLocalPort();

    /**
     * Return the creation date of the package.
     * @return Creation date of the package.
     */
    public abstract Date getDate();

    /**
     * Return the action event.
     * @return Action event.
     */
    public abstract ActionEvent getActionEvent();

    /**
     * Return the status of the package.
     * @return Package's status.
     */
    public abstract PackageStatus getPackageStatus();

    /**
     * Set a new package status.
     * @param packageStatus Package's status
     */
    public abstract void setPackageStatus(PackageStatus packageStatus);

    /**
     * Create a wrapper of the net package using other pay load.
     * @param netPackage Package to wrap.
     * @param newPayLoad New pay load.
     * @return Net package wrapper.
     */
    public static final NetPackage wrap(NetPackage netPackage, byte[] newPayLoad) {
        return new NetPackageWrapper(netPackage, newPayLoad);
    }

    public static final class NetPackageWrapper extends NetPackage {

        private final NetPackage netPackage;
        private final byte[] payLoad;

        private NetPackageWrapper(NetPackage netPackage, byte[] payLoad) {
            this.netPackage = netPackage;
            if(payLoad != null) {
                this.payLoad = payLoad;
            } else {
                this.payLoad = new byte[0];
            }
        }

        public NetPackage getNetPackage() {
            return netPackage;
        }

        @Override
        public NetSession getSession() {
            return netPackage.getSession();
        }

        @Override
        public void setSession(NetSession session) {
            netPackage.setSession(session);
        }

        @Override
        public byte[] getPayload() {
            return payLoad;
        }

        @Override
        public String getRemoteAddress() {
            return netPackage.getRemoteAddress();
        }

        @Override
        public String getRemoteHost() {
            return netPackage.getRemoteHost();
        }

        @Override
        public int getRemotePort() {
            return netPackage.getRemotePort();
        }

        @Override
        public int getLocalPort() {
            return netPackage.getLocalPort();
        }

        @Override
        public Date getDate() {
            return netPackage.getDate();
        }

        @Override
        public ActionEvent getActionEvent() {
            return netPackage.getActionEvent();
        }

        @Override
        public PackageStatus getPackageStatus() {
            return netPackage.getPackageStatus();
        }

        @Override
        public void setPackageStatus(PackageStatus packageStatus) {
            netPackage.setPackageStatus(packageStatus);
        }
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
