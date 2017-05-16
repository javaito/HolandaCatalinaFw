package org.hcjf.service;

import org.hcjf.layers.Layer;

import java.util.UUID;

/**
 * This are the thread created by the factory in the
 * class service, all the services run over this kind of
 * thread.
 * @author javaito
 *
 */
public class ServiceThread extends Thread {

    private static final String NAME = "ServiceThread";

    private ServiceSession session;

    public ServiceThread(Runnable target) {
        this(target, NAME + UUID.randomUUID().toString());
    }

    public ServiceThread(Runnable target, String name) {
        super(ServiceThreadGroup.getInstance(), target, name);
    }

    /**
     * Add an element into the layer stack.
     * @param layerClass Layer class.
     */
    public final void putLayer(Class<? extends Layer> layerClass) {
        getSession().putLayer(layerClass);
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
    public Class[] getLayerStack() {
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
        }

        this.session = session;
    }
}
