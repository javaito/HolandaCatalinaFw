package org.hcjf.cloud;

import org.hcjf.cloud.impl.network.Node;
import org.hcjf.cloud.impl.network.CloudOrchestrator;
import org.hcjf.properties.SystemProperties;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * @author javaito.
 */
public class CloudTestC {

    public static void main(String[] args) {
        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "false");
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
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Load done!");

        Map<String, String> testingMap = Cloud.getMap("testing-map");
        Lock lock = Cloud.getLock("testing-lock");

        byte[] buffer = new byte[1024];
        int readSize = 0;
        String[] arguments;
        while(!Thread.currentThread().isInterrupted()) {
            try {
                System.out.println(": ");
                readSize = System.in.read(buffer);

                long time = System.currentTimeMillis();
                arguments = new String(buffer, 0, readSize).trim().split(" ");

                if(arguments[0].equalsIgnoreCase("put") && arguments.length == 3) {
                    testingMap.put(arguments[1], arguments[2]);
                } else if(arguments[0].equalsIgnoreCase("get") && arguments.length == 2) {
                    System.out.println(testingMap.get(arguments[1]));
                } else if(arguments[0].equalsIgnoreCase("keys") && arguments.length == 1) {
                    System.out.println(testingMap.keySet().size());
                    System.out.println(testingMap.keySet());
                } else if(arguments[0].equalsIgnoreCase("load") && arguments.length == 1) {
                    for (int i = 0; i < 5000; i++) {
                        testingMap.put("nodeC-key" + i, "nodeC-value" + i);
                    }
                } else if(arguments[0].equalsIgnoreCase("size") && arguments.length == 1) {
                    System.out.println(testingMap.size());
                } else if(arguments[0].equalsIgnoreCase("size") && arguments.length == 1) {
                    System.out.println(testingMap.size());
                } else if(arguments[0].equalsIgnoreCase("values") && arguments.length == 1) {
                    System.out.println(testingMap.values());
                } else if(arguments[0].equalsIgnoreCase("lock") && arguments.length == 1) {
                    lock.lock();
                    System.out.println("Lock acquired!");
                } else if(arguments[0].equalsIgnoreCase("unlock") && arguments.length == 1) {
                    lock.unlock();
                    System.out.println("Unlocked");
                }
                System.out.println("Execution time: " + (System.currentTimeMillis() - time));
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

}
