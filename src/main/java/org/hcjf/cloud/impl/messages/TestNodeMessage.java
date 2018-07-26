package org.hcjf.cloud.impl.messages;

import org.hcjf.io.net.messages.Message;

import java.util.UUID;

/**
 * @author javaito
 */
public class TestNodeMessage extends Message {

    public TestNodeMessage(UUID id) {
        super(id);
    }

    public TestNodeMessage() {
    }
}
