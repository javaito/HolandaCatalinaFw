package org.hcjf.cloud.impl.network;

import org.hcjf.io.net.NetServiceConsumer;
import org.hcjf.io.net.NetSession;

import java.util.UUID;

/**
 * @author javaito.
 */
public class CloudSession extends NetSession {

    public CloudSession(NetServiceConsumer consumer) {
        super(UUID.randomUUID(), consumer);
    }

}
