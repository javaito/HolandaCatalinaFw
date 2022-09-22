package org.hcjf.io.net.ssl;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.log.Log;

import javax.net.ssl.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SslClient extends SslPeer {

    private final String protocol;
    private final String remoteAddress;
    private final Integer port;

    private SSLEngine engine;
    private ByteBuffer myAppData;
    private ByteBuffer myNetData;
    private ByteBuffer peerAppData;
    private ByteBuffer peerNetData;

    public ByteBuffer getMyAppData(SocketChannel socketChannel) {
        return myAppData;
    }

    public void setMyAppData(SocketChannel socketChannel, ByteBuffer myAppData) {
        this.myAppData = myAppData;
    }

    public ByteBuffer getMyNetData(SocketChannel socketChannel) {
        return myNetData;
    }

    public void setMyNetData(SocketChannel socketChannel, ByteBuffer myNetData) {
        this.myNetData = myNetData;
    }

    public ByteBuffer getPeerAppData(SocketChannel socketChannel) {
        return peerAppData;
    }

    public void setPeerAppData(SocketChannel socketChannel, ByteBuffer peerAppData) {
        this.peerAppData = peerAppData;
    }

    public ByteBuffer getPeerNetData(SocketChannel socketChannel) {
        return peerNetData;
    }

    public void setPeerNetData(SocketChannel socketChannel, ByteBuffer peerNetData) {
        this.peerNetData = peerNetData;
    }

    public SslClient(String protocol, String remoteAddress, int port) throws Exception  {
        this.protocol = protocol;
        this.remoteAddress = remoteAddress;
        this.port = port;
    }

    @Override
    protected synchronized SSLEngine getSslEngine(SocketChannel socketChannel) {
        try {
            if (engine == null) {
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }

                            public void checkClientTrusted(
                                    java.security.cert.X509Certificate[] certs, String authType) {
                            }

                            public void checkServerTrusted(
                                    java.security.cert.X509Certificate[] certs, String authType) {
                            }
                        }
                };
                SSLContext sslContext = SSLContext.getInstance(protocol);
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

                engine = sslContext.createSSLEngine(remoteAddress, port);
                engine.setUseClientMode(true);

                SSLSession session = engine.getSession();
                setMyAppData(socketChannel, ByteBuffer.allocate(session.getPacketBufferSize()));
                setMyNetData(socketChannel, ByteBuffer.allocate(10080));
                setPeerAppData(socketChannel, ByteBuffer.allocate(session.getPacketBufferSize()));
                setPeerNetData(socketChannel, ByteBuffer.allocate(10080));
            }
            return engine;
        } catch (Exception ex) {
            throw new HCJFRuntimeException("SSL Engine creation fail", ex);
        }
    }

    @Override
    public void write(SocketChannel socketChannel, ByteBuffer message) throws IOException {
        getMyAppData(socketChannel).clear();
        getMyAppData(socketChannel).put(message);
        getMyAppData(socketChannel).flip();
        while (getMyAppData(socketChannel).hasRemaining()) {
            // The loop has a meaning for (outgoing) messages larger than 16KB.
            // Every wrap call will remove 16KB from the original message and send it to the remote peer.
            getMyNetData(socketChannel).clear();
            SSLEngineResult result = engine.wrap(getMyAppData(socketChannel), getMyNetData(socketChannel));
            switch (result.getStatus()) {
                case OK:
                    getMyNetData(socketChannel).flip();
                    while (getMyNetData(socketChannel).hasRemaining()) {
                        socketChannel.write(getMyNetData(socketChannel));
                    }
                    break;
                case BUFFER_OVERFLOW:
                    setMyNetData(socketChannel, enlargePacketBuffer(engine, getMyNetData(socketChannel)));
                    break;
                case BUFFER_UNDERFLOW:
                    throw new SSLException("Buffer underflow occurs after a wrap");
                case CLOSED:
                    return;
                default:
                    throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
            }
        }

    }

    @Override
    public int read(SocketChannel socketChannel, ByteBuffer buffer) throws Exception  {
        getPeerNetData(socketChannel).clear();
        boolean exitReadLoop = false;
        int bytesRead = 0;
        while (!exitReadLoop) {
            bytesRead += socketChannel.read(getPeerNetData(socketChannel));
            if (bytesRead > 0) {
                getPeerNetData(socketChannel).flip();
                while (getPeerNetData(socketChannel).hasRemaining()) {
                    getPeerAppData(socketChannel).clear();
                    SSLEngineResult result = engine.unwrap(getPeerNetData(socketChannel), getPeerAppData(socketChannel));
                    switch (result.getStatus()) {
                        case OK:
                            getPeerAppData(socketChannel).flip();
                            bytesRead += getPeerAppData(socketChannel).limit();
                            buffer.put(getPeerAppData(socketChannel));
                            exitReadLoop = true;
                            break;
                        case BUFFER_OVERFLOW:
                            setPeerAppData(socketChannel, enlargeApplicationBuffer(engine, getPeerAppData(socketChannel)));
                            break;
                        case BUFFER_UNDERFLOW:
                            setPeerNetData(socketChannel, handleBufferUnderflow(engine, getPeerNetData(socketChannel)));
                            break;
                        case CLOSED:
                            bytesRead = -1;
                            break;
                        default:
                            throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                    }
                }
            } else {
                break;
            }
        }

        return bytesRead;
    }

}

