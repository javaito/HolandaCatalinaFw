package org.hcjf.io.net.http.rest.layers;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    public static class JsonEndPointEncoder extends Layer implements EndPointEncoderLayerInterface, ExclusionStrategy {

        private final Gson gson;

        public JsonEndPointEncoder() {
            super(MimeType.APPLICATION_JSON.toString());
            gson = new GsonBuilder().addSerializationExclusionStrategy(this).create();
        }

        @Override
        public HttpResponse encode(EndPointRequest request, EndPointResponse response) {
            HttpResponse httpResponse = new HttpResponse();
            byte[] body = gson.toJson(response.getLayerResponse()).getBytes();
            httpResponse.setResponseCode(HttpResponseCode.OK);
            httpResponse.setBody(body);
            httpResponse.addHeader(new HttpHeader(HttpHeader.CONNECTION, HttpHeader.CLOSED));
            httpResponse.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.APPLICATION_JSON.toString()));
            httpResponse.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.length)));
            return httpResponse;
        }

        @Override
        public HttpResponse encode(HttpRequest request, Throwable throwable) {
            HttpResponse httpResponse = new HttpResponse();
            byte[] body = gson.toJson(throwable).getBytes();
            httpResponse.setResponseCode(HttpResponseCode.INTERNAL_SERVER_ERROR);
            httpResponse.setBody(body);
            httpResponse.addHeader(new HttpHeader(HttpHeader.CONNECTION, HttpHeader.CLOSED));
            httpResponse.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.APPLICATION_JSON.toString()));
            httpResponse.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.length)));
            return httpResponse;
        }

        /**
         * This method verify if the attribute must by skipped for the
         * json decoding process.
         * @param fieldAttributes Field attribute.
         * @return True if the field must be excluded and false in the otherwise.
         */
        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return false;
        }

        /**
         * This method verify if the class must be excluded.
         * @param aClass Evaluation class.
         * @return Ture if the class must be excluded and false in the otherwise.
         */
        @Override
        public boolean shouldSkipClass(Class<?> aClass) {
            return false;
        }

        /**
         * Returns the gson instance of the encoder.
         * @return Gson instance.
         */
        protected Gson getGson() {
            return gson;
        }
    }
}
