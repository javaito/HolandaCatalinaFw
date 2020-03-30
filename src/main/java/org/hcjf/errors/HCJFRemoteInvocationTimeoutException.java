package org.hcjf.errors;

public class HCJFRemoteInvocationTimeoutException extends HCJFRuntimeException{
    public HCJFRemoteInvocationTimeoutException(String message, Object... params) {
        super(message, params);
    }
}
