package org.hcjf.io.net.http;

import org.hcjf.io.net.NetClient;
import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetSession;
import org.hcjf.properties.SystemProperties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class HttpClient extends NetClient<HttpSession, HttpPackage> {

    public static final String HTTP_CLIENT_LOG_TAG = "HTTP_CLIENT";
    private static final String HTTP_PROTOCOL = "http";
    private static final String HTTPS_PROTOCOL = "https";

    private final URL url;
    private Status status;
    private HttpResponse response;
    private HttpRequest request;
    private Long connectTimeout;
    private Long writeTimeout;
    private Long readTimeout;
    private HttpSession session;

    public HttpClient(URL url) {
        super(url.getHost(), url.getPort() == -1 ? url.getDefaultPort() :
                SystemProperties.getInteger(SystemProperties.HTTP_DEFAULT_CLIENT_PORT),
                url.getProtocol().equals(HTTPS_PROTOCOL) ? NetService.TransportLayerProtocol.TCP_SSL : NetService.TransportLayerProtocol.TCP);
        this.url = url;
        this.connectTimeout = SystemProperties.getLong(SystemProperties.HTTP_DEFAULT_CLIENT_CONNECT_TIMEOUT);
        this.writeTimeout = SystemProperties.getLong(SystemProperties.HTTP_DEFAULT_CLIENT_WRITE_TIMEOUT);
        this.readTimeout = SystemProperties.getLong(SystemProperties.HTTP_DEFAULT_CLIENT_READ_TIMEOUT);
        init();
    }

    private void init() {
        //Init defaults
        this.response = null;
        this.status = Status.INACTIVE;

        //Create default request
        request = new HttpRequest();
        request.setHttpVersion(HttpVersion.VERSION_1_1);
        request.setContext(url.getFile());
        request.setMethod(HttpMethod.GET);
        request.addHeader(new HttpHeader(HttpHeader.HOST, url.getHost()));
        request.addHeader(new HttpHeader(HttpHeader.USER_AGENT, HttpHeader.DEFAULT_USER_AGENT));
    }

    /**
     *
     * @return
     */
    public final Long getConnectTimeout() {
        return connectTimeout;
    }

    /**
     *
     * @param connectTimeout
     */
    public final void setConnectTimeout(Long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     *
     * @return
     */
    public final Long getWriteTimeout() {
        return writeTimeout;
    }

    /**
     *
     * @param writeTimeout
     */
    public final void setWriteTimeout(Long writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    /**
     *
     * @return
     */
    public final Long getReadTimeout() {
        return readTimeout;
    }

    /**
     *
     * @param readTimeout
     */
    public final void setReadTimeout(Long readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     *
     */
    public final void reset() {
        init();
    }

    /**
     *
     * @param method
     */
    public final void setHttpMethod(HttpMethod method) {
        request.setMethod(method);
    }

    /**
     *
     * @param header
     */
    public final void addHttpHeader(String header) {
        request.addHeader(new HttpHeader(header));
    }

    /**
     *
     * @param header
     */
    public final void addHttpHeader(HttpHeader header) {
        request.addHeader(header);
    }

    @Override
    protected SSLEngine createSSLEngine() {
        try {
            SSLEngine engine = SSLContext.getDefault().createSSLEngine();
            engine.setUseClientMode(true);
            engine.beginHandshake();
            return engine;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unsupported ssl engine", ex);
        }
    }

    /**
     * This method return the object that represent the
     * client's session.
     *
     * @return Client's session.
     */
    @Override
    public HttpSession getSession() {
        return session;
    }

    /**
     * This method decode the implementation data.
     *
     * @param payLoad Implementation data.
     * @return Implementation data encoded.
     */
    @Override
    protected byte[] encode(HttpPackage payLoad) {
        byte[] result = null;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            out.write(payLoad.getProtocolHeader());
            if(payLoad.getBody() != null) {
                out.write(payLoad.getBody());
            }
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
    protected HttpPackage decode(NetPackage netPackage) {
        if(response == null) {
            response = new HttpResponse();
        }
        response.addData(netPackage.getPayload());
        return response;
    }

    /**
     * Destroy the session.
     *
     * @param session Net session to be destroyed
     */
    @Override
    public void destroySession(NetSession session) {
        session = null;
    }

    /**
     *
     * @param session Connected session.
     * @param payLoad Decoded package payload.
     * @param netPackage Original package.
     */
    @Override
    protected void onConnect(HttpSession session, HttpPackage payLoad, NetPackage netPackage) {
        synchronized (this) {
            status = Status.CONNECTED;
            notifyAll();
        }
    }

    /**
     *
     * @param session Net session.
     * @param netPackage Net package.
     */
    @Override
    protected void onWrite(HttpSession session, NetPackage netPackage) {
    }

    /**
     *
     * @param session Net session.
     * @param payLoad Net package decoded
     * @param netPackage Net package.
     */
    @Override
    protected final void onRead(HttpSession session, HttpPackage payLoad, NetPackage netPackage) {
        if(response.isComplete()) {
            synchronized (this) {
                status = Status.DONE;
                notifyAll();
            }
        }
    }

    /**
     * @return
     */
    public final HttpResponse request() {
        session = new HttpSession(this, request);
        Integer errorCode = null;
        String errorPhrase = null;

        //Connection block
        status = Status.CONNECTING;
        connect();
        synchronized (this) {
            if (status == Status.CONNECTING) {
                try {
                    wait(getConnectTimeout());
                } catch (InterruptedException e) {}
            }

            if (status == Status.CONNECTING) {
                status = Status.ERROR;
                errorCode = HttpResponseCode.REQUEST_TIMEOUT;
                errorPhrase = "Connect timeout";
            }
        }

        //Request writing / response read block
        if(status != Status.ERROR) {
            status = Status.WRITING;
            try {
                write(getSession(), request);
            } catch (IOException ex) {
                status = Status.ERROR;
                errorCode = HttpResponseCode.BAD_REQUEST;
                errorPhrase = ex.getMessage();
            }

            synchronized (this) {
                if (status == Status.WRITING) {
                    try {
                        wait(getReadTimeout());
                    } catch (InterruptedException e) {
                    }
                }

                if (status == Status.WRITING) {
                    status = Status.ERROR;
                    errorCode = HttpResponseCode.REQUEST_TIMEOUT;
                    errorPhrase = "Read timeout";
                }
            }
        }

        HttpResponse response;
        if(status == Status.ERROR) {
            response = new HttpResponse();
            response.setHttpVersion(HttpVersion.VERSION_1_1);
            response.setResponseCode(errorCode);
            response.setReasonPhrase(errorPhrase);
        } else {
            response = this.response;
        }

        disconnect(getSession(), "");
        return response;
    }

    private enum Status {

        INACTIVE,

        CONNECTING,

        CONNECTED,

        WRITING,

        DONE,

        ERROR;

    }
}
