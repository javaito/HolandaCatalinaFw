package org.hcjf.properties;

/**
 * This exception is throws when the property value is not valid
 * @author javaito
 * @email javaito@gmail.com
 */
public final class IllegalPropertyValueException extends RuntimeException {

    /**
     * Constructor.
     */
    public IllegalPropertyValueException(String message) {
        super(message);
    }
}
