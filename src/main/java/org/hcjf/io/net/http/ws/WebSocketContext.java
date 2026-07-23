package org.hcjf.io.net.http.ws;

import org.hcjf.io.net.http.Context;
import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.HttpResponse;
import org.hcjf.io.net.http.HttpResponseCode;
import org.hcjf.io.net.http.HttpServer;
import org.hcjf.io.net.http.HttpSession;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for implementing WebSocket endpoints integrated into the HttpServer pipeline.
 *
 * When a WebSocketContext is added to the HttpServer via addContext(), the server
 * automatically detects upgrade requests (Upgrade: websocket), performs the
 * RFC 6455 handshake and starts routing frames to the corresponding context.
 *
 * Typical usage:
 * <pre>
 *   HttpServer server = new HttpServer(8080);
 *   server.addContext(new WebSocketContext("/ws/events") {
 *       public void onOpen(HttpSession session) { ... }
 *       public void onMessage(HttpSession session, String message) { ... }
 *       public void onClose(HttpSession session) { ... }
 *   });
 *   server.start();
 * </pre>
 *
 * @author javaito
 */
public abstract class WebSocketContext extends Context {

    private HttpServer server;
    private final Map<UUID, HttpSession> sessions = new ConcurrentHashMap<>();

    /**
     * @param contextRegex Regex of the path served by this context (e.g. "/ws/events").
     */
    public WebSocketContext(String contextRegex) {
        super(contextRegex);
    }

    /**
     * This method should not be invoked directly for WebSocket connections.
     * HttpServer intercepts the upgrade before reaching here.
     * If the client accesses the path without an upgrade header, returns 400.
     */
    @Override
    public final HttpResponse onContext(HttpRequest request) {
        HttpResponse response = new HttpResponse();
        response.setResponseCode(HttpResponseCode.BAD_REQUEST);
        return response;
    }

    // ── Internal lifecycle (called by HttpServer) ────────────────────────

    /**
     * Registers the session as active and notifies onOpen.
     * @param session HTTP session that completed the WS handshake.
     * @param server  Reference to the server to be able to send frames.
     */
    public final void registerSession(HttpSession session, HttpServer server) {
        this.server = server;
        sessions.put(session.getId(), session);
        onOpen(session);
    }

    /**
     * Removes the session and notifies onClose.
     * @param session Session that disconnected.
     */
    public final void unregisterSession(HttpSession session) {
        if (sessions.remove(session.getId()) != null) {
            onClose(session);
        }
    }

    /**
     * Dispatches a received frame to the corresponding hook.
     * PING frames are automatically answered with PONG.
     * @param session Source session of the frame.
     * @param frame   Decoded frame.
     */
    public final void dispatch(HttpSession session, WebSocketFrame frame) {
        switch (frame.getOpcode()) {
            case TEXT:
                onMessage(session, frame.getText());
                break;
            case BINARY:
                onBinaryMessage(session, frame.getPayload());
                break;
            case PING:
                server.sendWebSocketData(session, WebSocketFrame.encodePong(frame.getPayload()));
                break;
            case CLOSE:
                // The channel close is handled by HttpServer; here we only notify
                break;
            default:
                break;
        }
    }

    // ── API for subclasses ────────────────────────────────────────────────

    /**
     * Sends a text message to a specific session.
     * @param session Target session.
     * @param message Text to send.
     */
    protected final void sendText(HttpSession session, String message) {
        server.sendWebSocketData(session, WebSocketFrame.encodeText(message));
    }

    /**
     * Sends binary data to a specific session.
     * @param session Target session.
     * @param data    Data to send.
     */
    protected final void sendBinary(HttpSession session, byte[] data) {
        server.sendWebSocketData(session, WebSocketFrame.encodeBinary(data));
    }

    /**
     * Sends a text message to all active sessions in this context.
     * @param message Text to broadcast.
     */
    protected final void broadcast(String message) {
        byte[] frame = WebSocketFrame.encodeText(message);
        for (HttpSession session : sessions.values()) {
            server.sendWebSocketData(session, frame);
        }
    }

    /**
     * Returns an unmodifiable view of the active sessions.
     * @return Active sessions.
     */
    public Collection<HttpSession> getSessions() {
        return Collections.unmodifiableCollection(sessions.values());
    }

    /**
     * Returns the number of active sessions.
     * @return Session count.
     */
    public int getSessionCount() {
        return sessions.size();
    }

    // ── Abstract hooks ────────────────────────────────────────────────────

    /**
     * Called when a client completes the WebSocket handshake.
     * @param session Session of the new client.
     */
    public abstract void onOpen(HttpSession session);

    /**
     * Called when a TEXT frame is received from the client.
     * @param session Source session.
     * @param message Received text.
     */
    public abstract void onMessage(HttpSession session, String message);

    /**
     * Called when a BINARY frame is received from the client.
     * Does nothing by default; override if needed.
     * @param session Source session.
     * @param data    Received binary data.
     */
    public void onBinaryMessage(HttpSession session, byte[] data) {}

    /**
     * Called when the client closes the connection or disconnects abruptly.
     * @param session Session that was closed.
     */
    public abstract void onClose(HttpSession session);
}
