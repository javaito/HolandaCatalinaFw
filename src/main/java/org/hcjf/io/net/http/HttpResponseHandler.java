package org.hcjf.io.net.http;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.Layer;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;
import org.hcjf.service.ServiceThread;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public abstract class HttpResponseHandler extends Layer implements HttpPackage.TransferDecodingLayerInterface {

    private final Queue<ByteBuffer> queue;
    private Throwable throwable;
    private Boolean done;
    private Long length;
    private final AtomicLong counter;

    public HttpResponseHandler() {
        this.queue = new ArrayBlockingQueue<>(
                SystemProperties.getInteger(SystemProperties.Net.Http.CLIENT_RESPONSE_HANDLER_QUEUE_SIZE));
        this.counter = new AtomicLong();
        this.done = false;
        if(Thread.currentThread() instanceof ServiceThread) {
            Service.run(this::consume, ServiceSession.getCurrentIdentity());
        } else {
            new Thread(this::consume).start();
        }
    }

    @Override
    public String getImplName() {
        return "";
    }

    /**
     * Add a new fragment for the current body.
     * @param bodyFragment Body fragment.
     */
    @Override
    public final void add(ByteBuffer bodyFragment) {
        try {
            queue.offer(bodyFragment);
            synchronized (queue) {
                queue.notifyAll();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            this.throwable = ex;
            synchronized (this) {
                notifyAll();
            }
        }
    }

    /**
     * Verify if the body is done depends of the decode method.
     * @param httpPackage Package to verify if the body is complete.
     * @return Body done.
     */
    @Override
    public final boolean done(HttpPackage httpPackage) {
        if(length == null) {
            HttpHeader contentLengthHeader = httpPackage.getHeader(HttpHeader.CONTENT_LENGTH);
            if (contentLengthHeader != null) {
                length = Long.parseLong(contentLengthHeader.getHeaderValue().trim());
            }
        }
        if(counter.get() >= length) {
            done = true;
        }
        return done;
    }

    /**
     * Returns the decoded instance of the body.
     * @return Body.
     */
    @Override
    public final byte[] getBody() {
        return new byte[]{};
    }

    private void consume() {
        try {
            ByteBuffer fragment;
            while (!Thread.currentThread().isInterrupted() && !this.done) {
                synchronized (queue) {
                    fragment = queue.poll();
                }

                if (fragment != null) {
                    consume(fragment);
                    counter.addAndGet(fragment.limit());
                    System.out.println("Total: " + counter.get() + " of " + length);
                } else {
                    synchronized (queue) {
                        try {
                            queue.wait(1000);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
            this.throwable = ex;
        }
        synchronized (this) {
            notifyAll();
        }
    }

    protected abstract void consume(ByteBuffer fragment);

    public final void get() {
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) { }
        }
        if(throwable != null) {
            throw new HCJFRuntimeException("Response handler fail", throwable);
        }
    }
}
