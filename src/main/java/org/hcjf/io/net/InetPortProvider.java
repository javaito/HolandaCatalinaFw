package org.hcjf.io.net;

import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.TtlSet;

import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.HashSet;

/**
 * @author javaito
 */
public final class InetPortProvider {

    private static final Integer MAX_PORT_NUMBER = 65535;

    private static final TtlSet<Integer> reservedTcpPorts;
    private static final TtlSet<Integer> reservedUdpPorts;

    static {
        reservedTcpPorts = new TtlSet<>(new HashSet<>(),
                SystemProperties.getLong(SystemProperties.Net.PORT_PROVIDER_TIME_WINDOWS_SIZE));
        reservedUdpPorts = new TtlSet<>(new HashSet<>(),
                SystemProperties.getLong(SystemProperties.Net.PORT_PROVIDER_TIME_WINDOWS_SIZE));
    }

    public static synchronized Integer getTcpPort(Integer basePort) {
        Integer result = basePort;
        while(reservedTcpPorts.contains(basePort) || !probeTcpPort(result)) {
            result++;
            if(result > MAX_PORT_NUMBER) {
                throw new IllegalStateException("There aren't any port available from base port " + basePort);
            }
        }
        reservedTcpPorts.add(result);
        return result;
    }

    private static boolean probeTcpPort(Integer port) {
        boolean result = true;

        try(ServerSocket serverSocket = new ServerSocket(port)) {
        } catch (Exception ex) {
            result = false;
        }

        return  result;
    }

    public static synchronized Integer getUdpPort(Integer basePort) {
        Integer result = basePort;
        while(reservedUdpPorts.contains(basePort) || !probeUdpPort(result)) {
            result++;
            if(result > MAX_PORT_NUMBER) {
                throw new IllegalStateException("There aren't any port available from base port " + basePort);
            }
        }
        reservedTcpPorts.add(result);
        return result;
    }

    private static boolean probeUdpPort(Integer port) {
        boolean result = true;

        try(DatagramSocket datagramSocket = new DatagramSocket(port)) {
        } catch (Exception ex) {
            result = false;
        }

        return  result;
    }
}
