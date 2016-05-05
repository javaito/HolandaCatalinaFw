package org.hcjf.io.net.http;

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

    public HttpResponse() {
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

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(getHttpVersion()).append(LINE_FIELD_SEPARATOR);
        result.append(getResponseCode()).append(LINE_FIELD_SEPARATOR);
        result.append(getReasonPhrase()).append(STRING_LINE_SEPARATOR);
        String separator = "";
        for(HttpHeader header : getHeaders()) {
            result.append(separator).append(header);
            separator = STRING_LINE_SEPARATOR;
        }
        if(getBody() != null) {
            result.append(STRING_LINE_SEPARATOR).append(STRING_LINE_SEPARATOR);
            result.append(new String(getBody()));
        }

        return result.toString();
    }

}
