package org.hcjf.cloud.impl.network;

import org.hcjf.io.net.messages.Message;
import org.hcjf.cloud.impl.messages.ShutdownMessage;
import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.messages.MessageBuffer;
import org.hcjf.io.net.messages.MessagesServer;
import org.hcjf.properties.SystemProperties;

/**
 * @author javaito
 */
public class CloudServer extends MessagesServer<CloudSession> {

    public CloudServer() {
        super(SystemProperties.getInteger(SystemProperties.Cloud.Orchestrator.SERVER_LISTENER_PORT),
                NetService.TransportLayerProtocol.TCP,
                false, true);
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
    protected MessageBuffer getShutdownPackage(CloudSession session) {
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.append(new ShutdownMessage(session));
        return messageBuffer;
    }

    @Override
    protected void onRead(CloudSession session, Message message) {
        CloudOrchestrator.getInstance().incomingMessage(session, message);
    }
}
