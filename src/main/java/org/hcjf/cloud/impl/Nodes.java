package org.hcjf.cloud.impl;

import org.hcjf.cloud.impl.messages.NodeIdentificationMessage;
import org.hcjf.io.net.NetServiceConsumer;
import org.hcjf.io.net.NetSession;

import java.util.*;

/**
 * @author javaito.
 */
public class Nodes {

    public static final Map<String,Node> nodes;

    static {
        nodes = new HashMap<>();
    }

    /**
     *
     * @param remoteHostPort
     * @param consumer
     * @return
     */
    public static synchronized Node createNode(String remoteHostPort, NetServiceConsumer consumer) {
        Objects.requireNonNull(remoteHostPort, "Remote host:port null");
        Objects.requireNonNull(consumer, "Consumer instance null");

        Node result = nodes.get(remoteHostPort);
        if(result == null) {
            result = new Node(remoteHostPort, UUID.randomUUID(), consumer);
            nodes.put(remoteHostPort, result);
        }
        return result;
    }

    /**
     *
     * @param node
     * @return
     */
    public static boolean connected(Node node) {
        Objects.requireNonNull(node, "Null node");

        return changeStatus(node, Node.Status.CONNECTED);
    }

    /**
     *
     * @param node
     * @return
     */
    public static boolean connecting(Node node) {
        Objects.requireNonNull(node, "Null node");

        return changeStatus(node, Node.Status.CONNECTING);
    }

    /**
     *
     * @param node
     * @return
     */
    public static boolean disconnected(Node node) {
        Objects.requireNonNull(node, "Null node");

        return changeStatus(node, Node.Status.DISCONNECTED);
    }

    /**
     *
     * @param node
     * @param message
     */
    public static void updateNode(Node node, NodeIdentificationMessage message) {
        node.setName(message.getName());
        node.setVersion(message.getVersion());
        node.setStartupDate(message.getStartupDate());
    }

    /**
     *
     * @param node
     * @param status
     * @return
     */
    private static synchronized boolean changeStatus(Node node, Node.Status status) {
        boolean result = false;
        Node.Status currentStatus = node.getStatus();
        switch (currentStatus) {
            case CONNECTED: {
                if(status.equals(Node.Status.DISCONNECTED)) {
                    result = true;
                }
                break;
            }
            case DISCONNECTED: {
                if(status.equals(Node.Status.CONNECTING)) {
                    result = true;
                }
                break;
            }
            case CONNECTING: {
                if(status.equals(Node.Status.DISCONNECTED) || status.equals(Node.Status.CONNECTED)) {
                    result = true;
                }
                break;
            }
        }

        if(result) {
            node.setStatus(status);
        }

        return result;
    }

    public static class Node extends NetSession {

        private final String hostPort;
        private String name;
        private String version;
        private Date startupDate;
        private Status status;

        private Node(String hostPort, UUID id, NetServiceConsumer consumer) {
            super(id, consumer);
            this.hostPort = hostPort;
        }

        public String getHostPort() {
            return hostPort;
        }

        public String getName() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        private void setVersion(String version) {
            this.version = version;
        }

        public Date getStartupDate() {
            return startupDate;
        }

        private void setStartupDate(Date startupDate) {
            this.startupDate = startupDate;
        }

        public Status getStatus() {
            return status;
        }

        private void setStatus(Status status) {
            this.status = status;
        }

        public enum Status {

            DISCONNECTED,

            CONNECTED,

            CONNECTING

        }
    }
}
