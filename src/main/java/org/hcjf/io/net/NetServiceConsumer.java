package org.hcjf.io.net;

import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.ServiceConsumer;
import org.hcjf.service.ServiceThread;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This consumer provide an interface for the net service.
 * @author javaito
 */
public abstract class NetServiceConsumer<S extends NetSession, D extends Object> implements ServiceConsumer {

    private final Integer port;
    private final NetService.TransportLayerProtocol protocol;
    private NetService service;
    private final ThreadPoolExecutor ioExecutor;
    private int inputBufferSize;
    private int outputBufferSize;
    private long writeWaitForTimeout;
    private final Map<S, Thread> waitForMap;

    public NetServiceConsumer(Integer port, NetService.TransportLayerProtocol protocol) {
        this.port = port;
        this.protocol = protocol;
        ioExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool(new NetIOThreadFactory());
        ioExecutor.setKeepAliveTime(SystemProperties.getInteger(SystemProperties.Net.IO_THREAD_POOL_KEEP_ALIVE_TIME), TimeUnit.SECONDS);
        if(this instanceof NetClient) {
            ioExecutor.setMaximumPoolSize(1);
        } else {
            ioExecutor.setCorePoolSize(SystemProperties.getInteger(SystemProperties.Net.IO_THREAD_POOL_CORE_SIZE));
            ioExecutor.setMaximumPoolSize(SystemProperties.getInteger(SystemProperties.Net.IO_THREAD_POOL_MAX_SIZE));
        }
        inputBufferSize = SystemProperties.getInteger(SystemProperties.Net.DEFAULT_INPUT_BUFFER_SIZE);
        outputBufferSize = SystemProperties.getInteger(SystemProperties.Net.DEFAULT_OUTPUT_BUFFER_SIZE);
        writeWaitForTimeout = SystemProperties.getLong(SystemProperties.Net.WRITE_TIMEOUT);
        waitForMap = new HashMap<>();
    }

    /**
     * Return the waiting time to write a package.
     * @return Waiting time to write a apackage.
     */
    public long getWriteWaitForTimeout() {
        return writeWaitForTimeout;
    }

    /**
     * Set the waiting time to write a package.
     * @param writeWaitForTimeout Waiting time to write a package.
     */
    public void setWriteWaitForTimeout(long writeWaitForTimeout) {
        this.writeWaitForTimeout = writeWaitForTimeout;
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

    /**
     * Thread pool exclusively for handling I/O operations server
     * @return Thread pool.
     */
    public ThreadPoolExecutor getIoExecutor() {
        return ioExecutor;
    }

    /**
     * This method ser the reference to net service,
     * this method only can be called from the net service
     * that will be associated
     * @param service Net service that will be associated.
     * @throws SecurityException If the method was called from other
     * method that not is NetService.registerConsumer().
     */
    public final void setService(NetService service) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        if(element.getClassName().equals(NetService.class.getName()) &&
                element.getMethodName().equals("registerConsumer")) {
            this.service = service;
        } else {
            throw new SecurityException("The method 'NetServiceConsumer.setService() only can be called from " +
                    "the net service that will be associated.'");
        }
    }

    /**
     * Return the port of the consumer.
     * @return Port.
     */
    public final Integer getPort() {
        return port;
    }

    /**
     * This method should create the ssl engine for the consumer.
     * @return SSL engine implementation.
     */
    protected SSLEngine getSSLEngine() {
        throw new  UnsupportedOperationException("Unsupported ssl engine");
    }

    /**
     * Return the transport layer protocol of the consumer.
     * @return Transport layer consumer.
     */
    public final NetService.TransportLayerProtocol getProtocol() {
        return protocol;
    }

    /**
     * Disconnect the specific session.
     * @param session Net session.
     * @param message Disconnection message.
     */
    protected final void disconnect(S session, String message) {
        service.disconnect(session, message);
    }

    /**
     * This method writes some data over the session indicated,
     * this operation generate a blocking until the net service confirm
     * that the data was written over the communication channel
     * @param session Net session.
     * @param payLoad Data to be written
     * @throws IOException Exception for the io operations
     */
    protected final void write(S session, D payLoad) throws IOException {
        write(session, payLoad, true);
    }

    /**
     * This method writes some data over the session indicated.
     * @param session Net session.
     * @param payLoad Data to be written.
     * @param waitFor If this parameter is true then the operation generate
     *                a blocking over the communication channel.
     * @throws IOException Exception for io operations
     */
    protected final void write(S session, D payLoad, boolean waitFor) throws IOException {
        if(waitFor) {
            NetPackage netPackage;
            synchronized (session) {
                waitForMap.put(session, Thread.currentThread());
                netPackage = service.writeData(session, encode(payLoad));
                while (netPackage.getPackageStatus().equals(NetPackage.PackageStatus.WAITING)) {
                    try {
                        session.wait(getWriteWaitForTimeout());
                    } catch (InterruptedException e) {
                        Log.w(NetService.NET_SERVICE_LOG_TAG, "Write wait for interrupted", e);
                    }
                }
            }

            waitForMap.remove(netPackage);

            switch (netPackage.getPackageStatus()) {
                case CONNECTION_CLOSE: {
                    throw new IOException("Connection Close");
                }
                case IO_ERROR: {
                    throw new IOException("IO Error");
                }
                case REJECTED_SESSION_LOCK: {
                    throw new IOException("Session locked");
                }
                case UNKNOWN_SESSION: {
                    throw new IOException("Unknown session");
                }
            }
        } else {
            NetPackage netPackage = service.writeData(session, encode(payLoad));
        }
    }

    /**
     * This method abstracts the connection event to use the entities of the domain's implementation.
     * @param netPackage Connection package.
     */
    public final void onConnect(NetPackage netPackage) {
        onConnect((S) netPackage.getSession(), decode(netPackage), netPackage);
    }

    /**
     * Method that must be implemented by the custom implementation to know when a session is connected
     * @param session Connected session.
     * @param payLoad Decoded package payload.
     * @param netPackage Original package.
     */
    protected void onConnect(S session, D payLoad, NetPackage netPackage) {}

    /**
     * This method abstracts the disconnection event to use the entities of the domain's implementation.
     * @param netPackage Disconnection package.
     */
    public final void onDisconnect(NetPackage netPackage) {
        synchronized (netPackage.getSession()) {
            if(waitForMap.containsKey(netPackage.getSession())) {
                netPackage.getSession().notify();
            }
        }

        onDisconnect((S) netPackage.getSession(), netPackage);
    }

    /**
     * Method must be implemented by the custom implementation to known when a session is disconnected
     * @param session Disconnected session.
     * @param netPackage Original package.
     */
    protected void onDisconnect(S session, NetPackage netPackage) {}

    /**
     * When the net service receive data call this method to process the package
     * @param netPackage Net package.
     */
    public final void onRead(NetPackage netPackage) {
        S session = (S) netPackage.getSession();
        D decodedPackage = decode(netPackage);
        try {
            session = checkSession(session, decodedPackage, netPackage);
            session.setChecked(true);
        } catch (Exception ex){
            Log.w(NetService.NET_SERVICE_LOG_TAG, "Check session fail", ex);
            session.setChecked(false);
        }

        onRead(session, decodedPackage, netPackage);
    }

    /**
     * When the net service receive data call this method to process the package
     * @param session Net session.
     * @param payLoad Net package decoded
     * @param netPackage Net package.
     */
    protected void onRead(S session, D payLoad, NetPackage netPackage) {}

    /**
     * When the net service write data then call this method to process the package.
     * @param netPackage Net package.
     */
    public final void onWrite(NetPackage netPackage) {
        synchronized (netPackage.getSession()) {
            if(waitForMap.containsKey(netPackage.getSession())) {
                netPackage.getSession().notify();
            }
        }
        onWrite((S)netPackage.getSession(), netPackage);
    }

    /**
     * When the net service write data then call this method to process the package.
     * @param session Net session.
     * @param netPackage Net package.
     */
    protected void onWrite(S session, NetPackage netPackage){}

    /**
     * This method decode the implementation data.
     * @param payLoad Implementation data.
     * @return Implementation data encoded.
     */
    protected abstract byte[] encode(D payLoad);

    /**
     * This method decode the net package to obtain the implementation data
     * @param netPackage Net package.
     * @return Return the implementation data.
     */
    protected abstract D decode(NetPackage netPackage);

    /**
     * Destroy the session.
     * @param session Net session to be destroyed
     */
    public abstract void destroySession(NetSession session);

    /**
     * Check the channel session.
     * @param session Created session.
     * @param payLoad Decoded package.
     * @param netPackage Net package.
     * @return Updated session.
     */
    public abstract S checkSession(S session, D payLoad, NetPackage netPackage);

    /**
     * Return the socket options of the implementation.
     * @return Socket options.
     */
    public Map<SocketOption, Object> getSocketOptions() {
        return null;
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

        public NetIOThread(Runnable target) {
            super(target, "Net IO");
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

    }
}