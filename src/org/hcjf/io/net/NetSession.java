package org.hcjf.io.net;

/**
 * This interface define de behavior of the net session.
 * @author javaito
 * @email javaito@gmail.com
 */
public abstract class NetSession<I extends Object> implements Comparable {

    private final I sessionId;
    private final NetServiceConsumer consumer;
    private boolean locked;

    public NetSession(I sessionId, NetServiceConsumer consumer) {
        this.sessionId = sessionId;
        this.consumer = consumer;
        this.locked = false;
    }

    /**
     * Return the id of the session.
     * @return Id of the session.
     */
    public I getSessionId() {
        return sessionId;
    }

    /**
     * Return the consumer.
     * @return Consumer.
     */
    public NetServiceConsumer getConsumer() {
        return consumer;
    }

    /**
     * Lock session.
     */
    public final void lock() {
        locked = true;
    }

    /**
     * Unlock session.
     */
    public final void unlock() {
        locked = false;
    }

    /**
     * Return the lock status of the session.
     * @return True if the session is locked and false otherwise
     */
    public final boolean isLocked() {
        return locked;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * <p>
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     * <p>
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     * <p>
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     * <p>
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     * <p>
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(Object o) {
        int result = hashCode() - o.hashCode();
        NetSession otherSession = (NetSession) o;
        if(getSessionId().equals(otherSession.getSessionId())) {
            result = 0;
        }
        return result;
    }
}
