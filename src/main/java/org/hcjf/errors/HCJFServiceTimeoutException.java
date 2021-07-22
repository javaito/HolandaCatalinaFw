package org.hcjf.errors;

public class HCJFServiceTimeoutException extends TaggedException {

    public static final String TAG = "SERVICE_TIMEOUT";

    public HCJFServiceTimeoutException(String message, Object... params) {
        this(message, null, params);
    }

    public HCJFServiceTimeoutException(String message, Throwable cause, Object... params) {
        super(TAG, message, cause, params);
    }

}
