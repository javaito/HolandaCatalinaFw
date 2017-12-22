package org.hcjf.cloud.impl;

import org.hcjf.bson.BsonDocument;
import org.hcjf.io.net.NetPackage;
import org.hcjf.io.net.NetServer;
import org.hcjf.io.net.NetService;
import org.hcjf.io.net.NetSession;

/**
 * @author javaito
 */
public class CloudServer extends NetServer<Nodes.Node, BsonDocument> {

    public CloudServer() {
        super(18080, NetService.TransportLayerProtocol.TCP,
                true, true);
    }

    @Override
    public Nodes.Node createSession(NetPackage netPackage) {
        Nodes.Node node = Nodes.createNode(netPackage.getRemoteHost(), this);
        if(!Nodes.connecting(node)) {
            node = null;
        }
        return node;
    }

    @Override
    public Nodes.Node checkSession(Nodes.Node session, BsonDocument payLoad, NetPackage netPackage) {

        return null;
    }

    @Override
    protected byte[] encode(BsonDocument payLoad) {
        return new byte[0];
    }

    @Override
    protected BsonDocument decode(NetPackage netPackage) {
        return null;
    }

    @Override
    public void destroySession(NetSession session) {

    }

}
