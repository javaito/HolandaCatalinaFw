package org.hcjf.io.net;

import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.ServiceConsumer;

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
 * @email javaito@gmail.com
 */
public abstract class NetServiceConsumer<S extends NetSession, D extends Object> implements ServiceConsumer {

    private static final int MAX_IO_THREAD_POOL_SIZE = 15000;
    private static final int DEFAULT_INPUT_BUFFER_SIZE = 1024;
    private static final int DEFAULT_OUTPUT_BUFFER_SIZE = 1024;

    private final Integer port;
    private final NetService.TransportLayerProtocol protocol;
    private NetService service;
    private final ThreadPoolExecutor ioExecutor;
    private int inputBufferSize;
    private int outputBufferSize;
    private long writeWaitForTimeout;
    private final Map<NetPackage, Thread> waitForMap;

    public NetServiceConsumer(Integer port, NetService.TransportLayerProtocol protocol) {
        this.port = port;
        this.protocol = protocol;
        ioExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool(new NetIOThreadFactory());
        ioExecutor.setKeepAliveTime(10, TimeUnit.SECONDS);
        ioExecutor.setMaximumPoolSize(MAX_IO_THREAD_POOL_SIZE);
        inputBufferSize = DEFAULT_INPUT_BUFFER_SIZE;
        outputBufferSize = DEFAULT_OUTPUT_BUFFER_SIZE;
        writeWaitForTimeout = SystemProperties.getLong(SystemProperties.NET_WRITE_TIMEOUT);
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
     * Return the transport layer protocol of the consumer.
     * @return Transport layer consumer.
     */
    public final NetService.TransportLayerProtocol getProtocol() {
        return protocol;
    }

    /**
     *
     * @param session
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
     */
    protected final void write(S session, D payLoad) throws IOException {
        write(session, payLoad, null, true);
    }

    /**
     * This method writes some data over the session indicated.
     * @param session Net session.
     * @param payLoad Data to be written.
     * @param waitFor If this parameter is true then the operation generate
     *                a blocking over the communication channel.
     */
    protected final void write(S session, D payLoad, NetStreamingSource source, boolean waitFor) throws IOException {
        NetPackage netPackage = service.writeData(session, encode(payLoad));

        if(waitFor) {
            synchronized (session) {
                waitForMap.put(netPackage, Thread.currentThread());
                if (netPackage.getPackageStatus().equals(NetPackage.PackageStatus.WAITING)) {
                    try {
                        Log.d(NetService.NET_SERVICE_LOG_TAG, "Session waiting %s ....", session.getSessionId());
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
        onDisconnect((S) netPackage.getSession(), decode(netPackage), netPackage);
    }

    /**
     * Method must be implemented by the custom implementation to known when a session is disconnected
     * @param session Disconnected session.
     * @param payLoad Decoded package payload.
     * @param netPackage Original package.
     */
    protected void onDisconnect(S session, D payLoad, NetPackage netPackage) {}

    /**
     * When the net service receive data call this method to process the package
     * @param netPackage Net package.
     */
    public final void onRead(NetPackage netPackage) {
        onRead((S)netPackage.getSession(), decode(netPackage), netPackage);
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
            if(waitForMap.containsKey(netPackage)) {
                netPackage.getSession().notify();
                Log.d(NetService.NET_SERVICE_LOG_TAG, "Session notified %s ....", netPackage.getSession().getSessionId());
            }
        }
        onWrite((S)netPackage.getSession(), decode(netPackage), netPackage);
    }

    /**
     * When the net service write data then call this method to process the package.
     * @param session Net session.
     * @param payLoad Net package decoded.
     * @param netPackage Net package.
     */
    protected void onWrite(S session, D payLoad, NetPackage netPackage){}

    /**
     * This method encode the implementation data.
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
    public class NetIOThread extends Thread {

        private final ByteBuffer inputBuffer;
        private final ByteBuffer outputBuffer;

        public NetIOThread(Runnable target) {
            super(target, "Net IO");
            inputBuffer = ByteBuffer.allocate(getInputBufferSize());
            outputBuffer = ByteBuffer.allocate(getOutputBufferSize());
        }

        /**
         * Return the input buffer of the thread.
         * @return Input buffer.
         */
        public ByteBuffer getInputBuffer() {
            return inputBuffer;
        }

        /**
         * Return the output buffer of the thread.
         * @return Output buffer.
         */
        public ByteBuffer getOutputBuffer() {
            return outputBuffer;
        }

    }
}