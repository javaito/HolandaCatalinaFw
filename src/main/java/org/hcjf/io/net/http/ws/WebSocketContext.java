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
 * Clase base para implementar endpoints WebSocket integrados al pipeline de HttpServer.
 *
 * Al agregar un WebSocketContext al HttpServer mediante addContext(), el servidor
 * detecta automáticamente los requests de upgrade (Upgrade: websocket), realiza
 * el handshake RFC 6455 y comienza a enrutar frames al contexto correspondiente.
 *
 * Uso típico:
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
     * @param contextRegex Regex del path que atiende este contexto (e.g. "/ws/events").
     */
    public WebSocketContext(String contextRegex) {
        super(contextRegex);
    }

    /**
     * Este método no debería ser invocado directamente para conexiones WebSocket.
     * HttpServer intercepta el upgrade antes de llegar aquí.
     * Si el cliente accede al path sin header de upgrade, retorna 400.
     */
    @Override
    public final HttpResponse onContext(HttpRequest request) {
        HttpResponse response = new HttpResponse();
        response.setResponseCode(HttpResponseCode.BAD_REQUEST);
        return response;
    }

    // ── Ciclo de vida interno (llamado por HttpServer) ────────────────────

    /**
     * Registra la sesión como activa y notifica onOpen.
     * @param session Sesión HTTP que completó el handshake WS.
     * @param server  Referencia al server para poder enviar frames.
     */
    public final void registerSession(HttpSession session, HttpServer server) {
        this.server = server;
        sessions.put(session.getId(), session);
        onOpen(session);
    }

    /**
     * Elimina la sesión y notifica onClose.
     * @param session Sesión que se desconectó.
     */
    public final void unregisterSession(HttpSession session) {
        if (sessions.remove(session.getId()) != null) {
            onClose(session);
        }
    }

    /**
     * Despacha un frame recibido al hook correspondiente.
     * Los frames PING son respondidos automáticamente con PONG.
     * @param session Sesión origen del frame.
     * @param frame   Frame decodificado.
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
                // El cierre del canal lo maneja HttpServer; aquí solo notificamos
                break;
            default:
                break;
        }
    }

    // ── API para subclases ────────────────────────────────────────────────

    /**
     * Envía un mensaje de texto a una sesión específica.
     * @param session Sesión destino.
     * @param message Texto a enviar.
     */
    protected final void sendText(HttpSession session, String message) {
        server.sendWebSocketData(session, WebSocketFrame.encodeText(message));
    }

    /**
     * Envía datos binarios a una sesión específica.
     * @param session Sesión destino.
     * @param data    Datos a enviar.
     */
    protected final void sendBinary(HttpSession session, byte[] data) {
        server.sendWebSocketData(session, WebSocketFrame.encodeBinary(data));
    }

    /**
     * Envía un mensaje de texto a todas las sesiones activas en este contexto.
     * @param message Texto a broadcast.
     */
    protected final void broadcast(String message) {
        byte[] frame = WebSocketFrame.encodeText(message);
        for (HttpSession session : sessions.values()) {
            server.sendWebSocketData(session, frame);
        }
    }

    /**
     * Retorna una vista no modificable de las sesiones activas.
     * @return Sesiones activas.
     */
    public Collection<HttpSession> getSessions() {
        return Collections.unmodifiableCollection(sessions.values());
    }

    /**
     * Retorna el número de sesiones activas.
     * @return Cantidad de sesiones.
     */
    public int getSessionCount() {
        return sessions.size();
    }

    // ── Hooks abstractos ─────────────────────────────────────────────────

    /**
     * Llamado cuando un cliente completa el handshake WebSocket.
     * @param session Sesión del nuevo cliente.
     */
    public abstract void onOpen(HttpSession session);

    /**
     * Llamado cuando se recibe un frame TEXT del cliente.
     * @param session Sesión origen.
     * @param message Texto recibido.
     */
    public abstract void onMessage(HttpSession session, String message);

    /**
     * Llamado cuando se recibe un frame BINARY del cliente.
     * Por defecto no hace nada; sobreescribir si se necesita.
     * @param session Sesión origen.
     * @param data    Datos binarios recibidos.
     */
    public void onBinaryMessage(HttpSession session, byte[] data) {}

    /**
     * Llamado cuando el cliente cierra la conexión o se desconecta abruptamente.
     * @param session Sesión que se cerró.
     */
    public abstract void onClose(HttpSession session);
}
