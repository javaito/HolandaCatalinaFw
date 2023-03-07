package org.hcjf.utils.io.net.http;

import org.hcjf.io.net.http.HttpServer;
import org.hcjf.properties.SystemProperties;

import java.util.Map;
import java.util.regex.Pattern;

public class HttpUtils {
    public static HttpServer.AccessControl getAccessControl(String host, Map<String, HttpServer.AccessControl> accessControlMap) {
        String startChar = SystemProperties.get(SystemProperties.Net.Http.HOST_ACCESS_CONTROL_REGEX_START_CHAR);
        for(String accessHost : accessControlMap.keySet()) {
            if(accessHost.startsWith(startChar)) {
                if(Pattern.matches(accessHost.substring(startChar.length()),host)) {
                    return accessControlMap.get(accessHost);
                }
            } else if (accessHost.equals(host)) {
                return accessControlMap.get(accessHost);
            }
        }
        return null;
    }
}
