package org.hcjf.io.net.kubernetes;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1ConfigMap;
import io.kubernetes.client.models.V1EndpointSubset;
import io.kubernetes.client.models.V1Endpoints;
import io.kubernetes.client.util.Config;
import org.hcjf.layers.Layer;
import org.hcjf.layers.crud.ReadRowsLayerInterface;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Queryable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class KubernetesSpyResource extends Layer implements ReadRowsLayerInterface {

    public static final String NAME = "system_k8s";
    public static final String CONFIG_MAP = "system_k8s_config_map";
    public static final String END_POINT = "system_k8s_end_point";
    public static final String EVENT = "system_k8s_event";
    public static final String LIMIT_RANGE = "system_k8s_limit_range";
    public static final String NAMESPACE = "system_k8s_namespace";
    public static final String NODE = "system_k8s_node";
    public static final String PERSISTENT_VOLUME = "system_k8s_persistent_volume";
    public static final String PERSISTENT_VOLUME_CLAIM = "system_k8s_persistent_volume_claim";
    public static final String POD = "system_k8s_pod";
    public static final String POD_TEMPLATE = "system_k8s_pod_template";
    public static final String REPLICATION_CONTROLLER = "system_k8s_replication_controller";
    public static final String RESOURCE_QUOTA = "system_k8s_resource_quota";
    public static final String SECRET = "system_k8s_secret";
    public static final String SERVICE_ACCOUNT = "system_k8s_service_account";
    public static final String SERVICE = "system_k8s_service";

    private final ApiClient client;
    private final CoreV1Api api;

    public KubernetesSpyResource() {
        super(NAME);
        try {
            client = Config.fromCluster();
        } catch (IOException e) {
            throw new RuntimeException();
        }
        Configuration.setDefaultApiClient(client);
        this.api = new CoreV1Api();
    }

    @Override
    public Set<String> getAliases() {
        return Set.of(
                CONFIG_MAP,
                END_POINT,
                EVENT,
                LIMIT_RANGE,
                NAMESPACE,
                NODE,
                PERSISTENT_VOLUME,
                PERSISTENT_VOLUME_CLAIM,
                POD,
                POD_TEMPLATE,
                REPLICATION_CONTROLLER,
                RESOURCE_QUOTA,
                SECRET,
                SERVICE_ACCOUNT,
                SERVICE
        );
    }

    @Override
    public Collection<JoinableMap> readRows(Queryable queryable) {
        Collection<JoinableMap> result = new ArrayList<>();
        try {
            JoinableMap joinableMap;
            switch (queryable.getResourceName()) {
                case CONFIG_MAP: {
                    for(V1ConfigMap configMap : api.listConfigMapForAllNamespaces(null, null, null, null, null, null, null, null, null).getItems()) {
                        joinableMap = new JoinableMap(queryable.getResourceName());
                        joinableMap.putAll(configMap.getData());
                        result.add(joinableMap);
                    }
                    break;
                }
                case END_POINT: {
                    for(V1Endpoints endpoints : api.listEndpointsForAllNamespaces(null, null,null,null,null,null,null,null,null).getItems()) {
                        for(V1EndpointSubset endpointSubset : endpoints.getSubsets()) {
                            joinableMap = new JoinableMap(queryable.getResourceName());
//                            joinableMap.putAll(endpointSubset.get);
                            result.add(joinableMap);
                        }
                    }
                    break;
                }
            }
        }catch (Exception ex){

        }


        return null;
    }


}
