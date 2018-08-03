package org.hcjf.cloud.impl.network;

import org.hcjf.cloud.Cloud;
import org.hcjf.cloud.impl.LockImpl;
import org.hcjf.cloud.impl.messages.*;
import org.hcjf.cloud.impl.objects.*;
import org.hcjf.events.DistributedEvent;
import org.hcjf.events.Events;
import org.hcjf.events.RemoteEvent;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetServiceConsumer;
import org.hcjf.io.net.broadcast.BroadcastService;
import org.hcjf.io.net.messages.Message;
import org.hcjf.io.net.messages.ResponseMessage;
import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.Layers;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;
import org.hcjf.utils.Introspection;
import org.hcjf.utils.Strings;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This class implements a orchestrator in order to maintains the connection
 * between nodes, synchronize all the time the node information and admin the
 * communication between nodes using messages.
 * @author javaito.
 */
public final class CloudOrchestrator extends Service<NetworkComponent> {

    public static final CloudOrchestrator instance;

    static {
        instance = new CloudOrchestrator();
    }

    private Node thisNode;
    private Set<Node> nodes;
    private Map<String, Node> nodesByLanId;
    private Map<String, Node> nodesByWanId;
    private Map<UUID, CloudSession> sessionByNode;
    private Set<Node> sortedNodes;
    private Map<UUID, Node> waitingAck;
    private Map<UUID, ResponseListener> responseListeners;

    private ServiceEndPoint thisServiceEndPoint;
    private Map<UUID, ServiceEndPoint> endPoints;
    private Map<String,ServiceEndPoint> endPointsByGatewayId;

    private CloudWagonMessage wagonMessage;
    private Object wagonMonitor;
    private Long lastVisit;
    private Long lastServicePublication;
    private Map<String,List<Message>> wagonLoad;

    private CloudServer server;
    private DistributedTree sharedStore;
    private Random random;

    private CloudOrchestrator() {
        super(SystemProperties.get(SystemProperties.Cloud.Orchestrator.SERVICE_NAME),
                SystemProperties.getInteger(SystemProperties.Cloud.Orchestrator.SERVICE_PRIORITY));
    }

    public static CloudOrchestrator getInstance() {
        return instance;
    }

    /**
     * Init all the collections of the service, a thread for maintain the connections and
     * other thread to move the cloud wagon instance.
     */
    @Override
    protected void init() {
        nodes = new HashSet<>();
        nodesByLanId = new HashMap<>();
        nodesByWanId = new HashMap<>();
        sessionByNode = new HashMap<>();
        sortedNodes = new TreeSet<>(Comparator.comparing(Node::getId));
        waitingAck = new HashMap<>();
        responseListeners = new HashMap<>();

        thisNode = new Node();
        UUID thisNodeId = SystemProperties.getUUID(SystemProperties.Cloud.Orchestrator.ThisNode.ID);
        if(thisNodeId == null) {
            thisNodeId = UUID.randomUUID();
        }
        thisNode.setId(thisNodeId);
        thisNode.setDataCenterName(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisNode.DATA_CENTER_NAME));
        thisNode.setClusterName(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisNode.CLUSTER_NAME));
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

        thisServiceEndPoint = new ServiceEndPoint();
        UUID thisServiceEndPointId = SystemProperties.getUUID(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.ID);
        if(thisServiceEndPointId == null) {
            thisServiceEndPointId = UUID.randomUUID();
        }
        thisServiceEndPoint.setId(thisServiceEndPointId);
        thisServiceEndPoint.setName(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.NAME));
        thisServiceEndPoint.setGatewayAddress(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.GATEWAY_ADDRESS));
        thisServiceEndPoint.setGatewayPort(SystemProperties.getInteger(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.GATEWAY_PORT));

        endPoints = new HashMap<>();
        endPointsByGatewayId = new HashMap<>();

        wagonMonitor = new Object();
        lastVisit = System.currentTimeMillis();
        lastServicePublication = System.currentTimeMillis() - SystemProperties.getLong(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.PUBLICATION_TIMEOUT);
        wagonLoad = new HashMap<>();

        random = new Random();
        sharedStore = new DistributedTree(Strings.EMPTY_STRING);

        fork(this::maintainConnections);
        fork(this::initServicePublication);
        fork(this::initWagon);
        fork(this::initReorganizationTimer);
        server = new CloudServer();
        server.start();

        try {
            for (Node node : SystemProperties.getObjects(SystemProperties.Cloud.Orchestrator.NODES, Node.class)) {
                registerConsumer(node);
            }
        } catch (Exception ex) {
            Log.w(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Load nodes from properties fail", ex);
        }

        try {
            for (ServiceEndPoint serviceEndPoint : SystemProperties.getObjects(SystemProperties.Cloud.Orchestrator.SERVICE_END_POINTS, ServiceEndPoint.class)) {
                registerConsumer(serviceEndPoint);
            }
        } catch (Exception ex) {
            Log.w(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Load service end points from properties fail", ex);
        }

        if(SystemProperties.getBoolean(SystemProperties.Cloud.Orchestrator.Broadcast.ENABLED)) {
            CloudBroadcastConsumer broadcastConsumer = new CloudBroadcastConsumer();
            BroadcastService.getInstance().registerConsumer(broadcastConsumer);
        }
    }

    /**
     * Register a new component into the cluster.
     * @param networkComponent Network component to add.
     */
    @Override
    public void registerConsumer(NetworkComponent networkComponent) {
        Objects.requireNonNull(networkComponent, "Unable to register a null component");

        if(networkComponent instanceof Node) {
            Node node = (Node) networkComponent;
            String lanId = node.getLanId();
            String wanId = node.getWanId();
            boolean add = true;
            if (lanId != null && (thisNode.getLanId().equalsIgnoreCase(lanId) || nodesByLanId.containsKey(lanId))) {
                add = false;
            }
            if (wanId != null && (thisNode.getWanId().equalsIgnoreCase(wanId) || nodesByWanId.containsKey(wanId))) {
                add = false;
            }
            if (add) {
                node.setStatus(Node.Status.DISCONNECTED);
                if (lanId != null) {
                    nodesByLanId.put(lanId, node);
                }

                if (wanId != null) {
                    nodesByWanId.put(wanId, node);
                }

                nodes.add(node);
                Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "New node registered: %s", node);
            }
        } else if(networkComponent instanceof ServiceEndPoint) {
            ServiceEndPoint endPoint = (ServiceEndPoint) networkComponent;
            if(endPoint.getGatewayAddress() != null){
                if(!(thisServiceEndPoint.getGatewayId().equals(endPoint.getGatewayId()) ||
                        endPointsByGatewayId.containsKey(endPoint.getGatewayId()))) {
                    endPoints.put(endPoint.getId(), endPoint);
                    endPointsByGatewayId.put(endPoint.getGatewayId(), endPoint);
                    Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "New service end point registered: %s", endPoint);
                }
            }
        }
    }

    @Override
    public void unregisterConsumer(NetworkComponent networkComponent) {
    }

    private List<Node> getSortedNodes() {
        List < Node > nodes = new ArrayList<>();
        boolean insert = false;
        for(Node node : sortedNodes) {
            if(node.equals(thisNode)) {
                insert = true;
                continue;
            }

            if(insert) {
                nodes.add(0, node);
            } else {
                nodes.add(node);
            }
        }
        return nodes;
    }

    /**
     * This method is called when a node is connected.
     * @param networkComponent Component connected.
     * @param session Net session assigned to the connected node.
     */
    private void nodeConnected(NetworkComponent networkComponent, CloudSession session) {
        if(networkComponent instanceof Node) {
            Node node = (Node) networkComponent;
            synchronized (sessionByNode) {
                sessionByNode.put(node.getId(), session);
                sortedNodes.add(node);
                session.setNode(node);
                printNodes();
            }
            fork(() -> reorganize(node, session, ReorganizationAction.CONNECT));
        } else if(networkComponent instanceof ServiceEndPoint) {

        }
    }

    /**
     * This method is called when a node is disconnected.
     * @param networkComponent Component disconnected.
     */
    private void nodeDisconnected(NetworkComponent networkComponent) {
        if(networkComponent instanceof Node) {
            Node node = (Node) networkComponent;
            synchronized (sessionByNode) {
                sessionByNode.remove(node.getId());
                sortedNodes.remove(node);
                disconnected(node);
                printNodes();
            }

            fork(() -> reorganize(node, null, ReorganizationAction.DISCONNECT));
        } else if(networkComponent instanceof ServiceEndPoint) {

        }
    }

    /**
     * Prints a log record that show the information about all the connected nodes.
     */
    private void printNodes() {
        synchronized (sessionByNode) {
            Strings.Builder builder = new Strings.Builder();
            builder.append(Strings.START_SUB_GROUP);
            for (Node node : sortedNodes) {
                builder.append(Strings.CARRIAGE_RETURN_AND_LINE_SEPARATOR);
                builder.append(Strings.TAB).append(node, Strings.ARGUMENT_SEPARATOR);
            }
            builder.cleanBuffer();
            builder.append(Strings.CARRIAGE_RETURN_AND_LINE_SEPARATOR).append(Strings.END_SUB_GROUP);
            Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "\r\n\r\nNodes: %s\r\n", builder.toString());
        }
    }

    /**
     *
     * @param node
     * @param session
     */
    private synchronized void reorganize(Node node, CloudSession session, ReorganizationAction action) {
        long time = System.currentTimeMillis();

        switch(action) {
            case CONNECT: {
                LocalLeaf localLeaf;
                Object[] path;
                List<UUID> nodes = List.of(thisNode.getId());
                List<PublishObjectMessage.Path> paths = new ArrayList<>();

                for (DistributedTree.Entry entry : sharedStore.filter(LocalLeaf.class)) {
                    localLeaf = (LocalLeaf) entry.getValue();
                    path = entry.getPath();
                    if (localLeaf.getInstance() instanceof DistributedLayer) {
                        if(localLeaf.getNodes().contains(thisNode.getId())) {
                            PublishLayerMessage publishLayerMessage = new PublishLayerMessage(UUID.randomUUID());
                            publishLayerMessage.setPath(path);
                            publishLayerMessage.setNodeId(thisNode.getId());
                            sendMessageToNode(session, publishLayerMessage);
                        }
                    } else if (localLeaf.getInstance() instanceof DistributedLock) {
                        //Mmmm!!!!
                    } else {
                        paths.add(new PublishObjectMessage.Path(path, nodes));
                    }
                }

                PublishObjectMessage publishObjectMessage = new PublishObjectMessage(UUID.randomUUID());
                publishObjectMessage.setTimestamp(System.currentTimeMillis());
                publishObjectMessage.setPaths(paths);
                sendMessageToNode(session, publishObjectMessage);
                break;
            }
            case DISCONNECT: {
                DistributedLeaf distributedLeaf;
                for (DistributedTree.Entry entry : sharedStore.filter(LocalLeaf.class, RemoteLeaf.class)) {
                    distributedLeaf = (DistributedLeaf) entry.getValue();
                    distributedLeaf.getNodes().remove(node.getId());
                    if(distributedLeaf.getInstance() instanceof DistributedLayer) {
                        ((DistributedLayer)distributedLeaf.getInstance()).removeNode(node.getId());
                    }
                }
                break;
            }
            case TIME: {
                int replicationFactor = SystemProperties.getInteger(
                        SystemProperties.Cloud.Orchestrator.REPLICATION_FACTOR);

                LocalLeaf localLeaf;
                Map<UUID, List<PublishObjectMessage.Path>> pathsByNode = new HashMap<>();
                for(Node sortedNode : getSortedNodes()) {
                    pathsByNode.put(sortedNode.getId(), new ArrayList<>());
                }
                for(DistributedTree.Entry entry : sharedStore.filter(LocalLeaf.class)) {
                    localLeaf = (LocalLeaf) entry.getValue();
                    if(!(localLeaf.getInstance() instanceof DistributedLayer) &&
                            !(localLeaf.getInstance() instanceof DistributedLock)) {
                        if (localLeaf.getNodes().size() < replicationFactor) {
                            for (Node sortedNode : getSortedNodes()) {
                                if (!localLeaf.getNodes().contains(sortedNode.getId())) {
                                    List<UUID> nodeIds = new ArrayList<>(localLeaf.getNodes());
                                    nodeIds.add(sortedNode.getId());
                                    pathsByNode.get(sortedNode.getId()).add(
                                            new PublishObjectMessage.Path(entry.getPath(),
                                                    localLeaf.getInstance(), nodeIds));
                                }
                            }
                        }
                    }
                }

                for(UUID nodeId : pathsByNode.keySet()) {
                    List<PublishObjectMessage.Path> paths = pathsByNode.get(nodeId);
                    if(!paths.isEmpty()) {
                        PublishObjectMessage publishObjectMessage = new PublishObjectMessage(UUID.randomUUID());
                        publishObjectMessage.setPaths(paths);
                        publishObjectMessage.setTimestamp(System.currentTimeMillis());
                        sendMessageToNode(sessionByNode.get(nodeId), publishObjectMessage);
                        for(PublishObjectMessage.Path path : paths) {
                            addObject(path.getValue(), path.getNodes(), List.of(), 0L, path.getPath());
                        }
                    }
                }
                break;
            }
        }

        time = System.currentTimeMillis() - time;
        if(time > SystemProperties.getLong(SystemProperties.Cloud.Orchestrator.REORGANIZATION_WARNING_TIME_LIMIT)) {
            Log.w(System.getProperty(SystemProperties.Cloud.LOG_TAG), "End reorganization process by action: %s, time: %d",
                    action.toString(), time);
        }
    }

    /**
     * This method is called into a new service thread in order to try to establish a connection
     * for each node registered into the cloud service.
     */
    private void maintainConnections() {
        while(!Thread.currentThread().isInterrupted()) {
            for(Node node : nodes) {
                synchronized (sessionByNode) {
                    if(node.getStatus().equals(Node.Status.LOST) &&
                            (System.currentTimeMillis() - node.getLastStatusUpdate()) >
                                    SystemProperties.getLong(SystemProperties.Cloud.Orchestrator.NODE_LOST_TIMEOUT)) {
                        disconnected(node);
                    }

                    if(!node.getStatus().equals(Node.Status.LOST)) {
                        if (!sessionByNode.containsKey(node.getId()) && !node.getStatus().equals(Node.Status.CONNECTED)) {
                            CloudClient client = new CloudClient(node.getLanAddress(), node.getLanPort());
                            NetService.getInstance().registerConsumer(client);
                            if (client.waitForConnect()) {
                                Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Connected with %s:%d",
                                        node.getLanAddress(), node.getLanPort());

                                if (connecting(node)) {
                                    try {
                                        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Sending credentials to %s:%d",
                                                node.getLanAddress(), node.getLanPort());
                                        client.send(new NodeIdentificationMessage(thisNode));
                                    } catch (IOException e) {
                                        client.disconnect();
                                        disconnected(node);
                                    }
                                }
                            } else {
                                Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Unable to connect with %s:%d",
                                        node.getLanAddress(), node.getLanPort());
                                lost(node);
                            }
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

    private void initServicePublication() {
        while(!Thread.currentThread().isInterrupted()) {

            try {
                LocalLeaf localLeaf;
                Object[] path;
                PublishLayerMessage publishLayerMessage;
                for (DistributedTree.Entry entry : sharedStore.filter(LocalLeaf.class)) {
                    localLeaf = (LocalLeaf) entry.getValue();
                    path = entry.getPath();
                    if (localLeaf.getInstance() instanceof DistributedLayer) {
                        publishLayerMessage = new PublishLayerMessage(UUID.randomUUID());
                        publishLayerMessage.setPath(path);
                        publishLayerMessage.setServiceEndPointId(thisServiceEndPoint.getId());
                        for (ServiceEndPoint serviceEndPoint : endPoints.values()) {
                            invokeService(serviceEndPoint.getId(), publishLayerMessage);
                        }
                    }
                }
            } catch (Exception ex){
                Log.w(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Unable to publish the service", ex);
            }

            try {
                Thread.sleep(SystemProperties.getLong(
                        SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.PUBLICATION_TIMEOUT));
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     *
     */
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
                            Map<String,List<Message>> load = getWagonLoad();
                            wagonMessage.getMessages().putAll(load);
                            wagonMessage.getNodes().addAll(nodes);
                            sendMessageToNode(sessionByNode.get(nextDestination.getId()), wagonMessage);
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


    private void initReorganizationTimer() {
        while(!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(SystemProperties.getLong(
                        SystemProperties.Cloud.Orchestrator.REORGANIZATION_TIMEOUT));
                try {
                    reorganize(null, null, ReorganizationAction.TIME);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } catch (InterruptedException ex){
                break;
            }
        }
    }

    void connectionLost(CloudSession session) {
        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "connection lost with %s:%d",
                session.getRemoteHost(), session.getRemotePort());
        synchronized (sessionByNode) {
            if(session.getNode() != null) {
                nodeDisconnected(session.getNode());
            }
        }
    }

    private void putWagonLoad(Node node, Message message) {
        synchronized (wagonLoad) {
            if(!wagonLoad.containsKey(node.getId().toString())) {
                wagonLoad.put(node.getId().toString(), new ArrayList<>());
            }
            wagonLoad.get(node.getId().toString()).add(message);
        }
    }

    private Map<String,List<Message>> getWagonLoad() {
        Map<String,List<Message>> result = new HashMap<>();
        synchronized (wagonLoad) {
            result.putAll(wagonLoad);
            wagonLoad.clear();
        }
        return result;
    }

    void incomingMessage(CloudSession session, Message message) {
        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG),
                "Incoming '%s' message: %s", message.getClass(), message.getId());
        if(message instanceof NodeIdentificationMessage) {
            NodeIdentificationMessage nodeIdentificationMessage = (NodeIdentificationMessage) message;
            Node node = nodesByLanId.get(nodeIdentificationMessage.getNode().getLanId());
            if(node == null) {
                node = nodesByWanId.get(nodeIdentificationMessage.getNode().getWanId());
            }

            if(node == null && Objects.equals(nodeIdentificationMessage.getNode().getClusterName(), thisNode.getClusterName())) {
                //In this case we need to add the node ass a new node
                registerConsumer(nodeIdentificationMessage.getNode());

                //Search again the node into the class collections.
                node = nodesByLanId.get(nodeIdentificationMessage.getNode().getLanId());
                if(node == null) {
                    node = nodesByWanId.get(nodeIdentificationMessage.getNode().getWanId());
                }
            }

            if(node != null) {
                updateNode(node, nodeIdentificationMessage);
                if (session.getConsumer() instanceof CloudClient) {
                    Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Incoming credentials response from %s:%d",
                            node.getLanAddress(), node.getLanPort());
                    if (!connected(node)) {
                        ((CloudClient) session.getConsumer()).disconnect();
                    } else {
                        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Node connected as client %s", node);
                        nodeConnected(node, session);
                        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Ack sent to %s:%d",
                                node.getLanAddress(), node.getLanPort());
                        sendMessageToNode(session, new AckMessage(message));
                    }
                } else if (session.getConsumer() instanceof CloudServer) {
                    if (connecting(node)) {
                        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Incoming credentials from %s:%d",
                                node.getLanAddress(), node.getLanPort());
                        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Response credentials to %s:%d",
                                node.getLanAddress(), node.getLanPort());
                        NodeIdentificationMessage returnNodeIdentificationMessage = new NodeIdentificationMessage(thisNode);
                        waitingAck.put(returnNodeIdentificationMessage.getId(), node);
                        sendMessageToNode(session, returnNodeIdentificationMessage);
                    } else {
                        try {
                            ((CloudServer) session.getConsumer()).send(session, new BusyNodeMessage(thisNode));
                        } catch (IOException e) {
                            //TODO: Close the session
                        }
                    }
                }
            } else {
                server.destroySession(session);
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
            synchronized (wagonMonitor) {
                lastVisit = System.currentTimeMillis();
                if(wagonMessage != null) {
                    Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Wagon crash");
                }
                wagonMessage = (CloudWagonMessage) message;
                sendMessageToNode(session, new AckMessage(message));

                List<Message> wagonMessages = wagonMessage.getMessages().remove(thisNode.getId().toString());
                if(wagonMessages != null) {
                    for (Message messageOfWagon : wagonMessages) {
                        incomingMessage(session, messageOfWagon);
                    }
                }

                for(Node wagonNode : wagonMessage.getNodes()) {
                    if(!nodesByLanId.containsKey(wagonNode.getLanId()) && !nodesByWanId.containsKey(wagonNode.getWanId())) {
                        registerConsumer(wagonNode);
                    }
                }
            }
        } else if(message instanceof HidePathMessage) {
            removePath(((HidePathMessage)message).getPath());
        } else if(message instanceof PublishPathMessage) {
            addPath(((PublishPathMessage)message).getPath());
        } else if(message instanceof PublishObjectMessage) {
            PublishObjectMessage publishObjectMessage = (PublishObjectMessage) message;
            for(PublishObjectMessage.Path path : publishObjectMessage.getPaths()) {
                if(path.getValue() != null) {
                    addObject(path.getValue(), path.getNodes(), List.of(),
                            publishObjectMessage.getTimestamp(), path.getPath());
                } else {
                    addObject(publishObjectMessage.getTimestamp(), List.of(),
                            path.getNodes(), path.getPath());
                }
            }
        } else if(message instanceof InvokeMessage) {
            InvokeMessage invokeMessage = (InvokeMessage) message;

            ResponseMessage responseMessage = new ResponseMessage(invokeMessage);
            Object object = sharedStore.getInstance(invokeMessage.getPath());
            responseMessage.setValue(object);
            sendMessageToNode(session, responseMessage);
        } else if(message instanceof LockMessage) {
            LockMessage lockMessage = (LockMessage) message;
            ResponseMessage responseMessage = new ResponseMessage(lockMessage);
            responseMessage.setValue(distributedLock(lockMessage.getTimestamp(),
                    lockMessage.getNanos(), lockMessage.getPath()));
            sendMessageToNode(session, responseMessage);
        } else if(message instanceof UnlockMessage) {
            UnlockMessage unlockMessage = (UnlockMessage) message;
            distributedUnlock(unlockMessage.getPath());
        } else if(message instanceof SignalMessage) {
            SignalMessage signalMessage = (SignalMessage) message;
            distributedSignal(signalMessage.getLockName(), signalMessage.getConditionName());
        } else if(message instanceof SignalAllMessage) {
            SignalAllMessage signalAllMessage = (SignalAllMessage) message;
            distributedSignalAll(signalAllMessage.getLockName(), signalAllMessage.getConditionName());
        } else if(message instanceof EventMessage) {
            EventMessage eventMessage = (EventMessage) message;
            distributedDispatchEvent(eventMessage.getEvent());
            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setValue(true);
            sendMessageToNode(session, responseMessage);
        } else if(message instanceof PublishLayerMessage) {
            PublishLayerMessage publishLayerMessage = (PublishLayerMessage) message;
            ResponseMessage responseMessage = new ResponseMessage(publishLayerMessage);
            try {
                DistributedLayer distributedLayer = getDistributedLayer(false, publishLayerMessage.getPath());
                distributedLayer.addNode(publishLayerMessage.getNodeId());
                distributedLayer.addServiceEndPoint(publishLayerMessage.getServiceEndPointId());
                responseMessage.setValue(true);
            } catch (Exception ex) {
                responseMessage.setThrowable(ex);
            }
            sendMessageToNode(session, responseMessage);
        } else if(message instanceof LayerInvokeMessage) {
            LayerInvokeMessage layerInvokeMessage = (LayerInvokeMessage) message;
            Object result = null;
            Throwable throwable = null;
            try {
                result = distributedLayerInvoke(layerInvokeMessage.getSessionId(),
                        layerInvokeMessage.getParameterTypes(), layerInvokeMessage.getParameters(),
                        layerInvokeMessage.getMethodName(), layerInvokeMessage.getPath());
            } catch (Throwable t) {
                throwable = t;
            }
            ResponseMessage responseMessage = new ResponseMessage(message);
            responseMessage.setValue(result);
            responseMessage.setThrowable(throwable);
            Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG),
                    "Sending response message: %s", message.getId());
            sendMessageToNode(session, responseMessage);
        } else if(message instanceof TestNodeMessage) {
            sendMessageToNode(session, new ResponseMessage(message));
        } else if(message instanceof ResponseMessage) {
            ResponseListener responseListener = responseListeners.get(message.getId());
            if(responseListener != null) {
                responseListener.setMessage((ResponseMessage) message);
            }
        } else if(message instanceof AckMessage) {
            Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Incoming ack from %s:%d",
                    session.getRemoteHost(), session.getRemotePort());
            if(session.getConsumer() instanceof CloudServer) {
                Node node = waitingAck.remove(message.getId());
                if(node != null) {
                    if(connected(node)) {
                        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Node connected as server %s", node);
                        nodeConnected(node, session);
                    }
                }
            }

            synchronized (wagonMonitor) {
                if (wagonMessage != null) {
                    if (message.getId().equals(wagonMessage.getId())) {
                        wagonMessage = null;
                        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Wagon gone");
                    }
                }
            }
        }
    }

    private void sendMessageToNode(CloudSession session, Message message) {
        try {
            NetServiceConsumer consumer = session.getConsumer();
            if(consumer instanceof CloudClient) {
                ((CloudClient)consumer).send(message);
            } else {
                ((CloudServer)consumer).send(session, message);
            }
        } catch (IOException e) {
            server.destroySession(session);
        }
    }

    private Object invokeNode(CloudSession session, Message message, Long timeout) {
        ResponseListener responseListener = new ResponseListener(timeout);
        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG),
                "Sending invokeNode message: %s", message.getId().toString());
        responseListeners.put(message.getId(), responseListener);
        sendMessageToNode(session, message);
        return responseListener.getResponse(message);
    }

    private Object invokeNode(CloudSession session, Message message) {
        return invokeNode(session, message,
                SystemProperties.getLong(SystemProperties.Cloud.Orchestrator.INVOKE_TIMEOUT));
    }

    private Object invokeService(UUID serviceEndPointId, Message message) {
        return invokeService(serviceEndPointId, message,
                SystemProperties.getLong(SystemProperties.Cloud.Orchestrator.INVOKE_TIMEOUT));
    }

    private Object invokeService(UUID serviceEndPointId, Message message, Long timeout) {
        Object result;
        if(message.getId() == null) {
            message.setId(UUID.randomUUID());
        }
        ServiceEndPoint serviceEndPoint = endPoints.get(serviceEndPointId);
        if(serviceEndPoint != null) {
            try {
                CloudClient client = new CloudClient(serviceEndPoint.getGatewayAddress(), serviceEndPoint.getGatewayPort());
                NetService.getInstance().registerConsumer(client);
                if (client.waitForConnect()) {
                    ResponseListener responseListener = new ResponseListener(timeout);
                    responseListeners.put(message.getId(), responseListener);
                    try {
                        client.send(message);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    result = responseListener.getResponse(message);
                    client.disconnect();
                } else {
                    throw new RuntimeException("Connection timeout with service: " + serviceEndPoint.getName());
                }
            } catch (Exception ex) {
                throw new RuntimeException("Unable to connect with service: " + serviceEndPoint.getName(), ex);
            }
        } else {
            throw new RuntimeException("Service end point not found (" + serviceEndPoint.getId() + ")");
        }
        return result;
    }

    private boolean testNode(CloudSession session) {
        boolean result = true;
        try {
            UUID id = UUID.randomUUID();
            invokeNode(session, new TestNodeMessage(id),
                    SystemProperties.getLong(SystemProperties.Cloud.Orchestrator.TEST_NODE_TIMEOUT));
        } catch (Exception ex){
            ex.printStackTrace();
            result = false;
        }
        return result;
    }

    private DistributedLock getDistributedLock(Object... path) {
        DistributedLock distributedLock;
        synchronized (sharedStore) {
            distributedLock = (DistributedLock) sharedStore.getInstance(path);
            if (distributedLock == null) {
                distributedLock = new DistributedLock();
                distributedLock.setStatus(DistributedLock.Status.UNLOCKED);
                addObject(distributedLock, List.of(thisNode.getId()), List.of(), System.currentTimeMillis(), path);
            }
        }
        return distributedLock;
    }

    public void lock(Object... path) {
        DistributedLock distributedLock = getDistributedLock(path);
        synchronized (distributedLock) {
            while (!distributedLock.getStatus().equals(DistributedLock.Status.UNLOCKED)) {
                try {
                    distributedLock.wait();
                } catch (InterruptedException e) {
                }
            }
            distributedLock.setStatus(DistributedLock.Status.LOCKING);
        }

        LockMessage lockMessage = new LockMessage(UUID.randomUUID());
        lockMessage.setPath(path);
        lockMessage.setTimestamp(distributedLock.getTimestamp());
        boolean locked;
        while (!distributedLock.getStatus().equals(DistributedLock.Status.LOCKED)) {
            lockMessage.setNanos(distributedLock.getNanos());
            locked = true;
            List<CloudSession> sessions = new ArrayList<>(sessionByNode.values());
            for (CloudSession session : sessions) {
                try {
                    if (!(locked = locked & (boolean) invokeNode(session, lockMessage))) {
                        break;
                    }
                } catch (Exception ex){
                    Log.w(System.getProperty(SystemProperties.Cloud.LOG_TAG),
                            "Unable to send lock message to session: ", session.getId());
                }
            }
            if (locked) {
                distributedLock.setStatus(DistributedLock.Status.LOCKED);
            } else {
                distributedLock.setStatus(DistributedLock.Status.WAITING);
                try {
                    synchronized (distributedLock) {
                        distributedLock.wait(1000);
                    }
                } catch (InterruptedException e) { }
            }
        }
    }

    private boolean distributedLock(Long timestamp, Long nanos, Object... path) {
        boolean result;
        DistributedLock distributedLock = getDistributedLock(path);
        synchronized (distributedLock) {
            result = distributedLock.getStatus().equals(DistributedLock.Status.UNLOCKED) ||
                    (distributedLock.getTimestamp() > timestamp && distributedLock.getNanos() > nanos);
        }
        return result;
    }

    public void unlock(Object... path) {
        DistributedLock distributedLock = getDistributedLock(path);
        synchronized (distributedLock) {
            distributedLock.setStatus(DistributedLock.Status.UNLOCKED);
            distributedLock.notifyAll();
        }

        UnlockMessage unlockMessage = new UnlockMessage(UUID.randomUUID());
        unlockMessage.setPath(path);
        List<CloudSession> sessions = new ArrayList<>(sessionByNode.values());
        for (CloudSession session : sessions) {
            try {
                sendMessageToNode(session, unlockMessage);
            } catch (Exception ex){
                Log.w(System.getProperty(SystemProperties.Cloud.LOG_TAG),
                        "Unable to send unlock message to session: ", session.getId());
            }
        }
    }

    private synchronized void distributedUnlock(Object... path) {
        DistributedLock distributedLock = getDistributedLock(path);
        synchronized (distributedLock) {
            distributedLock.notifyAll();
        }
    }

    public void signal(String lockName, String conditionName) {
        SignalMessage signalMessage = new SignalMessage(UUID.randomUUID());
        signalMessage.setLockName(lockName);
        signalMessage.setConditionName(conditionName);
        for (CloudSession session : sessionByNode.values()) {
            sendMessageToNode(session, signalMessage);
        }
    }

    private void distributedSignal(String lockName, String conditionName) {
        LockImpl lock = (LockImpl) Cloud.getLock(lockName);
        if(lock != null) {
            ((LockImpl.ConditionImpl)lock.newCondition(conditionName)).distributedSignal();
        }
    }

    public void signalAll(String lockName, String conditionName) {
        SignalAllMessage signalAllMessage = new SignalAllMessage(UUID.randomUUID());
        signalAllMessage.setLockName(lockName);
        signalAllMessage.setConditionName(conditionName);
        for (CloudSession session : sessionByNode.values()) {
            sendMessageToNode(session, signalAllMessage);
        }
    }

    private void distributedSignalAll(String lockName, String conditionName) {
        LockImpl lock = (LockImpl) Cloud.getLock(lockName);
        if(lock != null) {
            ((LockImpl.ConditionImpl)lock.newCondition(conditionName)).distributedSignalAll();
        }
    }

    public void dispatchEvent(DistributedEvent event) {
        EventMessage eventMessage = new EventMessage(UUID.randomUUID());
        eventMessage.setEvent(event);
        for (CloudSession session : sessionByNode.values()) {
            sendMessageToNode(session, eventMessage);
        }
    }

    private void distributedDispatchEvent(DistributedEvent event) {
        RemoteEvent remoteEvent = new RemoteEvent(event);
        Events.sendEvent(remoteEvent);
    }

    private DistributedLayer getDistributedLayer(boolean local, Object... path) {
        DistributedLayer distributedLayer;
        synchronized (sharedStore) {
            distributedLayer = (DistributedLayer) sharedStore.getInstance(path);
            if (distributedLayer == null) {
                try {
                    distributedLayer = new DistributedLayer(Class.forName((String)path[path.length - 2]),
                            (String)path[path.length - 1]);
                    if(local) {
                        addObject(distributedLayer, List.of(thisNode.getId()), List.of(thisServiceEndPoint.getId()),
                                System.currentTimeMillis(), path);
                    } else {
                        addObject(distributedLayer, List.of(), List.of(), System.currentTimeMillis(), path);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException();
                }
            }
        }
        return distributedLayer;
    }

    public boolean isDistributedLayerPublished(Object... path) {
        return sharedStore.getInstance(path) != null;
    }

    public void publishDistributedLayer(Object... path) {
        getDistributedLayer(true, path);
        PublishLayerMessage publishLayerMessage = new PublishLayerMessage();
        publishLayerMessage.setPath(path);
        publishLayerMessage.setNodeId(thisNode.getId());
        publishLayerMessage.setServiceEndPointId(thisServiceEndPoint.getId());
        for (CloudSession session : sessionByNode.values()) {
            sendMessageToNode(session, publishLayerMessage);
        }


    }

    public <O extends Object> O layerInvoke(Object[] parameters, Method method, Object... path) {
        O result;
        DistributedLayer distributedLayer = getDistributedLayer(false, path);

        LayerInvokeMessage layerInvokeMessage = new LayerInvokeMessage(UUID.randomUUID());
        layerInvokeMessage.setMethodName(method.getName());
        layerInvokeMessage.setParameterTypes(method.getParameterTypes());
        layerInvokeMessage.setSessionId(ServiceSession.getCurrentIdentity().getId());
        layerInvokeMessage.setParameters(parameters);
        layerInvokeMessage.setPath(path);

        UUID nodeId = distributedLayer.getNodeToInvoke();
        CloudSession session = null;
        long startTime;
        while (nodeId != null) {
            session = sessionByNode.get(nodeId);
            startTime = System.currentTimeMillis();
            if(session != null && testNode(session)) {
                distributedLayer.addResponseTime(nodeId, (System.currentTimeMillis() - startTime));
                break;
            } else {
                distributedLayer.addResponseTime(nodeId, (System.currentTimeMillis() - startTime));
                nodeId = distributedLayer.getNodeToInvoke();
            }
        }

        if(session != null) {
            startTime = System.currentTimeMillis();
            try {
                result = (O) invokeNode(session, layerInvokeMessage);
            } finally {
                distributedLayer.addResponseTime(nodeId, (System.currentTimeMillis() - startTime));
            }
        } else {
            UUID serviceEndPointId = distributedLayer.getServiceToInvoke();
            if(serviceEndPointId != null) {
                startTime = System.currentTimeMillis();
                try {
                    result = (O) invokeService(serviceEndPointId, layerInvokeMessage);
                } finally {
                    distributedLayer.addResponseTime(nodeId, (System.currentTimeMillis() - startTime));
                }
            } else {
                throw new RuntimeException("Route not found to the layer: " + distributedLayer.getLayerInterface().getName() + "@" + distributedLayer.getLayerName());
            }
        }
        return result;
    }

    private Object distributedLayerInvoke(
            UUID sessionId, Class[] parameterTypes,
            Object[] parameters, String methodName, Object... path) {
        Object result;
        DistributedLayer distributedLayer = getDistributedLayer(true, path);
        Class layerInterface = distributedLayer.getLayerInterface();
        Map<String, DistributedLayerInvoker> invokers =
                Introspection.getInvokers(layerInterface, new DistributedLayerInvokerFilter(methodName, parameterTypes));
        if(invokers.size() == 1) {
            LayerInterface layer = Layers.get(layerInterface, distributedLayer.getLayerName());
            try {
                ServiceSession.getCurrentSession().addIdentity(ServiceSession.findSession(sessionId));
                result = invokers.values().iterator().next().invoke(layer, parameters);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException();
        }
        return result;
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
     * @return
     */
    private boolean lost(Node node) {
        Objects.requireNonNull(node, "Null node");

        boolean result = false;
        node.setConnectionAttempts(node.getConnectionAttempts() + 1);
        if(node.getConnectionAttempts() >= 2) {
            if(changeStatus(node, Node.Status.LOST)) {
                node.setConnectionAttempts(0);
            }
        }
        return result;
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
                if(status.equals(Node.Status.CONNECTING) || status.equals(Node.Status.LOST)) {
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
            case LOST: {
                if(status.equals(Node.Status.DISCONNECTED) || status.equals(Node.Status.CONNECTING)) {
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

    public void publishPath(Object... path) {
        synchronized (sharedStore) {
            addPath(path);
            PublishPathMessage publishPathMessage = new PublishPathMessage();
            publishPathMessage.setPath(path);
            for(CloudSession session : sessionByNode.values()) {
                sendMessageToNode(session, publishPathMessage);
            }
        }
    }

    public void publishObject(Object object, Long timestamp, Object... path) {
        List<UUID> nodeIds = new ArrayList<>();
        nodeIds.add(thisNode.getId());

        List < Node > nodes = getSortedNodes();

        int replicationFactor = SystemProperties.getInteger(
                SystemProperties.Cloud.Orchestrator.REPLICATION_FACTOR);

        for (int i = 0; i < (replicationFactor - 1) && !nodes.isEmpty(); i++) {
            nodeIds.add(nodes.get(i).getId());
        }

        addObject(object, nodeIds, List.of(), timestamp, path);

        PublishObjectMessage.Path pathObject = new PublishObjectMessage.Path(path, nodeIds);
        fork(() -> {
            if(!nodes.isEmpty()) {
                int counter = 0;
                for (Node node : nodes) {
                    PublishObjectMessage publishObjectMessage = new PublishObjectMessage(UUID.randomUUID());
                    publishObjectMessage.getPaths().add(pathObject);
                    publishObjectMessage.setTimestamp(timestamp);
                    if(counter < replicationFactor) {
                        pathObject.setValue(object);
                    } else {
                        pathObject.setValue(null);
                    }
                    sendMessageToNode(sessionByNode.get(node.getId()), publishObjectMessage);
                    counter++;
                }
            }
        });
    }

    public void hidePath(Object... path) {
        removePath(path);

        fork(()->{
            for (CloudSession session : sessionByNode.values()) {
                HidePathMessage hidePathMessage = new HidePathMessage(UUID.randomUUID());
                hidePathMessage.setPath(path);
                sendMessageToNode(session, hidePathMessage);
            }
        });
    }

    public <O extends Object> O invokeNode(Object... path) {
        O result = (O) sharedStore.getInstance(path);
        if(result instanceof RemoteLeaf) {
            InvokeMessage getMessage = new InvokeMessage(UUID.randomUUID());
            getMessage.setPath(path);
            CloudSession session = null;
            Iterator<UUID> ids = ((RemoteLeaf)result).getNodes().iterator();
            while (ids.hasNext() && session == null) {
                session = sessionByNode.get(ids.next());
            }

            if(session != null) {
                result = (O) invokeNode(session, getMessage);
            } else {
                result = null;
            }
        }
        return result;
    }

    private void removePath(Object... path) {
        sharedStore.remove(path);
    }

    private boolean addPath(Object... path) {
        boolean result = false;
        if(sharedStore.getInstance(path) == null) {
            sharedStore.createPath(path);
            Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Local path added: %s", Arrays.toString(path));
            result = true;
        }
        return result;
    }

    private void addObject(Object object, List<UUID> nodes, List<UUID> serviceEndPoints, Long timestamp, Object... path) {
        sharedStore.add(object, nodes, serviceEndPoints, timestamp, path);
        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Local leaf added: %s", Arrays.toString(path));
    }

    private void addObject(Long timestamp, List<UUID> nodes, List<UUID> serviceEndPoints, Object... path) {
        sharedStore.add(timestamp, nodes, serviceEndPoints, path);
        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Remote leaf added: %s", Arrays.toString(path));
    }

    private final class ResponseListener {

        private final Long timeout;
        private ResponseMessage responseMessage;

        public ResponseListener(Long timeout) {
            this.timeout = timeout;
        }

        public Object getResponse(Message message) {
            Object result;
            synchronized (this) {
                if(responseMessage == null) {
                    Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG),
                            "Response listener waiting for id: %s", message.getId().toString());
                    try {
                        wait(timeout);
                    } catch (InterruptedException e) {
                    }
                }
            }

            if(responseMessage != null) {
                if(responseMessage.getThrowable() != null) {
                    throw new RuntimeException(responseMessage.getThrowable());
                } else {
                    result = responseMessage.getValue();
                }
            } else {
                throw new RuntimeException("Remote invocation timeout");
            }
            responseListeners.remove(message.getId());
            return result;
        }

        public void setMessage(ResponseMessage message) {
            synchronized (this) {
                Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG),
                        "Response listener notified with id: %s", message.getId().toString());
                responseMessage = message;
                notifyAll();
            }
        }
    }

    private static final class DistributedLayerInvoker extends Introspection.Invoker {

        public DistributedLayerInvoker(Class implementationClass, Method method) {
            super(implementationClass, method);
        }

    }

    private static final class DistributedLayerInvokerFilter implements Introspection.InvokerFilter<CloudOrchestrator.DistributedLayerInvoker> {

        private final String name;
        private final Class[] parameterTypes;

        public DistributedLayerInvokerFilter(String name, Class[] parameterTypes) {
            this.name = name;
            this.parameterTypes = parameterTypes;
        }

        @Override
        public Introspection.InvokerEntry<CloudOrchestrator.DistributedLayerInvoker> filter(Method method) {
            Introspection.InvokerEntry<CloudOrchestrator.DistributedLayerInvoker> result = null;
            if(method.getName().equalsIgnoreCase(name) && Arrays.equals(parameterTypes, method.getParameterTypes())) {
                result = new Introspection.InvokerEntry<>(method.getName(),
                        new CloudOrchestrator.DistributedLayerInvoker(method.getDeclaringClass(), method));
            }
            return result;
        }
    }

    private enum ReorganizationAction {

        CONNECT,

        DISCONNECT,

        TIME

    }
}
