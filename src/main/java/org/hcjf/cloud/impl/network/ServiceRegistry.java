package org.hcjf.cloud.impl.network;

import org.hcjf.cloud.impl.objects.DistributedLayer;
import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.query.Query;

import java.util.List;
import java.util.UUID;

public interface ServiceRegistry extends LayerInterface {

    /**
     * Stores an object that contains all the information about node of ecosystem.
     * @param node Node object.
     */
    void updateNode(Node node);

    /**
     * Gets a list of nodes filtered by query.
     * @param query Query instance.
     * @return List of nodes.
     */
    List<Node> getNodes(Query query);

    /**
     * Gets a list of nodes.
     * @return List of nodes.
     */
    List<Node> getNodes();

    /**
     * Gets a node instance by id.
     * @param nodeId Node id.
     * @return Node instance.
     */
    Node getNode(UUID nodeId);

    /**
     * Gets a node instance that represents the local node.
     * @return Node instance.
     */
    Node getThisNode();

    /**
     * Stores an object that contains all the information about service end points of the ecosystem.
     * @param serviceEndPoint Service end point object
     */
    void updateService(ServiceEndPoint serviceEndPoint);

    /**
     * Gets a list of service end points objects filtered by query.
     * @param query Query instance.
     * @return List of service end point
     */
    List<ServiceEndPoint> getServices(Query query);

    /**
     * Gets a list of service end points objects.
     * @return List of service end point.
     */
    List<ServiceEndPoint> getServices();

    /**
     * Gets a service end point instance by instance id.
     * @param serviceId Service end point instance id.
     * @return Service end point instance.
     */
    ServiceEndPoint getService(UUID serviceId);

    /**
     * Gets a service end point instance that represents the local service
     * @return Service end point instance.
     */
    ServiceEndPoint getThisServiceEndPoint();

    /**
     * Stores an object that contains all the information about the distributed layer of the ecosystem.
     * @param distributedLayer Distributed layer instance.
     */
    void updateDistributedLayer(DistributedLayerRegistry distributedLayer);

    /**
     * Gets a list of distributed layer instance filtered by query.
     * @param query Query instance.
     * @return List of distributed layer instances.
     */
    List<DistributedLayerRegistry> getDistributedLayers(Query query);

    /**
     * Gets a list of distributed layer instance
     * @return List of distributed layer instances.
     */
    List<DistributedLayerRegistry> getDistributedLayers();

    /**
     * Return a distributed layer instance, looking fot it for id.
     * @param id Instance id.
     * @return Distributed layer instance.
     */
    DistributedLayerRegistry getDistributedLayer(UUID id);

    /**
     * Save the registry information into the persistent storage implemented by the current layer.
     */
    void save();
}
