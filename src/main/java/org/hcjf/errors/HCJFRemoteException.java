package org.hcjf.errors;

public class HCJFRemoteException extends HCJFRuntimeException{
    public HCJFRemoteException(String message, Object... params) {
        super(message, params);
    }

    public HCJFRemoteException(String message, Throwable cause, Object... params) {
        super(message, cause, params);
    }
}
