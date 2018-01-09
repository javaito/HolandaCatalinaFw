package org.hcjf.cloud.impl.network;

import org.hcjf.cloud.impl.CloudInterface;
import org.hcjf.cloud.impl.Node;
import org.hcjf.cloud.impl.messages.*;
import org.hcjf.io.net.NetService;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;

import java.io.IOException;
import java.util.*;

/**
 * @author javaito.
 */
public final class CloudImpl extends Service<Node> {

    public static final CloudImpl instance;

    static {
        instance = new CloudImpl();
    }

    private Node thisNode;
    private Set<Node> nodes;
    private Map<String, Node> nodesByLanId;
    private Map<String, Node> nodesByWanId;
    private Map<Node, CloudSession> sessionByNode;
    private CloudServer server;
    private Random random;

    private CloudImpl() {
        super(SystemProperties.get(SystemProperties.Cloud.DefaultImpl.SERVICE_NAME),
                SystemProperties.getInteger(SystemProperties.Cloud.DefaultImpl.SERVICE_PRIORITY));
    }

    public static CloudImpl getInstance() {
        return instance;
    }

    @Override
    protected void init() {
        nodes = new HashSet<>();
        nodesByLanId = new HashMap<>();
        nodesByWanId = new HashMap<>();
        sessionByNode = new HashMap<>();

        thisNode = new Node();
        thisNode.setId(UUID.randomUUID());
        thisNode.setName(SystemProperties.get(SystemProperties.Cloud.DefaultImpl.ThisNode.NAME));
        thisNode.setVersion(SystemProperties.get(SystemProperties.Cloud.DefaultImpl.ThisNode.VERSION));
        thisNode.setLanAddress(SystemProperties.get(SystemProperties.Cloud.DefaultImpl.ThisNode.LAN_ADDRESS));
        thisNode.setLanPort(SystemProperties.getInteger(SystemProperties.Cloud.DefaultImpl.ThisNode.LAN_PORT));
        if(SystemProperties.get(SystemProperties.Cloud.DefaultImpl.ThisNode.WAN_ADDRESS) != null) {
            thisNode.setWanAddress(SystemProperties.get(SystemProperties.Cloud.DefaultImpl.ThisNode.WAN_ADDRESS));
            thisNode.setWanPort(SystemProperties.getInteger(SystemProperties.Cloud.DefaultImpl.ThisNode.WAN_PORT));
        }
        thisNode.setStartupDate(new Date());
        thisNode.setStatus(Node.Status.CONNECTED);

        random = new Random();

        fork(this::maintainConnections);
        server = new CloudServer();
        server.start();
    }

    @Override
    public void registerConsumer(Node node) {
        nodes.add(node);
        String lanId = node.getLanId();
        if(lanId != null) {
            nodesByLanId.put(lanId, node);
        }

        String wanId = node.getWanId();
        if(wanId != null) {
            nodesByWanId.put(wanId, node);
        }
    }

    @Override
    public void unregisterConsumer(Node node) {

    }

    private void maintainConnections() {
        while(!Thread.currentThread().isInterrupted()) {
            for(Node node : nodes) {
                synchronized (sessionByNode) {
                    if (!sessionByNode.containsKey(node)) {
                        CloudClient client = new CloudClient(node.getLanAddress(), node.getLanPort());
                        NetService.getInstance().registerConsumer(client);
                        if (client.waitForConnect()) {
                            Log.i(SystemProperties.Cloud.LOG_TAG, "Connected with %s:%d",
                                    node.getLanAddress(), node.getLanPort());
                            sessionByNode.put(node, client.getSession());

                            if(connecting(node)) {
                                try {
                                    client.send(new NodeIdentificationMessage(thisNode));
                                } catch (IOException e) {
                                    client.disconnect();
                                    disconnected(node);
                                }
                            } else {
                                client.disconnect();
                            }
                        } else {
                            Log.i(SystemProperties.Cloud.LOG_TAG, "Unable to connected with %s:%d",
                                    node.getLanAddress(), node.getLanPort());
                        }
                    } else if(node.getStatus().equals(Node.Status.CONNECTED)) {
                        //TODO: Send keep alive message
                    }
                }
            }
            try {
                Thread.sleep(SystemProperties.getLong(
                        SystemProperties.Cloud.DefaultImpl.CONNECTION_LOOP_WAIT_TIME)
                        + (long)(random.nextDouble() * 1000));
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void connectionLost(CloudSession session) {
        Log.i(SystemProperties.Cloud.LOG_TAG, "connection lost with %s:%d",
                session.getRemoteHost(), session.getRemotePort());
        synchronized (sessionByNode) {
            for(Node node : sessionByNode.keySet()) {
                if(sessionByNode.get(node).equals(session)) {
                    sessionByNode.remove(node);
                }
            }
        }
    }

    public void incomingMessage(CloudSession session, Message message) {
        if(message instanceof NodeIdentificationMessage) {
            NodeIdentificationMessage nodeIdentificationMessage = (NodeIdentificationMessage) message;
            Node node = nodesByLanId.get(nodeIdentificationMessage.getNode().getLanId());
            if(node == null) {
                node = nodesByWanId.get(nodeIdentificationMessage.getNode().getWanId());
            }
            if(session.getConsumer() instanceof CloudClient) {
                if(!connected(node)){
                    ((CloudClient)session.getConsumer()).disconnect();
                }
            } else if(session.getConsumer() instanceof CloudServer) {
                if(connecting(node)) {
                    try {
                        if(connected(node)){
                            ((CloudServer)session.getConsumer()).send(session, new NodeIdentificationMessage(thisNode));
                        } else {
                            ((CloudServer)session.getConsumer()).send(session, new BusyNodeMessage(thisNode));
                        }
                    } catch (IOException e) {
                        disconnected(node);
                    }
                } else {
                    try {
                        ((CloudServer)session.getConsumer()).send(session, new BusyNodeMessage(thisNode));
                    } catch (IOException e) {
                        //TODO: Close the session
                    }
                }
            }
        } else if(message instanceof BusyNodeMessage) {

        }
    }

    /**
     *
     * @param node
     * @return
     */
    private boolean connected(Node node) {
        Objects.requireNonNull(node, "Null node");

        return changeStatus(node, Node.Status.CONNECTED);
    }

    /**
     *
     * @param node
     * @return
     */
    private boolean connecting(Node node) {
        Objects.requireNonNull(node, "Null node");

        return changeStatus(node, Node.Status.CONNECTING);
    }

    /**
     *
     * @param node
     * @return
     */
    private boolean disconnected(Node node) {
        Objects.requireNonNull(node, "Null node");

        return changeStatus(node, Node.Status.DISCONNECTED);
    }

    /**
     *
     * @param node
     * @param message
     */
    private void updateNode(Node node, NodeIdentificationMessage message) {
        node.setId(message.getNode().getId());
        node.setName(message.getNode().getName());
        node.setStartupDate(message.getNode().getStartupDate());
        node.setLanAddress(message.getNode().getLanAddress());
        node.setLanPort(message.getNode().getLanPort());
        node.setWanAddress(message.getNode().getWanAddress());
        node.setWanPort(message.getNode().getWanPort());
    }

    /**
     *
     * @param node
     * @param status
     * @return
     */
    private synchronized boolean changeStatus(Node node, Node.Status status) {
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

}
