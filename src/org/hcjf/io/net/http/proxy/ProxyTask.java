package org.hcjf.io.net.http.proxy;

import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.HttpResponse;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public abstract class ProxyTask {

    /**
     * This method execute the task.
     * @param request Incoming request.
     */
    public abstract void execute(HttpRequest request);

    /**
     * This method should return the http response as result of the execute task.
     * This method should block the invocation thread until .the result of the task
     * @return Result of the task.
     */
    public abstract HttpResponse getResponse();

}
