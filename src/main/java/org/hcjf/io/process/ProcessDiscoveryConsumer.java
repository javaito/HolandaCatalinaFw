package org.hcjf.io.process;

import org.hcjf.service.ServiceConsumer;

/**
 * @author javaito
 */
public interface ProcessDiscoveryConsumer extends ServiceConsumer {

    Boolean match(ProcessHandle processHandle);

    void onDiscovery(ProcessHandle processHandle);

    void onKill(ProcessHandle processHandle);
}
