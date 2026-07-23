package org.hcjf.cloud.impl.network;

import org.hcjf.io.net.kubernetes.KubernetesSpy;
import org.hcjf.io.net.messages.NetUtils;
import org.hcjf.layers.Layer;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;

import java.util.Date;
import java.util.UUID;

/**
 * This class creates and stores the base data about this service and node.
 */
public abstract class BaseServiceRegistry extends Layer implements ServiceRegistry {

    private final Node thisNode;
    private final ServiceEndPoint thisServiceEndPoint;

    public BaseServiceRegistry() {

        // This block creates an object to represent the current node into the ecosystem.
        thisNode = new Node();
        UUID thisNodeId = SystemProperties.getUUID(SystemProperties.Cloud.Orchestrator.ThisNode.ID);
        if(thisNodeId == null) {
            thisNodeId = UUID.randomUUID();
        }
        thisNode.setId(thisNodeId);
        thisNode.setDataCenterName(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisNode.DATA_CENTER_NAME));
        thisNode.setClusterName(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisNode.CLUSTER_NAME));
        thisNode.setName(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisNode.NAME));
        thisNode.setVersion(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisNode.VERSION));
        thisNode.setLanAddress(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisNode.LAN_ADDRESS));
        thisNode.setLanPort(SystemProperties.getInteger(SystemProperties.Cloud.Orchestrator.ThisNode.LAN_PORT));
        if(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisNode.WAN_ADDRESS) != null) {
            thisNode.setWanAddress(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisNode.WAN_ADDRESS));
            thisNode.setWanPort(SystemProperties.getInteger(SystemProperties.Cloud.Orchestrator.ThisNode.WAN_PORT));
        }
        thisNode.setStartupDate(new Date());
        thisNode.setStatus(Node.Status.CONNECTED);
        thisNode.setLocalNode(true);

        // This block creates an object to represent the current service into the ecosystem.
        thisServiceEndPoint = new ServiceEndPoint();
        UUID thisServiceEndPointId = SystemProperties.getUUID(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.ID);
        if(thisServiceEndPointId == null) {
            thisServiceEndPointId = UUID.randomUUID();
        }
        thisServiceEndPoint.setId(thisServiceEndPointId);
        thisServiceEndPoint.setName(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.NAME));
        thisServiceEndPoint.setGatewayAddress(SystemProperties.get(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.GATEWAY_ADDRESS));
        thisServiceEndPoint.setGatewayPort(SystemProperties.getInteger(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.GATEWAY_PORT));
        thisServiceEndPoint.setDistributedEventListener(SystemProperties.getBoolean(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.DISTRIBUTED_EVENT_LISTENER));
        thisServiceEndPoint.setDistributedEventListenerFilter(SystemProperties.getList(SystemProperties.Cloud.Orchestrator.ThisServiceEndPoint.DISTRIBUTED_EVENT_LISTENER_FILTER));

        // This block only works if the flag of kubernetes is true and change the ids and the end points of the local
        // node and the local service
        if(SystemProperties.getBoolean(SystemProperties.Cloud.Orchestrator.Kubernetes.ENABLED)) {
            thisNode.setId(new UUID((NetUtils.getLocalIp() + Node.class.getName()).hashCode(), KubernetesSpy.getHostName().hashCode()));
            thisServiceEndPoint.setId(new UUID(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.NAMESPACE).hashCode(),
                    SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.SERVICE_NAME).hashCode()));
            thisServiceEndPoint.setName(SystemProperties.get(SystemProperties.Cloud.Orchestrator.Kubernetes.SERVICE_NAME));
            thisNode.setLanAddress(NetUtils.getLocalIp());
            thisServiceEndPoint.setGatewayAddress(NetUtils.getLocalIp());
        }

        updateNode(thisNode);
        updateService(thisServiceEndPoint);
    }

    @Override
    public Node getThisNode() {
        return thisNode;
    }

    @Override
    public ServiceEndPoint getThisServiceEndPoint() {
        return thisServiceEndPoint;
    }

}
