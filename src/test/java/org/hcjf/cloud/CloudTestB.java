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

import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

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

        System.setProperty(SystemProperties.Cloud.Orchestrator.SERVER_LISTENER_PORT, "6163");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.NAME, "test-B");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.LAN_ADDRESS, "172.16.102.45");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.LAN_PORT, "6163");
        System.setProperty(SystemProperties.Cloud.Orchestrator.NODES, "{172.16.102.45:6162,172.16.102.45:6163,172.16.102.45:6164}");

        System.setProperty(SystemProperties.Layer.DISTRIBUTED_LAYER_ENABLED, "true");

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Load done!");

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
                        condition.signalAll();
                    } else if(arguments[0].equalsIgnoreCase("get") && arguments.length == 2) {
                        System.out.println(testingMap.get(arguments[1]));
                    } else if(arguments[0].equalsIgnoreCase("keys") && arguments.length == 1) {
                        System.out.println(testingMap.keySet().size());
                        System.out.println(testingMap.keySet());
                    } else if(arguments[0].equalsIgnoreCase("load") && arguments.length == 1) {
                        for (int i = 0; i < 5000; i++) {
                            testingMap.put("nodeB-key" + i, "nodeB-value" + i);
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
                    } else if(arguments[0].equalsIgnoreCase("publish") && arguments.length == 1) {
                        Layers.publishLayer(LayerTestB.class);
                        System.out.println("Layer published");
                    } else if(arguments[0].equalsIgnoreCase("invoke") && arguments.length == 2) {
                        DistributedLayerTest distributedLayerTest = Layers.get(DistributedLayerTest.class, arguments[1]);
                        System.out.println("Result: " + distributedLayerTest.method("valueB"));
                    }
                    System.out.println("Execution time: " + (System.currentTimeMillis() - time));
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }, ServiceSession.getSystemSession());
    }

    public static class LayerTestB extends Layer implements DistributedLayerTest, DistributedLayerInterface {

        public LayerTestB() {
            super("TestB");
        }

        @Override
        public String method(String value) {
            String result = String.format("Result of invoke test B with value %s", value);
            System.out.println(String.format("Test B invoked with value %s", value));
            return result;
        }

    }
}
