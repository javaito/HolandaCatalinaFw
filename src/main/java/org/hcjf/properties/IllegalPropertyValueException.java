package org.hcjf.properties;

/**
 * This exception is throws when the property value is not valid
 * @author javaito
 */
public final class IllegalPropertyValueException extends RuntimeException {

    /**
     * Constructor.
     * @param message Message.
     */
    public IllegalPropertyValueException(String message) {
        super(message);
    }
}
