package org.hcjf.io.net.http;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class HttpAccessControlTest {

    private static HttpServer server = new HttpServer();
    private static Method getAccessControlMethod;
    private static String[] hosts = new String[]{"localhost","www.sitrack.io","www-beta.sitrack.io","www-candidate.sitrack.io","lipigas.sitrack.io",
            "lipigas-beta.sitrack.io","lipigas-candidate.sitrack.io","beta-ui.sitrack.io","app-beta.sitrack.io","app-candidate.sitrack.io",
            "app.sitrack.io","beta-api.sitrack.io","ph-transportes.web.app"};
    private static String[] regex = new String[] {"^.*\\.sitrack\\.io\\.s3-website-sa-east-1\\.amazonaws\\.com"};

    private static Map<String, HttpServer.AccessControl> hostAccessControl = new HashMap<>();

    @BeforeClass
    public static void setup() {
        HttpServer.AccessControl accessControl;
        for(String host: hosts) {
            accessControl = addAccessControl(host);
            hostAccessControl.put(host, accessControl);
            server.addAccessControl(accessControl);
        }

        for(String host: regex) {
            accessControl = addAccessControl(host);
            hostAccessControl.put(host, accessControl);
            server.addAccessControl(accessControl);
        }

        try {
            getAccessControlMethod = server.getClass().getDeclaredMethod("getAccessControl", String.class);
            getAccessControlMethod.setAccessible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Test
    public void hostTest () {
        try {
            for (String host: hosts) {
                Assert.assertEquals(hostAccessControl.get(host),
                        getAccessControlMethod.invoke(server, host));
            }
            Assert.assertNull(getAccessControlMethod.invoke(server,"sitrack.com"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void regexTest () {

        Map <String, String> hostRegex = new HashMap<>();
        hostRegex.put("dev-alfa.sitrack.io.s3-website-sa-east-1.amazonaws.com","^.*\\.sitrack\\.io\\.s3-website-sa-east-1\\.amazonaws\\.com");
        hostRegex.put("bart-simpson.sitrack.io.s3-website-sa-east-1.amazonaws.com","^.*\\.sitrack\\.io\\.s3-website-sa-east-1\\.amazonaws\\.com");

        try {
            hostRegex.forEach((host, regexVal) -> {
                try {
                    Assert.assertEquals(hostAccessControl.get(regexVal),
                            getAccessControlMethod.invoke(server, host));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            });

            Assert.assertNull(getAccessControlMethod.invoke(server,"bart-simpson.sitrack.io.s3-website-sa-west-1.amazonaws.com"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static HttpServer.AccessControl addAccessControl(String host) {
        HttpServer.AccessControl accessControl = new HttpServer.AccessControl(host);
        accessControl.addAllowMethod(HttpMethod.POST.toString(), HttpMethod.GET.toString(), HttpMethod.PUT.toString(), HttpMethod.DELETE.toString());
        accessControl.addAllowHeader(HttpHeader.CONTENT_TYPE, HttpHeader.AUTHORIZATION, HttpHeader.X_REQUESTED_WITH);
        accessControl.addExposeHeader(
                HttpHeader.X_HCJF_QUERY_AVERAGE_TIME_EVALUATING_CONDITIONS,
                HttpHeader.X_HCJF_QUERY_AVERAGE_TIME_FORMATTING_DATA,
                HttpHeader.X_HCJF_QUERY_PRESENT_FIELDS,
                HttpHeader.X_HCJF_QUERY_TIME_AGGREGATING_DATA,
                HttpHeader.X_HCJF_QUERY_TIME_COLLECTING_DATA,
                HttpHeader.X_HCJF_QUERY_TIME_COMPILING,
                HttpHeader.X_HCJF_QUERY_TIME_EVALUATING_CONDITIONS,
                HttpHeader.X_HCJF_QUERY_TIME_FORMATTING_DATA,
                HttpHeader.X_HCJF_QUERY_TOTAL_TIME);
        return accessControl;
    }

}
