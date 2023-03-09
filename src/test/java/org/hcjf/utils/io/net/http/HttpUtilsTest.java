package org.hcjf.utils.io.net.http;

import org.hcjf.io.net.http.HttpServer;
import org.hcjf.properties.SystemProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class HttpUtilsTest {

    static Map<String, HttpServer.AccessControl> accessControlMapWithoutStartRegex;
    static Map<String, HttpServer.AccessControl> accessControlMapWithStartRegex;
    static String keyAccess = "localhost";

    @Before
    public void setup() {
        initializeAccessControlAndOriginHeader();
        System.setProperty(SystemProperties.Net.Http.HOST_ACCESS_CONTROL_REGEX_START_CHAR, "^");
    }

    @Test
    public void testGetAccessControlWithoutStartRegex() {
        Object accessControl1 = HttpUtils.getAccessControl(keyAccess.concat("1"), accessControlMapWithoutStartRegex);
        Object accessControl2 = HttpUtils.getAccessControl(keyAccess.concat("2"), accessControlMapWithoutStartRegex);
        Object accessControl3 = HttpUtils.getAccessControl(keyAccess.concat("3"), accessControlMapWithoutStartRegex);
        Assert.assertNotNull(accessControl1);
        Assert.assertNotNull(accessControl2);
        Assert.assertNotNull(accessControl3);
    }

    @Test
    public void testGetAccessControlWithStartRegex() {
        Object accessControl1 = HttpUtils.getAccessControl(keyAccess.concat("1"), accessControlMapWithStartRegex);
        Object accessControl2 = HttpUtils.getAccessControl(keyAccess.concat("2"), accessControlMapWithStartRegex);
        Object accessControl3 = HttpUtils.getAccessControl(keyAccess.concat("3"), accessControlMapWithStartRegex);
        Assert.assertNotNull(accessControl1);
        Assert.assertNotNull(accessControl2);
        Assert.assertNotNull(accessControl3);
    }

    static void initializeAccessControlAndOriginHeader() {
        accessControlMapWithoutStartRegex = new HashMap<>();
        accessControlMapWithStartRegex = new HashMap<>();
        for (int i = 1; i <= 3; i++) {
            String key = keyAccess.concat(String.valueOf(i));
            String keyRegex = "^".concat(keyAccess.concat(String.valueOf(i)));
            HttpServer.AccessControl accessControl = new HttpServer.AccessControl(key);
            HttpServer.AccessControl accessControlStart = new HttpServer.AccessControl(keyRegex);
            accessControlMapWithoutStartRegex.put(key, accessControl);
            accessControlMapWithStartRegex.put(keyRegex, accessControlStart);
        }
    }

}
