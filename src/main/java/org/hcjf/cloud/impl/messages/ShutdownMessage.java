package org.hcjf.cloud.impl.messages;

import org.hcjf.cloud.impl.network.CloudSession;

/**
 * @author javaito
 */
public class ShutdownMessage extends Message {

    public ShutdownMessage() {
    }

    public ShutdownMessage(CloudSession node) {
        super(node.getId());
    }

}
