package org.hcjf.io.net;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceThread;
import org.hcjf.utils.LruMap;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

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

    private Map<NetServiceConsumer, ServerSocketChannel> serverSocketChannelMap;
    private Map<NetSession, SelectableChannel> channels;
    private Map<SelectableChannel, NetSession> sessionsByChannel;
    private DatagramChannel udpServer;
    private Map<NetSession, SocketAddress> addresses;
    private Map<SocketAddress, NetSession> sessionsByAddress;
    private Map<SelectableChannel, Long> lastWrite;
    private Map<SelectableChannel, Queue<NetPackage>> outputQueue;
    private Map<NetSession, SSLHelper> sslHelpers;
    private Map<NetServiceConsumer,SelectorRunnable> selectors;
    private Map<NetServiceConsumer,Future> tasks;
    private Timer timer;
    private boolean creationTimeoutAvailable;
    private long creationTimeout;
    private boolean shuttingDown;

    private NetService(String serviceName) {
        super(serviceName, 2);
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
        this.timer = new Timer();
        selectors = new HashMap<>();
        tasks = new HashMap<>();

        this.creationTimeoutAvailable = SystemProperties.getBoolean(SystemProperties.Net.CONNECTION_TIMEOUT_AVAILABLE);
        this.creationTimeout = SystemProperties.getLong(SystemProperties.Net.CONNECTION_TIMEOUT);
        if (creationTimeoutAvailable && creationTimeout <= 0) {
            throw new IllegalArgumentException("Illegal creation timeout value: " + creationTimeout);
        }

        lastWrite = Collections.synchronizedMap(new HashMap<>());
        outputQueue = Collections.synchronizedMap(new HashMap<>());
        serverSocketChannelMap = Collections.synchronizedMap(new HashMap<>());
        channels = Collections.synchronizedMap(new TreeMap<>());
        sessionsByChannel = Collections.synchronizedMap(new HashMap<>());
        sessionsByAddress = Collections.synchronizedMap(new LruMap(SystemProperties.getInteger(SystemProperties.Net.IO_UDP_LRU_SESSIONS_SIZE)));
        sslHelpers = Collections.synchronizedMap(new HashMap<>());
        addresses = Collections.synchronizedMap(new LruMap<>(SystemProperties.getInteger(SystemProperties.Net.IO_UDP_LRU_ADDRESSES_SIZE)));
    }

    /**
     * This method will be called immediately after the static
     * method 'shutdown' of the class has been called.
     *
     * @param stage Shutdown stage.
     */
    @Override
    protected void shutdown(ShutdownStage stage) {
        shuttingDown = true;
        for(SelectorRunnable selectorRunnable : selectors.values()) {
            selectorRunnable.shutdown(stage);
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
     * @param server TCP Server.
     */
    private void registerTCPNetServer(NetServer server) throws IOException {
        ServerSocketChannel tcpServer = ServerSocketChannel.open();
        tcpServer.configureBlocking(false);
        InetSocketAddress tcpAddress = new InetSocketAddress(server.getPort());
        tcpServer.socket().bind(tcpAddress);
        registerChannel(server, tcpServer, SelectionKey.OP_ACCEPT, server);
        serverSocketChannelMap.put(server, tcpServer);
    }

    /**
     * This method registers a TCP client service.
     * @param client TCP Client.
     */
    private void registerTCPNetClient(NetClient client) throws IOException {
        final SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(client.getHost(), client.getPort()));
        registerChannel(client, channel, SelectionKey.OP_CONNECT | SelectionKey.OP_READ, client);
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
        registerChannel(server, udpServer, SelectionKey.OP_READ, server);
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
        addresses.put(client.getSession(), address);
        sessionsByAddress.put(channel.getRemoteAddress(), client.getSession());
        registerChannel(client, channel, SelectionKey.OP_READ, client);
        selectors.get(client).addSession(client.getSession());
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
     *
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
        if (channel != null) {
            result = channel.isOpen();
        }

        return result;
    }

    /**
     * This method blocks the selector to add a new channel to the key system
     * @param channel   The new channel to be register
     * @param operation The first channel operation.
     * @param attach    Object to be attached into the registered key.
     * @throws ClosedChannelException
     */
    private void registerChannel(NetServiceConsumer consumer, SelectableChannel channel, int operation, Object attach) throws ClosedChannelException {
        selectors.put(consumer, new SelectorRunnable(consumer));
        tasks.put(consumer, fork(selectors.get(consumer)));
        selectors.get(consumer).registerChannel(channel, operation, attach);
    }

    /**
     * Creates a internal package of data.
     * @param channel Socket channel.
     * @param data Payload.
     * @param event Action event.
     * @return Returns the instance of net package.
     */
    private NetPackage createPackage(SelectableChannel channel, byte[] data, NetPackage.ActionEvent event) {
        NetPackage netPackage;
        String remoteHost;
        String remoteAddress;
        int remotePort;
        int localPort;
        if (channel instanceof SocketChannel) {
            remoteHost = "";
            if(SystemProperties.getBoolean(SystemProperties.Net.REMOTE_ADDRESS_INTO_NET_PACKAGE)) {
                remoteHost = ((SocketChannel) channel).socket().getInetAddress().getHostName();
            }
            remoteAddress = ((SocketChannel) channel).socket().getInetAddress().getHostAddress();
            remotePort = ((SocketChannel) channel).socket().getPort();
            localPort = ((SocketChannel) channel).socket().getLocalPort();
        } else if (channel instanceof DatagramChannel) {
            remoteHost = "";
            remoteAddress ="";
            remotePort = -1;
            localPort = -1;
            try {
                Field field = channel.getClass().getDeclaredField("sender");
                field.setAccessible(true);
                InetSocketAddress socketAddress = (InetSocketAddress) field.get(channel);
                if(SystemProperties.getBoolean(SystemProperties.Net.REMOTE_ADDRESS_INTO_NET_PACKAGE)) {
                    remoteHost = socketAddress.getAddress().getHostName();
                }
                remoteAddress = socketAddress.getAddress().getHostAddress();
                remotePort = socketAddress.getPort();
                localPort = ((DatagramChannel) channel).socket().getLocalPort();
            } catch (Exception ex){
                Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "createPackage method exception", ex);
            }
        } else {
            throw new IllegalArgumentException("Unknown channel type");
        }

        netPackage = new DefaultNetPackage(remoteHost, remoteAddress, remotePort,
                localPort, data, event);

        return netPackage;
    }

    /**
     * This method notify to all the writer and put into the output buffer some package.
     * @param channel Channel to write the package.
     * @param netPackage Package.
     */
    private void writeWakeup(SelectableChannel channel, NetPackage netPackage) {
        SelectorRunnable selectorRunnable = selectors.get(netPackage.getSession().getConsumer());
        selectorRunnable.writeWakeup(channel, netPackage);
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
            writeWakeup(channel, netPackage);
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
                    writeWakeup(channel, netPackage);
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
            if (sslHelpers.containsKey(session)) {
                sslHelpers.remove(session).close();
            }
            List<NetSession> removedSessions = new ArrayList<>();

            try {
                if (session != null) {
                    channels.remove(session);
                    if (session.getConsumer() instanceof NetServer) {
                        NetServer server = (NetServer) session.getConsumer();
                        if (server.isDisconnectAndRemove()) {
                            destroySession(session);
                        }
                    }
                    removedSessions.add(session);

                    if(session.getConsumer() != null) {
                        session.getConsumer().onDisconnect(session, null);
                    }
                }

                if (channel.isConnected()) {
                    channel.close();
                }
            } catch (Exception ex) {
                Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Destroy method exception", ex);
            }

            if(session.getConsumer() instanceof NetClient) {
                SelectorRunnable selectorRunnable = selectors.remove(session.getConsumer());
                selectorRunnable.shutdown(ShutdownStage.START);
                selectorRunnable.shutdown(ShutdownStage.END);
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
    private NetSession getSession(NetServiceConsumer consumer, NetPackage netPackage, SocketChannel socketChannel) {
        NetSession result;

        if (consumer instanceof NetServer) {
            result = ((NetServer) consumer).createSession(netPackage);
        } else if (consumer instanceof NetClient) {
            result = ((NetClient) consumer).getSession();
        } else {
            throw new IllegalArgumentException("The service consumer must be instance of org.hcjf.io.net.NetServer or org.hcjf.io.net.NetClient.");
        }

        if(SystemProperties.getBoolean(SystemProperties.Net.REMOTE_ADDRESS_INTO_NET_SESSION)) {
            result.setRemoteHost(socketChannel.socket().getInetAddress().getHostName());
            result.setRemotePort(socketChannel.socket().getPort());
        } else {
            result.setRemoteHost(socketChannel.socket().getInetAddress().getHostAddress());
            result.setRemotePort(socketChannel.socket().getPort());
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
     * This runnable encapsulate all the components needing to the selection process.
     */
    private class SelectorRunnable implements Runnable {

        private final NetServiceConsumer consumer;
        private Selector selector;
        private final Object monitor;
        private Boolean blocking;
        private Set<NetSession> sessions;
        private final Queue<SelectionKey> readableKeys;
        private final Queue<SelectionKey> writableKeys;
        private final ThreadPoolExecutor readIoExecutor;
        private final ThreadPoolExecutor writeIoExecutor;

        private SelectorRunnable(NetServiceConsumer consumer) {
            this.consumer = consumer;
            this.monitor = new Object();
            this.blocking = false;
            this.sessions = new TreeSet<>();
            try {
                createSelector();
            } catch (IOException ex) {
                throw new HCJFRuntimeException("Unable to create selector", ex);
            }

            readableKeys = new ArrayBlockingQueue<>(SystemProperties.getInteger(SystemProperties.Net.IO_QUEUE_SIZE));
            writableKeys = new ArrayBlockingQueue<>(SystemProperties.getInteger(SystemProperties.Net.IO_QUEUE_SIZE));

            readIoExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool(new NetIOThreadFactory());
            readIoExecutor.setKeepAliveTime(SystemProperties.getInteger(SystemProperties.Net.IO_THREAD_POOL_KEEP_ALIVE_TIME), TimeUnit.SECONDS);
            writeIoExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool(new NetIOThreadFactory());
            writeIoExecutor.setKeepAliveTime(SystemProperties.getInteger(SystemProperties.Net.IO_THREAD_POOL_KEEP_ALIVE_TIME), TimeUnit.SECONDS);
            fork(new Reader(), SystemProperties.get(SystemProperties.Net.IO_THREAD_POOL_NAME), readIoExecutor);
            fork(new Writer(), SystemProperties.get(SystemProperties.Net.IO_THREAD_POOL_NAME), writeIoExecutor);
        }

        /**
         * Returns the set of sessions.
         * @return Set of sessions.
         */
        private Set<NetSession> getSessions() {
            return Collections.unmodifiableSet(sessions);
        }

        /**
         * Add a session on the selector artifact.
         * @param session Net session instance.
         */
        public void addSession(NetSession session) {
            sessions.add(session);
        }

        /**
         * Returns the selector instance.
         * @return Selector instance.
         */
        private Selector getSelector() {
            return selector;
        }

        /**
         * Set the selector instance.
         * @param selector Selector instance.
         */
        private void setSelector(Selector selector) {
            this.selector = selector;
        }

        /**
         * Returns the monitor instance.
         * @return Monitor instance.
         */
        public Object getMonitor() {
            return monitor;
        }

        /**
         * Returns the blocking value.
         * @return Blocking value.
         */
        public Boolean getBlocking() {
            return blocking;
        }

        /**
         * This method blocks the selector to add a new channel to the key system
         * @param channel   The new channel to be register
         * @param operation The first channel operation.
         * @param attach    Object to be attached into the registered key.
         * @throws ClosedChannelException
         */
        private void registerChannel(SelectableChannel channel, int operation, Object attach) throws ClosedChannelException {
            synchronized (monitor) {
                channel.register(getSelector(), operation, attach);
                wakeup();
            }
        }

        private void writeWakeup(SelectableChannel channel, NetPackage netPackage) {
            outputQueue.get(channel).add(netPackage);

            SelectionKey key = channel.keyFor(getSelector());
            synchronized (writableKeys) {
                if (key.isValid() && !writableKeys.contains(key)) {
                    if (!writableKeys.offer(key)) {
                        Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Unable to add writable key!!!!");
                    }
                }
                writableKeys.notifyAll();
            }
        }

        /**
         * This method will be called immediately after the static
         * method 'shutdown' of the class has been called.
         * @param stage Shutdown stage.
         */
        private void shutdown(ShutdownStage stage) {
            switch (stage) {
                case START: {
                    for (NetSession session : getSessions()) {
                        try {
                            writeData(session, session.getConsumer().getShutdownFrame(session));
                        } catch (IOException e) { }
                    }
                    wakeup();
                    break;
                }
                case END: {
                    for (NetSession session : getSessions()) {
                        disconnect(session, "");
                    }

                    tasks.remove(consumer).cancel(true);
                    wakeup();
                    break;
                }
            }
        }

        /**
         * Creates a new instance of a selector, if there are a previous instance then the previous keys are registered into
         * the new selector instance.
         * @return Returns the new instance of a selector
         * @throws IOException
         */
        private void createSelector() throws IOException {
            Selector newSelector = Selector.open();
            Selector selector = getSelector();

            if(selector != null) {
                //This loop is to register all the channels of the previous keys into the new selector.
                for (SelectionKey key : selector.keys()) {
                    try {
                        SelectableChannel ch = key.channel();
                        int ops = key.interestOps();
                        Object att = key.attachment();
                        // Cancel the old key
                        key.cancel();

                        // Register the channel with the new selector
                        ch.register(newSelector, ops, att);
                    } catch (Exception ex){}
                }
                try {
                    selector.close();
                    Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Previous selector closed");
                } catch (Throwable ex) {
                    Log.w(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Fail to close the old selector", ex);
                }
            }

            setSelector(newSelector);
            Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "New selector created");
        }

        /**
         * This method performs a non blocking select operation over the selector and check if the number of available
         * keys is bigger than zero. If the available keys are zero then the thread are waiting until some operation invoke
         * the wakeup method.
         * @return Returns the number of available keys into the selector.
         * @throws IOException
         */
        private int select() throws IOException {
            int result;
            synchronized (monitor) {
                blocking = true;
            }
            result = getSelector().select();
            synchronized (monitor) {
                blocking = false;
            }
            return result;
        }

        /**
         * This method wakeup the main thread in order to verify if there are some available keys into selector. All the
         * times verify if the selector is blocking into the select method, because only invoke the method wakeup of the
         * selector if it is blocking in the select method.
         */
        private void wakeup() {
            synchronized (monitor) {
                if(blocking) {
                    getSelector().wakeup();
                    blocking = false;
                }
            }
        }

        /**
         * This method run continuously the selection process.
         */
        @Override
        public void run() {
            try {
                try {
                    Thread.currentThread().setName(SystemProperties.get(SystemProperties.Net.LOG_TAG));
                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                } catch (SecurityException ex) {
                }

                long selectorMinWaitTime = SystemProperties.getLong(SystemProperties.Net.NIO_SELECTOR_MIN_WAIT_TIME);
                int selectorCounterLimit = SystemProperties.getInteger(SystemProperties.Net.NIO_SELECTOR_MIN_WAIT_COUNTER_LIMIT);
                int selectorCounter = 0;
                long selectionStartPeriod;
                long selectionPeriod;
                int selectionSize;
                Iterator<SelectionKey> selectedKeys;
                SelectionKey key;

                while (!Thread.currentThread().isInterrupted()) {
                    //Select the next schedule key or sleep if the aren't any key to select.
                    selectionStartPeriod = System.currentTimeMillis();
                    selectionSize = select();

                    if(selectionSize == 0) {
                        selectionPeriod = System.currentTimeMillis() - selectionStartPeriod;
                        if(selectionPeriod < selectorMinWaitTime) {
                            selectorCounter++;
                            if(selectorCounter > selectorCounterLimit) {
                                selectorCounter = 0;
                                Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Fixing selector loop");
                            }
                        }
                    } else {
                        selectorCounter = 0;
                        selectedKeys = getSelector().selectedKeys().iterator();
                        while (selectedKeys.hasNext()) {
                            key = selectedKeys.next();
                            selectedKeys.remove();

                            if (key.isValid()) {
                                try {
                                    final SelectableChannel keyChannel = key.channel();
                                    if (keyChannel != null && key.channel().isOpen() && key.isValid()) {
                                        final NetServiceConsumer consumer = (NetServiceConsumer) key.attachment();
                                        //If the kind of key is acceptable or connectable then
                                        //the processing do over this thread in the other case
                                        //the processing is delegated to the thread pool
                                        if (key.isAcceptable()) {
                                            accept(key.channel(), (NetServer) consumer);
                                        } else if (key.isConnectable()) {
                                            connect(key.channel(), (NetClient) consumer);
                                        } else if (key.isReadable()) {
                                            synchronized (readableKeys) {
                                                if (key.isValid() && !readableKeys.contains(key)) {
                                                    if (!readableKeys.offer(key)) {
                                                        Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Unable to add readable key!!!!");
                                                    }
                                                }
                                                readableKeys.notifyAll();
                                            }
                                        } else if (key.isWritable()) {
                                            synchronized (writableKeys) {
                                                if (key.isValid() && !writableKeys.contains(key)) {
                                                    if (!writableKeys.offer(key)) {
                                                        Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Unable to add writable key!!!!");
                                                    }
                                                }
                                                writableKeys.notifyAll();
                                            }
                                        }
                                    } else {
                                        key.cancel();
                                    }
                                } catch (CancelledKeyException ex) {
                                    Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Cancelled key");
                                } catch (Exception ex) {
                                    Log.e(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Net service main thread exception", ex);
                                }
                            }
                        }
                    }
                }

                try {
                    getSelector().close();
                } catch (IOException ex) {
                    Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Closing selector...", ex);
                }

                //Close all the servers.
                if(NetServer.class.isAssignableFrom(consumer.getClass())) {
                    ServerSocketChannel channel = serverSocketChannelMap.get(consumer);
                    try {
                        channel.close();
                    } catch (IOException ex) {
                        Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Closing channel...", ex);
                    }
                }
                selectors.remove(consumer);
            } catch (Exception ex) {
                Log.e(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Unexpected error", ex);
            }


            readIoExecutor.shutdownNow();
            writeIoExecutor.shutdownNow();
            Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Selector stopped");
        }

        /**
         * This class is a runnable that consume the readable keys queue and call the read method.
         */
        private class Reader implements Runnable {

            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()) {
                    SelectionKey key;
                    synchronized (readableKeys) {
                        key = readableKeys.poll();
                    }
                    if (key != null) {
                        try {
                            NetServiceConsumer consumer = (NetServiceConsumer) key.attachment();
                            SelectableChannel keyChannel = key.channel();
                            if (keyChannel != null && key.channel().isOpen()) {
                                synchronized (keyChannel) {
                                    try {
                                        if (key.isValid()) {
                                            read(keyChannel, consumer);
                                        }
                                    } catch (Exception ex) {
                                        Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Internal read exception", ex);
                                    } finally {
                                        ((ServiceThread) Thread.currentThread()).setSession(null);
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Internal IO thread exception, before to read process", ex);
                        }
                    } else {
                        try {
                            if(readableKeys.isEmpty()) {
                                synchronized (readableKeys) {
                                    readableKeys.wait();
                                }
                            }
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
                Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Reader finished");
            }
        }

        /**
         * This class is a runnable that consume the writable keys queue and call the read method.
         */
        private class Writer implements Runnable {

            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()) {
                    SelectionKey key;
                    synchronized (writableKeys) {
                        key = writableKeys.poll();
                    }
                    if (key != null) {
                        try {
                            NetServiceConsumer consumer = (NetServiceConsumer) key.attachment();
                            SelectableChannel keyChannel = key.channel();
                            if (keyChannel != null && key.channel().isOpen()) {
                                synchronized (keyChannel) {
                                    try {
                                        if (key.isValid()) {
                                            write(keyChannel, consumer);
                                        }
                                    } catch (Exception ex) {
                                        Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Internal write exception", ex);
                                    } finally {
                                        ((ServiceThread) Thread.currentThread()).setSession(null);
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Internal IO thread exception, before to write process", ex);
                        }
                    } else {
                        try {
                            if(writableKeys.isEmpty()) {
                                synchronized (writableKeys) {
                                    writableKeys.wait();
                                }
                            }
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
                Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Writer finished");
            }
        }
    }

    /**
     * Set the socket options into the specific socket channel.
     * @param socketChannel Socket channel instance.
     * @param consumer Consumer to obtain the options.
     * @throws IOException IO Exception.
     */
    private void setSocketOptions(SocketChannel socketChannel, NetServiceConsumer consumer) throws IOException {
        Map<SocketOption, Object> socketOptions = consumer.getSocketOptions();
        if (socketOptions != null) {
            for (SocketOption socketOption : socketOptions.keySet()) {
                socketChannel.setOption(socketOption, socketOptions.get(socketOption));
            }
        }
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
                setSocketOptions(channel, client);

                NetSession session = getSession(client,
                        createPackage(channel, null, NetPackage.ActionEvent.CONNECT),
                        (SocketChannel) keyChannel);
                if(session != null) {
                    selectors.get(client).addSession(session);
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
                } else {
                    Log.w(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Rejected connection, session null");
                    channel.close();
                    client.onConnectFail();
                    SelectorRunnable selectorRunnable = selectors.remove(client);
                    selectorRunnable.shutdown(ShutdownStage.START);
                    selectorRunnable.shutdown(ShutdownStage.END);
                }
            } catch (Exception ex) {
                Log.w(SystemProperties.get(SystemProperties.Net.LOG_TAG),
                        "Error creating new client connection, %s:%d", ex, client.getHost(), client.getPort());
                client.onConnectFail();
                SelectorRunnable selectorRunnable = selectors.remove(client);
                selectorRunnable.shutdown(ShutdownStage.START);
                selectorRunnable.shutdown(ShutdownStage.END);
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
                setSocketOptions(socketChannel, server);

                NetSession session = getSession(server,
                        createPackage(socketChannel, null, NetPackage.ActionEvent.CONNECT),
                        socketChannel);
                if(session != null) {
                    if (channels.containsKey(session)) {
                        updateChannel((SocketChannel) channels.remove(session), socketChannel);
                    } else {
                        sessionsByChannel.put(socketChannel, session);
                        outputQueue.put(socketChannel, new LinkedBlockingQueue<>());
                        lastWrite.put(socketChannel, System.currentTimeMillis());
                        channels.put(session, socketChannel);
                        selectors.get(server).addSession(session);
                    }

                    if (server.getProtocol().equals(TransportLayerProtocol.TCP_SSL)) {
                        SSLHelper sslHelper = new SSLHelper(server.getSSLEngine(), socketChannel, server, session);
                        sslHelpers.put(session, sslHelper);
                    }

                    //A new readable key is created associated to the channel.
                    socketChannel.register(selectors.get(server).getSelector(), SelectionKey.OP_READ, server);

                    if (isCreationTimeoutAvailable() && server.isCreationTimeoutAvailable()) {
                        getTimer().schedule(new ConnectionTimeout(socketChannel), getCreationTimeout());
                    }
                } else {
                    Log.w(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Rejected connection, session null");
                    socketChannel.close();
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
                NetIOThread ioThread = (NetIOThread) Thread.currentThread();

                try  {
                    int readSize;
                    int totalSize = 0;
                    ByteBuffer inputBuffer = ioThread.getInputBuffer();
                    inputBuffer.clear();
                    inputBuffer.rewind();
                    try {
                        //Put all the bytes into the buffer of the IO thread.
                        totalSize += readSize = channel.read(inputBuffer);
                        while (readSize > 0) {
                            totalSize += readSize = channel.read(inputBuffer);
                        }
                    } catch (IOException ex) {
                        destroyChannel(channel);
                    }

                    if (totalSize == -1) {
                        destroyChannel(channel);
                    } else if (totalSize > 0) {
                        Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Total size read: %d", totalSize);
                        byte[] data = new byte[inputBuffer.position()];
                        inputBuffer.rewind();
                        inputBuffer.get(data);
                        NetPackage netPackage = createPackage(channel, data, NetPackage.ActionEvent.READ);

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
                NetIOThread ioThread = (NetIOThread) Thread.currentThread();

                try {
                    ByteArrayOutputStream readData = new ByteArrayOutputStream();
                    ioThread.getInputBuffer().clear();
                    ioThread.getInputBuffer().rewind();

                    InetSocketAddress address = (InetSocketAddress) channel.receive(ioThread.getInputBuffer());
                    readData.write(ioThread.getInputBuffer().array(), 0, ioThread.getInputBuffer().position());

                    if (address != null) {
                        NetPackage netPackage = createPackage(channel, readData.toByteArray(), NetPackage.ActionEvent.READ);
                        NetSession session;
                        session = sessionsByAddress.get(address);

                        if(session == null && consumer instanceof NetServer) {
                            session = ((NetServer) consumer).createSession(netPackage);
                            sessionsByAddress.put(address, session);
                        }

                        if (!addresses.containsKey(session)) {
                            addresses.put(session, address);
                        }

                        if (!channels.containsKey(session)) {
                            channels.put(session, channel);
                        }

                        //Here the session is linked with the current thread
                        ((ServiceThread) Thread.currentThread()).setSession(session);

                        netPackage.setSession(session);

                        if (!outputQueue.containsKey(channel)) {
                            outputQueue.put(channel, new LinkedBlockingQueue<>());
                            lastWrite.put(channel, System.currentTimeMillis());
                        }

                        if (readData.size() > 0) {
                            onAction(netPackage, consumer);
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
        NetIOThread ioThread = (NetIOThread) Thread.currentThread();
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
                                    if(byteData != null){
                                        if (byteData.length == 0) {
                                            Log.d(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Empty write data");
                                        }
                                        int begin = 0;
                                        int length = (byteData.length - begin) > ioThread.getOutputBufferSize() ?
                                                ioThread.getOutputBufferSize() : byteData.length - begin;

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
                                            length = (byteData.length - begin) > ioThread.getOutputBufferSize() ?
                                                    ioThread.getOutputBufferSize() : byteData.length - begin;
                                        }
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

                            try {
                                //Change the key operation to finish write loop
                                channel.keyFor(selectors.get(consumer).getSelector()).interestOps(SelectionKey.OP_READ);
                            } catch (Exception ex) {
                                Log.e(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Write error", ex);
                            }

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
                        if(netPackage.getSession() != null && netPackage.getPayload() != null) {
                            netPackage.getSession().addIngressPackage(netPackage.getPayload().length);
                        }
                        consumer.onRead(netPackage);
                        break;
                    case WRITE:
                        if(netPackage.getSession() != null && netPackage.getPayload() != null) {
                            netPackage.getSession().addEgressPackage(netPackage.getPayload().length);
                        }
                        consumer.onWrite(netPackage);
                        break;
                }
            } catch (Exception ex) {
                Log.e(SystemProperties.get(SystemProperties.Net.LOG_TAG), "Action consumer exception", ex);
            }
        }
    }

    /**
     * This factory create the net io threads.
     */
    private class NetIOThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable runnable) {
            return new NetIOThread(runnable);
        }

    }

    /**
     * Net IO thread.
     */
    public class NetIOThread extends ServiceThread {

        private final ByteBuffer inputBuffer;
        private final ByteBuffer outputBuffer;
        private int inputBufferSize;
        private int outputBufferSize;

        public NetIOThread(Runnable target) {
            super(target, "Net IO");
            inputBufferSize = SystemProperties.getInteger(SystemProperties.Net.DEFAULT_INPUT_BUFFER_SIZE);
            outputBufferSize = SystemProperties.getInteger(SystemProperties.Net.DEFAULT_OUTPUT_BUFFER_SIZE);
            if(SystemProperties.getBoolean(SystemProperties.Net.IO_THREAD_DIRECT_ALLOCATE_MEMORY)) {
                inputBuffer = ByteBuffer.allocateDirect(getInputBufferSize());
                outputBuffer = ByteBuffer.allocateDirect(getOutputBufferSize());
            } else {
                inputBuffer = ByteBuffer.allocate(getInputBufferSize());
                outputBuffer = ByteBuffer.allocate(getOutputBufferSize());
            }
        }

        /**
         * Return the input buffer of the thread.
         * @return Input buffer.
         */
        public final ByteBuffer getInputBuffer() {
            return inputBuffer;
        }

        /**
         * Return the output buffer of the thread.
         * @return Output buffer.
         */
        public final ByteBuffer getOutputBuffer() {
            return outputBuffer;
        }

        /**
         * Return the size of the internal buffer used to read input data.
         * @return Size of the internal input buffer.
         */
        public int getInputBufferSize() {
            return inputBufferSize;
        }

        /**
         * Return the size of the internal buffer used to write output data.
         * @return Size of the internal output buffer.
         */
        public int getOutputBufferSize() {
            return outputBufferSize;
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

        private static final String IO_NAME_TEMPLATE = "SSL IO (%s)";
        private static final String ENGINE_NAME_TEMPLATE = "SSL ENGINE (%s)";

        private final String ioName;
        private final String engineName;
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
        private ByteArrayOutputStream decryptedPlace;

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

            ioName = String.format(IO_NAME_TEMPLATE, consumer.getName());
            engineName = String.format(ENGINE_NAME_TEMPLATE, consumer.getName());

            //Start handshaking
            instance.fork(this, ioName, ioExecutor);
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
                decryptedPlace.writeBytes(decryptedArray);
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
            srcWrap.put(netPackage.getPayload());
            SSLHelper.this.run();
            DefaultNetPackage defaultNetPackage = null;
            if (status.equals(SSLHelper.SSLHelperStatus.READY)) {
                try {
                    defaultNetPackage = new DefaultNetPackage("", "",
                            0, consumer.getPort(), netPackage.getPayload(), NetPackage.ActionEvent.WRITE);
                    defaultNetPackage.setSession(netPackage.getSession());
                } catch (Exception ex) {
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
            decryptedPlace = new ByteArrayOutputStream();
            srcUnwrap.put(netPackage.getPayload());
            SSLHelper.this.run();

            byte[] arrayResult = new byte[0];
            if (status.equals(SSLHelper.SSLHelperStatus.READY)) {
                try {
                    arrayResult = decryptedPlace.toByteArray();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    decryptedPlace.reset();
                    try {
                        decryptedPlace.close();
                    } catch (IOException e) { }
                    decryptedPlace = null;
                }
            }

            DefaultNetPackage defaultNetPackage = new DefaultNetPackage("", "",
                    0, consumer.getPort(), arrayResult, NetPackage.ActionEvent.READ);
            defaultNetPackage.setSession(netPackage.getSession());
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
                        instance.fork(SSLHelper.this, ioName, ioExecutor);
                    }, engineName, engineTaskExecutor);
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
