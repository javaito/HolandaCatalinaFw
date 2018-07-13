package org.hcjf.io.process;

import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author javaito
 */
public class ProcessDiscoveryService extends Service<ProcessDiscoveryConsumer> {

    private static final ProcessDiscoveryService instance;

    static {
        instance = new ProcessDiscoveryService();
    }

    private final Set<ProcessDiscoveryConsumer> consumers;
    private Boolean shuttingDown;

    private ProcessDiscoveryService() {
        super(SystemProperties.get(SystemProperties.ProcessDiscovery.SERVICE_NAME),
                SystemProperties.getInteger(SystemProperties.ProcessDiscovery.SERVICE_PRIORITY));
        consumers = new HashSet<>();
        shuttingDown = false;
    }

    @Override
    protected void init() {
        fork(new ProcessMatcher());
    }

    public static ProcessDiscoveryService getInstance() {
        return instance;
    }

    @Override
    public void registerConsumer(ProcessDiscoveryConsumer consumer) {
        consumers.add(consumer);
    }

    @Override
    public void unregisterConsumer(ProcessDiscoveryConsumer consumer) {
        consumers.remove(consumer);
    }

    @Override
    protected void shutdown(ShutdownStage stage) {
        shuttingDown = true;
    }

    private class ProcessMatcher implements Runnable {

        @Override
        public void run() {
            while(!shuttingDown) {
                try {
                    Stream<ProcessHandle> liveProcesses = ProcessHandle.allProcesses();

                    for (ProcessDiscoveryConsumer consumer : consumers) {
                        try {
                            liveProcesses.filter(consumer::match)
                                    .forEach(P -> {
                                        if (P.isAlive()) {
                                            consumer.onDiscovery(P);
                                        } else {
                                            consumer.onKill(P);
                                        }
                                    });
                        } catch (Exception ex) {
                            Log.w(SystemProperties.get(SystemProperties.ProcessDiscovery.LOG_TAG),
                                    "Process discovery match error", ex);
                        }
                    }
                } catch (Exception ex){
                    Log.w(SystemProperties.get(SystemProperties.ProcessDiscovery.LOG_TAG),
                            "Process discovery error", ex);
                }

                try {
                    Thread.sleep(SystemProperties.getLong(SystemProperties.ProcessDiscovery.DELAY));
                } catch (InterruptedException e) {
                    break;
                }
            }

            Log.d(SystemProperties.get(SystemProperties.ProcessDiscovery.LOG_TAG), "Process matcher end");
        }

    }
}
