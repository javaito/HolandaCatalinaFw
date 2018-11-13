package org.hcjf.cloud;

import org.hcjf.cloud.impl.network.CloudOrchestrator;
import org.hcjf.cloud.impl.network.Node;
import org.hcjf.cloud.timer.CloudTimerTask;
import org.hcjf.io.console.ConsoleServer;
import org.hcjf.io.console.ServerMetadata;
import org.hcjf.layers.Layers;
import org.hcjf.layers.distributed.DistributedLayerInterface;
import org.hcjf.layers.Layer;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Query;
import org.hcjf.layers.query.Queryable;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;
import org.hcjf.utils.Cryptography;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author javaito.
 */
public class CloudTestA {

    public static void main(String[] args) {
        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "false");
        System.setProperty(SystemProperties.Log.TRUNCATE_TAG, "true");
        System.setProperty(SystemProperties.Net.Http.DEFAULT_CLIENT_READ_TIMEOUT, "60000");
        System.setProperty(SystemProperties.Service.THREAD_POOL_CORE_SIZE, "100");
        System.setProperty(SystemProperties.Service.THREAD_POOL_MAX_SIZE, "2000");

        System.setProperty(SystemProperties.Cloud.Orchestrator.SERVER_LISTENER_PORT, "6162");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.DATA_CENTER_NAME, "dc1");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.CLUSTER_NAME, "test-cluster");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.NAME, "test-A");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.LAN_ADDRESS, "192.168.10.103");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.LAN_PORT, "6162");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.ID, "00000000-0000-0000-0000-000000000001");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.GATEWAY_ADDRESS, "192.168.10.103");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.GATEWAY_PORT, "6162");
        System.setProperty(SystemProperties.Cloud.Orchestrator.NODES,
                "[" +
                        "{lanAddress:192.168.10.103,lanPort:6162}," +
                        "{lanAddress:192.168.10.103,lanPort:6163}," +
                        "{lanAddress:192.168.10.103,lanPort:6164}," +
                        "{lanAddress:192.168.10.103,lanPort:6165}" +
                "]");
        System.setProperty(SystemProperties.Cloud.Orchestrator.SERVICE_END_POINTS,
                "[" +
                        "{id:00000000-0000-0000-0000-000000000000,gatewayAddress:172.16.102.45,gatewayPort:7070}" +
                "]");

        System.setProperty(SystemProperties.Layer.DISTRIBUTED_LAYER_ENABLED, "true");

        System.out.println("Load done!");

        Cryptography cryptography = new Cryptography();
        ConsoleServer consoleServer = new ConsoleServer(5900, cryptography) {

            @Override
            protected ServerMetadata getMetadata() {
                ServerMetadata metadata = new ServerMetadata();
                metadata.setInstanceId(SystemProperties.getUUID(SystemProperties.Cloud.Orchestrator.ThisNode.ID));
                metadata.setClusterName(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisNode.CLUSTER_NAME));
                metadata.setServerName(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisNode.NAME));
                metadata.setServerVersion(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisNode.VERSION));
                metadata.setLoginRequired(true);
                metadata.setLoginFields(List.of("user"));
                metadata.setLoginSecretFields(List.of("password"));
                return metadata;
            }

            @Override
            protected ServiceSession login(Map<String, Object> parameters) {
                ServiceSession serviceSession = new ServiceSession(ServiceSession.getGuestSession().getId());
                serviceSession.setSessionName(parameters.get("user").toString());
                return serviceSession;
            }

            @Override
            protected Collection<JoinableMap> evaluate(Queryable queryable) {
                return Query.evaluate(queryable);
            }
        };
        consoleServer.start();

        Layers.publishLayer(LayerTestA.class);
        Map<String, String> testingMap = Cloud.getMap("testing-map");
        Queue<String> testingQueue = Cloud.getQueue("testing-queue");
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

    public static class LayerTestA extends Layer implements DistributedLayerTest, DistributedLayerInterface {

        private AtomicInteger counter;

        public LayerTestA() {
            super("TestA");
            counter = new AtomicInteger(0);
        }

        @Override
        public String method(String value) {
            String result = String.format("Result of invoke test A with value %s", value);
            System.out.println(String.format("Test A invoked with value %s", value));
            System.out.println("Invocation counter: " + counter.addAndGet(1));
            return result;
        }

    }
}
