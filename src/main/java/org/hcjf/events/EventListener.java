package org.hcjf.events;

import org.hcjf.service.ServiceConsumer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * This class resolve the interface to receive events of the instance.
 * @author javaito
 */
public interface EventListener<E extends Event> extends ServiceConsumer {

    /**
     * When a event is received.
     * @param event Received event.
     */
    public void onEventReceive(E event);

    /**
     * Return the implementation type of the event listener.
     * @return Implementation type.
     */
    default Class<E> getEventType() {
        Type[] superInterfaces = getClass().getGenericInterfaces();
        int i = 0;
        Type superInterface;
        do {
            superInterface = superInterfaces[i++];
        } while(!(superInterface instanceof ParameterizedType &&
                ((ParameterizedType)superInterface).getRawType().equals(EventListener.class)));
        return (Class<E>) ((ParameterizedType)superInterface).getActualTypeArguments()[0];
    }

}
