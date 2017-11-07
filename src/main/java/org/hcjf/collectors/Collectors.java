package org.hcjf.collectors;

import org.hcjf.cloud.Cloud;
import org.hcjf.cloud.timer.CloudTimerTask;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements the collector service.
 * @author javaito
 */
public final class Collectors extends Service<Collector> {

    private static final Collectors instance;

    static {
        instance = new Collectors();
    }

    private final Map<String, Collector> collectors;

    private Collectors() {
        super(SystemProperties.get(SystemProperties.Collector.SERVICE_NAME),
                SystemProperties.getInteger(SystemProperties.Collector.SERVICE_PRIORITY));
        collectors = new HashMap<>();
    }

    @Override
    protected void init() {
        super.init();

        if(SystemProperties.getBoolean(SystemProperties.Collector.CLOUD_SAVE_MODE)) {
            Cloud.createTimerTask(new CloudTimerTask(SystemProperties.get(SystemProperties.Collector.CLOUD_TIMER_TASK_NAME)) {
                @Override
                protected Long getDelay() {
                    return SystemProperties.getLong(SystemProperties.Collector.FLUSH_PERIOD);
                }

                @Override
                protected void onRun() {
                    flush();
                }
            });
        } else {
            fork(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(SystemProperties.getLong(SystemProperties.Collector.FLUSH_PERIOD));
                    } catch (InterruptedException ex) {
                        //When this thread is interrupted is because the system is shutting down
                        //then needs end the last flush cycle.
                    }

                    flush();
                }
            });
        }
    }

    private void flush() {
        Log.i(SystemProperties.get(SystemProperties.Collector.LOG_TAG),
                "Starting collectors flush loop");
        for (Collector collector : collectors.values()) {
            try {
                collector.flush();
            } catch (Exception ex) {
                Log.w(SystemProperties.get(SystemProperties.Collector.LOG_TAG),
                        "Unable to flush collector %s", collector.getName());
            }
        }
        Log.i(SystemProperties.get(SystemProperties.Collector.LOG_TAG),
                "Collectors flush pool done");
    }

    @Override
    public synchronized void registerConsumer(Collector collector) {
        if(collector == null) {
            throw new NullPointerException("");
        }

        collectors.put(collector.getName(), collector);
    }

    @Override
    public void unregisterConsumer(Collector collector) {
        if(collector == null) {
            throw new NullPointerException("");
        }

        collectors.remove(collector.getName());
    }

    public static void collect(String collectorName, Object data) {
        Collector collector = instance.collectors.get(collectorName);
        if(collector != null) {
            collector.collect(data);
        } else {
            Log.w(SystemProperties.get(SystemProperties.Collector.LOG_TAG),
                    "The collector called '%s' doesn't exist", collectorName);
        }
    }

}
