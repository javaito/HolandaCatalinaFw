package org.hcjf.service;

import com.sun.management.ThreadMXBean;
import org.hcjf.layers.Layer;
import org.hcjf.properties.SystemProperties;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.UUID;

/**
 * This are the thread created by the factory in the
 * class service, all the services run over this kind of
 * thread.
 * @author javaito
 */
public class ServiceThread extends Thread {

    private static final String NAME = "ServiceThread";

    private ServiceSession session;
    private Long initialAllocatedMemory;
    private Long maxAllocatedMemory;
    private Long initialTime;
    private Long maxExecutionTime;

    public ServiceThread(Runnable target) {
        this(target, NAME + UUID.randomUUID().toString());
    }

    public ServiceThread(Runnable target, String name) {
        super(ServiceThreadGroup.getInstance(), target, name);
    }

    /**
     * Add an element into the layer stack.
     * @param element Layer stack element.
     */
    public final void putLayer(ServiceSession.LayerStackElement element) {
        getSession().putLayer(element);
    }

    /**
     * Remove the head of the layer stack.
     */
    public final void removeLayer() {
        getSession().removeLayer();
    }

    /**
     * This method return the stack of layer of the session.
     * @return Layer stack.
     */
    public Collection<ServiceSession.LayerStackElement> getLayerStack() {
        return getSession().getLayerStack();
    }

    /**
     * Return the session of the thread.
     * @return Session of the thread.
     */
    public final ServiceSession getSession() {
        return session;
    }

    /**
     * Returns the max allocated memory value for thread.
     * @return Max allocated memory value.
     */
    public final Long getMaxAllocatedMemory() {
        return maxAllocatedMemory;
    }

    /**
     * Sets the max allocated memory value for thread.
     * @param maxAllocatedMemory Max allocated memory value.
     */
    private void setMaxAllocatedMemory(Long maxAllocatedMemory) {
        this.maxAllocatedMemory = maxAllocatedMemory;
    }

    /**
     * Returns the max execution time value for thread.
     * @return Max execution time value.
     */
    public final Long getMaxExecutionTime() {
        return maxExecutionTime;
    }

    /**
     * Sets the max execution time value for thread.
     * @param maxExecutionTime Max execution time value.
     */
    private void setMaxExecutionTime(Long maxExecutionTime) {
        this.maxExecutionTime = maxExecutionTime;
    }

    /**
     * Returns the initial thread allocated memory counter when the current service session starts.
     * @return Initial thread allocated memory.
     */
    public Long getInitialAllocatedMemory() {
        return initialAllocatedMemory;
    }

    /**
     * Sets the initial thread allocated memory counter.
     * @param initialAllocatedMemory Initial thread allocated memory counter.
     */
    private void setInitialAllocatedMemory(Long initialAllocatedMemory) {
        this.initialAllocatedMemory = initialAllocatedMemory;
    }

    public final Long getAccumulatedAllocatedMemory() {
        return ((ThreadMXBean)ManagementFactory.getThreadMXBean()).
                getThreadAllocatedBytes(Thread.currentThread().getId()) - getInitialAllocatedMemory();
    }

    /**
     * Returns the initial thread time counter when the current service session starts.
     * @return Initial thread time counter.
     */
    public final Long getInitialTime() {
        return initialTime;
    }

    /**
     * Sets the initial time counter value.
     * @param initialTime Initial time counter value.
     */
    private void setInitialTime(Long initialTime) {
        this.initialTime = initialTime;
    }

    /**
     * Returns the accumulated time into the current thread.
     * @return Accumulated time.
     */
    public final Long getAccumulatedTime() {
        return ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - getInitialTime();
    }

    /**
     * Set the session for the thread.
     * @param session Service session.
     */
    public final void setSession(ServiceSession session) {
        if(this.session != null) {
            //Remove the status of the current thread stored into the old session
            this.session.endThread();
        }

        if(session != null) {
            //Start the status of the current thread into the new session.
            session.startThread();

            //Init the counters
            setInitialAllocatedMemory(((ThreadMXBean)ManagementFactory.getThreadMXBean()).
                    getThreadAllocatedBytes(Thread.currentThread().getId()));
            setInitialTime(ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime());

            //Init the max allocated memory value for thread
            configureMaxAllocatedMemory(SystemProperties.getLong(SystemProperties.Service.MAX_ALLOCATED_MEMORY_FOR_THREAD));

            //Init the max execution time value for thread
            configureMaxExecutionTime(SystemProperties.getLong(SystemProperties.Service.MAX_EXECUTION_TIME_FOR_THREAD));
        }

        this.session = session;
    }

    /**
     * This method configure the max allocated memory for the current thread.
     * This configuration will be reset when the thread finalize with the current service session.
     * If in the system properties the 'expressed in percentage flag' is true then this
     * values will be considered as a percentage in the otherwise this value will be
     * considered as a amount of bytes.
     * @param maxAllocatedMemory Max allocated memory value.
     */
    public static void configureMaxAllocatedMemory(Long maxAllocatedMemory) {
        if(SystemProperties.getBoolean(SystemProperties.Service.MAX_ALLOCATED_MEMORY_EXPRESSED_IN_PERCENTAGE)) {
            if(maxAllocatedMemory == null || maxAllocatedMemory < 1 || maxAllocatedMemory > 100) {
                maxAllocatedMemory = Runtime.getRuntime().maxMemory();
            } else {
                maxAllocatedMemory = (maxAllocatedMemory * Runtime.getRuntime().maxMemory()) / 100;
            }
        } else {
            if(maxAllocatedMemory == null || maxAllocatedMemory < 1 || maxAllocatedMemory > Runtime.getRuntime().maxMemory()) {
                maxAllocatedMemory = Runtime.getRuntime().maxMemory();
            }
        }
        ((ServiceThread)Thread.currentThread()).setMaxAllocatedMemory(maxAllocatedMemory);
    }

    /**
     * This method configure the max time of execution for the current thread.
     * This value are expressed in milliseconds.
     * @param maxExecutionTime Max execution time value.
     */
    public static void configureMaxExecutionTime(Long maxExecutionTime) {
        if(maxExecutionTime == null || maxExecutionTime < 1) {
            maxExecutionTime = Long.MAX_VALUE;
        }
        ((ServiceThread)Thread.currentThread()).setMaxExecutionTime(maxExecutionTime);
    }

    /**
     * Verify if the current thread is interrupted.
     * @throws InterruptedException Throws this exception if the current thread is interrupted.
     */
    public static void checkInterruptedThread() throws InterruptedException {
        if(Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Service thread interrupted");
        }
    }

    /**
     * Verify if the current thread allocate more bytes that the max configured.
     * @throws Throwable Throws a run time exception.
     */
    public static void checkAllocatedMemory() throws Throwable {
        ServiceThread serviceThread = (ServiceThread) Thread.currentThread();
        if(serviceThread.getAccumulatedAllocatedMemory() > serviceThread.getMaxAllocatedMemory()) {

            //Reset the initial value for the current thread in order to
            //continue with the throwable handling
            serviceThread.setInitialAllocatedMemory(((ThreadMXBean)ManagementFactory.getThreadMXBean()).
                    getThreadAllocatedBytes(Thread.currentThread().getId()));

            throw new RuntimeException("Max memory allocated for thread exceeded");
        }
    }

    /**
     * Verify if the current thread use more time that the max configured.
     * @throws Throwable Throws a run time exception.
     */
    public static void checkExecutionTime() throws Throwable {
        ServiceThread serviceThread = (ServiceThread) Thread.currentThread();
        if(serviceThread.getAccumulatedTime() > serviceThread.getMaxExecutionTime()) {

            //Reset the initial value for the current thread in order to
            //continue with the throwable handling
            serviceThread.setInitialTime(ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime());

            throw new RuntimeException("Max execution time for thread exceeded");
        }
    }
}
