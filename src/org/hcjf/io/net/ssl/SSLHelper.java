package org.hcjf.io.net.ssl;

import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetServiceConsumer;
import org.hcjf.properties.SystemProperties;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * This helper provides
 * @author javaito
 * @email javaito@gmail.com
 */
public final class SSLHelper implements Runnable {

    private SSLEngine sslEngine;
    private final NetServiceConsumer netServiceConsumer;
    private final Executor ioExecutor;
    private final Executor engineTaskExecutor;
    private final ByteBuffer srcWrap;
    private final ByteBuffer destWrap;
    private final ByteBuffer srcUnwrap;
    private final ByteBuffer destUnwrap;
    private Object result;

    public SSLHelper(SSLEngine sslEngine, NetServiceConsumer netServiceConsumer) {
        this.sslEngine = sslEngine;
        this.netServiceConsumer = netServiceConsumer;
        this.ioExecutor = Executors.newSingleThreadExecutor();
        this.engineTaskExecutor = Executors.newFixedThreadPool(
                SystemProperties.getInteger(SystemProperties.NET_SSL_MAX_IO_THREAD_POOL_SIZE));
        if(SystemProperties.getBoolean(SystemProperties.NET_IO_THREAD_DIRECT_ALLOCATE_MEMORY)) {
            srcWrap = ByteBuffer.allocateDirect(SystemProperties.getInteger(SystemProperties.NET_OUTPUT_BUFFER_SIZE));
            destWrap = ByteBuffer.allocateDirect(SystemProperties.getInteger(SystemProperties.NET_OUTPUT_BUFFER_SIZE));
            srcUnwrap = ByteBuffer.allocateDirect(SystemProperties.getInteger(SystemProperties.NET_INPUT_BUFFER_SIZE));
            destUnwrap = ByteBuffer.allocateDirect(SystemProperties.getInteger(SystemProperties.NET_INPUT_BUFFER_SIZE));
        } else {
            srcWrap = ByteBuffer.allocate(SystemProperties.getInteger(SystemProperties.NET_OUTPUT_BUFFER_SIZE));
            destWrap = ByteBuffer.allocate(SystemProperties.getInteger(SystemProperties.NET_OUTPUT_BUFFER_SIZE));
            srcUnwrap = ByteBuffer.allocate(SystemProperties.getInteger(SystemProperties.NET_INPUT_BUFFER_SIZE));
            destUnwrap = ByteBuffer.allocate(SystemProperties.getInteger(SystemProperties.NET_INPUT_BUFFER_SIZE));
        }
        this.ioExecutor.execute(this);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param decrypted
     */
    private void onRead(ByteBuffer decrypted) {
        byte[] decryptedArray = new byte[decrypted.limit()];
        decrypted.get(decryptedArray);
        result = decryptedArray;
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     *
     * @param encrypted
     */
    private void onWrite(ByteBuffer encrypted) {
        byte[] encryptedArray = new byte[encrypted.limit()];
        encrypted.get(encryptedArray);

        result = encryptedArray;
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     *
     * @param ex
     */
    private void onFailure(Exception ex) {
        result = ex;
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     *
     */
    private void onSuccess() {
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     *
     */
    private void onClosed() {

    }

    /**
     *
     */
    @Override
    public void run() {
        while (this.isHandShaking()) {
            continue;
        }
    }

    /**
     *
     * @param netPackage
     */
    public NetPackage write(NetPackage netPackage) {
        this.ioExecutor.execute(() -> {
            srcWrap.put(netPackage.getPayload());
            SSLHelper.this.run();
        });

        while(result == null) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }

        if(result instanceof Exception) {
            throw new RuntimeException((Exception) result);
        }

        NetPackage resultNetPackage = NetPackage.wrap(netPackage, (byte[])result);
        result = null;
        return resultNetPackage;
    }

    /**
     *
     * @param netPackage
     */
    public synchronized NetPackage read(NetPackage netPackage) {
        this.ioExecutor.execute(() -> {
            srcUnwrap.put(netPackage.getPayload());
            SSLHelper.this.run();
        });

        while(result == null) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }

        if(result instanceof Exception) {
            throw new RuntimeException((Exception) result);
        }

        NetPackage resultNetPackage = NetPackage.wrap(netPackage, (byte[])result);
        result = null;
        return resultNetPackage;
    }

    /**
     *
     * @return
     */
    private synchronized boolean isHandShaking() {
        switch (sslEngine.getHandshakeStatus()) {
            case NOT_HANDSHAKING:
                boolean occupied = false;{
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
                Runnable wrappedTask = new Runnable() {
                    @Override
                    public void run()
                    {
                        sslTask.run();
                        ioExecutor.execute(SSLHelper.this);
                    }
                };
                engineTaskExecutor.execute(wrappedTask);
                return false;

            case FINISHED:
                throw new IllegalStateException("FINISHED");
        }

        return true;
    }

    /**
     *
     * @return
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

        return true;
    }

    /**
     *
     * @return
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
}
