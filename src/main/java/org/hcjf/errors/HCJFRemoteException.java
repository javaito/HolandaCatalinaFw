package org.hcjf.errors;

public class HCJFRemoteException extends HCJFRuntimeException{
    public HCJFRemoteException(String message, Object... params) {
        super(message, params);
    }
}
