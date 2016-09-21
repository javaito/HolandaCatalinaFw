package org.hcjf.service;

import org.hcjf.layers.Layer;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
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

    private final Queue<Class<? extends Layer>> layerStack;

    /**
     * Constructor of the service thread.
     * @param target Runnable objet with the custom task.
     */
    public ServiceThread(Runnable target) {
        super(ServiceThreadGroup.getInstance(), target, NAME + UUID.randomUUID().toString());
        layerStack = new PriorityQueue<>();
    }

    /**
     * Add an element into the layer stack.
     * @param layerClass Layer class.
     */
    public final void putLayer(Class<? extends Layer> layerClass) {
        layerStack.add(layerClass);
    }

    /**
     * Remove the head of the layer stack.
     */
    public final void removeLayer() {
        layerStack.remove();
    }
}
