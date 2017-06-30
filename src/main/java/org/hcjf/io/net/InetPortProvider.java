package org.hcjf.io.net;

import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.TtlSet;

import java.net.ServerSocket;
import java.util.HashSet;

/**
 * @author javaito
 */
public final class InetPortProvider {

    private static final Integer MAX_PORT_NUMBER = 65535;

    private static final TtlSet<Integer> reservedPorts;

    static {
        reservedPorts = new TtlSet<>(new HashSet<>(),
                SystemProperties.getLong(SystemProperties.Net.PORT_PROVIDER_TIME_WINDOWS_SIZE));
    }

    public static synchronized Integer getPort(Integer basePort) {
        Integer result = basePort;
        while(reservedPorts.contains(basePort) || !probePort(result)) {
            result++;
            if(result > MAX_PORT_NUMBER) {
                throw new IllegalStateException("There aren't any port available from base port " + basePort);
            }
        }
        reservedPorts.add(result);
        return result;
    }

    private static boolean probePort(Integer port) {
        boolean result = true;

        try(ServerSocket serverSocket = new ServerSocket(port)) {
        } catch (Exception ex) {
            result = false;
        }

        return  result;
    }
}
