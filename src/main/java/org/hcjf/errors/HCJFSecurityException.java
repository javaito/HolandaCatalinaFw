package org.hcjf.errors;

import org.hcjf.utils.Strings;

public class HCJFSecurityException extends HCJFRuntimeException {

    private static final String SECURITY_TAG = "SECURITY";

    public HCJFSecurityException(String message, Object... params) {
        this(message, null, params);
    }

    public HCJFSecurityException(String message, Throwable cause, Object... params) {
        super(Strings.createTaggedMessage(String.format(message, params), getTag(SECURITY_TAG, cause)), cause);
    }
}
