package org.hcjf.io.net.ssl;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.log.Log;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;

import javax.net.ssl.*;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;

public abstract class SslPeer {

    public abstract ByteBuffer getMyAppData(SocketChannel socketChannel);

    public abstract void setMyAppData(SocketChannel socketChannel, ByteBuffer myAppData);

    public abstract ByteBuffer getMyNetData(SocketChannel socketChannel);

    public abstract void setMyNetData(SocketChannel socketChannel, ByteBuffer myNetData);

    public abstract ByteBuffer getPeerAppData(SocketChannel socketChannel);

    public abstract void setPeerAppData(SocketChannel socketChannel, ByteBuffer peerAppData);

    public abstract ByteBuffer getPeerNetData(SocketChannel socketChannel);

    public abstract void setPeerNetData(SocketChannel socketChannel, ByteBuffer peerNetData);

    public abstract int read(SocketChannel socketChannel, ByteBuffer buffer) throws Exception;

    public abstract void write(SocketChannel socketChannel, ByteBuffer buffer) throws Exception;

    protected abstract SSLEngine getSslEngine(SocketChannel socketChannel);

    public final boolean init(SocketChannel socketChannel) {
        try {
            getSslEngine(socketChannel).beginHandshake();
            return doHandshake(socketChannel, getSslEngine(socketChannel));
        } catch (Exception ex) {
            throw new HCJFRuntimeException("Ssl peer init fail", ex);
        }
    }

    /**
     *
     * @param socketChannel
     * @param engine
     * @return
     * @throws IOException
     */
    protected boolean doHandshake(SocketChannel socketChannel, SSLEngine engine) throws IOException {

        Log.i("SSL", "About to do handshake...");

        SSLEngineResult result;
        HandshakeStatus handshakeStatus;
        int appBufferSize = engine.getSession().getApplicationBufferSize();
        ByteBuffer myAppData = ByteBuffer.allocate(appBufferSize);
        ByteBuffer peerAppData = ByteBuffer.allocate(appBufferSize);
        getMyNetData(socketChannel).clear();
        getPeerNetData(socketChannel).clear();

        handshakeStatus = engine.getHandshakeStatus();
        while (handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED && handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            switch (handshakeStatus) {
                case NEED_UNWRAP:
                    if (socketChannel.read(getPeerNetData(socketChannel)) < 0) {
                        if (engine.isInboundDone() && engine.isOutboundDone()) {
                            return false;
                        }
                        try {
                            engine.closeInbound();
                        } catch (SSLException e) {
                            Log.i("SSL", "This engine was forced to close inbound, without having received the proper SSL/TLS close notification message from the peer, due to end of stream.");
                        }
                        engine.closeOutbound();
                        handshakeStatus = engine.getHandshakeStatus();
                        break;
                    }
                    getPeerNetData(socketChannel).flip();
                    try {
                        result = engine.unwrap(getPeerNetData(socketChannel), peerAppData);
                        getPeerNetData(socketChannel).compact();
                        handshakeStatus = result.getHandshakeStatus();
                    } catch (SSLException sslException) {
                        Log.i("SSL", "A problem was encountered while processing the data that caused the SSLEngine to abort. Will try to properly close connection...");
                        engine.closeOutbound();
                        handshakeStatus = engine.getHandshakeStatus();
                        break;
                    }
                    switch (result.getStatus()) {
                        case OK:
                            break;
                        case BUFFER_OVERFLOW:
                            peerAppData = enlargeApplicationBuffer(engine, peerAppData);
                            break;
                        case BUFFER_UNDERFLOW:
                            setPeerNetData(socketChannel, handleBufferUnderflow(engine, getPeerNetData(socketChannel)));
                            break;
                        case CLOSED:
                            if (engine.isOutboundDone()) {
                                return false;
                            } else {
                                engine.closeOutbound();
                                handshakeStatus = engine.getHandshakeStatus();
                                break;
                            }
                        default:
                            throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                    }
                    break;
                case NEED_WRAP:
                    getMyNetData(socketChannel).clear();
                    try {
                        result = engine.wrap(myAppData, getMyNetData(socketChannel));
                        handshakeStatus = result.getHandshakeStatus();
                    } catch (SSLException ex) {
                        Log.e("SSL", "A problem was encountered while processing the data that caused the SSLEngine to abort. Will try to properly close connection...", ex);
                        engine.closeOutbound();
                        handshakeStatus = engine.getHandshakeStatus();
                        break;
                    }
                    switch (result.getStatus()) {
                        case OK :
                            getMyNetData(socketChannel).flip();
                            while (getMyNetData(socketChannel).hasRemaining()) {
                                socketChannel.write(getMyNetData(socketChannel));
                            }
                            break;
                        case BUFFER_OVERFLOW:
                            setMyNetData(socketChannel, enlargePacketBuffer(engine, getMyNetData(socketChannel)));
                            break;
                        case BUFFER_UNDERFLOW:
                            throw new SSLException("Buffer underflow occurred after a wrap. I don't think we should ever get here.");
                        case CLOSED:
                            try {
                                getMyNetData(socketChannel).flip();
                                while (getMyNetData(socketChannel).hasRemaining()) {
                                    socketChannel.write(getMyNetData(socketChannel));
                                }
                                getPeerNetData(socketChannel).clear();
                            } catch (Exception e) {
                                Log.w("SSL", "Failed to send server's CLOSE message due to socket channel's failure.");
                                handshakeStatus = engine.getHandshakeStatus();
                            }
                            break;
                        default:
                            throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                    }
                    break;
                case NEED_TASK:
                    Runnable task;
                    while ((task = engine.getDelegatedTask()) != null) {
                        Service.run(task, ServiceSession.getSystemSession());
                    }
                    handshakeStatus = engine.getHandshakeStatus();
                    break;
                case FINISHED:
                case NOT_HANDSHAKING:
                    Log.w("SSL", "Not handshaking");
                    break;
                default:
                    throw new IllegalStateException("Invalid SSL status: " + handshakeStatus);
            }
        }

        Log.i("SSL", "Return handshake");
        return true;

    }

    protected ByteBuffer enlargePacketBuffer(SSLEngine engine, ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getPacketBufferSize());
    }

    protected ByteBuffer enlargeApplicationBuffer(SSLEngine engine, ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getApplicationBufferSize());
    }

    protected ByteBuffer enlargeBuffer(ByteBuffer buffer, int sessionProposedCapacity) {
        if (sessionProposedCapacity > buffer.capacity()) {
            buffer = ByteBuffer.allocate(10000);
        } else {
            buffer = ByteBuffer.allocate(buffer.capacity() * 2);
        }
        return buffer;
    }

    protected ByteBuffer handleBufferUnderflow(SSLEngine engine, ByteBuffer buffer) {
        ByteBuffer replaceBuffer = enlargePacketBuffer(engine, buffer);
        buffer.flip();
        replaceBuffer.put(buffer);
        return replaceBuffer;
    }

    public void close(SocketChannel socketChannel) throws IOException  {
        getSslEngine(socketChannel).closeOutbound();
        doHandshake(socketChannel, getSslEngine(socketChannel));
    }

    protected KeyManager[] createKeyManagers(String filepath, String keystorePassword, String keyPassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream keyStoreIS = new FileInputStream(filepath)) {
            keyStore.load(keyStoreIS, keystorePassword.toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyPassword.toCharArray());
        return kmf.getKeyManagers();
    }

    protected TrustManager[] createTrustManagers(String filepath, String keystorePassword) throws Exception {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (InputStream trustStoreIS = new FileInputStream(filepath)) {
            trustStore.load(trustStoreIS, keystorePassword.toCharArray());
        }
        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(trustStore);
        return trustFactory.getTrustManagers();
    }

}
