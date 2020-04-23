package org.hcjf.events;

import org.hcjf.cloud.Cloud;
import org.hcjf.errors.Errors;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class implements the event service for the instance.
 * @author javaito
 */
public final class Events extends Service<EventListener> {

    private static final Events instance;

    static {
        instance = new Events();
    }

    public final List<EventListener> listeners;

    private Events() {
        super(SystemProperties.get(SystemProperties.Event.SERVICE_NAME),
                SystemProperties.getInteger(SystemProperties.Event.SERVICE_PRIORITY));
        listeners = new ArrayList<>();
    }

    /**
     * Register a new event listener to the service.
     * @param consumer Event listener.
     */
    @Override
    public void registerConsumer(EventListener consumer) {
        if(consumer == null) {
            throw new IllegalArgumentException(Errors.getMessage(Errors.ORG_HCJF_EVENTS_1));
        }

        synchronized (listeners) {
            listeners.add(consumer);
        }
    }

    /**
     * Unregister a event listener to the service.
     * @param consumer Event listener.
     */
    @Override
    public void unregisterConsumer(EventListener consumer) {
        if(consumer == null) {
            throw new IllegalArgumentException(Errors.getMessage(Errors.ORG_HCJF_EVENTS_1));
        }

        synchronized (listeners) {
            listeners.remove(consumer);
        }
    }

    /**
     * Return all the listeners to be able of process the specific event.
     * @param event Event to dispatch.
     * @return List of listeners.
     */
    private List<EventListener> getListeners(Event event) {
        List<EventListener> result = new ArrayList<>();
        synchronized (listeners) {
            for(EventListener listener : listeners) {
                if(listener.getEventType().isAssignableFrom(event.getClass())) {
                    result.add(listener);
                }
            }
        }
        return result;
    }

    /**
     * Dispatch the event to all the listeners.
     * @param event Event to dispatch.
     */
    private void dispatchEvent(Event event) {
        if(event != null) {
            if(event instanceof DistributedEvent) {
                dispatchDistributedEvent((DistributedEvent) event);
            } else if(event instanceof RemoteEvent) {
                dispatchLocalEvent(((RemoteEvent)event).getEvent());
            } else {
                dispatchLocalEvent(event);
            }
        }
    }

    private void dispatchLocalEvent(Event event) {
        for (EventListener listener : getListeners(event)) {
            try {
                run(() -> listener.onEventReceived(event), ServiceSession.getCurrentIdentity());
            } catch(Exception ex){
                Log.e(SystemProperties.get(SystemProperties.Event.LOG_TAG), "Unable to dispatch event", ex);
            }
        }
    }

    /**
     * Dispatch the event to all the listeners and wait that the all listeners finish.
     * @param event Event to dispatch.
     */
    private void synchronousDispatchEvent(Event event) {
        if(event != null) {
            if(event instanceof DistributedEvent) {
                throw new HCJFRuntimeException("Unable to use synchronous dispatch for distributed events");
            } else if(event instanceof RemoteEvent) {
                synchronousDispatchLocalEvent(((RemoteEvent)event).getEvent());
            } else {
                synchronousDispatchLocalEvent(event);
            }
        }
    }

    /**
     * Dispatch the event to all the listeners and wait that the all listeners finish.
     * @param event Event to dispatch.
     */
    private void synchronousDispatchLocalEvent(Event event) {
        for (EventListener listener : getListeners(event)) {
            try {
                listener.onEventReceived(event);
            } catch(Exception ex){
                Log.e(SystemProperties.get(SystemProperties.Event.LOG_TAG), "Unable to dispatch event", ex);
            }
        }
    }

    /**
     * Dispatch a distributed event instance only for the cloud singleton.
     * @param distributedEvent Distributed event.
     */
    private void dispatchDistributedEvent(DistributedEvent distributedEvent) {
        Cloud.dispatchEvent(distributedEvent);
    }

    /**
     * Dispatch the event and return control immediately
     * @param event Event to dispatch.
     */
    public static void sendEvent(Event event) {
        instance.dispatchEvent(event);
    }

    /**
     * Dispatch the event and wait all the listeners process the event.
     * @param event Event to dispatch
     */
    public static void processEvent(Event event) {
        instance.synchronousDispatchEvent(event);
    }

    /**
     * Add event listener.
     * @param eventListener Event listener.
     */
    public static void addEventListener(EventListener eventListener) {
        instance.registerConsumer(eventListener);
    }

    /**
     * Remove event listener.
     * @param eventListener Event listener.
     */
    public static void removeEventListener(EventListener eventListener) {
        instance.unregisterConsumer(eventListener);
    }

    public static <E extends Event> E waitForEvent(Class<E> eventClass) throws InterruptedException {
        return waitForEvent(eventClass, Long.MAX_VALUE);
    }

    public static <E extends Event> E waitForEvent(Class<E> eventClass, long timeout) throws InterruptedException {
        AtomicReference<E> result = new AtomicReference<>();
        addEventListener(new EventListener<E>(){
            @Override
            public void onEventReceived(E event) {
                result.set(event);
                synchronized (result) {
                    result.notifyAll();
                }
            }
            @Override
            public Class<E> getEventType() {
                return eventClass;
            }
        });
        synchronized (result) {
            result.wait(timeout);
        }
        return result.get();
    }

    public static <O extends Object, E extends Event> O waitForEvent(Class<E> eventClass, EventCollector<E> eventCollector) throws InterruptedException {
        return waitForEvent(eventClass, eventCollector, Long.MAX_VALUE);
    }

    public static <O extends Object, E extends Event> O waitForEvent(Class<E> eventClass, EventCollector<E> eventCollector, long timeout) throws InterruptedException {
        return eventCollector.collect(waitForEvent(eventClass, timeout));
    }

    public interface EventCollector<E extends Event> {

        <O extends Object> O collect(E event);

    }
}
