package org.hcjf.events;

import org.hcjf.cloud.Cloud;
import org.hcjf.errors.Errors;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;

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
            fork(() -> listener.onEventReceive(event));
        }
    }

    private void dispatchDistributedEvent(DistributedEvent distributedEvent) {
        dispatchLocalEvent(distributedEvent);
        Cloud.dispatchEvent(distributedEvent);
    }

    /**
     * Send event.
     * @param event Event to send.
     */
    public static void sendEvent(Event event) {
        instance.dispatchEvent(event);
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

    public static <E extends Event> E waitForEvent() throws InterruptedException {
        return waitForEvent(Long.MAX_VALUE);
    }

    public static <E extends Event> E waitForEvent(long timeout) throws InterruptedException {
        AtomicReference<E> result = new AtomicReference<>();
        addEventListener((EventListener<E>) event -> {
            result.set(event);
            synchronized (result) {
                result.notifyAll();
            }
        });
        synchronized (result) {
            result.wait(timeout);
        }
        return result.get();
    }

    public static <O extends Object, E extends Event> O waitForEvent(EventCollector<E> eventCollector) throws InterruptedException {
        return waitForEvent(eventCollector, Long.MAX_VALUE);
    }

    public static <O extends Object, E extends Event> O waitForEvent(EventCollector<E> eventCollector, long timeout) throws InterruptedException {
        AtomicReference<E> result = new AtomicReference<>();
        addEventListener((EventListener<E>) event -> {
            result.set(event);
            synchronized (result) {
                result.notifyAll();
            }
        });
        synchronized (result) {
            result.wait(timeout);
        }
        return eventCollector.collect(result.get());
    }

    public interface EventCollector<E extends Event> {

        <O extends Object> O collect(E event);

    }
}
