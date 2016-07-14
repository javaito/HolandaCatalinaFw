package org.hcjf.io.net.http;

import org.hcjf.io.net.NetStreamingSource;
import org.hcjf.io.net.StreamingNetPackage;
import org.hcjf.properties.SystemProperties;
import sun.java2d.pipe.SpanShapeRenderer;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by javaito on 13/4/2016.
 */
public class HttpResponse extends HttpPackage {

    private Integer responseCode;
    private String reasonPhrase;
    private NetStreamingSource netStreamingSource;

    public HttpResponse() {
    }

    protected HttpResponse(HttpResponse httpResponse) {
        super(httpResponse);
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * @param body
     * @return
     */
    @Override
    protected void processBody(byte[] body) {

    }

    /**
     * @param firstLine
     */
    @Override
    protected void processFirstLine(String firstLine) {

    }

    public NetStreamingSource getNetStreamingSource() {
        return netStreamingSource;
    }

    public void setNetStreamingSource(NetStreamingSource netStreamingSource) {
        this.netStreamingSource = netStreamingSource;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(getHttpVersion()).append(LINE_FIELD_SEPARATOR);
        builder.append(getResponseCode()).append(LINE_FIELD_SEPARATOR);
        builder.append(getReasonPhrase()).append(STRING_LINE_SEPARATOR);
        for(HttpHeader header : getHeaders()) {
            builder.append(header).append(STRING_LINE_SEPARATOR);
        }
        builder.append(STRING_LINE_SEPARATOR);
        if(getBody() != null) {
            if (getBody().length > 1024) {
                builder.append(new String(getBody(), 0, 1024));
            } else {
                builder.append(new String(getBody()));
            }
        }

        return builder.toString();
    }

}
