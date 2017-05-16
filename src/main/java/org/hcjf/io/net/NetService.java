package org.hcjf.io.net;

import org.hcjf.io.net.ssl.SSLHelper;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceThread;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * This class implements a service that provide an
 * up-level interface to open tcp and udp connections like a
 * server side or client side.
 * @author javaito
 */
public final class NetService extends Service<NetServiceConsumer> {

    private static final String NET_SERVICE_NAME = "Net service";
    public static final String NET_SERVICE_LOG_TAG = "NET_SERVICE";

    private static final NetService instance;

    static {
        instance = new NetService(NET_SERVICE_NAME);
    }

    private final List<ServerSocketChannel> tcpServers;
    private final Map<NetSession, SelectableChannel> channels;
    private final Map<SelectableChannel, Set<NetSession>> sessionsByChannel;

    private DatagramChannel udpServer;
    private final Map<NetSession, SocketAddress> addresses;
    private final Map<SocketAddress, Set<NetSession>> sessionsByAddress;

    private final Map<SelectableChannel, Long> lastWrite;
    private final Map<SelectableChannel, Queue<NetPackage>> outputQueue;
    private final Map<Integer, Boolean> portMultiSessionChannel;

    private final Set<NetSession> sessions;
    private final List<NetServiceConsumer> consumers;
    private final Map<NetSession, SSLHelper> sslHelpers;

    private Selector selector;
    private final Object selectorMonitor;

    private final Timer timer;
    private boolean creationTimeoutAvailable;
    private long creationTimeout;

    private boolean shuttingDown;
    private boolean running;

    private NetService(String serviceName) {
        super(serviceName, 2);

        this.timer = new Timer();
        this.selectorMonitor = new Object();

        this.creationTimeoutAvailable = SystemProperties.getBoolean(SystemProperties.Net.CONNECTION_TIMEOUT_AVAILABLE);
        this.creationTimeout = SystemProperties.getLong(SystemProperties.Net.CONNECTION_TIMEOUT);
        if(creationTimeoutAvailable && creationTimeout <= 0){
            throw new IllegalArgumentException("Illegal creation timeout value: " + creationTimeout);
        }

        lastWrite = Collections.synchronizedMap(new HashMap<SelectableChannel, Long>());
        outputQueue = Collections.synchronizedMap(new HashMap<SelectableChannel, Queue<NetPackage>>());
        portMultiSessionChannel = Collections.synchronizedMap(new HashMap<Integer, Boolean>());
        tcpServers = Collections.synchronizedList(new ArrayList<ServerSocketChannel>());
        channels = Collections.synchronizedMap(new TreeMap<NetSession, SelectableChannel>());
        sessionsByChannel = Collections.synchronizedMap(new HashMap<SelectableChannel, Set<NetSession>>());
        sessionsByAddress = Collections.synchronizedMap(new HashMap<SocketAddress, Set<NetSession>>());
        sessions = Collections.synchronizedSet(new TreeSet<NetSession>());
        consumers = Collections.synchronizedList(new ArrayList<NetServiceConsumer>());
        sslHelpers = Collections.synchronizedMap(new HashMap<>());
        addresses = Collections.synchronizedMap(new HashMap<NetSession, SocketAddress>());
    }

    /**
     * Return the unique instance of the service.
     * @return Instance of the service.
     */
    public static final NetService getInstance() {
        return instance;
    }

    /**
     * This method will be called immediately after
     * of the execution of the service's constructor method
     */
    @Override
    protected void init() {
        try {
            setSelector(SelectorProvider.provider().openSelector());
            running = true;

            fork(() -> runNetService());
        } catch (IOException ex) {
            Log.e(NET_SERVICE_LOG_TAG, "Unable to init net service $1", ex, this);
        }
    }

    /**
     * This method will be called immediately after the static
     * method 'shutdown' of the class has been called.
     * @param stage Shutdown stage.
     */
    @Override
    protected void shutdown(ShutdownStage stage) {
        switch (stage) {
            case START: {
                shuttingDown = true;
                getSelector().wakeup();
                break;
            }
            case END: {
                for(NetSession session : getSessions()){
                    disconnect(session, "");
                }

                running = false;
                getSelector().wakeup();
                break;
            }
        }
    }

    /**
     * Shutdown all the service consumer executors.
     * @param executor Service consumer executor.
     */
    @Override
    protected void shutdownRegisteredExecutor(ThreadPoolExecutor executor) {
        int activityCount = 0;
        while(activityCount < 3) {
            if(executor.getActiveCount() == 0) {
                activityCount++;
            } else {
                activityCount = 0;
            }
            try {
                Thread.sleep(SystemProperties.getLong(
                        SystemProperties.Service.SHUTDOWN_TIME_OUT));
            } catch (InterruptedException e) {
            }
        }

        executor.shutdown();
        while(!executor.isTerminated()) {
            try {
                Thread.sleep(SystemProperties.getLong(
                        SystemProperties.Service.SHUTDOWN_TIME_OUT));
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * This method register the consumer in the service.
     * @param consumer Consumer.
     * @throws NullPointerException If the consumer is null.
     * @throws IllegalArgumentException If the consumer is not a NetClient instance
     * of a NetServer instance.
     * @throws RuntimeException With a IOException like a cause.
     */
    @Override
    public final void registerConsumer(NetServiceConsumer consumer) {

        if(consumer == null) {
            throw new NullPointerException("Net consumer null");
        }

        boolean illegal = false;
        try {
            switch (consumer.getProtocol()) {
                case TCP: case TCP_SSL: {
                    if (consumer instanceof NetServer) {
                        registerTCPNetServer((NetServer) consumer);
                    } else if (consumer instanceof NetClient) {
                        registerTCPNetClient((NetClient) consumer);
                    } else {
                        illegal = true;
                    }
                    break;
                }
                case UDP: {
                    if (consumer instanceof NetServer) {
                        registerUDPNetServer((NetServer) consumer);
                    } else if (consumer instanceof NetClient) {
                        registerUDPNetClient((NetClient) consumer);
                    } else {
                        illegal = true;
                    }
                    break;
                }
            }
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }

        if(illegal) {
            throw new IllegalArgumentException("Is not a legal consumer.");
        }

        consumer.setService(this);
    }

    @Override
    public void unregisterConsumer(NetServiceConsumer consumer) {

    }

    /**
     * This method registers a TCP server service.
     * @param server TCP Server.
     */
    private void registerTCPNetServer(NetServer server) throws IOException {
        ServerSocketChannel tcpServer = ServerSocketChannel.open();
        tcpServer.configureBlocking(false);
        InetSocketAddress tcpAddress = new InetSocketAddress(server.getPort());
        tcpServer.socket().bind(tcpAddress);
        registerChannel(tcpServer, SelectionKey.OP_ACCEPT, server);
        tcpServers.add(tcpServer);
        portMultiSessionChannel.put(server.getPort(), server.isMultiSession());
    }

    /**
     * This method registers a TCP client service.
     * @param client TCP Client.
     */
    private void registerTCPNetClient(NetClient client) throws IOException {
        final SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(client.getHost(), client.getPort()));
        registerChannel(channel, SelectionKey.OP_CONNECT | SelectionKey.OP_READ, client);
    }

    /**
     * This method registers a UDP server service.
     * @param server UDP Server.
     */
    private void registerUDPNetServer(NetServer server) throws IOException {
        udpServer = DatagramChannel.open();
        udpServer.configureBlocking(false);
        InetSocketAddress udpAddress = new InetSocketAddress(server.getPort());
        udpServer.socket().bind(udpAddress);
        registerChannel(udpServer, SelectionKey.OP_READ, server);
        portMultiSessionChannel.put(server.getPort(), server.isMultiSession());
    }

    /**
     * This method registers a UDP client service.
     * @param client UDP Client.
     */
    private void registerUDPNetClient(NetClient client) throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        InetSocketAddress address = new InetSocketAddress(client.getHost(), client.getPort());
        channel.connect(address);

        sessions.add(client.getSession());
        addresses.put(client.getSession(), address);
        sessionsByAddress.put(channel.getRemoteAddress(), createSessionSet(client.getSession()));
        portMultiSessionChannel.put(channel.socket().getLocalPort(), false);

        registerChannel(channel, SelectionKey.OP_READ, client);
    }

    /**
     * Return an unmodificable add with all the sessions created en the service.
     * @return Set with se sessions.
     */
    public final Set<NetSession> getSessions() {
        return Collections.unmodifiableSet(sessions);
    }

    /**
     * Return the net selector.
     * @return Net selector.
     */
    private Selector getSelector() {
        return selector;
    }

    /**
     * Set the net selector.
     * @param selector Net selector.
     */
    private void setSelector(Selector selector) {
        this.selector = selector;
    }

    /**
     * Return a value to indicate if the session creation timeout is available ot not.
     * @return True if it is available.
     */
    private boolean isCreationTimeoutAvailable() {
        return creationTimeoutAvailable;
    }

    /**
     * Return the value in milliseconds that the server wait before destroy the channel if
     * it has not session assigned.
     * @return Session creation timeout.
     */
    private long getCreationTimeout() {
        return creationTimeout;
    }

    /**
     * Return the server timer
     * @return server timer.
     */
    private Timer getTimer() {
        return timer;
    }

    /**
     * Return a boolean to knows if the instance of the java vm is into the
     * shutdown process or not.
     * @return True if the vm is into the shutdown porcess and false in the otherwise.
     */
    public final boolean isShuttingDown() {
        return shuttingDown;
    }

    /**
     * Check if the specific session is active into the sercie.
     * @param session Specific session.
     * @return Return true if the session is active into the
     */
    public final boolean checkSession(NetSession session) {
        boolean result = false;

        SelectableChannel channel = channels.get(session);
        if(channel != null) {
            result = channel.isOpen();
        }

        return result;
    }

    /**
     * Create the correct implementation of the add containing
     * the sessions associated with the same channel
     * @param firstValue First value of the add.
     * @return Return the add implementation.
     */
    private Set<NetSession> createSessionSet(NetSession firstValue) {
        Set<NetSession> result = new TreeSet<>();
        result.add(firstValue);
        return result;
    }

    /**
     * This method blocks the selector to add a new channel to the key system
     * @param channel The new channel to be register
     * @param operation The first channel operation.
     * @param attach Object to be attached into the registered key.
     * @throws ClosedChannelException
     */
    private void registerChannel(SelectableChannel channel, int operation, Object attach) throws ClosedChannelException {
        synchronized(selectorMonitor) {
            getSelector().wakeup();
            channel.register(getSelector(), operation, attach);
        }
    }

    /**
     *
     * @param channel
     * @param data
     * @param event
     * @param source
     * @return
     */
    private NetPackage createPackage(SelectableChannel channel, byte[] data,
                                     NetPackage.ActionEvent event, NetStreamingSource source) {
        NetPackage netPackage;
        String remoteHost;
        String remoteAddress;
        int remotePort;
        int localPort;
        if (channel instanceof SocketChannel) {
            remoteHost = "";//((SocketChannel) channel).socket().getInetAddress().getHostName();
            remoteAddress = "";//((SocketChannel) channel).socket().getInetAddress().getHostAddress();
            remotePort = ((SocketChannel) channel).socket().getPort();
            localPort = ((SocketChannel) channel).socket().getLocalPort();
        } else if (channel instanceof DatagramChannel) {
            remoteHost = ((DatagramChannel) channel).socket().getInetAddress().getHostName();
            remoteAddress = ((DatagramChannel) channel).socket().getInetAddress().getHostAddress();
            remotePort = ((DatagramChannel) channel).socket().getPort();
            localPort = ((DatagramChannel) channel).socket().getLocalPort();
        } else {
            throw new IllegalArgumentException("Unknown channel type");
        }
        if (source == null) {
            netPackage = new DefaultNetPackage(remoteHost, remoteAddress, remotePort,
                    localPort, data, event);
        } else {
            netPackage = new StreamingNetPackage(remoteHost, remoteAddress, remotePort,
                    localPort, data, source);
        }
        return netPackage;
    }

    /**
     * This method put a net package on the output queue of the session.
     * @param session Net session.
     * @param data Data to create the package.
     * @return Return the id of the created package.
     * @throws IOException
     */
    public final NetPackage writeData(NetSession session, byte[] data) throws IOException {
        return writeData(session, data, null);
    }

    /**
     * This method put a net package on the output queue of the session.
     * @param session Net session.
     * @param data Data to create the package.
     * @param source Data source.
     * @return Return the id of the created package.
     * @throws IOException
     */
    public final NetPackage writeData(NetSession session, byte[] data, NetStreamingSource source) throws IOException {
        NetPackage netPackage;
        SelectableChannel channel = channels.get(session);
        if(channel != null) {
            netPackage = createPackage(channel, data, NetPackage.ActionEvent.WRITE, source);
            netPackage.setSession(session);
            outputQueue.get(channel).add(netPackage);
            channel.keyFor(getSelector()).interestOps(SelectionKey.OP_WRITE);
            getSelector().wakeup();
        } else {
            throw new IOException("Unknown session");
        }

        return netPackage;
    }

    /**
     *
     * @param session
     * @param message
     */
    public final void disconnect(NetSession session, String message) {
        SelectableChannel channel = channels.get(session);
        if(channel != null){
            synchronized (channel) {
                if(channels.containsKey(session)) {
                    NetPackage netPackage = createPackage(channel, message.getBytes(), NetPackage.ActionEvent.DISCONNECT, null);
                    netPackage.setSession(session);
                    outputQueue.get(channel).add(netPackage);
                    channel.keyFor(getSelector()).interestOps(SelectionKey.OP_WRITE);
                    getSelector().wakeup();
                }
            }
        }
    }

    /**
     * This method must destroy the channel and remove all the
     * netPackage related.
     * @param channel Channel that will destroy.
     */
    private void destroyChannel(SocketChannel channel) {
        synchronized (channel) {
            Set<NetSession> sessions = sessionsByChannel.remove(channel);
            lastWrite.remove(channel);
            outputQueue.remove(channel);
            List<NetSession> removedSessions = new ArrayList<>();

            try {
                if (sessions != null) {
                    for (NetSession session : sessions) {
                        channels.remove(session);
                        if (session.getConsumer() instanceof NetServer) {
                            NetServer server = (NetServer) session.getConsumer();
                            if (server.isDisconnectAndRemove()) {
                                sessions.remove(session);
                                destroySession(session);
                            }
                        }
                        removedSessions.add(session);
                    }
                }

                if (channel.isConnected()) {
                    channel.close();
                }
            } catch (Exception ex) {
                Log.d(NET_SERVICE_LOG_TAG, "Destroy method exception", ex);
            }
        }
    }

    /**
     * This method updates the linking information  a channel with a particular session
     * @param oldChannel Obsolete channel.
     * @param newChannel New channel.
     */
    private void updateChannel(SocketChannel oldChannel, SocketChannel newChannel) {
        Set<NetSession> sessions = sessionsByChannel.remove(oldChannel);

        try {
            if (oldChannel.isConnected()) {
                oldChannel.finishConnect();
                oldChannel.close();
            }
        } catch (Exception ex) {
        } finally {
            for(NetSession session : sessions) {
                channels.put(session, newChannel);
            }
        }

        sessionsByChannel.put(newChannel, sessions);
        outputQueue.put(newChannel, outputQueue.remove(oldChannel));
        lastWrite.put(newChannel, lastWrite.remove(oldChannel));
    }

    /**
     * Indicates if the session is connected or not
     * @param session Session
     * @return Return true if the session is connected and false in the other case.
     */
    public final boolean isConnected(NetSession session){
        return channels.containsKey(session);
    }

    /**
     * This method call the method to create the session implemented en the
     * instance of the consumer.
     * @param consumer Net consumer.
     * @param netPackage Net package.
     * @return Net session from the consumer.
     * @throws IllegalArgumentException If the consumer is not instance of org.hcjf.io.net.NetServer or org.hcjf.io.net.NetClient
     */
    private NetSession getSession(NetServiceConsumer consumer, NetPackage netPackage){
        NetSession result;

        if(consumer instanceof NetServer) {
            result = ((NetServer)consumer).createSession(netPackage);
        } else if(consumer instanceof NetClient) {
            result = ((NetClient)consumer).getSession();
        } else {
            throw new IllegalArgumentException("The service consumer must be instance of org.hcjf.io.net.NetServer or org.hcjf.io.net.NetClient.");
        }

        return result;
    }

    /**
     * This method destroy the net session.
     * @param session Net session.
     */
    private void destroySession(NetSession session) {
        session.getConsumer().destroySession(session);
    }

    /**
     * This method is the body of the net service.
     */
    public final void runNetService() {
        try {
            try {
                Thread.currentThread().setName(NET_SERVICE_LOG_TAG);
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            } catch(SecurityException ex){}

            boolean removeKey;
            while(running){
                //Select the next schedule key or sleep if the aren't any key
                //to select.
                getSelector().select();

                Iterator selectedKeys;
                synchronized(selectorMonitor) {
                    selectedKeys = getSelector().selectedKeys().iterator();
                }
                while (selectedKeys.hasNext()) {
                    final SelectionKey key = (SelectionKey) selectedKeys.next();

                    //This flag is to indicate whether the key has to be removed once processed
                    removeKey = true;

                    if(key.isValid()){

                        try {
                            final NetServiceConsumer consumer = (NetServiceConsumer) key.attachment();
                            //If the kind of key is acceptable or connectable then
                            //the processing do over this thread in the other case
                            //the processing is delegated to the thread pool
                            if(key.isAcceptable()){
                                accept(key.channel(), (NetServer) consumer);
                            } else if(key.isConnectable()) {
                                connect(key.channel(), (NetClient) consumer);
                            } else {
                                final SelectableChannel keyChannel = key.channel();
                                if(keyChannel != null && key.channel().isOpen()) {
                                    if(key.isValid()) {
                                        try {
                                            fork(() -> {
                                                synchronized (keyChannel) {
                                                    try {
                                                        if (key.isValid()) {
                                                            if (key.isReadable()) {
                                                                read(keyChannel, consumer);
                                                            } else if (key.isWritable()) {
                                                                write(keyChannel, consumer);
                                                                if (consumer instanceof NetClient) {
                                                                    read(keyChannel, consumer);
                                                                }
                                                            }
                                                        }
                                                    } catch (Exception ex) {
                                                        Log.d(NET_SERVICE_LOG_TAG, "Internal IO thread exception", ex);
                                                    } finally {
                                                        ((ServiceThread) Thread.currentThread()).setSession(null);
                                                    }
                                                }
                                            }, consumer.getIoExecutor());
                                        } catch (RejectedExecutionException ex){
                                            //Update the flag in order to process the key again
                                            if(key.isValid() && sessionsByChannel.containsKey(keyChannel)) {
                                                removeKey = false;
                                            }
                                        } catch (Exception ex) {
                                            key.cancel();
                                            Log.w(NET_SERVICE_LOG_TAG, "Unable to for key");
                                        }
                                    } else {
                                        key.cancel();
                                    }
                                }
                            }
                        } catch (CancelledKeyException ex){
                            Log.d(NET_SERVICE_LOG_TAG, "Cancelled key");
                        }
                    }

                    if(removeKey) {
                        selectedKeys.remove();
                    }
                }
            }

            try {
                getSelector().close();
            } catch (IOException ex) {
                Log.d(NET_SERVICE_LOG_TAG, "Closing selector...", ex);
            }

            //Close all the servers.
            for(ServerSocketChannel channel : tcpServers){
                try {
                    channel.close();
                } catch (IOException ex) {
                    Log.d(NET_SERVICE_LOG_TAG, "Closing channel...", ex);
                }
            }
        } catch (Exception ex){
            Log.e(NET_SERVICE_LOG_TAG, "Unexpected error", ex);
        }

        Log.d(NET_SERVICE_LOG_TAG, "Net service stopped");
    }

    /**
     * This method finalize the connection process when start a client connection.
     * @param keyChannel Key associated to the connection channel.
     * @param client Net client asociated to the connectable key.
     */
    private void connect(SelectableChannel keyChannel, NetClient client) {
        if(!isShuttingDown()) {
            try {
                SocketChannel channel = (SocketChannel) keyChannel;
                channel.configureBlocking(false);
                channel.socket().setKeepAlive(true);
                channel.socket().setSoTimeout(100);
                channel.finishConnect();
                Map<SocketOption, Object> socketOptions = client.getSocketOptions();
                if(socketOptions != null){
                    for(SocketOption socketOption : socketOptions.keySet()){
                        channel.setOption(socketOption, socketOptions.get(socketOption));
                    }
                }

                sessions.add(client.getSession());
                sessionsByChannel.put(channel, createSessionSet(client.getSession()));
                channels.put(client.getSession(), channel);
                outputQueue.put(channel, new LinkedBlockingQueue<>());
                lastWrite.put(channel, System.currentTimeMillis());
                portMultiSessionChannel.put(channel.socket().getLocalPort(), false);

                if(client.getProtocol().equals(TransportLayerProtocol.TCP_SSL)) {
                    SSLHelper sslHelper = new SSLHelper(client.createSSLEngine(), channel);
                    sslHelpers.put(client.getSession(), sslHelper);
                }

                NetPackage connectionPackage = createPackage(keyChannel, new byte[]{}, NetPackage.ActionEvent.CONNECT, null);
                onAction(connectionPackage, client);
            } catch (Exception ex){
                Log.w(NET_SERVICE_LOG_TAG, "Error creating new client connection.", ex);
            }
        }
    }

    /**
     * This internal method is colled for the main thread when the selector accept
     * an acceptable key to create a new socket with a remote host.
     * This method only will create a socket but without session because the session
     * depends of the communication payload
     * @param keyChannel Select's key.
     */
    private void accept(SelectableChannel keyChannel, NetServer server) {
        if(!isShuttingDown()) {
            try {
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) keyChannel;

                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);

                Map<SocketOption, Object> socketOptions = server.getSocketOptions();
                if(socketOptions != null){
                    for(SocketOption socketOption : socketOptions.keySet()){
                        socketChannel.setOption(socketOption, socketOptions.get(socketOption));
                    }
                }

                //A new readable key is created associated to the channel.
                socketChannel.register(getSelector(), SelectionKey.OP_READ, server);

                if(isCreationTimeoutAvailable()){
                    getTimer().schedule(new ConnectionTimeout(socketChannel), getCreationTimeout());
                }
            } catch (Exception ex){
                Log.w(NET_SERVICE_LOG_TAG, "Error accepting a new connection.", ex);
            }
        }
    }

    /**
     * This method is called from the main thread in order to read data
     * from a particular key.
     * @param keyChannel Readable key from selector.
     */
    private void read(SelectableChannel keyChannel, NetServiceConsumer consumer) {
        if(!isShuttingDown()) {
            if(keyChannel instanceof SocketChannel){
                SocketChannel channel = (SocketChannel) keyChannel;

                //Ger the instance of the current IO thread.
                NetServiceConsumer.NetIOThread ioThread = (NetServiceConsumer.NetIOThread) Thread.currentThread();

                try (ByteArrayOutputStream readData = new ByteArrayOutputStream()) {
                    int readSize;
                    int totalSize = 0;

                    try {
                        //Put all the bytes into the buffer of the IO thread.
                        ioThread.getInputBuffer().rewind();
                        totalSize += readSize = channel.read(ioThread.getInputBuffer());
                        while(readSize > 0){
                            readData.write(ioThread.getInputBuffer().array(), 0, readSize);
                            readData.flush();
                            ioThread.getInputBuffer().rewind();
                            totalSize += readSize = channel.read(ioThread.getInputBuffer());
                        }
                    } catch (IOException ex) {
                        destroyChannel(channel);
                    }

                    if(totalSize == -1) {
                        destroyChannel(channel);
                    } else if(readData.size() > 0) {
                        NetPackage netPackage = new DefaultNetPackage(
                                "",
                                "",
                                channel.socket().getPort(), channel.socket().getLocalPort(),
                                readData.toByteArray(), NetPackage.ActionEvent.READ);

                        NetSession session = null;
                        Set<NetSession> sessions = sessionsByChannel.get(channel);
                        if(sessions != null && !portMultiSessionChannel.get(channel.socket().getLocalPort())) {
                            session = sessions.iterator().next();
                        }

                        if(session == null){
                            session = getSession(consumer, netPackage);
                        }

                        //If there an't any session the channel must be destroyed.
                        if(session == null){
                            destroyChannel(channel);
                        } else {
                            //Here the session is linked with the current thread
                            ((ServiceThread)Thread.currentThread()).setSession(session);

                            netPackage.setSession(session);
                            if(!sessionsByChannel.containsKey(channel)){
                                if(channels.containsKey(session)){
                                    updateChannel((SocketChannel) channels.remove(session), channel);
                                } else {
                                    sessionsByChannel.put(channel, createSessionSet(session));
                                    outputQueue.put(channel, new LinkedBlockingQueue<>());
                                    lastWrite.put(channel, System.currentTimeMillis());
                                    channels.put(session, channel);
                                }
                            } else if(portMultiSessionChannel.get(channel.socket().getLocalPort())) {
                                sessionsByChannel.get(channel).add(session);
                                channels.put(session, channel);
                            }

                            if(consumer.getProtocol().equals(TransportLayerProtocol.TCP_SSL)) {
                                netPackage = sslHelpers.get(session).read(netPackage);
                            }
                            onAction(netPackage, consumer);
                        }
                    }
                } catch (Exception ex){
                    Log.e(NET_SERVICE_LOG_TAG, "Net service read exception, on TCP context", ex);
                    destroyChannel(channel);
                }
            } else if(keyChannel instanceof DatagramChannel){
                DatagramChannel channel = (DatagramChannel) keyChannel;

                //Ger the instance of the current IO thread.
                NetServiceConsumer.NetIOThread ioThread = (NetServiceConsumer.NetIOThread) Thread.currentThread();

                try {
                    ByteArrayOutputStream readData = new ByteArrayOutputStream();
                    ioThread.getInputBuffer().clear();
                    ioThread.getInputBuffer().rewind();

                    InetSocketAddress address = (InetSocketAddress) channel.receive(ioThread.getInputBuffer());
                    readData.write(ioThread.getInputBuffer().array(), 0, ioThread.getInputBuffer().position());

                    if(address != null){
                        NetPackage netPackage = new DefaultNetPackage(
                                channel.socket().getInetAddress().getHostName(),
                                channel.socket().getInetAddress().getHostAddress(),
                                channel.socket().getPort(), channel.socket().getLocalPort(),
                                readData.toByteArray(), NetPackage.ActionEvent.READ);

                        NetSession session = null;
                        Set<NetSession> sessions = sessionsByAddress.get(address);
                        if(sessions != null && !portMultiSessionChannel.get(channel.socket().getLocalPort())) {
                            session = sessions.iterator().next();
                        }

                        if(session == null){
                            session = getSession(consumer, netPackage);
                        }

                        if(session != null){
                            //Here the session is linked with the current thread
                            ((ServiceThread)Thread.currentThread()).setSession(session);

                            netPackage.setSession(session);
                            if(addresses.containsKey(session)) {
                                addresses.put(session, address);
                                if (sessions != null) {
                                    sessions.add(session);
                                } else if (portMultiSessionChannel.get(channel.socket().getLocalPort())) {
                                    sessionsByAddress.put(address, createSessionSet(session));
                                }
                            }

                            if(!channels.containsKey(session)){
                                channels.put(session, channel);
                            }
                            if(!outputQueue.containsKey(channel)){
                                outputQueue.put(channel, new LinkedBlockingQueue<>());
                                lastWrite.put(channel, System.currentTimeMillis());
                            }

                            if(readData.size() > 0) {
                                onAction(netPackage, consumer);
                            }
                        }
                    }
                } catch (Exception ex){
                    Log.e(NET_SERVICE_LOG_TAG, "Net service read exception, on UDP context", ex);
                }
            }
        }
    }

    /**
     * This method take the output queue associated to the consumer and write over the
     * session channel all the packages.
     * If one of the packages is a disconnection package then the channel is closed and
     * the rest of the packages are discarded.
     * @param channel Session channel.
     * @param consumer Net service consumer.
     */
    private void write(SelectableChannel channel, NetServiceConsumer consumer) {
        NetServiceConsumer.NetIOThread ioThread = (NetServiceConsumer.NetIOThread) Thread.currentThread();
        try {
            Queue<NetPackage> queue = outputQueue.get(channel);

            if(queue != null) {
                lastWrite.put(channel, System.currentTimeMillis());
                boolean stop = false;

                while(!queue.isEmpty() && !stop) {
                    NetPackage netPackage = queue.poll();
                    if(netPackage == null) {
                        break;
                    }
                    NetSession session = netPackage.getSession();

                    switch(netPackage.getActionEvent()) {
                        case WRITE:
                        case STREAMING: {
                            try {
                                if(!session.isLocked()){

                                    if(consumer.getProtocol().equals(TransportLayerProtocol.TCP_SSL)) {
                                        netPackage = sslHelpers.get(session).write(netPackage);
                                    } else {

                                        byte[] byteData = netPackage.getPayload();
                                        if (byteData.length == 0) {
                                            Log.d(NET_SERVICE_LOG_TAG, "Empty write data");
                                        }
                                        int begin = 0;
                                        int length = (byteData.length - begin) > session.getConsumer().getOutputBufferSize() ?
                                                session.getConsumer().getOutputBufferSize() : byteData.length - begin;

                                        while (begin < byteData.length) {
                                            ioThread.getOutputBuffer().limit(length);
                                            ioThread.getOutputBuffer().put(byteData, begin, length);
                                            ioThread.getOutputBuffer().rewind();

                                            if (channel instanceof SocketChannel) {
                                                int writtenData = 0;
                                                while (writtenData < length) {
                                                    writtenData += ((SocketChannel) channel).write(ioThread.getOutputBuffer());
                                                }
                                            } else if (channel instanceof DatagramChannel) {
                                                SocketAddress address = addresses.get(netPackage.getSession());
                                                if (sessionsByAddress.get(address).equals(netPackage.getSession())) {
                                                    ((DatagramChannel) channel).send(ioThread.getOutputBuffer(), address);
                                                }
                                            }

                                            ioThread.getOutputBuffer().rewind();
                                            begin += length;
                                            length = (byteData.length - begin) > session.getConsumer().getOutputBufferSize() ?
                                                    session.getConsumer().getOutputBufferSize() : byteData.length - begin;
                                        }
                                    }

                                    if(netPackage.getActionEvent().equals(NetPackage.ActionEvent.STREAMING) && channel instanceof SocketChannel){
                                        streamingInit((SocketChannel)channel, (StreamingNetPackage) netPackage);
                                    } else {
                                        netPackage.setPackageStatus(NetPackage.PackageStatus.OK);
                                    }
                                } else {
                                    netPackage.setPackageStatus(NetPackage.PackageStatus.REJECTED_SESSION_LOCK);
                                }
                            } catch (Exception ex){
                                netPackage.setPackageStatus(NetPackage.PackageStatus.IO_ERROR);
                                throw ex;
                            } finally {
                                if(netPackage.getActionEvent().equals(NetPackage.ActionEvent.WRITE)) {
                                    onAction(netPackage, consumer);
                                }
                            }

                            //Change the key operation to finish write loop
                            channel.keyFor(getSelector()).interestOps(SelectionKey.OP_READ);

                            break;
                        }
                        case DISCONNECT: {
                            if(channel instanceof SocketChannel) {
                                destroyChannel((SocketChannel) channel);
                            } else if(channel instanceof DatagramChannel && !channel.equals(udpServer)) {
                                outputQueue.remove(channel);
                                lastWrite.remove(channel);
                                channels.remove(netPackage.getSession());
                                if(netPackage.getSession().getConsumer() instanceof NetServer) {
                                    NetServer server = (NetServer) netPackage.getSession().getConsumer();
                                    if (server.isDisconnectAndRemove()) {
                                        sessions.remove(netPackage.getSession());
                                        destroySession(session);
                                    }
                                }
                            }
                            onAction(netPackage, consumer);
                            stop = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex){
            Log.d(NET_SERVICE_LOG_TAG, "Write global thread exception", ex);
        } finally {
            ioThread.getOutputBuffer().clear();
            ioThread.getOutputBuffer().rewind();
        }
    }

    /**
     * Start the streaming process.
     * @param channel Represents the client side of the pipe.
     * @param netPackage Net package.
     */
    private void streamingInit(SocketChannel channel, StreamingNetPackage netPackage) {
        netPackage.getSource().init(this, channel, netPackage);
        synchronized(netPackage.getSession()){
            netPackage.getSession().lock();
            fork(netPackage.getSource());
        }
    }

    /**
     * This method is called when the streaming process done.
     * @param netPackage Net Package.
     */
    public final void streamingDone(NetPackage netPackage) {
        synchronized(netPackage.getSession()){
            netPackage.getSession().unlock();
        }

        netPackage.getSession().getConsumer().onWrite(netPackage);
    }

    /**
     * This method put all the action events in a queue by session and then start a
     * new thread to notify all the consumers
     * @param netPackage Received data.
     * @param consumer Consumer associated to the session.
     */
    private void onAction(final NetPackage netPackage, final NetServiceConsumer consumer){
        try {
            switch (netPackage.getActionEvent()) {
                case CONNECT: consumer.onConnect(netPackage); break;
                case DISCONNECT: consumer.onDisconnect(netPackage); break;
                case READ: consumer.onRead(netPackage); break;
                case WRITE: consumer.onWrite(netPackage); break;
            }
        } catch (Exception ex){
            Log.e(NET_SERVICE_LOG_TAG, "Action consumer exception", ex);
        }
    }

    /**
     * Timer task to destroy the channel if has not session assigned.
     */
    private class ConnectionTimeout extends TimerTask {

        private final SocketChannel channel;

        public ConnectionTimeout(SocketChannel channel) {
            this.channel = channel;
        }

        /**
         * Destroy channel
         */
        @Override
        public void run() {
            fork(() -> {
                if(!sessionsByChannel.containsKey(channel)){
                    try {
                        destroyChannel(channel);
                    } catch (Exception ex){}
                }
            });
        }

    }

    /**
     * Transport layer protocols.
     */
    public enum TransportLayerProtocol {

        TCP,

        TCP_SSL,

        UDP
    }

}
