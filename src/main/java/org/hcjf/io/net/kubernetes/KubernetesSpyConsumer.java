package org.hcjf.io.net.kubernetes;

import io.kubernetes.client.models.V1Pod;
import org.hcjf.service.ServiceConsumer;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public abstract class KubernetesSpyConsumer implements ServiceConsumer {

    private final Filter filter;
    private final Long initTimestamp;

    public KubernetesSpyConsumer(Filter filter) {
        this.filter = filter;
        this.initTimestamp = System.currentTimeMillis();
    }

    public Filter getFilter() {
        return filter;
    }

    public final Long getInitTimestamp() {
        return initTimestamp;
    }

    protected abstract void onDiscoveryPod(V1Pod pod);

    protected abstract void onLostPod(V1Pod pod);

    public interface Filter {

        boolean filter(V1Pod pod);

    }
}
