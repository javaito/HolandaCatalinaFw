package org.hcjf.io.console;

import org.hcjf.io.net.NetService;
import org.hcjf.io.net.messages.MessageBuffer;
import org.hcjf.layers.Layer;
import org.hcjf.layers.Layers;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Query;
import org.hcjf.layers.query.Queryable;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.ServiceSession;
import org.hcjf.utils.Cryptography;

import java.util.*;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class ConsoleTest {

    public static void main(String[] args) {

        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "true");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.ID, UUID.randomUUID().toString());
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.NAME, "Test");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.VERSION, "1.0");
        System.setProperty(SystemProperties.Cloud.Orchestrator.ThisNode.CLUSTER_NAME, "Test");

        Layers.publishLayer(TestLayer.class);

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

                try {
                    Thread.sleep(8000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Query query = (Query) queryable;
                if(!query.getResourceName().equals("resource")) {
                    throw new RuntimeException("Resource not found");
                }

                System.out.println(queryable.toString());
                Collection<JoinableMap> result = new ArrayList<>();

                for (int i = 0; i < 40; i++) {
                    JoinableMap joinableMap = new JoinableMap("resource");
                    joinableMap.put("field1", "value1");
                    joinableMap.put("field2", "value2");
                    joinableMap.put("field3", "value3");
                    joinableMap.put("field4", "value4");
                    joinableMap.put("field4", "value1");
                    joinableMap.put("field5", "value2");
                    joinableMap.put("field6", "value3");
                    joinableMap.put("field7", "value4");
                    joinableMap.put("field8", "value1");
                    joinableMap.put("field9", "value2");
                    joinableMap.put("field10", "value3");
                    joinableMap.put("field11", "value4");
                    joinableMap.put("field12", "value1");
                    joinableMap.put("field13", "value2");
                    joinableMap.put("field14", "value3");
                    joinableMap.put("field15", "value4");
                    joinableMap.put("field16", "value1");
                    joinableMap.put("field17", "value2");
                    joinableMap.put("field17", "value3");
                    joinableMap.put("field19", "value4");
                    result.add(joinableMap);
                }



                return result;
            }
        };
        NetService.getInstance().registerConsumer(consoleServer);

    }

    public static class TestLayer extends Layer implements ConsoleCommandLayerInterface {

        public TestLayer() {
            super("test");
        }

        @Override
        public Object execute(List<Object> parameters) {

            System.out.println(parameters);

            return "Todo bien!!!";
        }
    }

}
