package org.hcjf.io.net.http.proxy;

import org.hcjf.io.net.http.HttpRequest;

/**
 * This class analyze the requests and return a set of redirection
 * task to be executed for the http proxy.
 * @author javaito
 * @email javaito@gmail.com
 */
public abstract class HttpProxyRule {

    /**
     * Checks whether the request meets the conditions of the rule.
     * @param request Incoming request.
     * @return True if the request meets the conditions.
     */
    public abstract boolean evaluate(HttpRequest request);

    /**
     * Return the proxy task of the rule.
     * @return Proxy task.
     */
    public abstract ProxyTask getTask();

}
