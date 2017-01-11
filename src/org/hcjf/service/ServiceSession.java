package org.hcjf.service;

import org.hcjf.layers.Layer;
import org.hcjf.properties.SystemProperties;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class ServiceSession implements Comparable {

    private static final ServiceSession guestSession;

    static {
        guestSession = new GuestSession();
    }

    private final UUID id;
    private String sessionName;
    private final Map<Long, List<Class<? extends Layer>>> layerStack;
    private final Map<Long, Map<String, Object>> properties;
    private final Map<Long, Long> systemTimeByThread;
    private final ThreadMXBean threadMXBean;

    public ServiceSession(UUID id) {
        this.id = id;
        properties = new HashMap<>();
        layerStack = Collections.synchronizedMap(new HashMap<>());
        systemTimeByThread = new HashMap<>();
        threadMXBean = ManagementFactory.getThreadMXBean();
    }

    public UUID getId() {
        return id;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getSessionName() {
        return sessionName;
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties.get(Thread.currentThread().getId()));
    }

    /**
     * Start some thread over this session.
     */
    public synchronized void startThread() {
        systemTimeByThread.put(Thread.currentThread().getId(),
                threadMXBean.getThreadUserTime(Thread.currentThread().getId()));
        layerStack.put(Thread.currentThread().getId(), new ArrayList<>());
        properties.put(Thread.currentThread().getId(), new HashMap<>());
        onStartThread();
    }

    /**
     * Call to hook of the starts thread.
     */
    protected void onStartThread() {}

    /**
     * End some thread over this session.
     */
    public synchronized void endThread() {
        layerStack.remove(Thread.currentThread().getId());
        properties.remove(Thread.currentThread().getId());
        addThreadTime(System.currentTimeMillis() -
                threadMXBean.getThreadUserTime(Thread.currentThread().getId()));
        onEndThread();
    }

    /**
     * Call to hook of the ends thread.
     */
    protected void onEndThread(){}

    /**
     * Put all the properties over the session.
     * @param properties Properties.
     */
    public void putAll(Map<String, Object> properties) {
        this.properties.get(Thread.currentThread().getId()).putAll(properties);
    }

    /**
     * Put a property over the session.
     * @param propertyName Property name.
     * @param propertyValue Property value.
     */
    public void put(String propertyName, Object propertyValue) {
        properties.get(Thread.currentThread().getId()).put(propertyName, propertyValue);
    }

    /**
     * Return a session property.
     * @param propertyName Property name.
     * @return Session value.
     */
    public <O extends Object> O get(String propertyName) {
        return (O) properties.get(Thread.currentThread().getId()).get(propertyName);
    }

    /**
     * Add an element into the layer stack.
     * @param layerClass Layer class.
     */
    public final void putLayer(Class<? extends Layer> layerClass) {
        layerStack.get(Thread.currentThread().getId()).add(0, layerClass);
    }

    /**
     * Remove the head of the layer stack.
     */
    public final void removeLayer() {
        layerStack.get(Thread.currentThread().getId()).remove(0);
    }

    /**
     * Return the layer stack of the session.
     * @return Layer stack.
     */
    public Class[] getLayerStack() {
        return layerStack.get(Thread.currentThread().getId()).toArray(new Class[]{});
    }

    /**
     * Return the instance of the guest session.
     * @return Guest session.
     */
    public static final ServiceSession getGuestSession() {
        return guestSession;
    }

    /**
     * Compare this session with other object.
     * @param object Object to compare.
     * @return Return an integer to represent the difference between this session
     * an the object.
     */
    @Override
    public int compareTo(Object object) {
        int result = hashCode() - object.hashCode();
        if(getClass().equals(object.getClass())) {
            ServiceSession otherSession = (ServiceSession) object;
            if (getId().equals(otherSession.getId())) {
                result = 0;
            }
        }
        return result;
    }

    /**
     * Add system use time to specific session.
     * @param time System use time.
     */
    protected void addThreadTime(long time){
    }

    /**
     * This class is the default session of the system.
     */
    private static class GuestSession extends ServiceSession {

        public GuestSession() {
            super(UUID.randomUUID());
            setSessionName(SystemProperties.get(SystemProperties.Service.GUEST_SESSION_NAME));
        }
    }
}
