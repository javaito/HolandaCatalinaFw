package org.hcjf.io.net.ssl;

import org.hcjf.log.Log;
import org.hcjf.service.ServiceSession;

import javax.net.ssl.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class SslServer extends SslPeer {

    private final SSLContext context;
    private final Map<SocketChannel, SSLEngine> engineBySocket;
    private final Map<SocketChannel, ByteBuffer> myAppDataMap;
    private final Map<SocketChannel, ByteBuffer> myNetDataMap;
    private final Map<SocketChannel, ByteBuffer> peerAppDataMap;
    private final Map<SocketChannel, ByteBuffer> peerNetDataMap;

    public SslServer(String protocol) throws Exception {
        context = SSLContext.getInstance(protocol);
        context.init(createKeyManagers("/home/javaito/Git/HolandaCatalinaFw/src/main/resources/org/hcjf/io/net/https/client.jks", "storepass", "keypass"),
                createTrustManagers("/home/javaito/Git/HolandaCatalinaFw/src/main/resources/org/hcjf/io/net/https/trustedCerts.jks", "storepass"), new SecureRandom());

        engineBySocket = new HashMap<>();
        myAppDataMap = new HashMap<>();
        myNetDataMap = new HashMap<>();
        peerAppDataMap = new HashMap<>();
        peerNetDataMap = new HashMap<>();
    }

    @Override
    public void close(SocketChannel socketChannel) throws IOException {
        super.close(socketChannel);
        myAppDataMap.remove(socketChannel);
        myNetDataMap.remove(socketChannel);
        peerAppDataMap.remove(socketChannel);
        peerNetDataMap.remove(socketChannel);
    }

    public ByteBuffer getMyAppData(SocketChannel socketChannel) {
        return myAppDataMap.get(socketChannel);
    }

    public void setMyAppData(SocketChannel socketChannel, ByteBuffer myAppData) {
        myAppDataMap.put(socketChannel, myAppData);
    }

    public ByteBuffer getMyNetData(SocketChannel socketChannel) {
        return myNetDataMap.get(socketChannel);
    }

    public void setMyNetData(SocketChannel socketChannel, ByteBuffer myNetData) {
        myNetDataMap.put(socketChannel, myNetData);
    }

    public ByteBuffer getPeerAppData(SocketChannel socketChannel) {
        return peerAppDataMap.get(socketChannel);
    }

    public void setPeerAppData(SocketChannel socketChannel, ByteBuffer peerAppData) {
        peerAppDataMap.put(socketChannel, peerAppData);
    }

    public ByteBuffer getPeerNetData(SocketChannel socketChannel) {
        return peerNetDataMap.get(socketChannel);
    }

    public void setPeerNetData(SocketChannel socketChannel, ByteBuffer peerNetData) {
        peerNetDataMap.put(socketChannel, peerNetData);
    }

    @Override
    protected synchronized SSLEngine getSslEngine(SocketChannel socketChannel) {
        SSLEngine engine = engineBySocket.get(socketChannel);
        if (engine == null) {
            SSLSession dummySession = context.createSSLEngine().getSession();
            setMyAppData(socketChannel, ByteBuffer.allocate(dummySession.getApplicationBufferSize()));
            setMyNetData(socketChannel, ByteBuffer.allocate(dummySession.getPacketBufferSize()));
            setPeerAppData(socketChannel, ByteBuffer.allocate(dummySession.getApplicationBufferSize()));
            setPeerNetData(socketChannel, ByteBuffer.allocate(dummySession.getPacketBufferSize()));
            dummySession.invalidate();

            engine = context.createSSLEngine();
            engine.setUseClientMode(false);
            engineBySocket.put(socketChannel, engine);
        }
        return engine;
    }

    /**
     * This method read
     * @param socketChannel
     * @param buffer
     * @return
     * @throws IOException
     */
    @Override
    public int read(SocketChannel socketChannel, ByteBuffer buffer) throws IOException {

        System.out.println("Thread: " + Thread.currentThread().getId());
        System.out.println("Session:" + socketChannel.hashCode());

        getPeerNetData(socketChannel).clear();
        int bytesRead = socketChannel.read(getPeerNetData(socketChannel));
        if (bytesRead > 0) {
            getPeerNetData(socketChannel).flip();
            while (getPeerNetData(socketChannel).hasRemaining()) {
                getPeerAppData(socketChannel).clear();
                SSLEngineResult result = getSslEngine(socketChannel).unwrap(getPeerNetData(socketChannel), getPeerAppData(socketChannel));
                switch (result.getStatus()) {
                    case OK:
                        getPeerAppData(socketChannel).flip();
                        bytesRead = getPeerAppData(socketChannel).limit();
                        buffer.put(getPeerAppData(socketChannel));
                        break;
                    case BUFFER_OVERFLOW:
                        setPeerAppData(socketChannel, enlargeApplicationBuffer(getSslEngine(socketChannel), getPeerAppData(socketChannel)));
                        break;
                    case BUFFER_UNDERFLOW:
                        setPeerNetData(socketChannel, handleBufferUnderflow(getSslEngine(socketChannel), getPeerNetData(socketChannel)));
                        break;
                    case CLOSED:
                        // In this case returns -1 value because it's interpreted by net service as socket close.
                        bytesRead = -1;
                        break;
                    default:
                        throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                }
            }
        }

        return bytesRead;
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
            SSLEngineResult result = getSslEngine(socketChannel).wrap(getMyAppData(socketChannel), getMyNetData(socketChannel));
            switch (result.getStatus()) {
                case OK:
                    getMyNetData(socketChannel).flip();
                    while (getMyNetData(socketChannel).hasRemaining()) {
                        socketChannel.write(getMyNetData(socketChannel));
                    }
                    Log.i("SSL", "Message sent to the client: " + message);
                    break;
                case BUFFER_OVERFLOW:
                    setMyNetData(socketChannel, enlargePacketBuffer(getSslEngine(socketChannel), getMyNetData(socketChannel)));
                    break;
                case BUFFER_UNDERFLOW:
                    throw new SSLException("Buffer underflow occurred after a wrap. I don't think we should ever get here.");
                case CLOSED:
                default:
                    throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
            }
        }
    }

}

