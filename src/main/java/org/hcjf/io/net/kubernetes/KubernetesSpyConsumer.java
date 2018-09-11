package org.hcjf.io.net.kubernetes;

import org.hcjf.io.net.kubernetes.beans.Pod;
import org.hcjf.service.ServiceConsumer;

/**
 * @author javaito
 */
public abstract class KubernetesSpyConsumer implements ServiceConsumer {

    private final Filter filter;
    private Long lastUpdate;

    public KubernetesSpyConsumer(Filter filter) {
        this.filter = filter;
        this.lastUpdate = System.currentTimeMillis();
    }

    public Filter getFilter() {
        return filter;
    }

    public final Long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    protected abstract void onDiscoveryPod(Pod pod);

    protected abstract void onLostPod(Pod pod);

    public interface Filter {

        boolean filter(Pod pod);

    }
}
