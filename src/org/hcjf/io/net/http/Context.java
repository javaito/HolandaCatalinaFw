package org.hcjf.io.net.http;

/**
 * Created by javaito on 24/4/2016.
 */
public abstract class Context {

    private final String contextRegex;

    public Context(String contextRegex) {
        this.contextRegex = contextRegex;
    }

    public String getContextRegex() {
        return contextRegex;
    }

    public abstract HttpResponse onContext(HttpRequest request);

    public abstract HttpResponse onError(HttpRequest request, Throwable throwable);
}
