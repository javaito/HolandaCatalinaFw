package org.hcjf.io.console;

import org.hcjf.io.console.messages.EvaluateQueryableMessage;
import org.hcjf.io.console.messages.ExecuteMessage;
import org.hcjf.io.console.messages.GetMetadataMessage;
import org.hcjf.io.console.messages.LoginMessage;
import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.messages.Message;
import org.hcjf.io.net.messages.MessageBuffer;
import org.hcjf.io.net.messages.MessagesServer;
import org.hcjf.io.net.messages.ResponseMessage;
import org.hcjf.layers.Layers;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Queryable;
import org.hcjf.service.ServiceSession;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * This server accept connections from hcj-console instances.
 * @author javaito
 */
public abstract class ConsoleServer extends MessagesServer<ConsoleSession> {

    public ConsoleServer(Integer port) {
        super(port, NetService.TransportLayerProtocol.TCP,
                false, true);
    }

    /**
     * Creates a default session.
     * @param netPackage Net package.
     * @return Session instance.
     */
    @Override
    public final ConsoleSession createSession(NetPackage netPackage) {
        return new ConsoleSession(UUID.randomUUID(), this);
    }

    /**
     * Check the session.
     * @param session Created session.
     * @param payLoad Decoded package.
     * @param netPackage Net package.
     * @return Session checked.
     */
    @Override
    public final ConsoleSession checkSession(ConsoleSession session, MessageBuffer payLoad, NetPackage netPackage) {
        session.setChecked(true);
        return session;
    }

    /**
     * Delegate the message for the different methods.
     * @param session Net session.
     * @param message Incoming message.
     */
    @Override
    protected final void onRead(ConsoleSession session, Message message) {
        ResponseMessage responseMessage = new ResponseMessage(message);
        try {
            if (message instanceof GetMetadataMessage) {
                responseMessage.setValue(getMetadata());
            } else if (message instanceof LoginMessage) {
                ServiceSession serviceSession = login(((LoginMessage) message).getParameters());
                SessionMetadata sessionMetadata = new SessionMetadata();
                sessionMetadata.setId(serviceSession.getId());
                sessionMetadata.setSessionName(serviceSession.getSessionName());
                responseMessage.setValue(sessionMetadata);
            } else if (message instanceof EvaluateQueryableMessage) {
                ServiceSession serviceSession = ServiceSession.findSession(message.getSessionId());
                try {
                    ServiceSession.getCurrentIdentity().addIdentity(serviceSession);
                    responseMessage.setValue(evaluate(((EvaluateQueryableMessage) message).getQueryable()));
                } finally {
                    ServiceSession.getCurrentIdentity().removeIdentity();
                }
            } else if (message instanceof ExecuteMessage) {
                ServiceSession serviceSession = ServiceSession.findSession(message.getSessionId());
                try {
                    ServiceSession.getCurrentIdentity().addIdentity(serviceSession);
                    ConsoleCommandLayerInterface consoleCommandLayerInterface = Layers.get(
                            ConsoleCommandLayerInterface.class,
                            ((ExecuteMessage) message).getCommandName());
                    responseMessage.setValue(consoleCommandLayerInterface.execute(((ExecuteMessage) message).getParameters()));
                } finally {
                    ServiceSession.getCurrentIdentity().removeIdentity();
                }
            }
        } catch (Exception ex) {
            responseMessage.setThrowable(ex);
        }
        try {
            send(session, responseMessage);
        } catch (IOException e) {
        }
    }

    /**
     * Returns the metadata of the server.
     * @return Metadata of the server.
     */
    protected abstract ServerMetadata getMetadata();

    /**
     * Make the login in the server.
     * @param parameters Login parameters.
     * @return Returns the service session instance.
     */
    protected abstract ServiceSession login(Map<String,Object> parameters);

    /**
     * Evaluate a queryable in the server.
     * @param queryable Queryable instance.
     * @return Queryable result set.
     */
    protected abstract Collection<JoinableMap> evaluate(Queryable queryable);
}
