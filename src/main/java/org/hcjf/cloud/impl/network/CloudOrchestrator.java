package org.hcjf.cloud.impl.network;

import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1ServicePort;
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
import org.hcjf.io.net.kubernetes.KubernetesSpy;
import org.hcjf.io.net.kubernetes.KubernetesSpyConsumer;
import org.hcjf.io.net.messages.Message;
import org.hcjf.io.net.messages.MessageCollection;
import org.hcjf.io.net.messages.NetUtils;
import org.hcjf.io.net.messages.ResponseMessage;
import org.hcjf.layers.Layer;
import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.ReadRowsLayerInterface;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Queryable;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;
import org.hcjf.utils.Introspection;
import org.hcjf.utils.Strings;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
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

        Layers.publishLayer(SystemCloudNodeReadableImplementation.class);
        Layers.publishLayer(SystemCloudServiceReadableImplementation.class);
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
    private Object publishMeMonitor;
    private Boolean publishMeFlag;

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

        publishMeMonitor = new Object();
        publishMeFlag = false;

        wagonMonitor = new Object();
        lastVisit = System.currentTimeMillis();
        lastServicePublication = System.currentTimeMillis() - SystemProperties.getLong(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.PUBLICATION_TIMEOUT);
        wagonLoad = new HashMap<>();

        random = new Random();
        sharedStore = new DistributedTree(Strings.EMPTY_STRING);

        fork(this::maintainConnections);
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

        if(SystemProperties.getBoolean(SystemProperties.Cloud.Orchestrator.Kubernetes.ENABLED)) {
            thisNode.setId(new UUID((NetUtils.getLocalIp() + Node.class.getName()).hashCode(), KubernetesSpy.getHostName().hashCode()));
            thisServiceEndPoint.setId(new UUID(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE).hashCode(),
                    SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.SERVICE_NAME).hashCode()));
            Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Kubernetes service id %s", thisServiceEndPoint.getId());
            Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Kubernetes node id %s", thisNode.getId());
            Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Local IP %s", NetUtils.getLocalIp());
            thisNode.setLanAddress(NetUtils.getLocalIp());
            thisServiceEndPoint.setGatewayAddress(NetUtils.getLocalIp());
            Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Kubernetes consumer starting");

            Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Kubernetes pod labels: %s",
                    SystemProperties.getMap(SystemProperties.Cloud.Orchestrator.Kubernetes.POD_LABELS).toString());
            Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Kubernetes service labels: %s",
                    SystemProperties.getMap(SystemProperties.Cloud.Orchestrator.Kubernetes.SERVICE_LABELS).toString());
            KubernetesSpy.getInstance().registerConsumer(new KubernetesSpyConsumer(
                    pod -> {
                        Map<String,String> expectedLabels = SystemProperties.getMap(SystemProperties.Cloud.Orchestrator.Kubernetes.POD_LABELS);
                        Map<String,String> labels = pod.getMetadata().getLabels();
                        return verifyLabels(expectedLabels, labels);
                    },
                    service -> {
                        Map<String,String> expectedLabels = SystemProperties.getMap(SystemProperties.Cloud.Orchestrator.Kubernetes.SERVICE_LABELS);
                        Map<String,String> labels = service.getMetadata().getLabels();
                        return verifyLabels(expectedLabels, labels);
                    }) {

                private final Map<String,Node> nodesByPodId = new HashMap<>();

                @Override
                protected void onPodDiscovery(V1Pod pod) {
                    Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Kubernetes pod discovery: %s", pod.getMetadata().getUid());
                    Node node = new Node();
                    node.setLanAddress(pod.getStatus().getPodIP());
                    //Use the same port because supposed that the node is a replica of this node.
                    node.setLanPort(SystemProperties.getInteger(SystemProperties.Cloud.Orchestrator.ThisNode.LAN_PORT));
                    registerConsumer(node);
                    nodesByPodId.put(pod.getMetadata().getUid(),node);
                }

                @Override
                protected void onPodLost(V1Pod pod) {
                    Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Kubernetes pod lost: %s", pod.getMetadata().getUid());
                    unregisterConsumer(nodesByPodId.remove(pod.getMetadata().getUid()));
                }

                @Override
                protected void onServiceDiscovery(V1Service service) {
                    Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Kubernetes service discovery: %s", service.getMetadata().getUid());
                    ServiceEndPoint serviceEndPoint = new ServiceEndPoint();
                    serviceEndPoint.setId(new UUID(service.getMetadata().getNamespace().hashCode(), service.getMetadata().getName().hashCode()));
                    serviceEndPoint.setGatewayAddress(service.getMetadata().getName());
                    for(V1ServicePort port : service.getSpec().getPorts()) {
                        if(port.getName().equals(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.SERVICE_PORT_NAME))) {
                            serviceEndPoint.setGatewayPort(port.getPort());
                            break;
                        }
                    }
                    registerConsumer(serviceEndPoint);
                }

                @Override
                protected void onServiceLost(V1Service service) {
                    ServiceEndPoint serviceEndPoint = new ServiceEndPoint();
                    serviceEndPoint.setId(new UUID(service.getMetadata().getNamespace().hashCode(), service.getMetadata().getName().hashCode()));
                    unregisterConsumer(serviceEndPoint);
                }
            });
        }
    }

    public boolean verifyLabels(Map<String,String> expectedLabels, Map<String,String> labels) {
        boolean result = true;
        for(String labelKey : expectedLabels.keySet()) {
            if(!(labels.containsKey(labelKey) && labels.get(labelKey).equals(expectedLabels.get(labelKey)))) {
                result = false;
                break;
            }
        }
        return result;
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
            if(lanId != null || wanId != null) {
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
            }
        } else if(networkComponent instanceof ServiceEndPoint) {
            ServiceEndPoint endPoint = (ServiceEndPoint) networkComponent;
            if(endPoint.getGatewayAddress() != null){
                if(!(thisServiceEndPoint.getGatewayId().equals(endPoint.getGatewayId()) ||
                        endPointsByGatewayId.containsKey(endPoint.getGatewayId()))) {
                    endPoints.put(endPoint.getId(), endPoint);
                    endPointsByGatewayId.put(endPoint.getGatewayId(), endPoint);
                    Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "New service end point registered: %s", endPoint);

                    fork(() -> initServicePublication(endPoint));
                }
            }
        }
    }

    /**
     * This method run continuously publishing all the distributed layers for all the registered services.
     */
    private void initServicePublication(ServiceEndPoint serviceEndPoint) {
        while(!Thread.currentThread().isInterrupted()) {

            synchronized (publishMeMonitor) {
                if(!publishMeFlag) {
                    try {
                        publishMeMonitor.wait();
                    } catch (InterruptedException e) { }
                    continue;
                }
            }

            try {
                Collection<Message> messages = createServicePublicationCollection();
                ServiceDefinitionMessage serviceDefinitionMessage = new ServiceDefinitionMessage();
                serviceDefinitionMessage.setId(UUID.randomUUID());
                serviceDefinitionMessage.setMessages(messages);
                serviceDefinitionMessage.setServiceId(thisServiceEndPoint.getId());
                serviceDefinitionMessage.setServiceName(thisServiceEndPoint.getName());
                Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Sending interfaces to: %s", serviceEndPoint);
                try {
                    invokeService(serviceEndPoint.getId(), serviceDefinitionMessage);
                    break;
                } catch (Exception ex) {
                    Log.w(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Unable to publish the service: %s", ex, serviceEndPoint);
                    try {
                        Thread.sleep(SystemProperties.getLong(
                                SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.PUBLICATION_TIMEOUT));
                    } catch (InterruptedException e) {
                        break;
                    }
                    continue;
                }
            } catch (Exception ex){
                Log.w(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Fail to trying publish the service", ex);
            }
        }
    }

    public final void publishMe() {
        synchronized (publishMeMonitor) {
            publishMeFlag = true;
            publishMeMonitor.notifyAll();
        }
    }

    @Override
    public void unregisterConsumer(NetworkComponent networkComponent) {
        if(networkComponent instanceof Node) {
            Node node = (Node) networkComponent;
            String lanId = node.getLanId();
            String wanId = node.getWanId();
            nodesByLanId.remove(lanId);
            nodesByWanId.remove(wanId);
            nodes.remove(node);
        }
    }

    /**
     * Returns a list with the nodes sorted by id, this list has the same
     * order in all the nodes into the cluster.
     * @return List with sorted nodes.
     */
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
     * This method is called periodically or for some particular events like the connection or disconnection of a node,
     * and verify the memory organization to fix it if necessary.
     * @param node Node that produce the event.
     * @param session Session that represents the node.
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
                    if (!(localLeaf.getInstance() instanceof DistributedLayer) &&
                            !(localLeaf.getInstance() instanceof DistributedLock)) {
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
                            addLocalObject(path.getValue(), path.getNodes(), List.of(), 0L, path.getPath());
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

    /**
     * Creates all the message needed to publish the service.
     * @return Collection of messages.
     */
    private Collection<Message> createServicePublicationCollection() {
        LocalLeaf localLeaf;
        Object[] path;
        Collection<Message> messages = new ArrayList<>();
        PublishLayerMessage publishLayerMessage;
        for (DistributedTree.Entry entry : sharedStore.filter(LocalLeaf.class)) {
            localLeaf = (LocalLeaf) entry.getValue();
            path = entry.getPath();
            if (localLeaf.getInstance() instanceof DistributedLayer) {
                publishLayerMessage = new PublishLayerMessage(UUID.randomUUID());
                publishLayerMessage.setPath(path);
                publishLayerMessage.setServiceEndPointId(thisServiceEndPoint.getId());
                messages.add(publishLayerMessage);
            }
        }
        return messages;
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

    public void incomingMessage(CloudSession session, Message message) {
        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG),
                "Incoming '%s' message: %s", message.getClass(), message.getId());
        Message responseMessage = null;
        if(message instanceof ServiceDefinitionMessage) {
            ServiceDefinitionMessage serviceDefinitionMessage = (ServiceDefinitionMessage) message;
            if(serviceDefinitionMessage.getMessages() != null) {
                for(Message innerMessage : serviceDefinitionMessage.getMessages()) {
                    processMessage(session, innerMessage);
                }
            }
            endPoints.get(((ServiceDefinitionMessage) message).getServiceId()).setName(
                    ((ServiceDefinitionMessage) message).getServiceName());
            responseMessage = new ServiceDefinitionResponseMessage(message);
            ((ServiceDefinitionResponseMessage) responseMessage).setMessages(createServicePublicationCollection());
        } else if(message instanceof MessageCollection) {
            MessageCollection collection = (MessageCollection) message;
            for(Message innerMessage : collection.getMessages()) {
                processMessage(session, innerMessage);
            }
        } else {
            responseMessage = processMessage(session, message);
        }

        if(!(message instanceof ResponseMessage)) {
            if (responseMessage == null) {
                responseMessage = new ResponseMessage(message);
            }

            Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG),
                    "Sending response message: %s", message.getId());
            sendMessageToNode(session, responseMessage);
        }
    }

    private Message processMessage(CloudSession session, Message message) {
        Message responseMessage = null;
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
                        responseMessage = new AckMessage(message);
                    }
                } else if (session.getConsumer() instanceof CloudServer) {
                    if (connecting(node)) {
                        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Incoming credentials from %s:%d",
                                node.getLanAddress(), node.getLanPort());
                        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Response credentials to %s:%d",
                                node.getLanAddress(), node.getLanPort());
                        NodeIdentificationMessage returnNodeIdentificationMessage = new NodeIdentificationMessage(thisNode);
                        waitingAck.put(returnNodeIdentificationMessage.getId(), node);
                        responseMessage = returnNodeIdentificationMessage;
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
        } else if(message instanceof HidePathMessage) {
            removePath(((HidePathMessage)message).getPath());
        } else if(message instanceof PublishPathMessage) {
            addPath(((PublishPathMessage)message).getPath());
        } else if(message instanceof PublishObjectMessage) {
            PublishObjectMessage publishObjectMessage = (PublishObjectMessage) message;
            for(PublishObjectMessage.Path path : publishObjectMessage.getPaths()) {
                if(path.getValue() != null) {
                    addLocalObject(path.getValue(), path.getNodes(), List.of(),
                            publishObjectMessage.getTimestamp(), path.getPath());
                } else {
                    addRemoteObject(null, path.getNodes(),
                            path.getNodes(), publishObjectMessage.getTimestamp(), path.getPath());
                }
            }
        } else if(message instanceof InvokeMessage) {
            InvokeMessage invokeMessage = (InvokeMessage) message;

            responseMessage = new ResponseMessage(invokeMessage);
            Object object = sharedStore.getInstance(invokeMessage.getPath());
            ((ResponseMessage)responseMessage).setValue(object);
        } else if(message instanceof LockMessage) {
            LockMessage lockMessage = (LockMessage) message;
            responseMessage = new ResponseMessage(lockMessage);
            ((ResponseMessage)responseMessage).setValue(distributedLock(lockMessage.getTimestamp(),
                    lockMessage.getNanos(), lockMessage.getPath()));
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
            responseMessage = new ResponseMessage(eventMessage);
            ((ResponseMessage)responseMessage).setValue(true);
        } else if(message instanceof PublishLayerMessage) {
            PublishLayerMessage publishLayerMessage = (PublishLayerMessage) message;
            responseMessage = new ResponseMessage(publishLayerMessage);

            try {
                boolean localImpl = false;
                Object[] path = publishLayerMessage.getPath();
                Class<? extends LayerInterface> layerInterfaceClass = (Class<? extends LayerInterface>) Class.forName((String)path[path.length - 2]);
                String implName = (String)path[path.length - 1];

                try {
                    Layers.get(layerInterfaceClass, implName);
                    localImpl = true;
                } catch (Exception ex) {}

                if(!localImpl) {
                    DistributedLayer distributedLayer = getDistributedLayer(false, publishLayerMessage.getPath());
                    distributedLayer.addServiceEndPoint(publishLayerMessage.getServiceEndPointId());
                    Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Remote %s layer founded %s in %s",
                            layerInterfaceClass.getName(), implName, endPoints.get(publishLayerMessage.getServiceEndPointId()).getGatewayAddress());
                }
                ((ResponseMessage)responseMessage).setValue(true);
            } catch (Exception ex) {
                ((ResponseMessage)responseMessage).setThrowable(ex);
            }
        } else if(message instanceof PublishPluginMessage) {
            PublishPluginMessage publishPluginMessage = (PublishPluginMessage) message;
            Layers.publishPlugin(ByteBuffer.wrap(publishPluginMessage.getJarFile()));
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
            responseMessage = new ResponseMessage(message);
            ((ResponseMessage)responseMessage).setValue(result);
            ((ResponseMessage)responseMessage).setThrowable(throwable);
        } else if(message instanceof TestNodeMessage) {
            responseMessage = new ResponseMessage(message);
        } else if(message instanceof ResponseMessage) {
            ResponseListener responseListener = responseListeners.get(message.getId());
            if(responseListener != null) {
                responseListener.setMessage((ResponseMessage) message);
                if(message instanceof ServiceDefinitionResponseMessage) {
                    for(Message innerMessage : ((ServiceDefinitionResponseMessage)message).getMessages()) {
                        processMessage(session, innerMessage);
                    }
                }
            } else {
                Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG),
                        "Response listener not found: %s", message.getId());
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
        }
        return responseMessage;
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

    private void registerListener(Message message, ResponseListener listener) {
        synchronized (responseListeners) {
            while (responseListeners.containsKey(message.getId())) {
                Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Message id crash!! %s", message.getId());
                message.setId(UUID.randomUUID());
            }
            responseListeners.put(message.getId(), listener);
        }
    }

    private Object invokeNode(CloudSession session, Message message) {
        return invokeNode(session, message,
                SystemProperties.getLong(SystemProperties.Cloud.Orchestrator.INVOKE_TIMEOUT));
    }

    private Object invokeNode(CloudSession session, Message message, Long timeout) {
        ResponseListener responseListener = new ResponseListener(timeout);
        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG),
                "Sending invokeNode message: %s", message.getId().toString());
        registerListener(message, responseListener);
        sendMessageToNode(session, message);
        return responseListener.getResponse(message);
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
            CloudClient client;
            try {
                client = new CloudClient(serviceEndPoint.getGatewayAddress(), serviceEndPoint.getGatewayPort());
                NetService.getInstance().registerConsumer(client);
            } catch (Exception ex) {
                throw new RuntimeException("Unable to connect with service: " + serviceEndPoint.getName(), ex);
            }
            try {
                if (client.waitForConnect()) {
                    ResponseListener responseListener = new ResponseListener(timeout);
                    registerListener(message, responseListener);
                    try {
                        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG),
                                "Sending invoke service message: '%s' %s", message.getClass().getName(), message.getId().toString());
                        client.send(message);
                    } catch (Exception ex) {
                        throw new RuntimeException("Unable to send message", ex);
                    }
                    result = responseListener.getResponse(message);
                } else {
                    throw new RuntimeException("Connection timeout with service: " + serviceEndPoint.getName());
                }
            } finally {
                try {
                    client.disconnect();
                } catch (Exception ex){}
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
                addLocalObject(distributedLock, List.of(thisNode.getId()), List.of(), System.currentTimeMillis(), path);
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
        for (ServiceEndPoint serviceEndPoint : endPoints.values()) {
            invokeService(serviceEndPoint.getId(), eventMessage);
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
                        addLocalObject(distributedLayer, List.of(thisNode.getId()), List.of(thisServiceEndPoint.getId()),
                                System.currentTimeMillis(), path);
                    } else {
                        addRemoteObject(distributedLayer, List.of(), List.of(), System.currentTimeMillis(), path);
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
    }

    public void publishPlugin(byte[] jarFile) {
        PublishPluginMessage publishPluginMessage = new PublishPluginMessage();
        publishPluginMessage.setJarFile(jarFile);
        publishPluginMessage.setId(UUID.randomUUID());
        publishPluginMessage.setSessionId(ServiceSession.getCurrentIdentity().getId());
        for (CloudSession session : sessionByNode.values()) {
            sendMessageToNode(session, publishPluginMessage);
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

        UUID serviceEndPointId = distributedLayer.getServiceToInvoke();
        if(serviceEndPointId != null) {
                result = (O) invokeService(serviceEndPointId, layerInvokeMessage);
        } else {
            throw new RuntimeException("Route not found to the layer: " + distributedLayer.getLayerInterface().getName() + "@" + distributedLayer.getLayerName());
        }

        return result;
    }

    /**
     * This method is called when a layer invoke message incoming.
     * @param sessionId Is of the session that invoke the remote layer.
     * @param parameterTypes Array with all the parameter types.
     * @param parameters Array with all the parameter instances.
     * @param methodName Name of the method.
     * @param path Path to found the layer implementation.
     * @return Return value of the layer invoke.
     */
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

        addLocalObject(object, nodeIds, List.of(), timestamp, path);

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

    private void addLocalObject(Object object, List<UUID> nodes, List<UUID> serviceEndPoints, Long timestamp, Object... path) {
        sharedStore.addLocalObject(object, nodes, serviceEndPoints, timestamp, path);
        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Local leaf added: %s", Arrays.toString(path));
    }

    private void addRemoteObject(Object object, List<UUID> nodes, List<UUID> serviceEndPoints, Long timestamp, Object... path) {
        sharedStore.addRemoteObject(object, nodes, serviceEndPoints, timestamp, path);
        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Remote leaf added: %s", Arrays.toString(path));
    }

    private Collection<JoinableMap> getNodesAsJoinableMap() {
        Collection<JoinableMap> result = new ArrayList<>();
        for(Node node : nodes) {
            result.add(new JoinableMap(Introspection.toMap(node)));
        }
        return result;
    }

    private Collection<JoinableMap> getServiceAsJoinableMap() {
        Collection<JoinableMap> result = new ArrayList<>();
        for(ServiceEndPoint serviceEndPoint : endPoints.values()) {
            result.add(new JoinableMap(Introspection.toMap(serviceEndPoint)));
        }
        return result;
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
                    throw new RuntimeException("Remote exception", responseMessage.getThrowable());
                } else {
                    result = responseMessage.getValue();
                }
            } else {
                throw new RuntimeException("Remote invocation timeout, message id: " + message.getId().toString());
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
        private String hash;

        public DistributedLayerInvokerFilter(String name, Class[] parameterTypes) {
            this.name = name;
            this.parameterTypes = parameterTypes;

            hash = name;
            if(parameterTypes != null) {
                for (Class type : parameterTypes) {
                    hash += type.getName();
                }
            }
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

        @Override
        public String getName() {
            return hash;
        }
    }

    private enum ReorganizationAction {

        CONNECT,

        DISCONNECT,

        TIME

    }

    public static final class SystemCloudNodeReadableImplementation extends Layer implements ReadRowsLayerInterface {

        public SystemCloudNodeReadableImplementation() {
            super(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisNode.READABLE_LAYER_IMPLEMENTATION_NAME));
        }

        @Override
        public Collection<JoinableMap> readRows(Queryable queryable) {
            return queryable.evaluate(getInstance().getNodesAsJoinableMap());
        }
    }

    public static final class SystemCloudServiceReadableImplementation extends Layer implements ReadRowsLayerInterface {

        public SystemCloudServiceReadableImplementation() {
            super(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.READABLE_LAYER_IMPLEMENTATION_NAME));
        }

        @Override
        public Collection<JoinableMap> readRows(Queryable queryable) {
            return queryable.evaluate(getInstance().getServiceAsJoinableMap());
        }
    }
}
