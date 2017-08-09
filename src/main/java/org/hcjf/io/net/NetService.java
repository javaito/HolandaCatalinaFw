package org.hcjf.io.net;

import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceThread;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * This class implements a service that provide an
 * up-level interface to open tcp and udp connections like a
 * server side or client side.
 *
 * @author javaito
 */
public final class NetService extends Service<NetServiceConsumer> {

    private static final NetService instance;

    static {
        instance = new NetService(SystemProperties.get(SystemProperties.Net.SERVICE_NAME));
    }

    private final List<ServerSocketChannel> tcpServers;
    private final Map<NetSession, SelectableChannel> channels;
    private final Map<SelectableChannel, NetSession> sessionsByChannel;

    private DatagramChannel udpServer;
    private final Map<NetSession, SocketAddress> addresses;
    private final Map<SocketAddress, NetSession> sessionsByAddress;

    private final Map<SelectableChannel, Long> lastWrite;
    private final Map<SelectableChannel, Queue<NetPackage>> outputQueue;

    private final Set<NetSession> sessions;
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
        if (creationTimeoutAvailable && creationTimeout <= 0) {
            throw new IllegalArgumentException("Illegal creation timeout value: " + creationTimeout);
        }

        lastWrite = Collections.synchronizedMap(new HashMap<>());
        outputQueue = Collections.synchronizedMap(new HashMap<>());
        tcpServers = Collections.synchronizedList(new ArrayList<>());
        channels = Collections.synchronizedMap(new TreeMap<>());
        sessionsByChannel = Collections.synchronizedMap(new HashMap<>());
        sessionsByAddress = Collections.synchronizedMap(new HashMap<>());
        sessions = Collections.synchronizedSet(new TreeSet<>());
        sslHelpers = Collections.synchronizedMap(new HashMap<>());
        addresses = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * Return the unique instance of the service.
     *
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
            Log.e(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Unable to init net service $1", ex, this);
        }
    }

    /**
     * This method will be called immediately after the static
     * method 'shutdown' of the class has been called.
     *
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
                for (NetSession session : getSessions()) {
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
     *
     * @param executor Service consumer executor.
     */
    @Override
    protected void shutdownRegisteredExecutor(ThreadPoolExecutor executor) {
        int activityCount = 0;
        while (activityCount < 3) {
            if (executor.getActiveCount() == 0) {
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
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(SystemProperties.getLong(
                        SystemProperties.Service.SHUTDOWN_TIME_OUT));
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * This method register the consumer in the service.
     *
     * @param consumer Consumer.
     * @throws NullPointerException     If the consumer is null.
     * @throws IllegalArgumentException If the consumer is not a NetClient instance
     *                                  of a NetServer instance.
     * @throws RuntimeException         With a IOException like a cause.
     */
    @Override
    public final void registerConsumer(NetServiceConsumer consumer) {

        if (consumer == null) {
            throw new NullPointerException("Net consumer null");
        }

        boolean illegal = false;
        try {
            switch (consumer.getProtocol()) {
                case TCP:
                case TCP_SSL: {
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

        if (illegal) {
            throw new IllegalArgumentException("Is not a legal consumer.");
        }

        consumer.setService(this);
    }

    @Override
    public void unregisterConsumer(NetServiceConsumer consumer) {

    }

    /**
     * This method registers a TCP server service.
     *
     * @param server TCP Server.
     */
    private void registerTCPNetServer(NetServer server) throws IOException {
        ServerSocketChannel tcpServer = ServerSocketChannel.open();
        tcpServer.configureBlocking(false);
        InetSocketAddress tcpAddress = new InetSocketAddress(server.getPort());
        tcpServer.socket().bind(tcpAddress);
        registerChannel(tcpServer, SelectionKey.OP_ACCEPT, server);
        tcpServers.add(tcpServer);
    }

    /**
     * This method registers a TCP client service.
     *
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
     *
     * @param server UDP Server.
     */
    private void registerUDPNetServer(NetServer server) throws IOException {
        udpServer = DatagramChannel.open();
        udpServer.configureBlocking(false);
        InetSocketAddress udpAddress = new InetSocketAddress(server.getPort());
        udpServer.socket().bind(udpAddress);
        registerChannel(udpServer, SelectionKey.OP_READ, server);
    }

    /**
     * This method registers a UDP client service.
     *
     * @param client UDP Client.
     */
    private void registerUDPNetClient(NetClient client) throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        InetSocketAddress address = new InetSocketAddress(client.getHost(), client.getPort());
        channel.connect(address);

        sessions.add(client.getSession());
        addresses.put(client.getSession(), address);
        sessionsByAddress.put(channel.getRemoteAddress(), client.getSession());

        registerChannel(channel, SelectionKey.OP_READ, client);
    }

    /**
     * Return an unmodificable add with all the sessions created en the service.
     *
     * @return Set with se sessions.
     */
    public final Set<NetSession> getSessions() {
        return Collections.unmodifiableSet(sessions);
    }

    /**
     * Return the net selector.
     *
     * @return Net selector.
     */
    private Selector getSelector() {
        return selector;
    }

    /**
     * Set the net selector.
     *
     * @param selector Net selector.
     */
    private void setSelector(Selector selector) {
        this.selector = selector;
    }

    /**
     * Return a value to indicate if the session creation timeout is available ot not.
     *
     * @return True if it is available.
     */
    private boolean isCreationTimeoutAvailable() {
        return creationTimeoutAvailable;
    }

    /**
     * Return the value in milliseconds that the server wait before destroy the channel if
     * it has not session assigned.
     *
     * @return Session creation timeout.
     */
    private long getCreationTimeout() {
        return creationTimeout;
    }

    /**
     * Return the server timer
     *
     * @return server timer.
     */
    private Timer getTimer() {
        return timer;
    }

    /**
     * Return a boolean to knows if the instance of the java vm is into the
     * shutdown process or not.
     *
     * @return True if the vm is into the shutdown porcess and false in the otherwise.
     */
    public final boolean isShuttingDown() {
        return shuttingDown;
    }

    /**
     * Check if the specific session is active into the sercie.
     *
     * @param session Specific session.
     * @return Return true if the session is active into the
     */
    public final boolean checkSession(NetSession session) {
        boolean result = false;

        SelectableChannel channel = channels.get(session);
        if (channel != null) {
            result = channel.isOpen();
        }

        return result;
    }

    /**
     * This method blocks the selector to add a new channel to the key system
     *
     * @param channel   The new channel to be register
     * @param operation The first channel operation.
     * @param attach    Object to be attached into the registered key.
     * @throws ClosedChannelException
     */
    private void registerChannel(SelectableChannel channel, int operation, Object attach) throws ClosedChannelException {
        synchronized (selectorMonitor) {
            getSelector().wakeup();
            channel.register(getSelector(), operation, attach);
        }
    }

    /**
     * @param channel
     * @param data
     * @param event
     * @return
     */
    private NetPackage createPackage(SelectableChannel channel, byte[] data, NetPackage.ActionEvent event) {
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

        netPackage = new DefaultNetPackage(remoteHost, remoteAddress, remotePort,
                localPort, data, event);

        return netPackage;
    }

    /**
     * This method put a net package on the output queue of the session.
     *
     * @param session Net session.
     * @param data    Data to create the package.
     * @return Return the id of the created package.
     * @throws IOException Exception of the write operation.
     */
    public final NetPackage writeData(NetSession session, byte[] data) throws IOException {
        NetPackage netPackage;
        SelectableChannel channel = channels.get(session);
        if (channel != null) {
            netPackage = createPackage(channel, data, NetPackage.ActionEvent.WRITE);
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
     * Disconnect a specific session.
     *
     * @param session Session to disconnect.
     * @param message Disconnection message.
     */
    public final void disconnect(NetSession session, String message) {
        SelectableChannel channel = channels.get(session);
        if (channel != null) {
            synchronized (channel) {
                if (channels.containsKey(session)) {
                    NetPackage netPackage = createPackage(channel, message.getBytes(), NetPackage.ActionEvent.DISCONNECT);
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
     *
     * @param channel Channel that will destroy.
     */
    private void destroyChannel(SocketChannel channel) {
        synchronized (channel) {
            NetSession session = sessionsByChannel.remove(channel);
            lastWrite.remove(channel);
            outputQueue.remove(channel);
            if (sslHelpers.containsKey(sessions)) {
                sslHelpers.remove(sessions).close();
            }
            List<NetSession> removedSessions = new ArrayList<>();

            try {
                if (session != null) {
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

                if (channel.isConnected()) {
                    channel.close();
                }
            } catch (Exception ex) {
                Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Destroy method exception", ex);
            }
        }
    }

    /**
     * This method updates the linking information  a channel with a particular session
     *
     * @param oldChannel Obsolete channel.
     * @param newChannel New channel.
     */
    private void updateChannel(SocketChannel oldChannel, SocketChannel newChannel) {
        NetSession session = sessionsByChannel.remove(oldChannel);

        try {
            if (oldChannel.isConnected()) {
                oldChannel.finishConnect();
                oldChannel.close();
            }
        } catch (Exception ex) {
        } finally {
            channels.put(session, newChannel);
        }

        sessionsByChannel.put(newChannel, session);
        outputQueue.put(newChannel, outputQueue.remove(oldChannel));
        lastWrite.put(newChannel, lastWrite.remove(oldChannel));
    }

    /**
     * Indicates if the session is connected or not
     *
     * @param session Session
     * @return Return true if the session is connected and false in the other case.
     */
    public final boolean isConnected(NetSession session) {
        return channels.containsKey(session);
    }

    /**
     * This method call the method to create the session implemented en the
     * instance of the consumer.
     *
     * @param consumer   Net consumer.
     * @param netPackage Net package.
     * @return Net session from the consumer.
     * @throws IllegalArgumentException If the consumer is not instance of org.hcjf.io.net.NetServer or org.hcjf.io.net.NetClient
     */
    private NetSession getSession(NetServiceConsumer consumer, NetPackage netPackage) {
        NetSession result;

        if (consumer instanceof NetServer) {
            result = ((NetServer) consumer).createSession(netPackage);
        } else if (consumer instanceof NetClient) {
            result = ((NetClient) consumer).getSession();
        } else {
            throw new IllegalArgumentException("The service consumer must be instance of org.hcjf.io.net.NetServer or org.hcjf.io.net.NetClient.");
        }

        return result;
    }

    /**
     * This method destroy the net session.
     *
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
                Thread.currentThread().setName(SystemProperties.get(SystemProperties.Net.LOG_TAG));
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            } catch (SecurityException ex) {
            }

            boolean removeKey;
            while (running) {
                //Select the next schedule key or sleep if the aren't any key
                //to select.
                getSelector().select();

                Iterator selectedKeys;
                synchronized (selectorMonitor) {
                    selectedKeys = getSelector().selectedKeys().iterator();
                }
                while (selectedKeys.hasNext()) {
                    final SelectionKey key = (SelectionKey) selectedKeys.next();

                    //This flag is to indicate whether the key has to be removed once processed
                    removeKey = true;

                    if (key.isValid()) {

                        try {
                            final NetServiceConsumer consumer = (NetServiceConsumer) key.attachment();
                            //If the kind of key is acceptable or connectable then
                            //the processing do over this thread in the other case
                            //the processing is delegated to the thread pool
                            if (key.isAcceptable()) {
                                accept(key.channel(), (NetServer) consumer);
                            } else if (key.isConnectable()) {
                                connect(key.channel(), (NetClient) consumer);
                            } else {
                                final SelectableChannel keyChannel = key.channel();
                                if (keyChannel != null && key.channel().isOpen()) {
                                    if (key.isValid()) {
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
                                                        Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Internal IO thread exception", ex);
                                                    } finally {
                                                        ((ServiceThread) Thread.currentThread()).setSession(null);
                                                    }
                                                }
                                            }, consumer.getIoExecutor());
                                        } catch (RejectedExecutionException ex) {
                                            //Update the flag in order to process the key again
                                            if (key.isValid() && sessionsByChannel.containsKey(keyChannel)) {
                                                removeKey = false;
                                            }
                                        } catch (Exception ex) {
                                            key.cancel();
                                            Log.w(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Unable to for key");
                                        }
                                    } else {
                                        key.cancel();
                                    }
                                }
                            }
                        } catch (CancelledKeyException ex) {
                            Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Cancelled key");
                        }
                    }

                    if (removeKey) {
                        selectedKeys.remove();
                    }
                }
            }

            try {
                getSelector().close();
            } catch (IOException ex) {
                Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Closing selector...", ex);
            }

            //Close all the servers.
            for (ServerSocketChannel channel : tcpServers) {
                try {
                    channel.close();
                } catch (IOException ex) {
                    Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Closing channel...", ex);
                }
            }
        } catch (Exception ex) {
            Log.e(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Unexpected error", ex);
        }

        Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Net service stopped");
    }

    /**
     * This method finalize the connection process when start a client connection.
     *
     * @param keyChannel Key associated to the connection channel.
     * @param client     Net client asociated to the connectable key.
     */
    private void connect(SelectableChannel keyChannel, NetClient client) {
        if (!isShuttingDown()) {
            try {
                SocketChannel channel = (SocketChannel) keyChannel;
                channel.configureBlocking(false);
                channel.socket().setKeepAlive(true);
                channel.socket().setSoTimeout(100);
                channel.finishConnect();
                Map<SocketOption, Object> socketOptions = client.getSocketOptions();
                if (socketOptions != null) {
                    for (SocketOption socketOption : socketOptions.keySet()) {
                        channel.setOption(socketOption, socketOptions.get(socketOption));
                    }
                }

                NetSession session = getSession(client, null);
                sessions.add(session);
                sessionsByChannel.put(channel, session);
                channels.put(session, channel);
                outputQueue.put(channel, new LinkedBlockingQueue<>());
                lastWrite.put(channel, System.currentTimeMillis());

                if (client.getProtocol().equals(TransportLayerProtocol.TCP_SSL)) {
                    SSLHelper sslHelper = new SSLHelper(client.getSSLEngine(), channel, client, session);
                    sslHelpers.put(session, sslHelper);
                } else {
                    NetPackage connectionPackage = createPackage(keyChannel, new byte[]{}, NetPackage.ActionEvent.CONNECT);
                    onAction(connectionPackage, client);
                }
            } catch (Exception ex) {
                Log.w(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Error creating new client connection.", ex);
            }
        }
    }

    /**
     * This internal method is colled for the main thread when the selector accept
     * an acceptable key to create a new socket with a remote host.
     * This method only will create a socket but without session because the session
     * depends of the communication payload
     *
     * @param keyChannel Select's key.
     */
    private void accept(SelectableChannel keyChannel, NetServer server) {
        if (!isShuttingDown()) {
            try {
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) keyChannel;

                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);

                Map<SocketOption, Object> socketOptions = server.getSocketOptions();
                if (socketOptions != null) {
                    for (SocketOption socketOption : socketOptions.keySet()) {
                        socketChannel.setOption(socketOption, socketOptions.get(socketOption));
                    }
                }

                NetSession session = getSession(server, null);
                if (channels.containsKey(session)) {
                    updateChannel((SocketChannel) channels.remove(session), socketChannel);
                } else {
                    sessionsByChannel.put(socketChannel, session);
                    outputQueue.put(socketChannel, new LinkedBlockingQueue<>());
                    lastWrite.put(socketChannel, System.currentTimeMillis());
                    channels.put(session, socketChannel);
                }

                if (server.getProtocol().equals(TransportLayerProtocol.TCP_SSL)) {
                    SSLHelper sslHelper = new SSLHelper(server.getSSLEngine(), socketChannel, server, session);
                    sslHelpers.put(session, sslHelper);
                }

                //A new readable key is created associated to the channel.
                socketChannel.register(getSelector(), SelectionKey.OP_READ, server);

                if (isCreationTimeoutAvailable()) {
                    getTimer().schedule(new ConnectionTimeout(socketChannel), getCreationTimeout());
                }
            } catch (Exception ex) {
                Log.w(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Error accepting a new connection.", ex);
            }
        }
    }

    /**
     * This method is called from the main thread in order to read data
     * from a particular key.
     *
     * @param keyChannel Readable key from selector.
     */
    private void read(SelectableChannel keyChannel, NetServiceConsumer consumer) {
        if (!isShuttingDown()) {
            if (keyChannel instanceof SocketChannel) {
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
                        while (readSize > 0) {
                            readData.write(ioThread.getInputBuffer().array(), 0, readSize);
                            readData.flush();
                            ioThread.getInputBuffer().rewind();
                            totalSize += readSize = channel.read(ioThread.getInputBuffer());
                        }
                    } catch (IOException ex) {
                        destroyChannel(channel);
                    }

                    if (totalSize == -1) {
                        destroyChannel(channel);
                    } else if (readData.size() > 0) {
                        NetPackage netPackage = new DefaultNetPackage(
                                "",
                                "",
                                channel.socket().getPort(), channel.socket().getLocalPort(),
                                readData.toByteArray(), NetPackage.ActionEvent.READ);

                        NetSession session = sessionsByChannel.get(channel);
                        //Here the session is linked with the current thread
                        ((ServiceThread) Thread.currentThread()).setSession(session);

                        netPackage.setSession(session);

                        if (consumer.getProtocol().equals(TransportLayerProtocol.TCP_SSL)) {
                            netPackage = sslHelpers.get(session).read(netPackage);
                        }

                        onAction(netPackage, consumer);
                    }
                } catch (Exception ex) {
                    Log.e(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Net service read exception, on TCP context", ex);
                    destroyChannel(channel);
                }
            } else if (keyChannel instanceof DatagramChannel) {
                DatagramChannel channel = (DatagramChannel) keyChannel;

                //Ger the instance of the current IO thread.
                NetServiceConsumer.NetIOThread ioThread = (NetServiceConsumer.NetIOThread) Thread.currentThread();

                try {
                    ByteArrayOutputStream readData = new ByteArrayOutputStream();
                    ioThread.getInputBuffer().clear();
                    ioThread.getInputBuffer().rewind();

                    InetSocketAddress address = (InetSocketAddress) channel.receive(ioThread.getInputBuffer());
                    readData.write(ioThread.getInputBuffer().array(), 0, ioThread.getInputBuffer().position());

                    if (address != null) {
                        NetPackage netPackage = new DefaultNetPackage(
                                channel.socket().getInetAddress().getHostName(),
                                channel.socket().getInetAddress().getHostAddress(),
                                channel.socket().getPort(), channel.socket().getLocalPort(),
                                readData.toByteArray(), NetPackage.ActionEvent.READ);

                        NetSession session = sessionsByAddress.get(address);

                        if (session != null) {
                            //Here the session is linked with the current thread
                            ((ServiceThread) Thread.currentThread()).setSession(session);

                            netPackage.setSession(session);
                            if (addresses.containsKey(session)) {
                                addresses.put(session, address);
                            }

                            if (!channels.containsKey(session)) {
                                channels.put(session, channel);
                            }
                            if (!outputQueue.containsKey(channel)) {
                                outputQueue.put(channel, new LinkedBlockingQueue<>());
                                lastWrite.put(channel, System.currentTimeMillis());
                            }

                            if (readData.size() > 0) {
                                onAction(netPackage, consumer);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Log.e(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Net service read exception, on UDP context", ex);
                }
            }
        }
    }

    /**
     * This method take the output queue associated to the consumer and write over the
     * session channel all the packages.
     * If one of the packages is a disconnection package then the channel is closed and
     * the rest of the packages are discarded.
     *
     * @param channel  Session channel.
     * @param consumer Net service consumer.
     */
    private void write(SelectableChannel channel, NetServiceConsumer consumer) {
        NetServiceConsumer.NetIOThread ioThread = (NetServiceConsumer.NetIOThread) Thread.currentThread();
        try {
            Queue<NetPackage> queue = outputQueue.get(channel);

            if (queue != null) {
                lastWrite.put(channel, System.currentTimeMillis());
                boolean stop = false;

                int count = 0;
                while (!queue.isEmpty() && !stop) {
                    NetPackage netPackage = queue.poll();
                    if (netPackage == null) {
                        break;
                    }

                    NetSession session = netPackage.getSession();

                    switch (netPackage.getActionEvent()) {
                        case WRITE: {
                            try {
                                if (consumer.getProtocol().equals(TransportLayerProtocol.TCP_SSL)) {
                                    netPackage = sslHelpers.get(session).write(netPackage);
                                } else {
                                    byte[] byteData = netPackage.getPayload();
                                    if (byteData.length == 0) {
                                        Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Empty write data");
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

                                if (netPackage != null) {
                                    netPackage.setPackageStatus(NetPackage.PackageStatus.OK);
                                }
                            } catch (Exception ex) {
                                netPackage.setPackageStatus(NetPackage.PackageStatus.IO_ERROR);
                                throw ex;
                            } finally {
                                onAction(netPackage, consumer);
                            }

                            //Change the key operation to finish write loop
                            channel.keyFor(getSelector()).interestOps(SelectionKey.OP_READ);

                            break;
                        }
                        case DISCONNECT: {
                            if (channel instanceof SocketChannel) {
                                destroyChannel((SocketChannel) channel);
                            } else if (channel instanceof DatagramChannel && !channel.equals(udpServer)) {
                                outputQueue.remove(channel);
                                lastWrite.remove(channel);
                                channels.remove(netPackage.getSession());
                                if (netPackage.getSession().getConsumer() instanceof NetServer) {
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
                    count++;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Write global thread exception", ex);
        } finally {
            ioThread.getOutputBuffer().clear();
            ioThread.getOutputBuffer().rewind();
        }
    }

    /**
     * This method put all the action events in a queue by session and then start a
     * new thread to notify all the consumers
     *
     * @param netPackage Received data.
     * @param consumer   Consumer associated to the session.
     */
    private void onAction(final NetPackage netPackage, final NetServiceConsumer consumer) {
        if (netPackage != null) {
            try {
                switch (netPackage.getActionEvent()) {
                    case CONNECT:
                        consumer.onConnect(netPackage);
                        break;
                    case DISCONNECT:
                        consumer.onDisconnect(netPackage);
                        break;
                    case READ:
                        consumer.onRead(netPackage);
                        break;
                    case WRITE:
                        consumer.onWrite(netPackage);
                        break;
                }
            } catch (Exception ex) {
                Log.e(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Action consumer exception", ex);
            }
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
                if (!sessionsByChannel.containsKey(channel)) {
                    try {
                        destroyChannel(channel);
                    } catch (Exception ex) {
                    }
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

    private static class SSLHelper implements Runnable {

        private SSLEngine sslEngine;
        private final SelectableChannel selectableChannel;
        private final NetServiceConsumer consumer;
        private final NetSession session;

        private final ThreadPoolExecutor ioExecutor;
        private final ThreadPoolExecutor engineTaskExecutor;
        private final ByteBuffer srcWrap;
        private final ByteBuffer destWrap;
        private final ByteBuffer srcUnwrap;
        private final ByteBuffer destUnwrap;

        private SSLHelper.SSLHelperStatus status;

        private final Object writeSemaphore;
        private final Object readSemaphore;
        private ByteBuffer decryptedPlace;
        private boolean read;
        private boolean written;

        /**
         * SSL Helper default constructor.
         *
         * @param sslEngine         SSL Engine.
         * @param selectableChannel Selectable channel.
         */
        public SSLHelper(SSLEngine sslEngine, SelectableChannel selectableChannel, NetServiceConsumer consumer, NetSession session) {
            this.sslEngine = sslEngine;
            this.selectableChannel = selectableChannel;
            this.consumer = consumer;
            this.session = session;
            this.ioExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
            this.ioExecutor.setThreadFactory(R -> (new ServiceThread(R, SystemProperties.get(SystemProperties.Net.Ssl.IO_THREAD_NAME))));
            this.engineTaskExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(
                    SystemProperties.getInteger(SystemProperties.Net.SSL_MAX_IO_THREAD_POOL_SIZE));
            this.engineTaskExecutor.setThreadFactory(R -> (new ServiceThread(R, SystemProperties.get(SystemProperties.Net.Ssl.ENGINE_THREAD_NAME))));
            srcWrap = ByteBuffer.allocate(SystemProperties.getInteger(SystemProperties.Net.OUTPUT_BUFFER_SIZE));
            destWrap = ByteBuffer.allocate(SystemProperties.getInteger(SystemProperties.Net.OUTPUT_BUFFER_SIZE));
            srcUnwrap = ByteBuffer.allocate(SystemProperties.getInteger(SystemProperties.Net.INPUT_BUFFER_SIZE));
            destUnwrap = ByteBuffer.allocate(SystemProperties.getInteger(SystemProperties.Net.INPUT_BUFFER_SIZE));
            srcUnwrap.limit(0);

            //SSL Helper first status
            status = SSLHelper.SSLHelperStatus.WAITING;

            //IO Semaphores
            readSemaphore = new Object();
            writeSemaphore = new Object();

            //Start handshaking
            instance.fork(this, ioExecutor);
        }

        /**
         * This method is called when there are data into the read buffer.
         *
         * @param decrypted Read buffer.
         */
        private void onRead(ByteBuffer decrypted) {
            byte[] decryptedArray = new byte[decrypted.limit()];
            decrypted.get(decryptedArray);
            if (status.equals(SSLHelper.SSLHelperStatus.READY)) {
                synchronized (readSemaphore) {
                    read = true;
                    decryptedPlace = ByteBuffer.wrap(decryptedArray);
                    readSemaphore.notifyAll();
                }
            }
        }

        /**
         * This method is called when there are data into the write buffer.
         *
         * @param encrypted Write buffer.
         */
        private void onWrite(ByteBuffer encrypted) {
            try {
                long size = encrypted.limit();
                long total = 0;
                while (total < size) {
                    total += ((SocketChannel) selectableChannel).write(encrypted);
                }
            } catch (IOException ex) {
                throw new RuntimeException("", ex);
            }
            if (status.equals(SSLHelper.SSLHelperStatus.READY)) {
                synchronized (writeSemaphore) {
                    written = true;
                    writeSemaphore.notifyAll();
                }
            }
        }

        /**
         * This method is called when the operation fail.
         *
         * @param ex Fail exception.
         */
        private void onFailure(Exception ex) {
            status = SSLHelper.SSLHelperStatus.FAIL;
        }

        /**
         * This method is called when the operation is success.
         */
        private void onSuccess() {
            Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "SSL handshaking success");
            status = SSLHelper.SSLHelperStatus.READY;
            DefaultNetPackage defaultNetPackage = new DefaultNetPackage("", "",
                    0, consumer.getPort(), new byte[0], NetPackage.ActionEvent.CONNECT);
            defaultNetPackage.setSession(session);
            if (consumer instanceof NetClient) {
                consumer.onConnect(defaultNetPackage);
            }
        }

        /**
         * This method is called when the helper is closed.
         */
        private void onClosed() {
            DefaultNetPackage defaultNetPackage = new DefaultNetPackage("", "",
                    0, consumer.getPort(), new byte[0], NetPackage.ActionEvent.DISCONNECT);
            consumer.onDisconnect(session, defaultNetPackage);
        }

        /**
         * Run method of the helper.
         */
        @Override
        public void run() {
            while (this.isHandShaking()) {
                continue;
            }
        }

        /**
         * Write data into the associated channel.
         *
         * @param netPackage Net package.
         * @return Net package.
         */
        public synchronized NetPackage write(NetPackage netPackage) {
            instance.fork(() -> {
                srcWrap.put(netPackage.getPayload());
                SSLHelper.this.run();
            }, ioExecutor);

            DefaultNetPackage defaultNetPackage = null;
            if (status.equals(SSLHelper.SSLHelperStatus.READY)) {
                synchronized (writeSemaphore) {
                    try {
                        if (!written) {
                            readSemaphore.wait();
                            defaultNetPackage = new DefaultNetPackage("", "",
                                    0, consumer.getPort(), netPackage.getPayload(), NetPackage.ActionEvent.READ);
                            defaultNetPackage.setSession(netPackage.getSession());
                        }
                    } catch (Exception ex) {
                    } finally {
                        written = false;
                    }
                }
            }

            return defaultNetPackage;
        }

        /**
         * Read data from the associated channel.
         *
         * @param netPackage Net package.
         * @return Input data.
         */
        public synchronized NetPackage read(NetPackage netPackage) {
            instance.fork(() -> {
                srcUnwrap.put(netPackage.getPayload());
                SSLHelper.this.run();
            }, ioExecutor);

            DefaultNetPackage defaultNetPackage = null;
            if (status.equals(SSLHelper.SSLHelperStatus.READY)) {
                synchronized (readSemaphore) {
                    try {
                        if (!read) {
                            readSemaphore.wait();
                        }
                        defaultNetPackage = new DefaultNetPackage("", "",
                                0, consumer.getPort(), decryptedPlace.array(), NetPackage.ActionEvent.READ);
                        defaultNetPackage.setSession(netPackage.getSession());
                    } catch (InterruptedException e) {
                    } finally {
                        read = false;
                    }
                }
            }

            return defaultNetPackage;
        }

        /**
         * Close the ssl engine instance.
         */
        public void close() {
            try {
                sslEngine.closeInbound();
            } catch (SSLException e) {
            }
            sslEngine.closeOutbound();
        }

        /**
         * Return boolean to indicate if the hand shaking process is running.
         *
         * @return True if the process is running and false in otherwise.
         */
        private boolean isHandShaking() {
            switch (sslEngine.getHandshakeStatus()) {
                case NOT_HANDSHAKING:
                    boolean occupied = false;
                {
                    if (srcWrap.position() > 0)
                        occupied |= this.wrap();
                    if (srcUnwrap.position() > 0)
                        occupied |= this.unwrap();
                }
                return occupied;

                case NEED_WRAP:
                    if (!this.wrap())
                        return false;
                    break;

                case NEED_UNWRAP:
                    if (!this.unwrap())
                        return false;
                    break;

                case NEED_TASK:
                    final Runnable sslTask = sslEngine.getDelegatedTask();
                    instance.fork(() -> {
                        sslTask.run();
                        instance.fork(SSLHelper.this, ioExecutor);
                    }, engineTaskExecutor);
                    return false;

                case FINISHED:
                    throw new IllegalStateException("SSL handshaking fail");
            }

            return true;
        }

        /**
         * Wrap the output data.
         *
         * @return Return true if the process was success.
         */
        private boolean wrap() {
            SSLEngineResult wrapResult;

            try {
                srcWrap.flip();
                wrapResult = sslEngine.wrap(srcWrap, destWrap);
                srcWrap.compact();
            } catch (SSLException exc) {
                this.onFailure(exc);
                return false;
            }

            switch (wrapResult.getStatus()) {
                case OK:
                    if (destWrap.position() > 0) {
                        destWrap.flip();
                        this.onWrite(destWrap);
                        destWrap.compact();
                    }
                    break;

                case BUFFER_UNDERFLOW:
                    // try again later
                    break;

                case BUFFER_OVERFLOW:
                    throw new IllegalStateException("SSL failed to wrap");

                case CLOSED:
                    this.onClosed();
                    return false;
            }

            if (consumer instanceof NetServer &&
                    wrapResult.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
                this.onSuccess();
                return false;
            }

            return true;
        }

        /**
         * Unwrap the input data.
         *
         * @return Return true if the process was success.
         */
        private boolean unwrap() {
            SSLEngineResult unwrapResult;

            try {
                srcUnwrap.flip();
                unwrapResult = sslEngine.unwrap(srcUnwrap, destUnwrap);
                srcUnwrap.compact();
            } catch (SSLException ex) {
                this.onFailure(ex);
                return false;
            }

            switch (unwrapResult.getStatus()) {
                case OK:
                    if (destUnwrap.position() > 0) {
                        destUnwrap.flip();
                        this.onRead(destUnwrap);
                        destUnwrap.compact();
                    }
                    break;

                case CLOSED:
                    this.onClosed();
                    return false;

                case BUFFER_OVERFLOW:
                    throw new IllegalStateException("SSL failed to unwrap");

                case BUFFER_UNDERFLOW:
                    return false;
            }

            if (consumer instanceof NetClient &&
                    unwrapResult.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
                this.onSuccess();
                return false;
            }

            return true;
        }

        /**
         * Contains all the possible helper status.
         */
        public enum SSLHelperStatus {

            WAITING,

            READY,

            FAIL

        }
    }
}
