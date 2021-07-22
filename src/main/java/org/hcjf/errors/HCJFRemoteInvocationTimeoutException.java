package org.hcjf.errors;

public class HCJFRemoteInvocationTimeoutException extends TaggedException {

    private static final String TAG = "REMOTE_TIMEOUT";

    public HCJFRemoteInvocationTimeoutException(String message, Object... params) {
        this(message, null, params);
    }

    public HCJFRemoteInvocationTimeoutException(String message, Throwable cause, Object... params) {
        super(TAG, message, cause, params);
    }
}
