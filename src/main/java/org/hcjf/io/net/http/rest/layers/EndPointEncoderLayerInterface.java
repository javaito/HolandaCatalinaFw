package org.hcjf.io.net.http.rest.layers;

import com.google.gson.Gson;
import org.hcjf.encoding.MimeType;
import org.hcjf.io.net.http.HttpHeader;
import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.HttpResponse;
import org.hcjf.io.net.http.HttpResponseCode;
import org.hcjf.io.net.http.rest.EndPointRequest;
import org.hcjf.io.net.http.rest.EndPointResponse;
import org.hcjf.layers.Layer;
import org.hcjf.layers.LayerInterface;

/**
 * This interface provides the statement to encode the end point result.
 * @author javaito
 */
public interface EndPointEncoderLayerInterface extends LayerInterface {

    /**
     * This method must create a http response object using the information
     * that the crud invocation returns.
     * @param request Invocation request.
     * @param response Invocation result.
     * @return Http response object.
     */
    public HttpResponse encode(EndPointRequest request, EndPointResponse response);

    /**
     * This method must create a http response object using the throwable information.
     * @param request Invocation request.
     * @param throwable Throwable information.
     * @return Http response object.
     */
    public HttpResponse encode(HttpRequest request, Throwable throwable);

    public static class JsonEndPointEncoder extends Layer implements EndPointEncoderLayerInterface {

        private final Gson gson;

        public JsonEndPointEncoder() {
            super(MimeType.APPLICATION_JSON.toString());
            gson = new Gson();
        }

        @Override
        public HttpResponse encode(EndPointRequest request, EndPointResponse response) {
            HttpResponse httpResponse = new HttpResponse();
            String body = gson.toJson(response.getLayerResponse());
            httpResponse.setResponseCode(HttpResponseCode.OK);
            httpResponse.setReasonPhrase("OK");
            httpResponse.setBody(body.getBytes());
            httpResponse.addHeader(new HttpHeader(HttpHeader.CONNECTION, HttpHeader.CLOSED));
            httpResponse.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.APPLICATION_JSON.toString()));
            httpResponse.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.getBytes().length)));
            return httpResponse;
        }

        @Override
        public HttpResponse encode(HttpRequest request, Throwable throwable) {
            HttpResponse httpResponse = new HttpResponse();
            String body = gson.toJson(throwable);
            httpResponse.setResponseCode(HttpResponseCode.INTERNAL_SERVER_ERROR);
            httpResponse.setReasonPhrase("Internal server error");
            httpResponse.setBody(body.getBytes());
            httpResponse.addHeader(new HttpHeader(HttpHeader.CONNECTION, HttpHeader.CLOSED));
            httpResponse.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.APPLICATION_JSON.toString()));
            httpResponse.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.getBytes().length)));
            return httpResponse;
        }

    }
}
