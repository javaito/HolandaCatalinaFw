package org.hcjf.cloud.timer;

import org.hcjf.cloud.Cloud;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.ServiceSession;
import org.hcjf.service.ServiceThread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author javaito
 *
 */
public abstract class CloudTimerTask implements Runnable {

    private final String name;
    private final Lock lock;
    private final Condition condition;
    private final Map<String, Long> timerTaskMap;

    private final String mapName;
    private final String lockName;
    private final String conditionName;

    public CloudTimerTask(String name) {
        this.name = name;

        mapName = SystemProperties.get(SystemProperties.Cloud.TimerTask.MAP_SUFFIX_NAME) + name;
        lockName = SystemProperties.get(SystemProperties.Cloud.TimerTask.LOCK_SUFFIX_NAME) + name;
        conditionName = SystemProperties.get(SystemProperties.Cloud.TimerTask.CONDITION_SUFFIX_NAME) + name;

        lock = Cloud.getLock(lockName);
        condition = Cloud.getCondition(conditionName, lock);
        timerTaskMap = Cloud.getMap(SystemProperties.get(SystemProperties.Cloud.TimerTask.MAP_NAME));
    }

    /**
     * Implementation of the life cycle of the distributed timer task.
     */
    @Override
    public final void run() {
        Long lastExecution;
        Long currentExecution;
        Long delay;
        while(!Thread.currentThread().isInterrupted()) {
            try {
                //Verify if the delay value of the task is grater than the min value of
                //the system properties. If not bigger then the value is truncated to the min
                //value of the system properties.
                delay = getDelay();
                if (delay < SystemProperties.getLong(SystemProperties.Cloud.TimerTask.MIN_VALUE_OF_DELAY)) {
                    delay = SystemProperties.getLong(SystemProperties.Cloud.TimerTask.MIN_VALUE_OF_DELAY);
                }

                //Try to block the task with the same name in the cloud.
                lock.lock();

                //Get the timestamp of the las execution of the task in the cloud.
                lastExecution = timerTaskMap.get(mapName);
                if (lastExecution == null) {
                    //If the last execution timestamp is null then this value is
                    //initialized with the current system timestamp.
                    lastExecution = System.currentTimeMillis();
                    timerTaskMap.put(mapName, lastExecution);
                }

                //Recalculate the delay value based on the last execution timestamp and
                //the current system timestamp.
                delay = delay - (System.currentTimeMillis() - lastExecution);

                //Sleep the task with the recalculated delay value.
                condition.await(delay, TimeUnit.MILLISECONDS);

                //If the current timestamp is equals than the las execution timestamp then
                //this threat is the first in get the lock, for this is in charge to execute the task.
                currentExecution = timerTaskMap.get(mapName);
                if (currentExecution.equals(lastExecution)) {
                    ServiceSession previousSession = ((ServiceThread) Thread.currentThread()).getSession();
                    //Execute the custom logic
                    try {
                        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG),"Starting timer task execution %s", name);
                        ((ServiceThread) Thread.currentThread()).setSession(ServiceSession.getSystemSession());
                        onRun();
                    } catch (Throwable ex) {
                        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG),"Starting timer task error execution %s", name);
                        onError(ex);
                    } finally {
                        ((ServiceThread) Thread.currentThread()).setSession(previousSession);
                        Log.d(System.getProperty(SystemProperties.Cloud.LOG_TAG),"Ending timer task execution %s", name);
                    }
                    //Update the las execution value.
                    timerTaskMap.put(mapName, System.currentTimeMillis());
                }
                lock.unlock();
            } catch (InterruptedException ex) {
                break;
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    /**
     * Return the delay value to each cycle of the task.
     * @return Delay value.
     */
    protected abstract Long getDelay();

    /**
     * Custom logic implementation.
     */
    protected abstract void onRun();

    /**
     * Implements this method in order to process the error.
     * @param throwable Throwable instance.
     */
    protected void onError(Throwable throwable) {}

}
