package org.hcjf.cloud.impl.messages;

import org.hcjf.io.net.messages.Message;
import org.hcjf.io.net.messages.ResponseMessage;

import java.util.Collection;

public class ServiceDefinitionResponseMessage extends ResponseMessage {

    private Collection<Message> messages;

    public ServiceDefinitionResponseMessage() {
    }

    public ServiceDefinitionResponseMessage(Message message) {
        super(message);
    }

    public Collection<Message> getMessages() {
        return messages;
    }

    public void setMessages(Collection<Message> messages) {
        this.messages = messages;
    }
}
