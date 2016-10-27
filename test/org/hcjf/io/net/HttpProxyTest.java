package org.hcjf.io.net;

import org.hcjf.io.net.http.proxy.HttpProxy;
import org.hcjf.io.net.http.proxy.RedirectionRule;
import org.junit.Test;

import java.net.URL;

/**
 * Created by javaito on 26/08/16.
 */
public class HttpProxyTest {

    @Test()
    public void test() throws Exception {
        HttpProxy proxy = new HttpProxy();
        proxy.addRule(new RedirectionRule(".*", new URL("http://www.google.com")));
        proxy.start();
    }

}
