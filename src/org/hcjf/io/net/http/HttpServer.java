package org.hcjf.io.net.http;

import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetServer;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetSession;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;

import java.util.*;

/**
 *
 */
public class HttpServer extends NetServer<HttpSession, HttpPackage>  {

    private static final Integer DEFAULT_HTTP_PORT = 80;
    public static final String HTTP_SERVER_LOG_TAG = "HTTP_SERVER";

    private Map<NetSession, HttpRequest> requestBuffers;
    private Set<Context> contexts;

    public HttpServer() {
        this(DEFAULT_HTTP_PORT);
    }

    public HttpServer(Integer port) {
        super(port, NetService.TransportLayerProtocol.TCP, false, true);
        requestBuffers = new HashMap<>();
        contexts = new TreeSet<>(new Comparator<Context>() {

            @Override
            public int compare(Context context, Context newContext) {
                int result = context.getContextRegex().compareTo(newContext.getContextRegex());

                if(result == 0) {
                    Log.w(HTTP_SERVER_LOG_TAG, "Duplicated context: %s", context.getContextRegex());
                }

                return result;
            }

        });
    }

    /**
     * This method must implements the session creation based on
     * the net package that incoming.
     *
     * @param payLoad    Data to create the session.
     * @param netPackage Net package.
     * @return Return the session based on the package.
     */
    @Override
    protected HttpSession createSession(HttpPackage payLoad, NetPackage netPackage) {
        return new HttpSession(this, (HttpRequest)payLoad);
    }

    /**
     * This method encode the implementation data.
     *
     * @param payLoad Implementation data.
     * @return Implementation data encoded.
     */
    @Override
    protected final byte[] encode(HttpPackage payLoad) {
        return payLoad.toString().getBytes();
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
            request = new HttpRequest();
            requestBuffers.put(netPackage.getSession(), request);
        }
        request.addData(netPackage.getPayload());
        return request;
    }

    public final void addContext(Context context) {
        contexts.add(context);
    }

    /**
     *
     * @param contextName
     * @return
     */
    private Context findContext(String contextName) {
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
     *
     * @param session Net session to be destroyed
     */
    @Override
    public void destroySession(NetSession session) {
        Log.d(HTTP_SERVER_LOG_TAG, "Do something to destroy session: %s", session.getSessionId());
    }

    @Override
    protected final void onRead(HttpSession session, HttpPackage payLoad, NetPackage netPackage) {
        if(payLoad.isComplete()) {
            HttpResponse response = null;
            HttpRequest request = (HttpRequest) payLoad;
            try {
                if (request.isComplete()) {
                    Context context = findContext(request.getContext());
                    if (context != null) {
                        try {
                            Log.d(HTTP_SERVER_LOG_TAG, "Request context: %s", request.getContext());
                            response = context.onContext(request);
                        } catch (Throwable throwable) {
                            Log.e(HTTP_SERVER_LOG_TAG, "Exception on context %s", throwable, context.getContextRegex());
                            response = context.onError(request, throwable);
                        }
                    } else {

                        String body = "Context not found: " + request.getContext();

                        response = new HttpResponse();
                        response.setResponseCode(HttpResponseCode.NOT_FOUND);
                        response.setReasonPhrase("Context not found: " + request.getContext());
                        response.setBody(body.getBytes());
                        response.addHeader(new HttpHeader(HttpHeader.CONNECTION, HttpHeader.CLOSED));
                        response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, "text/plain"));
                        response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.getBytes().length)));
                    }
                }

                if(response == null) {
                    String body = "Context unresponsive: " + request.getContext();

                    response = new HttpResponse();
                    response.setResponseCode(HttpResponseCode.INTERNAL_SERVER_ERROR);
                    response.setReasonPhrase("Context unresponsive: " + request.getContext());
                    response.setBody(body.getBytes());
                    response.addHeader(new HttpHeader(HttpHeader.CONNECTION, HttpHeader.CLOSED));
                    response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, "text/plain"));
                    response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.getBytes().length)));
                }

                response.addHeader(new HttpHeader(HttpHeader.DATE,
                        SystemProperties.getFormattedDate(SystemProperties.HTTP_RESPONSE_DATE_HEADER_FORMAT_VALUE, new Date())));
                response.addHeader(new HttpHeader(HttpHeader.SERVER,
                        SystemProperties.get(SystemProperties.HTTP_SERVER_NAME)));

                write(session, response, response.getNetStreamingSource(), true);
            } catch (Throwable throwable) {
                Log.e(NetService.NET_SERVICE_LOG_TAG, "Http server error", throwable);
            } finally {
                if(response == null || response.getNetStreamingSource() == null) {
                    disconnect(session, "Http request end");
                }
            }
        }
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
     * @param payLoad
     * @param netPackage
     */
    @Override
    protected final void onDisconnect(HttpSession session, HttpPackage payLoad, NetPackage netPackage) {
        super.onDisconnect(session, payLoad, netPackage);
    }

    /**
     * When the net service write data then call this method to process the package.
     *
     * @param session    Net session.
     * @param payLoad    Net package decoded.
     * @param netPackage Net package.
     */
    @Override
    protected final void onWrite(HttpSession session, HttpPackage payLoad, NetPackage netPackage) {
        super.onWrite(session, payLoad, netPackage);
    }

    @Override
    protected void onStart() {
        Log.d(HTTP_SERVER_LOG_TAG, "Http server started, listening on port %d", getPort());
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(HTTP_SERVER_LOG_TAG, "Http server stopped.");
        super.onStop();
    }
}
