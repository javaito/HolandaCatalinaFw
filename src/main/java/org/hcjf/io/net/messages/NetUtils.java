package org.hcjf.io.net.messages;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class NetUtils {

    private static String localIp;

    public static synchronized String getLocalIp() {
        if(localIp == null) {
            try(final DatagramSocket socket = new DatagramSocket()){
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                localIp = socket.getLocalAddress().getHostAddress();
            } catch (Exception ex){}
        }
        return localIp;
    }

}
