package org.hcjf.cloud.impl;

import org.hcjf.cloud.impl.messages.Message;

/**
 * @author javaito
 */
public interface CloudInterface {

    void disconnect();

    void write(Message message);

}
