package org.hcjf.io.console;

import org.hcjf.io.net.NetService;
import org.hcjf.layers.Layer;
import org.hcjf.layers.Layers;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Query;
import org.hcjf.layers.query.Queryable;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.ServiceSession;

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

        ConsoleServer consoleServer = new ConsoleServer(5900) {

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
