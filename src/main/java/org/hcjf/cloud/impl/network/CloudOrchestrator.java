package org.hcjf.cloud.impl.network;

import org.hcjf.cloud.impl.messages.*;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetServiceConsumer;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.utils.Strings;

import java.io.IOException;
import java.util.*;

/**
 * @author javaito.
 */
public final class CloudOrchestrator extends Service<Node> {

    public static final CloudOrchestrator instance;

    static {
        instance = new CloudOrchestrator();
    }

    private Node thisNode;
    private Set<Node> nodes;
    private Map<String, Node> nodesByLanId;
    private Map<String, Node> nodesByWanId;
    private Map<Node, CloudSession> sessionByNode;
    private Set<Node> sortedNodes;
    private Map<UUID, Node> waitingAck;

    private CloudWagonMessage wagonMessage;
    private Object wagonMonitor;
    private Long lastVisit;

    private CloudServer server;
    private Random random;

    private CloudOrchestrator() {
        super(SystemProperties.get(SystemProperties.Cloud.Orchestrator.SERVICE_NAME),
                SystemProperties.getInteger(SystemProperties.Cloud.Orchestrator.SERVICE_PRIORITY));
    }

    public static CloudOrchestrator getInstance() {
        return instance;
    }

    @Override
    protected void init() {
        nodes = new HashSet<>();
        nodesByLanId = new HashMap<>();
        nodesByWanId = new HashMap<>();
        sessionByNode = new HashMap<>();
        sortedNodes = new TreeSet<>(Comparator.comparing(Node::getId));
        waitingAck = new HashMap<>();

        thisNode = new Node();
        thisNode.setId(UUID.randomUUID());
        thisNode.setName(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisNode.NAME));
        thisNode.setVersion(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisNode.VERSION));
        thisNode.setLanAddress(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisNode.LAN_ADDRESS));
        thisNode.setLanPort(SystemProperties.getInteger(SystemProperties.Cloud.Orchestrator.ThisNode.LAN_PORT));
        if(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisNode.WAN_ADDRESS) != null) {
            thisNode.setWanAddress(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisNode.WAN_ADDRESS));
            thisNode.setWanPort(SystemProperties.getInteger(SystemProperties.Cloud.Orchestrator.ThisNode.WAN_PORT));
        }
        thisNode.setStartupDate(new Date());
        thisNode.setStatus(Node.Status.CONNECTED);
        thisNode.setLocalNode(true);
        sortedNodes.add(thisNode);

        wagonMonitor = new Object();
        lastVisit = System.currentTimeMillis();

        random = new Random();

        fork(this::maintainConnections);
        fork(this::initWagon);
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
            sortedNodes.add(node);
            printNodes();
        }
    }

    private void removeNode(Node node) {
        synchronized (sessionByNode) {
            sessionByNode.remove(node);
            sortedNodes.remove(node);
            disconnected(node);
            printNodes();
        }
    }

    private void printNodes() {
        synchronized (sessionByNode) {
            Strings.Builder builder = new Strings.Builder();
            builder.append(Strings.START_SUB_GROUP).append(Strings.CARRIAGE_RETURN_AND_LINE_SEPARATOR);
            for (Node node : sortedNodes) {
                builder.append(Strings.CARRIAGE_RETURN_AND_LINE_SEPARATOR);
                builder.append(Strings.TAB).append(node.toJson(), Strings.ARGUMENT_SEPARATOR);
            }
            builder.cleanBuffer();
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
                        SystemProperties.Cloud.Orchestrator.CONNECTION_LOOP_WAIT_TIME)
                        + (long)(random.nextDouble() * 1000));
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void initWagon() {
        while(!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(SystemProperties.getLong(
                        SystemProperties.Cloud.Orchestrator.WAGON_TIMEOUT)
                        + (long)(random.nextDouble() * 1000));

                synchronized (wagonMonitor) {
                    if(wagonMessage != null) {
                        Node nextDestination = null;
                        Iterator<Node> nodesIterator = sortedNodes.iterator();
                        while(nodesIterator.hasNext()) {
                            if(nodesIterator.next().equals(thisNode)) {
                                if(nodesIterator.hasNext()) {
                                    nextDestination = nodesIterator.next();
                                } else if(sortedNodes.size() > 1) {
                                    nextDestination = sortedNodes.iterator().next();
                                }
                            }
                        }
                        if(nextDestination != null) {
                            Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Sending wagon");
                            sendMessage(sessionByNode.get(nextDestination), wagonMessage);
                        }
                    } else {
                        if(System.currentTimeMillis() - lastVisit > SystemProperties.getLong(
                                SystemProperties.Cloud.Orchestrator.WAGON_TIMEOUT) * sortedNodes.size()) {
                            if(thisNode.equals(sortedNodes.iterator().next())) {
                                wagonMessage = new CloudWagonMessage(UUID.randomUUID());
                            }
                        }
                    }
                }
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
                    Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Ack sent to %s:%d",
                            node.getLanAddress(), node.getLanPort());
                    sendMessage(session, new AckMessage(message));
                }
            } else if(session.getConsumer() instanceof CloudServer) {
                if(connecting(node)) {
                    Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Incoming credentials from %s:%d",
                            node.getLanAddress(), node.getLanPort());
                    Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Response credentials to %s:%d",
                            node.getLanAddress(), node.getLanPort());
                    NodeIdentificationMessage returnNodeIdentificationMessage = new NodeIdentificationMessage(thisNode);
                    waitingAck.put(returnNodeIdentificationMessage.getId(), node);
                    sendMessage(session, returnNodeIdentificationMessage);
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
        } else if(message instanceof CloudWagonMessage) {
            Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Incoming wagon");
            synchronized (wagonMonitor) {
                lastVisit = System.currentTimeMillis();
                if(wagonMessage != null) {
                    Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Wagon crash");
                }
                wagonMessage = (CloudWagonMessage) message;
                sendMessage(session, new AckMessage(message));
                //TODO: put something into wagon
            }
        } else if(message instanceof PublishObjectMessage) {

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

            synchronized (wagonMonitor) {
                if (wagonMessage != null) {
                    if (message.getId().equals(wagonMessage.getId())) {
                        wagonMessage = null;
                        Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Wagon gone");
                    }
                }
            }
        }
    }

    private void sendMessage(CloudSession session, Message message) {
        try {
            NetServiceConsumer consumer = session.getConsumer();
            if(consumer instanceof CloudClient) {
                ((CloudClient)consumer).send(message);
            } else {
                ((CloudServer)consumer).send(session, message);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        node.setClusterName(message.getNode().getClusterName());
        node.setDataCenterName(message.getNode().getDataCenterName());
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
