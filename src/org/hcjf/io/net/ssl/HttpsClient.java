package org.hcjf.io.net.ssl;

import org.hcjf.io.net.http.HttpClient;

import java.net.URL;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class HttpsClient extends HttpClient {

    public HttpsClient(URL url) {
        super(url);
    }

}
