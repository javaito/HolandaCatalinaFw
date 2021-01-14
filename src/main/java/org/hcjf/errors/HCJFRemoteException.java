package org.hcjf.errors;

public class HCJFRemoteException extends TaggedException {

    private static final String TAG = "REMOTE";

    public HCJFRemoteException(String message, Object... params) {
        this(message, null, params);
    }

    public HCJFRemoteException(String message, Throwable cause, Object... params) {
        super(TAG, message, cause, params);
    }

}
