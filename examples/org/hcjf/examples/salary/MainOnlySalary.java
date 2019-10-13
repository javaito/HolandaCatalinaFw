package org.hcjf.examples.salary;

import org.hcjf.cloud.Cloud;
import org.hcjf.io.net.http.HttpServer;
import org.hcjf.io.net.http.RestContext;
import org.hcjf.layers.Layers;
import org.hcjf.properties.SystemProperties;

public class MainOnlySalary {

    public static void main(String[] args) {
        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");
        System.setProperty(SystemProperties.Log.LEVEL, "0");
        System.setProperty(SystemProperties.Net.Http.DEFAULT_CLIENT_READ_TIMEOUT, "60000");
        System.setProperty(SystemProperties.Service.THREAD_POOL_CORE_SIZE, "100");
        System.setProperty(SystemProperties.Service.THREAD_POOL_MAX_SIZE, "2000");
        System.setProperty(SystemProperties.Cloud.Orchestrator.SERVER_LISTENER_PORT, "6161");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.DATA_CENTER_NAME, "dc1");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.CLUSTER_NAME, "test-cluster");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.NAME, "org/hcjf/examples/salary");
        System.setProperty(SystemProperties.Layer.DISTRIBUTED_LAYER_ENABLED, "true");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.LAN_ADDRESS, "localhost");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.LAN_PORT, "6161");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.ID, "00000000-0000-0000-0000-000000000001");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.GATEWAY_ADDRESS, "localhost");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.GATEWAY_PORT, "6161");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.NAME, "org/hcjf/examples/salary");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.PUBLICATION_TIMEOUT, "2000");
        System.setProperty(SystemProperties.Cloud.Orchestrator.SERVICE_END_POINTS,
                "[" +
                        "{id:00000000-0000-0000-0000-000000000000,gatewayAddress:localhost,gatewayPort:6160}" +
                "]");

        Layers.publishLayer(SalaryResource.class);
        HttpServer server = new HttpServer(9091);
        server.addContext(new RestContext("/api"));
        server.start();
        Cloud.publishMe();
    }

}
