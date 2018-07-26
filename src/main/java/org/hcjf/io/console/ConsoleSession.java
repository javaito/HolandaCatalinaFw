package org.hcjf.io.console;

import org.hcjf.io.net.NetServiceConsumer;
import org.hcjf.io.net.NetSession;

import java.util.UUID;

/**
 * Default session for the console connection.
 * @author javaito
 */
public class ConsoleSession extends NetSession {

    public ConsoleSession(UUID id, NetServiceConsumer consumer) {
        super(id, consumer);
    }

}
