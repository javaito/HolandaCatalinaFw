package org.hcjf.io.net.ssl;

import org.hcjf.io.net.DefaultNetPackage;
import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetServiceConsumer;
import org.hcjf.io.net.NetSession;
import org.hcjf.properties.SystemProperties;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * This helper provides the ssl life cycle.
 * @author javaito
 */
public final class SSLHelper implements Runnable {

    private SSLEngine sslEngine;
    private final SelectableChannel selectableChannel;
    private final Executor ioExecutor;
    private final Executor engineTaskExecutor;
    private final ByteBuffer srcWrap;
    private final ByteBuffer destWrap;
    private final ByteBuffer srcUnwrap;
    private final ByteBuffer destUnwrap;
    private SSLHelperStatus status;
    private  final Object handShakingSemaphore;
    private final NetServiceConsumer consumer;
    private final NetSession session;
    private Object result;

    /**
     * SSL Helper default constructor.
     * @param sslEngine SSL Engine.
     * @param selectableChannel Selectable channel.
     */
    public SSLHelper(SSLEngine sslEngine, SelectableChannel selectableChannel, NetServiceConsumer consumer, NetSession session) {
        this.sslEngine = sslEngine;
        this.selectableChannel = selectableChannel;
        this.ioExecutor = Executors.newSingleThreadExecutor();
        this.consumer = consumer;
        this.session = session;
        this.engineTaskExecutor = Executors.newFixedThreadPool(
                SystemProperties.getInteger(SystemProperties.Net.SSL_MAX_IO_THREAD_POOL_SIZE));
        if(SystemProperties.getBoolean(SystemProperties.Net.IO_THREAD_DIRECT_ALLOCATE_MEMORY)) {
            srcWrap = ByteBuffer.allocateDirect(SystemProperties.getInteger(SystemProperties.Net.OUTPUT_BUFFER_SIZE) * 128);
            destWrap = ByteBuffer.allocateDirect(SystemProperties.getInteger(SystemProperties.Net.OUTPUT_BUFFER_SIZE) * 128);
            srcUnwrap = ByteBuffer.allocateDirect(SystemProperties.getInteger(SystemProperties.Net.INPUT_BUFFER_SIZE) * 128);
            destUnwrap = ByteBuffer.allocateDirect(SystemProperties.getInteger(SystemProperties.Net.INPUT_BUFFER_SIZE) * 128);
        } else {
            srcWrap = ByteBuffer.allocate(SystemProperties.getInteger(SystemProperties.Net.OUTPUT_BUFFER_SIZE) * 128);
            destWrap = ByteBuffer.allocate(SystemProperties.getInteger(SystemProperties.Net.OUTPUT_BUFFER_SIZE) * 128);
            srcUnwrap = ByteBuffer.allocate(SystemProperties.getInteger(SystemProperties.Net.INPUT_BUFFER_SIZE) * 128);
            destUnwrap = ByteBuffer.allocate(SystemProperties.getInteger(SystemProperties.Net.INPUT_BUFFER_SIZE) * 128);
        }
        handShakingSemaphore = new Object();
        srcUnwrap.limit(0);
        status = SSLHelperStatus.WAITING;
        this.ioExecutor.execute(this);
    }

    /**
     * Return the helper status.
     * @return Helper status.
     */
    public SSLHelperStatus getStatus() {
        return status;
    }

    /**
     * This method is called when there are data into the read buffer.
     * @param decrypted Read buffer.
     */
    private void onRead(ByteBuffer decrypted) {
        byte[] decryptedArray = new byte[decrypted.limit()];
        decrypted.get(decryptedArray);
        if(status.equals(SSLHelperStatus.READY)) {
            DefaultNetPackage defaultNetPackage = new DefaultNetPackage("", "", 0, consumer.getPort(), decryptedArray, NetPackage.ActionEvent.READ);
            defaultNetPackage.setSession(session);
            consumer.onRead(defaultNetPackage);
        }
    }

    /**
     * This method is called when there are data into the write buffer.
     * @param encrypted Write buffer.
     */
    private void onWrite(ByteBuffer encrypted) {
        try {
            ((SocketChannel)selectableChannel).write(encrypted);
        } catch (IOException e) {
            e.printStackTrace();
        }
        encrypted.rewind();
        byte[] decryptedArray = new byte[encrypted.limit()];
        encrypted.rewind();
        encrypted.get(decryptedArray);
        result = decryptedArray;
        if(status.equals(SSLHelperStatus.READY)) {
            DefaultNetPackage defaultNetPackage = new DefaultNetPackage("", "", 0, consumer.getPort(), decryptedArray, NetPackage.ActionEvent.WRITE);
            defaultNetPackage.setSession(session);
            consumer.onWrite(defaultNetPackage);
        }
    }

    /**
     * This method is called when the operation fail.
     * @param ex Fail exception.
     */
    private void onFailure(Exception ex) {
        status = SSLHelperStatus.FAIL;
    }

    /**
     * This method is called when the operation is success.
     */
    private void onSuccess() {
        status = SSLHelperStatus.READY;
        DefaultNetPackage defaultNetPackage = new DefaultNetPackage("", "", 0, consumer.getPort(), new byte[0], NetPackage.ActionEvent.CONNECT);
        defaultNetPackage.setSession(session);
        consumer.onConnect(defaultNetPackage);
    }

    /**
     * This method is called when the helper is closed.
     */
    private void onClosed() {
        System.out.println("SSL CLOSE");
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
     * @param netPackage Net package.
     * @return Net package.
     */
    public NetPackage write(NetPackage netPackage) {
        this.ioExecutor.execute(() -> {
            srcWrap.put(netPackage.getPayload());
            SSLHelper.this.run();
        });

        return netPackage;
    }

    /**
     * Read data from the associated channel.
     * @param netPackage Net package.
     * @return Input data.
     */
    public NetPackage read(NetPackage netPackage) {
        this.ioExecutor.execute(() -> {
            srcUnwrap.put(netPackage.getPayload());
            SSLHelper.this.run();
        });

        return netPackage;
    }

    /**
     * Return boolean to indicate if the hand shaking process is running.
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
                Runnable wrappedTask = () -> {
                    sslTask.run();
                    ioExecutor.execute(SSLHelper.this);
                };
                engineTaskExecutor.execute(wrappedTask);
                return false;

            case FINISHED:
                throw new IllegalStateException("FINISHED");
        }

        return true;
    }

    /**
     * Wrap the output data.
     * @return Return true if the process was success.
     */
    private boolean wrap() {
        SSLEngineResult wrapResult;

        try {
            srcWrap.flip();
            wrapResult = sslEngine.wrap(srcWrap, destWrap);
            srcWrap.compact();
        }
        catch (SSLException exc) {
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
                throw new IllegalStateException("failed to wrap");

            case CLOSED:
                this.onClosed();
                return false;
        }

        if (wrapResult.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
            this.onSuccess();
            return false;
        }

        return true;
    }

    /**
     * Unwrap the input data.
     * @return Return true if the process was success.
     */
    private boolean unwrap() {
        SSLEngineResult unwrapResult;

        try {
            srcUnwrap.flip();
            unwrapResult = sslEngine.unwrap(srcUnwrap, destUnwrap);
            srcUnwrap.compact();
        }
        catch (SSLException ex) {
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
                throw new IllegalStateException("failed to unwrap");

            case BUFFER_UNDERFLOW:
                return false;
        }

        if (unwrapResult.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
            this.onSuccess();
            return false;
        }

        return true;
    }

    /**
     * Contains all the possible helper status.
     */
    public static enum SSLHelperStatus {

        WAITING,

        READY,

        FAIL

    }
}
