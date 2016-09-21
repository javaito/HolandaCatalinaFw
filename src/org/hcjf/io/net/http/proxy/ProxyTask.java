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
    public abstract HttpResponse execute(HttpRequest request);

}
