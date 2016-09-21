package org.hcjf.io.net.http.proxy;

import org.hcjf.io.net.http.HttpClient;
import org.hcjf.io.net.http.HttpHeader;
import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.HttpResponse;

import java.net.URL;

/**
 * Created by javaito on 26/08/16.
 */
public class RedirectionRule extends HttpProxyRule {

    private final String contextRegex;
    private final URL url;

    public RedirectionRule(String contextRegex, URL url) {
        this.contextRegex = contextRegex;
        this.url = url;
    }

    @Override
    public boolean evaluate(HttpRequest request) {
        return request.getContext().matches(contextRegex);
    }

    @Override
    public ProxyTask getTask() {
        return new RedirectionTask();
    }

    private class RedirectionTask extends ProxyTask {

        private HttpResponse response;

        @Override
        public HttpResponse execute(HttpRequest request) {
            HttpClient client = new HttpClient(url);
            client.setHttpMethod(request.getMethod());
            //TODO: Set body
            request.getHeaders().stream().filter(
                    header -> !header.getHeaderName().equals(HttpHeader.HOST))
                    .forEach(client::addHttpHeader);
            return client.request();
        }

    }
}
