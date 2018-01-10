package org.hcjf.cloud;

import org.hcjf.cloud.impl.network.CloudImpl;
import org.hcjf.cloud.impl.network.Node;
import org.hcjf.properties.SystemProperties;

/**
 * @author javaito.
 */
public class CloudTestB {

    public static void main(String[] args) {
        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");
        System.setProperty(SystemProperties.Log.TRUNCATE_TAG, "true");
        System.setProperty(SystemProperties.Net.Http.DEFAULT_CLIENT_READ_TIMEOUT, "60000");
        System.setProperty(SystemProperties.Service.THREAD_POOL_CORE_SIZE, "100");
        System.setProperty(SystemProperties.Service.THREAD_POOL_MAX_SIZE, "2000");

        System.setProperty(SystemProperties.Cloud.DefaultImpl.SERVER_LISTENER_PORT, "6163");
        System.setProperty(SystemProperties.Cloud.DefaultImpl.ThisNode.NAME, "test-B");
        System.setProperty(SystemProperties.Cloud.DefaultImpl.ThisNode.LAN_ADDRESS, "192.168.1.123");
        System.setProperty(SystemProperties.Cloud.DefaultImpl.ThisNode.LAN_PORT, "6163");


        Node node = new Node();
        node.setLanAddress("192.168.1.123");
        node.setLanPort(6162);
        CloudImpl.getInstance().registerConsumer(node);

        node = new Node();
        node.setLanAddress("192.168.1.123");
        node.setLanPort(6164);
        CloudImpl.getInstance().registerConsumer(node);
    }

}