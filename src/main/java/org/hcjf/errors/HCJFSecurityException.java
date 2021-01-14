package org.hcjf.errors;

public class HCJFSecurityException extends TaggedException {

    private static final String TAG = "SECURITY";

    public HCJFSecurityException(String message, Object... params) {
        this(message, null, params);
    }

    public HCJFSecurityException(String message, Throwable cause, Object... params) {
        super(TAG, message, cause, params);
    }
}
