package org.hcjf.service;

import org.hcjf.layers.Layer;

import java.util.UUID;

/**
 * This are the thread created by the factory in the
 * class service, all the services run over this kind of
 * thread.
 * @author javaito
 * @email javaito@gmail.com
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
     *
     * @return
     */
    public Class[] getLayerStack() {
        return getSession().getLayerStack();
    }

    /**
     *
     * @return
     */
    public ServiceSession getSession() {
        return session;
    }

    /**
     *
     * @param session
     */
    public void setSession(ServiceSession session) {
        this.session = session;
    }
}
