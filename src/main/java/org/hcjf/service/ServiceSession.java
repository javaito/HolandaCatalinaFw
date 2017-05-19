package org.hcjf.service;

import org.hcjf.layers.Layer;
import org.hcjf.layers.query.Evaluator;
import org.hcjf.properties.SystemProperties;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

/**
 * This class must be implemented for all implementation
 * of the service session.
 * This kind of session provides an authenticated environment for all the
 * service thread.
 * @author javaito
 */
public class ServiceSession implements Comparable {

    private static final SystemSession SYSTEM_SESSION;
    private static final ServiceSession GUEST_SESSION;

    static {
        GUEST_SESSION = new GuestSession();
        SYSTEM_SESSION = new SystemSession();
    }

    private final UUID id;
    private String sessionName;
    private final Map<Long, List<Class<? extends Layer>>> layerStack;
    private final Map<Long, Map<String, Object>> properties;
    private final Map<Long, Long> systemTimeByThread;
    private final ThreadMXBean threadMXBean;
    private final List<ServiceSession> identities;
    private Locale locale;

    public ServiceSession(UUID id) {
        this.id = id;
        properties = new HashMap<>();
        layerStack = Collections.synchronizedMap(new HashMap<>());
        systemTimeByThread = new HashMap<>();
        threadMXBean = ManagementFactory.getThreadMXBean();
        locale = SystemProperties.getLocale();
        identities = new ArrayList<>();
    }

    /**
     * Add a new identity to the service session.
     * @param serviceSession New identity.
     */
    public final void addIdentity(ServiceSession serviceSession) {
        identities.add(0, serviceSession);
    }

    /**
     * Remove the last added identity to the session.
     */
    public final void removeIdentity() {
        if(!identities.isEmpty()) {
            identities.remove(0);
        }
    }

    /**
     * Return the last identity added into the session.
     * @param <S> Expected identity type.
     * @return Service session that represents the current identity.
     */
    public final <S extends ServiceSession> S currentIdentity() {
        S result = (S) this;
        if(!identities.isEmpty()) {
            result = (S) identities.get(0);
        }
        return result;
    }

    /**
     * Return the session id.
     * @return Session id.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Set the session name.
     * @param sessionName Session name.
     */
    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    /**
     * Return the session name.
     * @return Session Session name.
     */
    public String getSessionName() {
        return sessionName;
    }

    /**
     * Return the properties name of the session.
     * @return Unmodifiable properties map.
     */
    public Map<String, Object> getProperties() {
        Map<String, Object> result = null;
        if(properties.containsKey(Thread.currentThread().getId())) {
            result = Collections.unmodifiableMap(properties.get(Thread.currentThread().getId()));
        }
        return result;
    }

    /**
     * Start some thread over this session.
     */
    public synchronized void startThread() {
        systemTimeByThread.put(Thread.currentThread().getId(),
                threadMXBean.getCurrentThreadCpuTime());
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
        onEndThread();
        addThreadTime(threadMXBean.getCurrentThreadCpuTime() -
                systemTimeByThread.get(Thread.currentThread().getId()));
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
     * @param <O> Expected return type.
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
     * Return the instance of the system session.
     * @return System session.
     */
    public static final ServiceSession getSystemSession() {
        return SYSTEM_SESSION;
    }

    /**
     * Return the instance of the guest session.
     * @return Guest session.
     */
    public static final ServiceSession getGuestSession() {
        return GUEST_SESSION;
    }

    /**
     * This method obtain the current session from the current thread.
     * @param <S> Expected session type.
     * @return Current session.
     */
    public static final <S extends ServiceSession> S getCurrentSession() {
        Thread currentThread = Thread.currentThread();
        if(ServiceThread.class.isAssignableFrom(currentThread.getClass())) {
            return (S) ((ServiceThread)currentThread).getSession();
        } else {
            throw new IllegalStateException("The current thread is not a service thread.");
        }
    }

    /**
     * Return the current identity associated to the current thread.
     * @param <S> Expected session type.
     * @return Current identity.
     */
    public static final <S extends ServiceSession> S getCurrentIdentity() {
        Thread currentThread = Thread.currentThread();
        if(ServiceThread.class.isAssignableFrom(currentThread.getClass())) {
            return (S) ((ServiceThread)currentThread).getSession().currentIdentity();
        } else {
            throw new IllegalStateException("The current thread is not a service thread.");
        }
    }

    /**
     * Return locale of the session.
     * @return Session locale.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Set locale of the session.
     * @param locale Session locale.
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
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
     * Add system use time to specific session in nanoseconds.
     * @param time System use time in nanoseconds.
     */
    protected void addThreadTime(long time){
    }

    /**
     * This session is the session used in the case of some internal system task
     * run over a service.
     */
    private static class SystemSession extends ServiceSession {

        public SystemSession() {
            super(new UUID(0,0));
            setSessionName(SystemProperties.get(SystemProperties.Service.SYSTEM_SESSION_NAME));
        }

    }

    /**
     * This class is the default session of the system.
     */
    private static class GuestSession extends ServiceSession {

        public GuestSession() {
            super(new UUID(0,1));
            setSessionName(SystemProperties.get(SystemProperties.Service.GUEST_SESSION_NAME));
        }
    }

}
