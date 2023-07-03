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
    private ByteBuffer previousNetData;

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
                setMyAppData(socketChannel, ByteBuffer.allocate(1000000));
                setMyNetData(socketChannel, ByteBuffer.allocate(1000000));
                setPeerAppData(socketChannel, ByteBuffer.allocate(1000000));
                setPeerNetData(socketChannel, ByteBuffer.allocate(1000000));
            }
            return engine;
        } catch (Exception ex) {
            throw new HCJFRuntimeException("SSL Engine creation fail", ex);
        }
    }

    @Override
    public synchronized void write(SocketChannel socketChannel, ByteBuffer message) throws IOException {
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
    public synchronized int read(SocketChannel socketChannel, ByteBuffer buffer) throws Exception  {
        boolean exitReadLoop = false;
        int bytesRead = 0;
        while (!exitReadLoop) {
            try {
                System.out.println("Peer net buffer remaining at begin: " + getPeerNetData(socketChannel).remaining());
                getPeerNetData(socketChannel).clear();
                if(previousNetData != null) {
                    getPeerNetData(socketChannel).put(previousNetData);
                }
                bytesRead = socketChannel.read(getPeerNetData(socketChannel));
                if (bytesRead > 0) {
                    getPeerNetData(socketChannel).flip();
                    while (getPeerNetData(socketChannel).hasRemaining()) {
                        getPeerAppData(socketChannel).clear();
                        SSLEngineResult result = engine.unwrap(getPeerNetData(socketChannel), getPeerAppData(socketChannel));
                        switch (result.getStatus()) {
                            case OK:
                                System.out.println("SSL Ok");
                                getPeerAppData(socketChannel).flip();
                                getPeerAppData(socketChannel).limit();
                                buffer.put(getPeerAppData(socketChannel));
                                previousNetData = null;
                                exitReadLoop = true;
                                break;
                            case BUFFER_OVERFLOW:
                                System.out.println("SSL Overflow");
                                setPeerAppData(socketChannel, enlargeApplicationBuffer(engine, getPeerAppData(socketChannel)));
                                break;
                            case BUFFER_UNDERFLOW:
                                System.out.println("SSL Underflow");
                                byte[] previous = new byte[getPeerNetData(socketChannel).remaining()];
                                getPeerNetData(socketChannel).get(previous);
                                previousNetData = ByteBuffer.wrap(previous);
                                exitReadLoop = true;
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
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println("Read loop ends");
        return bytesRead;
    }

}

