package org.hcjf.io.net.messages;

/**
 * This kind of messges are to send a response for a particular message.
 * @author javaito
 */
public class ResponseMessage extends Message {

    private Object value;
    private Throwable throwable;

    public ResponseMessage() {
    }

    public ResponseMessage(Message message) {
        super(message.getId());
    }

    /**
     * Returns the response value.
     * @return Response value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Set the response value.
     * @param value Response value.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Returns the throwable value.
     * @return Throwable value.
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Set the throwable value.
     * @param throwable Throwable value.
     */
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
