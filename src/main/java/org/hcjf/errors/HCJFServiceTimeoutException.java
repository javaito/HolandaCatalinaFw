package org.hcjf.errors;

public class HCJFServiceTimeoutException extends HCJFRuntimeException{

    public HCJFServiceTimeoutException(String message, Object... params) {
        super(message, params);
    }

    public HCJFServiceTimeoutException(String message, Throwable cause, Object... params) {
        super(message, cause, params);
    }
}
