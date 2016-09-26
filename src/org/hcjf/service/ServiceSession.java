package org.hcjf.service;

import org.hcjf.layers.Layer;
import org.hcjf.properties.SystemProperties;

import java.util.*;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class ServiceSession {

    private static final ServiceSession guestSession;

    static {
        guestSession = new GuestSession();
    }

    private final UUID id;
    private final String sessionName;
    private final Map<Long, Queue<Class<? extends Layer>>> layerStack;
    private final Map<String, Object> properties;

    public ServiceSession(UUID id, String sessionName) {
        this.id = id;
        this.sessionName = sessionName;
        properties = new HashMap<>();
        layerStack = Collections.synchronizedMap(new HashMap<>());
    }

    public UUID getId() {
        return id;
    }

    public String getSessionName() {
        return sessionName;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     *
     */
    public synchronized void startThread() {
        layerStack.put(Thread.currentThread().getId(), new PriorityQueue<>());
    }

    /**
     *
     */
    public synchronized void endThread() {
        layerStack.remove(Thread.currentThread().getId());
    }

    /**
     *
     * @param propertyName
     * @param propertyValue
     */
    public void put(String propertyName, Object propertyValue) {
        properties.put(propertyName, propertyValue);
    }

    /**
     *
     * @param propertyName
     * @return
     */
    public Object get(String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * Add an element into the layer stack.
     * @param layerClass Layer class.
     */
    public final void putLayer(Class<? extends Layer> layerClass) {
        layerStack.get(Thread.currentThread().getId()).add(layerClass);
    }

    /**
     * Remove the head of the layer stack.
     */
    public final void removeLayer() {
        layerStack.get(Thread.currentThread().getId()).remove();
    }

    /**
     *
     * @return
     */
    public Class[] getLayerStack() {
        return layerStack.get(Thread.currentThread().getId()).toArray(new Class[]{});
    }

    /**
     *
     * @return
     */
    public static final ServiceSession getGuestSession() {
        return guestSession;
    }

    /**
     * This class is the default session of the system.
     */
    private static class GuestSession extends ServiceSession {

        public GuestSession() {
            super(UUID.randomUUID(), SystemProperties.get(SystemProperties.SERVICE_GUEST_SESSION_NAME));
        }
    }
}
