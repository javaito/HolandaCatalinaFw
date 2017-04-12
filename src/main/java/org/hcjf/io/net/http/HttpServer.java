package org.hcjf.io.net.http;

import org.hcjf.encoding.MimeType;
import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetServer;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetSession;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

/**
 *
 * @author javaito
 * @email javaito@gmail.com
 */
public class HttpServer extends NetServer<HttpSession, HttpPackage>  {

    public static final String HTTP_SERVER_LOG_TAG = "HTTP_SERVER";

    private Map<NetSession, HttpRequest> requestBuffers;
    private List<Context> contexts;
    private HttpSessionManager sessionManager;

    public HttpServer() {
        this(SystemProperties.getInteger(SystemProperties.Net.Http.DEFAULT_SERVER_PORT));
    }

    public HttpServer(Integer port) {
        super(port, NetService.TransportLayerProtocol.TCP, false, true);
        requestBuffers = new HashMap<>();
        contexts = new ArrayList<>();
    }

    /**
     * Return the instance of the session factory.
     * @return Session factory.
     */
    public final HttpSessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * Set the instance of the session factory.
     * @param sessionManager Session factory.
     */
    public final void setSessionManager(HttpSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * This method must implements the session creation based on
     * the net package that incoming.
     * @param netPackage Net package.
     * @return Return the session based on the package.
     */
    @Override
    public final HttpSession createSession(NetPackage netPackage) {
        HttpSessionManager sessionManager = getSessionManager();
        if(sessionManager == null) {
            sessionManager = HttpSessionManager.DEFAULT;
        }
        return sessionManager.createSession(this, netPackage);
    }

    /**
     * This method must update the http session with all the information of the request.
     * @param session Current session.
     * @param payLoad Decoded package.
     * @param netPackage Net package.
     * @return Updated http session.
     */
    @Override
    public HttpSession checkSession(HttpSession session, HttpPackage payLoad, NetPackage netPackage) {
        HttpSessionManager sessionManager = getSessionManager();
        if(sessionManager == null) {
            sessionManager = HttpSessionManager.DEFAULT;
        }
        return sessionManager.checkSession(session, (HttpRequest) payLoad);
    }

    /**
     * This method decode the implementation data.
     *
     * @param payLoad Implementation data.
     * @return Implementation data encoded.
     */
    @Override
    protected final byte[] encode(HttpPackage payLoad) {
        byte[] result = null;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            out.write(payLoad.getProtocolHeader());
            out.write(payLoad.getBody());
            out.flush();
            result = out.toByteArray();
        } catch (Exception ex){}
        return result;
    }

    /**
     * This method decode the net package to obtain the implementation data
     *
     * @param netPackage Net package.
     * @return Return the implementation data.
     */
    @Override
    protected final HttpPackage decode(NetPackage netPackage) {
        HttpRequest request = requestBuffers.get(netPackage.getSession());
        if(request == null){
            synchronized (requestBuffers) {
                request = new HttpRequest();
                requestBuffers.put(netPackage.getSession(), request);
            }
        }
        request.addData(netPackage.getPayload());
        return request;
    }

    /**
     * Add context to the server.
     * @param context Context instance.
     */
    public synchronized void addContext(Context context) {
        boolean duplicated = false;
        for(Context ctx : contexts) {
            if(ctx.getContextRegex().equals(context.getContextRegex())) {
                duplicated = true;
                break;
            }
        }

        if(!duplicated) {
            contexts.add(context);
            Log.i(HTTP_SERVER_LOG_TAG, "Context added: [%s] %s",
                    context.getClass().getName(),  context.getContextRegex());
        } else {
            Log.w(HTTP_SERVER_LOG_TAG, "Duplicated context: [%s] %s",
                    context.getClass().getName(),  context.getContextRegex());
        }
    }

    /**
     * Find the context instance that response to the request's context name.
     * @param contextName Request's context name.
     * @return Founded context.
     */
    protected Context findContext(String contextName) {
        Context result = null;

        for(Context context : contexts) {
            if(contextName.matches(context.getContextRegex())) {
                result = context;
                break;
            }
        }

        return result;
    }

    /**
     * Destroy the session.
     * @param session Net session to be destroyed
     */
    @Override
    public void destroySession(NetSession session) {
        HttpSessionManager sessionManager = getSessionManager();
        if(sessionManager == null) {
            sessionManager = HttpSessionManager.DEFAULT;
        }
        sessionManager.destroySession((HttpSession) session);
    }

    /**
     * First check if the package is complete, then try to found the context using the
     * http request information a create the response package.
     * @param session Net session.
     * @param payLoad Net package decoded
     * @param netPackage Net package.
     */
    @Override
    protected final void onRead(HttpSession session, HttpPackage payLoad, NetPackage netPackage) {
        if(payLoad.isComplete()) {
            boolean connectionKeepAlive = false;
            long time = System.currentTimeMillis();
            HttpResponse response = null;
            HttpRequest request = (HttpRequest) payLoad;
            Log.in(HTTP_SERVER_LOG_TAG, "Request\r\n%s", request.toString());
            try {
                if(netPackage.getSession().isChecked()) {
                    Context context = findContext(request.getContext());
                    if (context != null) {
                        try {
                            Log.d(HTTP_SERVER_LOG_TAG, "Request context: %s", request.getContext());
                            response = context.onContext(request);
                            if(request.containsHeader(HttpHeader.CONNECTION)) {
                                if(request.getHeader(HttpHeader.CONNECTION).getHeaderValue().equalsIgnoreCase(HttpHeader.KEEP_ALIVE)) {
                                    Log.d(HTTP_SERVER_LOG_TAG, "Http connection keep alive");
                                    connectionKeepAlive = true;
                                }
                                response.addHeader(request.getHeader(HttpHeader.CONNECTION));
                            }
                            if(response.getNetStreamingSource() != null) {
                                connectionKeepAlive = true;
                            }
                        } catch (Throwable throwable) {
                            Log.e(HTTP_SERVER_LOG_TAG, "Exception on context %s", throwable, context.getContextRegex());
                            response = context.onError(request, throwable);
                            if (response == null) {
                                response = createDefaultErrorResponse(throwable);
                            }
                        }
                    } else {
                        response = onContextNotFound(request);
                    }

                    if (response == null) {
                        response = onUnresponsiveContext(request);
                    }

                    response.addHeader(new HttpHeader(HttpHeader.DATE,

                            SystemProperties.getDateFormat(
                                    SystemProperties.Net.Http.RESPONSE_DATE_HEADER_FORMAT_VALUE).format(new Date())));
                    response.addHeader(new HttpHeader(HttpHeader.SERVER,
                            SystemProperties.get(SystemProperties.Net.Http.SERVER_NAME)));
                } else {
                    response = onNotCheckedSession(request);
                }
            } catch (Throwable throwable) {
                response = createDefaultErrorResponse(throwable);
            }

            try {
                write(session, response, response.getNetStreamingSource(), false);
                Log.out(HTTP_SERVER_LOG_TAG, "Response -> [Time: %d ms] \r\n%s",
                        (System.currentTimeMillis() - time), response.toString());
            } catch (Throwable throwable) {
                Log.e(NetService.NET_SERVICE_LOG_TAG, "Http server error", throwable);
                connectionKeepAlive = false;
            } finally {
                if(!connectionKeepAlive) {
                    disconnect(session, "Http request end");
                }
            }
        }
    }

    /**
     * Create default error response.
     * @param throwable Throwable
     * @return Http response package.
     */
    private HttpResponse createDefaultErrorResponse(Throwable throwable) {
        HttpResponse response = new HttpResponse();
        response.setReasonPhrase(throwable.getMessage());
        response.setResponseCode(HttpResponseCode.INTERNAL_SERVER_ERROR);

        byte[] body;
        if(SystemProperties.getBoolean(SystemProperties.Net.Http.DEFAULT_ERROR_FORMAT_SHOW_STACK)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream printer = new PrintStream(out);
            throwable.printStackTrace(printer);
            body = out.toByteArray();
        } else {
            body = throwable.getMessage().getBytes();
        }

        response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Long.toString(body.length)));
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN.toString()));
        response.setBody(body);
        return response;
    }

    /**
     *
     * @param request
     * @return
     */
    protected HttpResponse onContextNotFound(HttpRequest request) {
        HttpResponse response = new HttpResponse();
        String body = "Context not found: " + request.getContext();
        response.setResponseCode(HttpResponseCode.NOT_FOUND);
        response.setReasonPhrase("Context not found: " + request.getContext());
        response.setBody(body.getBytes());
        response.addHeader(new HttpHeader(HttpHeader.CONNECTION, HttpHeader.CLOSED));
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN.toString()));
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.getBytes().length)));
        return  response;
    }

    /**
     *
     * @param request
     * @return
     */
    protected HttpResponse onUnresponsiveContext(HttpRequest request) {
        HttpResponse response = new HttpResponse();
        String body = "Context unresponsive: " + request.getContext();
        response.setResponseCode(HttpResponseCode.NO_CONTENT);
        response.setReasonPhrase("Context unresponsive: " + request.getContext());
        response.setBody(body.getBytes());
        response.addHeader(new HttpHeader(HttpHeader.CONNECTION, HttpHeader.CLOSED));
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN.toString()));
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.getBytes().length)));
        return response;
    }

    /**
     *
     * @param request
     * @return
     */
    protected HttpResponse onNotCheckedSession(HttpRequest request) {
        HttpResponse response = new HttpResponse();
        response.setResponseCode(HttpResponseCode.UNAUTHORIZED);
        response.setReasonPhrase("Unchecked session: " + request.getContext());
        response.addHeader(new HttpHeader(HttpHeader.CONNECTION, HttpHeader.CLOSED));
        return response;
    }

    /**
     *
     * @param session
     * @param payLoad
     * @param netPackage
     */
    @Override
    protected final void onConnect(HttpSession session, HttpPackage payLoad, NetPackage netPackage) {
        super.onConnect(session, payLoad, netPackage);
    }

    /**
     * @param session
     * @param netPackage
     */
    @Override
    protected final void onDisconnect(HttpSession session, NetPackage netPackage) {
        requestBuffers.remove(session);
    }

    /**
     * When the net service write data then call this method to process the package.
     * @param session    Net session.
     * @param netPackage Net package.
     */
    @Override
    protected final void onWrite(HttpSession session, NetPackage netPackage) {
    }

    /**
     * Only put in the log the moment to server start.
     */
    @Override
    protected void onStart() {
        Log.d(HTTP_SERVER_LOG_TAG, "Http server started, listening on port %d", getPort());
    }

    /**
     * Only put in the log the moment to server stop.
     */
    @Override
    protected void onStop() {
        Log.d(HTTP_SERVER_LOG_TAG, "Http server stopped.");
    }

}
