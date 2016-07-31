import org.hcjf.io.net.http.*;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;

/**
 * Created by javaito on 15/1/2016.
 */
public class Main {

    public static void main(String[] args) throws Exception {
//        Log.d("", "Hola mundo!!");
//
//        Log.d("", "Hola, excepcion!!", new NullPointerException());
//
//        SystemProperties.get("bla", V -> (V.length() == 4));


        HttpServer server = new HttpServer(1338);
        server.addContext(new Context(".*") {
            @Override
            public HttpResponse onContext(HttpRequest request) {
                byte[] body = "Hello world!!".getBytes();
                HttpResponse response = new HttpResponse();
                response.setResponseCode(200);
                response.setBody(body);
                response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.length)));
                return response;
            }

            @Override
            protected HttpResponse onError(HttpRequest request, Throwable throwable) {
                return null;
            }
        });
        server.start();

    }

}
