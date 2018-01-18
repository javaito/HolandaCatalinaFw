package org.hcjf.cloud.impl.messages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author javaito
 */
public class CloudWagonMessage extends Message {

    private Map<String,List<Message>> messages;

    public CloudWagonMessage() {
        messages = new HashMap<>();
    }

    public CloudWagonMessage(UUID id) {
        super(id);
        messages = new HashMap<>();
    }

    public Map<String,List<Message>> getMessages() {
        return messages;
    }

    public void setMessages(Map<String,List<Message>> messages) {
        this.messages = messages;
    }
}
