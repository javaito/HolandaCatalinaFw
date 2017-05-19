package org.hcjf.io.net.http.proxy;

import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.io.net.http.HttpResponse;

/**
 * Proxy task, this task could be one of many task into the some
 * http proxy instance.
 * @author javaito
 */
public abstract class ProxyTask {

    /**
     * This method execute the task.
     * @param request Incoming request.
     * @return Http response.
     */
    public abstract HttpResponse execute(HttpRequest request);

}
