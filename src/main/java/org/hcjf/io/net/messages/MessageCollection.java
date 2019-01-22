package org.hcjf.io.net.messages;

import java.util.Collection;

public class MessageCollection extends Message {

    private Collection<Message> messages;

    public Collection<Message> getMessages() {
        return messages;
    }

    public void setMessages(Collection<Message> messages) {
        this.messages = messages;
    }
}
