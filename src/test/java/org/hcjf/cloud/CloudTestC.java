package org.hcjf.cloud;

import org.hcjf.cloud.impl.network.Node;
import org.hcjf.cloud.impl.network.CloudOrchestrator;
import org.hcjf.properties.SystemProperties;

/**
 * @author javaito.
 */
public class CloudTestC {

    public static void main(String[] args) {
        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");
        System.setProperty(SystemProperties.Log.TRUNCATE_TAG, "true");
        System.setProperty(SystemProperties.Net.Http.DEFAULT_CLIENT_READ_TIMEOUT, "60000");
        System.setProperty(SystemProperties.Service.THREAD_POOL_CORE_SIZE, "100");
        System.setProperty(SystemProperties.Service.THREAD_POOL_MAX_SIZE, "2000");

        System.setProperty(SystemProperties.Cloud.Orchestrator.SERVER_LISTENER_PORT, "6164");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.NAME, "test-C");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.LAN_ADDRESS, "172.16.102.45");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.LAN_PORT, "6164");


        Node node = new Node();
        node.setLanAddress("172.16.102.45");
        node.setLanPort(6162);
        CloudOrchestrator.getInstance().registerConsumer(node);

        node = new Node();
        node.setLanAddress("172.16.102.45");
        node.setLanPort(6163);
        CloudOrchestrator.getInstance().registerConsumer(node);

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(CloudOrchestrator.getInstance().invoke("maps", "test"));
    }

}
