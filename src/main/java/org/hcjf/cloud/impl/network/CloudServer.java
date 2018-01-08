package org.hcjf.cloud.impl.network;

import org.hcjf.cloud.impl.messages.ShutdownMessage;
import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetServer;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetSession;
import org.hcjf.properties.SystemProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author javaito
 */
public class CloudServer extends NetServer<CloudSession, MessageBuffer> {

    private final Map<CloudSession,MessageBuffer> buffersBySession;

    public CloudServer() {
        super(SystemProperties.getInteger(SystemProperties.Cloud.DefaultImpl.SERVER_LISTENER_PORT),
                NetService.TransportLayerProtocol.TCP,
                false, true);
        buffersBySession = new HashMap<>();
    }

    @Override
    public CloudSession createSession(NetPackage netPackage) {
        CloudSession session = new CloudSession( this);
        return session;
    }

    @Override
    public CloudSession checkSession(CloudSession session, MessageBuffer payLoad, NetPackage netPackage) {
        return session;
    }

    @Override
    protected byte[] encode(MessageBuffer payLoad) {
        return payLoad.getBytes();
    }

    @Override
    protected MessageBuffer decode(NetPackage netPackage) {
        MessageBuffer messageBuffer = buffersBySession.remove(netPackage.getSession());
        if(messageBuffer == null) {
            messageBuffer = new MessageBuffer();
        }
        messageBuffer.append(netPackage.getPayload());
        if(!messageBuffer.isComplete()) {
            buffersBySession.put((CloudSession) netPackage.getSession(), messageBuffer);
        }
        return messageBuffer;
    }

    @Override
    public void destroySession(NetSession session) {
        if(session instanceof CloudSession) {
            buffersBySession.remove(session);
        }
    }

    @Override
    protected MessageBuffer getShutdownPackage(CloudSession session) {
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.append(new ShutdownMessage(session));
        return messageBuffer;
    }

    @Override
    protected void onDisconnect(CloudSession session, NetPackage netPackage) {
        CloudImpl.getInstance().connectionLost(session);
    }
}
