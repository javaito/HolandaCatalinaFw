package org.hcjf.io.net.http;

import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetServer;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetSession;
import org.hcjf.io.net.http.http2.Stream;
import org.hcjf.io.net.http.http2.StreamSettings;
import org.hcjf.io.net.http.pipeline.HttpPipelineResponse;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;
import org.hcjf.utils.Strings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Implementation of the net service that provides the http protocol server.
 * @author javaito
 */
public class HttpServer extends NetServer<HttpSession, HttpPackage>  {

    private final Map<NetSession, HttpRequest> requestBuffers;
    private final List<Context> contexts;
    private HttpSessionManager sessionManager;
    private HttpPackage.HttpProtocol httpProtocol;
    private final Map<String,AccessControl> accessControlMap;

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
        accessControlMap = new HashMap<>();
        httpProtocol = sslProtocol ? HttpPackage.HttpProtocol.HTTPS : HttpPackage.HttpProtocol.HTTP;
    }

    public static void create(Integer port, Context... contexts) {
        HttpServer server = new HttpServer(port);
        for(Context context : contexts) {
            server.addContext(context);
        }
        server.start();
    }

    /**
     * Adda new access control into the server.
     * @param accessControl Access control instance.
     */
    public final void addAccessControl(AccessControl accessControl) {
        accessControlMap.put(accessControl.getDomain(), accessControl);
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

        HttpSession checkedSession = sessionManager.checkSession(session, (HttpRequest) payLoad);

        Log.d(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "[CHECK_SESSION] Http session %s", session);

        return checkedSession;
    }

    /**
     * This method decode the implementation data.
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
        } catch (Exception ex){}
        return result;
    }

    /**
     * This method decode the net package to obtain the implementation data
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
    protected ContextMatcher findContext(String contextName) {
        ContextMatcher result = null;
        Matcher matcher;

        for(Context context : contexts) {
            matcher = context.getPattern().matcher(contextName);
            if(matcher.matches()) {
                result = new ContextMatcher(context, matcher);
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

            if(SystemProperties.getBoolean(SystemProperties.Net.Http.INPUT_LOG_ENABLED)) {
                Log.in(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Request\r\n%s", request.toString());
            }
            try {
                if(netPackage.getSession().isChecked()) {
                    HttpHeader upgrade = request.getHeader(HttpHeader.UPGRADE);
                    if(upgrade != null) {
                        HttpHeader connection = request.getHeader(HttpHeader.CONNECTION);
                        HttpHeader http2Settings = request.getHeader(HttpHeader.HTTP2_SETTINGS);

                        if(upgrade.getHeaderValue().trim().equalsIgnoreCase(HttpHeader.HTTP2_REQUEST)) {
                            session.setStream(new Stream(new StreamSettings()));
                            response = new HttpResponse();
                            response.setResponseCode(HttpResponseCode.SWITCHING_PROTOCOLS);
                            response.addHeader(upgrade);
                        } else {
                            throw new IllegalArgumentException("Unsupported upgrade connection " + upgrade.getHeaderValue());
                        }
                    } else {
                        ContextMatcher contextMatcher = findContext(request.getContext());
                        if (contextMatcher != null) {
                            Context context = contextMatcher.getContext();
                            HttpHeader originHeader = request.getHeader(HttpHeader.ORIGIN);
                            try {
                                request.setMatcher(contextMatcher.getMatcher());
                                Log.d(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Request context: %s", request.getContext());
                                if (originHeader != null && request.getMethod().equals(HttpMethod.OPTIONS)) {
                                    URL url = new URL(originHeader.getHeaderValue());
                                    response = new HttpResponse();
                                    if(accessControlMap.containsKey(url.getHost())) {
                                        AccessControl accessControl = accessControlMap.get(url.getHost());
                                        response.addHeader(new HttpHeader(HttpHeader.ACCESS_CONTROL_MAX_AGE, accessControl.maxAge.toString()));
                                        if(!accessControl.getAllowMethods().isEmpty()) {
                                            response.addHeader(new HttpHeader(HttpHeader.ACCESS_CONTROL_ALLOW_METHODS,
                                                    Strings.join(accessControl.getAllowMethods(), Strings.ARGUMENT_SEPARATOR)));
                                        }
                                        if(!accessControl.getAllowHeaders().isEmpty()) {
                                            response.addHeader(new HttpHeader(HttpHeader.ACCESS_CONTROL_ALLOW_HEADERS,
                                                    Strings.join(accessControl.getAllowHeaders(), Strings.ARGUMENT_SEPARATOR)));
                                        }
                                        if(!accessControl.getExposeHeaders().isEmpty()) {
                                            response.addHeader(new HttpHeader(HttpHeader.ACCESS_CONTROL_EXPOSE_HEADERS,
                                                    Strings.join(accessControl.getAllowHeaders(), Strings.ARGUMENT_SEPARATOR)));
                                        }
                                    }
                                } else {
                                    if (context.getTimeout() > 0) {
                                        response = Service.call(() -> context.onContext(request),
                                                ServiceSession.getCurrentIdentity(), context.getTimeout());
                                    } else {
                                        response = context.onContext(request);
                                    }
                                }
                                if (request.containsHeader(HttpHeader.CONNECTION)) {
                                    if (request.getHeader(HttpHeader.CONNECTION).getHeaderValue().equalsIgnoreCase(HttpHeader.KEEP_ALIVE)) {
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
                    }
                } else {
                    response = onNotCheckedSession(request);
                }
            } catch (Throwable throwable) {
                response = createDefaultErrorResponse(throwable);
            }

            response = addOriginHeader(request, response);

            try {
                response.setProtocol(httpProtocol);
                if(isContentLengthRequired(response)) {
                    Integer length = response.getBody() == null ? 0 : response.getBody().length;
                    response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, length.toString()));
                }

                if(response instanceof HttpPipelineResponse) {
                    connectionKeepAlive = true;
                    final HttpResponse finalResponse = response;
                    Service.run(() -> {
                        HttpPipelineResponse pipelineResponse = (HttpPipelineResponse) finalResponse;
                        pipelineResponse.onStart();
                        while(pipelineResponse.read() >= 0) {
                            try {
                                write(session, finalResponse, false);
                            } catch (IOException e) {
                                Log.e(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Http server error", e);
                                break;
                            }
                        }
                        pipelineResponse.onEnd();
                        disconnect(session, "Http request end.");
                    }, ServiceSession.getCurrentIdentity());
                } else {
                    write(session, response, false);
                }

                if(SystemProperties.getBoolean(SystemProperties.Net.Http.OUTPUT_LOG_ENABLED)) {
                    Log.out(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Response -> [Time: %d ms] \r\n%s",
                            (System.currentTimeMillis() - time), response.toString());
                }
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
     * Check if the request contains origin header then add the same header into the response.
     * @param request Request instance.
     * @param response Response instance.
     * @return Returns the same response instance with origin header
     */
    private HttpResponse addOriginHeader(HttpRequest request, HttpResponse response) {
        HttpHeader originHeader = request.getHeader(HttpHeader.ORIGIN);
        if(originHeader != null) {
            response.addHeader(new HttpHeader(HttpHeader.ACCESS_CONTROL_ALLOW_ORIGIN, originHeader.getHeaderValue()));
        }
        return response;
    }

    /**
     * Verify if the response needs add automatic content length header.
     * @param response Http response instance to verify this condition.
     * @return True if the is required generate the content length header and false in the otherwise.
     */
    private boolean isContentLengthRequired(HttpResponse response) {
        boolean result = false;

        if(!response.containsHeader(HttpHeader.CONTENT_LENGTH) &&
                SystemProperties.getBoolean(SystemProperties.Net.Http.ENABLE_AUTOMATIC_RESPONSE_CONTENT_LENGTH) &&
                !(response instanceof HttpPipelineResponse)) {
            result = true;

            //Verify if exist some response code to change the response value
            try {
                List<String> skipCodes = SystemProperties.getList(SystemProperties.Net.Http.AUTOMATIC_CONTENT_LENGTH_SKIP_CODES);
                String responseCodeToString = response.getResponseCode().toString();
                if(skipCodes.contains(responseCodeToString)) {
                    result = false;
                }
            } catch (Exception ex) { }
        }

        return result;
    }

    /**
     * Manages an exception thrown while trying to check session (authenticate)
     * by calling to the specific context for get an error response depending on exception information.
     * Thus, the response is written to the consumer.
     * @param session Net session.
     * @param requestPayLoad Net package decoded as {@link HttpRequest}
     * @param netPackage Net package.
     * @param exception exception
     */
    @Override
    protected void onCheckSessionError(HttpSession session, HttpPackage requestPayLoad, NetPackage netPackage, Throwable exception) {
        HttpRequest request = (HttpRequest)requestPayLoad;
        ContextMatcher contextMatcher = findContext(request.getContext());
        HttpResponse response = contextMatcher.getContext().onError(request, exception);
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
        return Context.createDefaultErrorResponse(throwable);
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
        return  Context.addDefaultResponseHeaders(response, body.getBytes());
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
        return  Context.addDefaultResponseHeaders(response, body.getBytes());
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
     * For http implementation the connection timeout is not available.
     * @return Connection timeout available.
     */
    @Override
    public boolean isCreationTimeoutAvailable() {
        return false;
    }

    /**
     * Only put in the log the moment to server stop.
     */
    @Override
    protected void onStop() {
        Log.d(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Http server stopped.");
    }

    public static class ContextMatcher {

        private final Context context;
        private final Matcher matcher;

        public ContextMatcher(Context context, Matcher matcher) {
            this.context = context;
            this.matcher = matcher;
        }

        public Context getContext() {
            return context;
        }

        public Matcher getMatcher() {
            return matcher;
        }
    }

    public static class AccessControl {

        private static Integer MAX_AGE = 86400;

        private final String domain;
        private final Integer maxAge;
        private final List<String> allowMethods;
        private final List<String> allowHeaders;
        private final List<String> exposeHeaders;

        public AccessControl(String domain, Integer maxAge) {
            this.domain = domain;
            this.maxAge = maxAge <= 0 || maxAge > MAX_AGE ? MAX_AGE : maxAge;
            this.allowMethods = new ArrayList<>();
            this.allowHeaders = new ArrayList<>();
            this.exposeHeaders = new ArrayList<>();
        }

        public AccessControl(String domain) {
            this(domain, MAX_AGE);
        }

        public String getDomain() {
            return domain;
        }

        public Integer getMaxAge() {
            return maxAge;
        }

        public List<String> getAllowMethods() {
            return Collections.unmodifiableList(allowMethods);
        }

        public List<String> getAllowHeaders() {
            return Collections.unmodifiableList(allowHeaders);
        }

        public List<String> getExposeHeaders() {
            return Collections.unmodifiableList(exposeHeaders);
        }

        public void addAllowMethod(String... methods) {
            allowMethods.addAll(Arrays.asList(methods));
        }

        public void addAllowHeader(String... headers) {
            allowHeaders.addAll(Arrays.asList(headers));
        }

        public void addExposeHeader(String... headers) {
            exposeHeaders.addAll(Arrays.asList(headers));
        }
    }
}
