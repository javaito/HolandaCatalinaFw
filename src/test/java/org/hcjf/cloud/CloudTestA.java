package org.hcjf.cloud;

import org.hcjf.cloud.impl.network.CloudOrchestrator;
import org.hcjf.cloud.impl.network.Node;
import org.hcjf.cloud.timer.CloudTimerTask;
import org.hcjf.layers.Layers;
import org.hcjf.layers.distributed.DistributedLayerInterface;
import org.hcjf.layers.Layer;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author javaito.
 */
public class CloudTestA {

    public static void main(String[] args) {
        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");
        System.setProperty(SystemProperties.Log.TRUNCATE_TAG, "true");
        System.setProperty(SystemProperties.Net.Http.DEFAULT_CLIENT_READ_TIMEOUT, "60000");
        System.setProperty(SystemProperties.Service.THREAD_POOL_CORE_SIZE, "100");
        System.setProperty(SystemProperties.Service.THREAD_POOL_MAX_SIZE, "2000");

        System.setProperty(SystemProperties.Cloud.Orchestrator.SERVER_LISTENER_PORT, "6162");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.NAME, "test-A");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.LAN_ADDRESS, "172.16.102.45");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.LAN_PORT, "6162");
        System.setProperty(SystemProperties.Cloud.Orchestrator.NODES,
                "[" +
                        "{lanAddress:172.16.102.45,lanPort:6162}," +
                        "{lanAddress:172.16.102.45,lanPort:6163}," +
                        "{lanAddress:172.16.102.45,lanPort:6164}" +
                "]");

        System.setProperty(SystemProperties.Layer.DISTRIBUTED_LAYER_ENABLED, "true");

        System.out.println("Load done!");

        Layers.publishLayer(LayerTestA.class);
        Map<String, String> testingMap = Cloud.getMap("testing-map");
        Lock lock = Cloud.getLock("testing-lock");
        Lock mapLock = Cloud.getLock("map-lock");
        Condition condition = mapLock.newCondition();

        new Thread(() -> {
            mapLock.lock();
            while(!Thread.currentThread().isInterrupted()) {
                System.out.println("Map lock waiting!");
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Map lock notified!");
                System.out.println(testingMap.entrySet());
            }
            mapLock.unlock();
        }).start();

//        Service.run(new CloudTimerTask("testing-cloud-task") {
//            @Override
//            protected Long getDelay() {
//                return 1000L;
//            }
//
//            @Override
//            protected void onRun() {
//                System.out.println("Testing task executed!!!");
//            }
//        }, ServiceSession.getSystemSession());

        Service.run(() -> {
            byte[] buffer = new byte[1024];
            int readSize;
            String[] arguments;
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    System.out.println(": ");
                    readSize = System.in.read(buffer);

                    long time = System.currentTimeMillis();
                    arguments = new String(buffer, 0, readSize).trim().split(" ");

                    if(arguments[0].equalsIgnoreCase("put") && arguments.length == 3) {
                        testingMap.put(arguments[1], arguments[2]);
                        condition.signalAll();
                    } else if(arguments[0].equalsIgnoreCase("get") && arguments.length == 2) {
                        System.out.println(testingMap.get(arguments[1]));
                    } else if(arguments[0].equalsIgnoreCase("keys") && arguments.length == 1) {
                        System.out.println(testingMap.keySet().size());
                        System.out.println(testingMap.keySet());
                    } else if(arguments[0].equalsIgnoreCase("load") && arguments.length == 1) {
                        for (int i = 0; i < 5000; i++) {
                            testingMap.put("nodeA-key" + i, "nodeA-value" + i);
                        }
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
                    } else if(arguments[0].equalsIgnoreCase("invoke") && arguments.length == 2) {
                        DistributedLayerTest distributedLayerTest = Layers.get(DistributedLayerTest.class, arguments[1]);
                        System.out.println("Result: " + distributedLayerTest.method("valueA"));
                    }

                    System.out.println("Execution time: " + (System.currentTimeMillis() - time));
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }, ServiceSession.getSystemSession());
    }

    public static class LayerTestA extends Layer implements DistributedLayerTest, DistributedLayerInterface {

        public LayerTestA() {
            super("TestA");
        }

        @Override
        public String method(String value) {
            String result = String.format("Result of invoke test A with value %s", value);
            System.out.println(String.format("Test A invoked with value %s", value));
            return result;
        }

    }
}
