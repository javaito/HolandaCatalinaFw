package org.hcjf.cloud;

import org.hcjf.layers.Layer;
import org.hcjf.layers.Layers;
import org.hcjf.layers.distributed.DistributedLayerInterface;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

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
        System.setProperty(SystemProperties.Service.STATIC_THREAD_POOL_CORE_SIZE, "100");
        System.setProperty(SystemProperties.Service.STATIC_THREAD_POOL_MAX_SIZE, "2000");

        System.setProperty(SystemProperties.Cloud.Orchestrator.SERVER_LISTENER_PORT, "6164");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.DATA_CENTER_NAME, "dc1");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.CLUSTER_NAME, "test-cluster");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.NAME, "test-C");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.LAN_ADDRESS, "192.168.10.103");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.LAN_PORT, "6164");
        System.setProperty(SystemProperties.Cloud.Orchestrator.NODES,
                "[" +
                    "{lanAddress:192.168.10.103,lanPort:6162}," +
                    "{lanAddress:192.168.10.103,lanPort:6163}," +
                    "{lanAddress:192.168.10.103,lanPort:6164}," +
                    "{lanAddress:192.168.10.103,lanPort:6165}" +
                "]");

        System.setProperty(SystemProperties.Layer.DISTRIBUTED_LAYER_ENABLED, "true");

        System.out.println("Load done!");

        Layers.publishLayer(LayerTestC.class);
        Map<String, String> testingMap = Cloud.getMap("testing-map");
        Queue<String> testingQueue = Cloud.getQueue("testing-queue");
        Lock lock = Cloud.getLock("testing-lock");
        Lock mapLock = Cloud.getLock("map-lock");
        Condition condition = mapLock.newCondition();

        Service.run(()->{
            mapLock.lock();
            while(!Thread.currentThread().isInterrupted()) {
                System.out.println("Map lock waiting!");
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    break;
                }
                System.out.println("Map lock notified!");
                System.out.println(testingMap.entrySet());
            }
            mapLock.unlock();
        }, ServiceSession.getSystemSession());

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

        Service.run(()->{
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
                    } else if(arguments[0].equalsIgnoreCase("invoke") && arguments.length == 2) {
                        DistributedLayerTest distributedLayerTest = Layers.get(DistributedLayerTest.class, arguments[1]);
                        for (int i = 0; i < 200; i++) {
                            Service.run(()->System.out.println("Result: " + distributedLayerTest.method("valueC")), ServiceSession.getSystemSession());
                            Service.run(()->System.out.println("Result: " + distributedLayerTest.method("valueC")), ServiceSession.getSystemSession());
                            Service.run(()->System.out.println("Result: " + distributedLayerTest.method("valueC")), ServiceSession.getSystemSession());
                        }
                    } else if(arguments[0].equalsIgnoreCase("offer") && arguments.length == 2) {
                        testingQueue.offer(arguments[1]);
                    } else if(arguments[0].equalsIgnoreCase("peek") && arguments.length == 1) {
                        System.out.println(testingQueue.peek());
                    } else if(arguments[0].equalsIgnoreCase("poll") && arguments.length == 1) {
                        System.out.println(testingQueue.poll());
                    }
                    System.out.println("Execution time: " + (System.currentTimeMillis() - time));
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }, ServiceSession.getSystemSession());
    }

    public static class LayerTestC extends Layer implements DistributedLayerTest, DistributedLayerInterface {

        public LayerTestC() {
            super("TestC");
        }

        @Override
        public String method(String value) {
            String result = String.format("Result of invoke test C with value %s", value);
            System.out.println(String.format("Test C invoked with value %s", value));
            throw new RuntimeException("Exception in c-node");
        }

    }

}
