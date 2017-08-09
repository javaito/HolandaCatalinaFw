package org.hcjf.io.net.http;

import org.hcjf.encoding.MimeType;
import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetServer;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetSession;
import org.hcjf.io.net.http.pipeline.HttpPipelineResponse;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Implementation of the net service that provides the http protocol server.
 * @author javaito
 */
public class HttpServer extends NetServer<HttpSession, HttpPackage>  {

    private Map<NetSession, HttpRequest> requestBuffers;
    private List<Context> contexts;
    private HttpSessionManager sessionManager;
    private HttpPackage.HttpProtocol httpProtocol;

    public HttpServer() {
        this(SystemProperties.getInteger(SystemProperties.Net.Http.DEFAULT_SERVER_PORT));
    }

    public HttpServer(Integer port) {
        this(port, false);
    }

    protected HttpServer(Integer port, boolean sslProtocol) {
        super(port, sslProtocol ? NetService.TransportLayerProtocol.TCP_SSL :
                NetService.TransportLayerProtocol.TCP, false, true);
        requestBuffers = new HashMap<>();
        contexts = new ArrayList<>();
        httpProtocol = sslProtocol ? HttpPackage.HttpProtocol.HTTPS : HttpPackage.HttpProtocol.HTTP;
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

        HttpSession session = sessionManager.createSession(this, netPackage);

        Log.d(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "[CREATE_SESSION] Http session %s", session);

        return session;
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

        HttpSession session1 = sessionManager.checkSession(session, (HttpRequest) payLoad);

        Log.d(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "[CHECK_SESSION] Http session %s", session);

        return session1;
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
            if(payLoad instanceof HttpPipelineResponse) {
                if(((HttpPipelineResponse)payLoad).isFirstRead()) {
                    out.write(payLoad.getProtocolHeader());
                }
                ByteBuffer mainBuffer = ((HttpPipelineResponse)payLoad).getMainBuffer();
                out.write(mainBuffer.array(), 0, mainBuffer.position());
                out.flush();
            } else {
                out.write(payLoad.getProtocolHeader());
                out.write(payLoad.getBody());
                out.flush();
            }
            result = out.toByteArray();

            Log.out(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "%s", new String(result));
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
                request.setProtocol(httpProtocol);
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
            Log.i(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Context added: [%s] %s",
                    context.getClass().getName(),  context.getContextRegex());
        } else {
            Log.w(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Duplicated context: [%s] %s",
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

            //Flag to pipe line.
            boolean connectionKeepAlive = false;

            //Remove the http buffer because the payload is complete.
            requestBuffers.remove(session);

            //Value to calculate the request execution time
            long time = System.currentTimeMillis();

            HttpResponse response = null;
            HttpRequest request = (HttpRequest) payLoad;
            Log.in(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Request\r\n%s", request.toString());
            try {
                if(netPackage.getSession().isChecked()) {
                    Context context = findContext(request.getContext());
                    if (context != null) {
                        boolean originHeaderPresent = request.containsHeader(HttpHeader.ORIGIN);
                        try {
                            Log.d(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Request context: %s", request.getContext());
                            if(originHeaderPresent && request.getMethod().equals(HttpMethod.OPTIONS)){
                                //If there's a Cross-Origin-Resource-Sharing preflight request returns a empty response
                                response = new HttpResponse();
                            } else{
                                response = context.onContext(request);
                            }
                            if(request.containsHeader(HttpHeader.CONNECTION)) {
                                if(request.getHeader(HttpHeader.CONNECTION).getHeaderValue().equalsIgnoreCase(HttpHeader.KEEP_ALIVE)) {
                                    Log.d(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Http connection keep alive");
                                    connectionKeepAlive = true;
                                }
                            }
                        } catch (Throwable throwable) {
                            Log.e(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Exception on context %s", throwable, context.getContextRegex());
                            response = context.onError(request, throwable);
                            if (response == null) {
                                response = createDefaultErrorResponse(throwable);
                            }
                        } finally{
                            if(originHeaderPresent){
                                for(HttpHeader header : context.getCrossOriginHeaders(request)){
                                    response.addHeader(header);
                                }
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
                response.setProtocol(httpProtocol);
                if(!response.containsHeader(HttpHeader.CONTENT_LENGTH) &&
                        SystemProperties.getBoolean(SystemProperties.Net.Http.ENABLE_AUTOMATIC_RESPONSE_CONTENT_LENGTH) &&
                        !(response instanceof HttpPipelineResponse)) {
                    Integer length = response.getBody() == null ? 0 : response.getBody().length;
                    response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, length.toString()));
                }

                if(response instanceof HttpPipelineResponse) {
                    HttpPipelineResponse pipelineResponse = (HttpPipelineResponse) response;
                    pipelineResponse.onStart();
                    while(pipelineResponse.read() >= 0) {
                        write(session, response, false);
                    }
                    pipelineResponse.onEnd();
                } else {
                    write(session, response, false);
                }

                Log.out(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Response -> [Time: %d ms] \r\n%s",
                        (System.currentTimeMillis() - time), response.toString());
            } catch (Throwable throwable) {
                Log.e(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Http server error", throwable);
                connectionKeepAlive = false;
            } finally {
                if(!connectionKeepAlive) {
                    disconnect(session, "Http request end.");
                    Log.d(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Http connection closed by server.");
                }
            }
        }
    }

    /**
     * Manages an exception thrown while trying to check session (authenticate)
     * by calling to the specific context for get an error response depending on exception information.
     * Thus, the response is written to the consumer.
     *
     * @param session Net session.
     * @param requestPayLoad Net package decoded as {@link HttpRequest}
     * @param netPackage Net package.
     * @param exception exception
     */
    @Override
    protected void onCheckSessionError(HttpSession session, HttpPackage requestPayLoad, NetPackage netPackage, Throwable exception) {
        HttpRequest request = (HttpRequest)requestPayLoad;
        Context context = findContext(request.getContext());
        HttpResponse response = context.onError(request, exception);
        String logTag = SystemProperties.get(SystemProperties.Net.Http.LOG_TAG);
        try {
            write(session, response, false);
        }catch (Throwable throwable) {
            Log.e(logTag, "Http server error on check session error.", throwable);
        } finally {
            disconnect(session, "Http request denied end.");
            Log.d(logTag, "Http connection closed by server.");
        }
    }

    /**
     * Create default error response.
     * @param throwable Throwable
     * @return Http response package.
     */
    private HttpResponse createDefaultErrorResponse(Throwable throwable) {
        HttpResponse response = new HttpResponse();
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
     * This method must create the response package when the context not found.
     * @param request Http request.
     * @return Context not found response.
     */
    protected HttpResponse onContextNotFound(HttpRequest request) {
        HttpResponse response = new HttpResponse();
        String body = "Context not found: " + request.getContext();
        response.setResponseCode(HttpResponseCode.NOT_FOUND);
        response.setBody(body.getBytes());
        response.addHeader(new HttpHeader(HttpHeader.CONNECTION, HttpHeader.CLOSED));
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN.toString()));
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.getBytes().length)));
        return  response;
    }

    /**
     * This method must create the response package when the context result is null.
     * @param request Http request.
     * @return Unresponsive context response.
     */
    protected HttpResponse onUnresponsiveContext(HttpRequest request) {
        HttpResponse response = new HttpResponse();
        String body = "Context unresponsive: " + request.getContext();
        response.setResponseCode(HttpResponseCode.NO_CONTENT);
        response.setBody(body.getBytes());
        response.addHeader(new HttpHeader(HttpHeader.CONNECTION, HttpHeader.CLOSED));
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.TEXT_PLAIN.toString()));
        response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.getBytes().length)));
        return response;
    }

    /**
     * This method must create the response package when the session check fail.
     * @param request Http request.
     * @return Session check fail response.
     */
    protected HttpResponse onNotCheckedSession(HttpRequest request) {
        HttpResponse response = new HttpResponse();
        response.setResponseCode(HttpResponseCode.UNAUTHORIZED);
        response.addHeader(new HttpHeader(HttpHeader.CONNECTION, HttpHeader.CLOSED));
        return response;
    }

    /**
     * This method is called when the session is closed.
     * @param session Closed session.
     * @param netPackage Close package.
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
        Log.d(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Http server started, listening on port %d", getPort());
    }

    /**
     * Only put in the log the moment to server stop.
     */
    @Override
    protected void onStop() {
        Log.d(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Http server stopped.");
    }

}
