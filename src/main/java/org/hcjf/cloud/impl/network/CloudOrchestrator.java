package org.hcjf.cloud.impl.network;

import org.hcjf.cloud.impl.messages.*;
import org.hcjf.cloud.impl.objects.*;
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
    private Map<UUID, CloudSession> sessionByNode;
    private Set<Node> sortedNodes;
    private Map<UUID, Node> waitingAck;
    private Map<UUID, ResponseListener> responseListeners;

    private CloudWagonMessage wagonMessage;
    private Object wagonMonitor;
    private Long lastVisit;
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
        wagonLoad = new HashMap<>();

        random = new Random();
        sharedStore = new DistributedTree(Strings.EMPTY_STRING);

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
            sessionByNode.put(node.getId(), session);
            sortedNodes.add(node);
            printNodes();
        }
    }

    private void removeNode(Node node) {
        synchronized (sessionByNode) {
            sessionByNode.remove(node.getId());
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
                    if (!sessionByNode.containsKey(node.getId()) && !node.getStatus().equals(Node.Status.CONNECTED)) {
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
                            Map<String,List<Message>> load = getWagonLoad();
                            wagonMessage.getMessages().putAll(load);
                            sendMessage(sessionByNode.get(nextDestination.getId()), wagonMessage);
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
            for(Node node : sortedNodes) {
                if(sessionByNode.get(node.getId()).equals(session)) {
                    removeNode(node);
                    break;
                }
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

                for(Message messageOfWagon : wagonMessage.getMessages().remove(thisNode.getId().toString())) {
                    incomingMessage(session, messageOfWagon);
                }
            }
        } else if(message instanceof HidePathMessage) {
            removePath(((HidePathMessage)message).getPath());
        } else if(message instanceof PublishPathMessage) {
            addPath(((PublishPathMessage)message).getPath());
        } else if(message instanceof PublishObjectMessage) {
            PublishObjectMessage publishObjectMessage = (PublishObjectMessage) message;
            if (publishObjectMessage.getValue() != null) {
                addObject(publishObjectMessage.getValue(), publishObjectMessage.getTimestamp(), publishObjectMessage.getPath());
            } else {
                addObject(publishObjectMessage.getTimestamp(), publishObjectMessage.getNodes(), publishObjectMessage.getPath());
            }
        } else if(message instanceof InvokeMessage) {
            InvokeMessage invokeMessage = (InvokeMessage) message;

            ResponseMessage responseMessage = new ResponseMessage(invokeMessage.getId());
            Object object = sharedStore.getInstance(invokeMessage.getPath());
            if(object instanceof RemoteLeaf.RemoteValue) {
                responseMessage.setNotFound(true);
            } else {
                responseMessage.setValue(object);
                responseMessage.setNotFound(false);
            }
            sendMessage(session, responseMessage);
        } else if(message instanceof LockMessage) {
            LockMessage lockMessage = (LockMessage) message;
            ResponseMessage responseMessage = new ResponseMessage(lockMessage.getId());
            responseMessage.setValue(distributedLock(lockMessage.getTimestamp(), lockMessage.getPath()));
            sendMessage(session, responseMessage);
        } else if(message instanceof UnlockMessage) {
            UnlockMessage unlockMessage = (UnlockMessage) message;
            distributedUnlock(unlockMessage.getPath());
        } else if(message instanceof ResponseMessage) {
            Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Incoming response message: %s", message.getId().toString());
            ResponseListener responseListener = responseListeners.get(message.getId());
            if(responseListener != null) {
                responseListener.setMessage((ResponseMessage) message);
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

    private Object invoke(CloudSession session, Message message) {
        ResponseListener responseListener = new ResponseListener();
        Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Sending invoke message: %s", message.getId().toString());
        responseListeners.put(message.getId(), responseListener);
        sendMessage(session, message);
        return responseListener.getResponse(message);
    }

    private DistributedLock getDistributedLock(Object... path) {
        DistributedLock distributedLock;
        synchronized (sharedStore) {
            distributedLock = (DistributedLock) sharedStore.getInstance(path);
            if (distributedLock == null) {
                distributedLock = new DistributedLock();
                distributedLock.setStatus(DistributedLock.Status.UNLOCKED);
                addObject(distributedLock, System.currentTimeMillis(), path);
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
            locked = true;
            for (CloudSession session : sessionByNode.values()) {
                if (!(locked = locked & (boolean) invoke(session, lockMessage))) {
                    break;
                }
            }
            if (locked) {
                distributedLock.setStatus(DistributedLock.Status.LOCKED);
            } else {
                distributedLock.setStatus(DistributedLock.Status.WAITING);
                try {
                    synchronized (distributedLock) {
                        distributedLock.wait();
                    }
                } catch (InterruptedException e) { }
            }
        }
    }

    private boolean distributedLock(Long timestamp, Object... path) {
        boolean result;
        DistributedLock distributedLock = getDistributedLock(path);
        synchronized (distributedLock) {
            result = distributedLock.getStatus().equals(DistributedLock.Status.UNLOCKED) ||
                    distributedLock.getTimestamp() > timestamp;
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
        for (CloudSession session : sessionByNode.values()) {
            sendMessage(session, unlockMessage);
        }
    }

    public synchronized void distributedUnlock(Object... path) {
        DistributedLock distributedLock = getDistributedLock(path);
        synchronized (distributedLock) {
            distributedLock.notifyAll();
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

    public void publishPath(Object... path) {
        synchronized (sharedStore) {
            addPath(path);
            PublishPathMessage publishPathMessage = new PublishPathMessage();
            publishPathMessage.setPath(path);
            for(CloudSession session : sessionByNode.values()) {
                sendMessage(session, publishPathMessage);
            }
        }
    }

    public void publishObject(Object object, Long timestamp, Object... path) {
        addObject(object, timestamp, path);

        fork(() -> {
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

            if(!nodes.isEmpty()) {
                List<UUID> nodeIds = new ArrayList<>();
                nodeIds.add(thisNode.getId());

                int replicationFactor = SystemProperties.getInteger(
                        SystemProperties.Cloud.Orchestrator.REPLICATION_FACTOR);

                for (int i = 0; i < replicationFactor; i++) {
                    nodeIds.add(nodes.get(i).getId());
                }

                int counter = 0;
                for (Node node : nodes) {
                    PublishObjectMessage publishObjectMessage = new PublishObjectMessage(UUID.randomUUID());
                    publishObjectMessage.setPath(path);
                    publishObjectMessage.setTimestamp(timestamp);
                    publishObjectMessage.setNodes(nodeIds);
                    if(counter < replicationFactor) {
                        publishObjectMessage.setValue(object);
                    }
                    sendMessage(sessionByNode.get(node.getId()), publishObjectMessage);
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
                sendMessage(session, hidePathMessage);
            }
        });
    }

    public <O extends Object> O invoke(Object... path) {
        O result = (O) sharedStore.getInstance(path);
        if(result instanceof RemoteLeaf.RemoteValue) {
            InvokeMessage getMessage = new InvokeMessage(UUID.randomUUID());
            getMessage.setPath(path);
            CloudSession session = null;
            Iterator<UUID> ids = ((RemoteLeaf.RemoteValue)result).getNodes().iterator();
            while (ids.hasNext() && session == null) {
                session = sessionByNode.get(ids.next());
            }

            if(session != null) {
                result = (O) invoke(session, getMessage);
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
            Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Local path added: %s", Arrays.toString(path));
            result = true;
        }
        return result;
    }

    private void addObject(Object object, Long timestamp, Object... path) {
        sharedStore.add(object, timestamp, path);
        Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Local leaf added: %s", Arrays.toString(path));
    }

    private void addObject(Long timestamp, List<UUID> nodes, Object... path) {
        sharedStore.add(timestamp, nodes, path);
        Log.i(System.getProperty(SystemProperties.Cloud.LOG_TAG), "Remote leaf added: %s", Arrays.toString(path));
    }

    private final class ResponseListener {

        private ResponseMessage responseMessage;

        public Object getResponse(Message message) {
            Object result = null;
            synchronized (this) {
                if(responseMessage == null) {
                    try {
                        this.wait(SystemProperties.getLong(SystemProperties.Cloud.Orchestrator.INVOKE_TIMEOUT));
                    } catch (InterruptedException e) {
                    }
                }
            }

            if(responseMessage != null) {
                result = responseMessage.getValue();
            }
            responseListeners.remove(message.getId());
            return result;
        }

        public void setMessage(ResponseMessage message) {
            synchronized (this) {
                responseMessage = message;
                notifyAll();
            }
        }
    }
}
