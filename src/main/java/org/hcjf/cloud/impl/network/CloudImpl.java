package org.hcjf.cloud.impl.network;

import org.hcjf.cloud.impl.messages.*;
import org.hcjf.io.net.NetService;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.utils.Strings;

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
    private Map<UUID, Node> waitingAck;
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
        waitingAck = new HashMap<>();

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
        String lanId = node.getLanId();
        String wanId = node.getWanId();
        boolean add = true;
        if(lanId != null && nodesByLanId.containsKey(lanId)) {
            add = false;
        }
        if(wanId != null && nodesByWanId.containsKey(wanId)) {
            add = false;
        }
        if(add) {
            if (lanId != null) {
                nodesByLanId.put(lanId, node);
            }

            if (wanId != null) {
                nodesByWanId.put(wanId, node);
            }

            nodes.add(node);
        }
    }

    @Override
    public void unregisterConsumer(Node node) {

    }

    private void addNode(Node node, CloudSession session) {
        synchronized (sessionByNode) {
            sessionByNode.put(node, session);
            printNodes();
        }
    }

    private void removeNode(Node node) {
        synchronized (sessionByNode) {
            sessionByNode.remove(node);
            disconnected(node);
            printNodes();
        }
    }

    private void printNodes() {
        synchronized (sessionByNode) {
            Strings.Builder builder = new Strings.Builder();
            builder.append(Strings.START_SUB_GROUP).append(Strings.CARRIAGE_RETURN_AND_LINE_SEPARATOR);
            builder.append(Strings.TAB).append(thisNode.toJson());
            for (Node connectedNode : sessionByNode.keySet()) {
                builder.append(Strings.ARGUMENT_SEPARATOR).append(Strings.CARRIAGE_RETURN_AND_LINE_SEPARATOR);
                builder.append(Strings.TAB).append(connectedNode.toJson());
            }
            builder.append(Strings.CARRIAGE_RETURN_AND_LINE_SEPARATOR).append(Strings.END_SUB_GROUP);
            Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "\r\n\r\nNodes: %s\r\n", builder.toString());
        }
    }

    private void maintainConnections() {
        while(!Thread.currentThread().isInterrupted()) {
            for(Node node : nodes) {
                synchronized (sessionByNode) {
                    if (!sessionByNode.containsKey(node) && !node.getStatus().equals(Node.Status.CONNECTED)) {
                        CloudClient client = new CloudClient(node.getLanAddress(), node.getLanPort());
                        NetService.getInstance().registerConsumer(client);
                        if (client.waitForConnect()) {
                            Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Connected with %s:%d",
                                    node.getLanAddress(), node.getLanPort());

                            if(connecting(node)) {
                                try {
                                    Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Sending credentials to %s:%d",
                                            node.getLanAddress(), node.getLanPort());
                                    client.send(new NodeIdentificationMessage(thisNode));
                                } catch (IOException e) {
                                    client.disconnect();
                                    disconnected(node);
                                }
                            }
                        } else {
                            Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Unable to connected with %s:%d",
                                    node.getLanAddress(), node.getLanPort());
                        }
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
        Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "connection lost with %s:%d",
                session.getRemoteHost(), session.getRemotePort());
        synchronized (sessionByNode) {
            for(Node node : sessionByNode.keySet()) {
                if(sessionByNode.get(node).equals(session)) {
                    removeNode(node);
                    break;
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
            updateNode(node, nodeIdentificationMessage);
            if(session.getConsumer() instanceof CloudClient) {
                Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Incoming credentials response from %s:%d",
                        node.getLanAddress(), node.getLanPort());
                if(!connected(node)){
                    ((CloudClient)session.getConsumer()).disconnect();
                } else {
                    Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Node connected as client %s", node);
                    addNode(node, session);
                    try {
                        Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Ack sent to %s:%d",
                                node.getLanAddress(), node.getLanPort());
                        ((CloudClient)session.getConsumer()).send(new AckMessage(message));
                    } catch (IOException e) {
                    }
                }
            } else if(session.getConsumer() instanceof CloudServer) {
                if(connecting(node)) {
                    Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Incoming credentials from %s:%d",
                            node.getLanAddress(), node.getLanPort());
                    try {
                        Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Response credentials to %s:%d",
                                node.getLanAddress(), node.getLanPort());
                        NodeIdentificationMessage returnNodeIdentificationMessage = new NodeIdentificationMessage(thisNode);
                        waitingAck.put(returnNodeIdentificationMessage.getId(), node);
                        ((CloudServer) session.getConsumer()).send(session, returnNodeIdentificationMessage);
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
            BusyNodeMessage busyNodeMessage = (BusyNodeMessage) message;
            Node node = nodesByLanId.get(busyNodeMessage.getNode().getLanId());
            if(node == null) {
                node = nodesByWanId.get(busyNodeMessage.getNode().getWanId());
            }
            if(session.getConsumer() instanceof CloudClient) {
                disconnected(node);
                ((CloudClient)session.getConsumer()).disconnect();
            }
        } else if(message instanceof AckMessage) {
            Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Incoming ack from %s:%d",
                    session.getRemoteHost(), session.getRemotePort());
            if(session.getConsumer() instanceof CloudServer) {
                Node node = waitingAck.remove(message.getId());
                if(node != null) {
                    if(connected(node)) {
                        Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Node connected as server %s", node);
                        addNode(node, session);
                    }
                }
            }
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
        node.setVersion(message.getNode().getVersion());
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
