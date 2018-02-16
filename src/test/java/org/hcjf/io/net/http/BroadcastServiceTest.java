package org.hcjf.io.net.http;

import org.hcjf.io.net.InetPortProvider;
import org.hcjf.io.net.broadcast.BroadcastConsumer;
import org.hcjf.io.net.broadcast.BroadcastService;
import org.hcjf.properties.SystemProperties;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author javaito
 */
public class BroadcastServiceTest {

    public static void main(String[] args) {

        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");
        System.setProperty(SystemProperties.Log.TRUNCATE_TAG, "true");
        System.setProperty(SystemProperties.Net.Http.DEFAULT_CLIENT_READ_TIMEOUT, "60000");
        System.setProperty(SystemProperties.Net.Http.OUTPUT_LOG_BODY_MAX_LENGTH, Integer.toString(Integer.MAX_VALUE));

        FolderContext folderContext = new FolderContext("", Paths.get("/home/javaito/NetBeansProjects/Test/dist/Test.jar"));

        BroadcastService.getInstance().registerConsumer(new BroadcastConsumerTest());
    }

    private static class BroadcastConsumerTest implements BroadcastConsumer {

        @Override
        public String getTaskName() {
            return "test";
        }

        @Override
        public String getPrivateKey() {
            return "test";
        }

        @Override
        public String getIpVersion() {
            return "4";
        }

        @Override
        public String getNetInterfaceName() {
            return "eno1";
        }

        @Override
        public Integer getPort() {
            return InetPortProvider.getUdpPort(10025);
        }

        @Override
        public Map<String, Object> getPingParameters() {
            return new HashMap<>();
        }

        @Override
        public void onPing(BroadcastService.PingMessage pingMessage) {
            System.out.println("On ping: " + pingMessage.getHost());
        }

        @Override
        public void onPong(BroadcastService.PongMessage pongMessage) {
            System.out.println("On pong");
        }

        @Override
        public void onShutdown(BroadcastService.ShutdownMessage shutdownMessage) {
            System.out.println("On shutdown");
        }
    }

}
