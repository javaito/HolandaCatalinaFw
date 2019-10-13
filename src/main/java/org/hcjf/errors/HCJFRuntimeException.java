package org.hcjf.errors;

import org.hcjf.properties.SystemProperties;

public class HCJFRuntimeException extends TaggedException {

    private static final String EXCEPTION_TAG = "SYSTEM_EX";
    private static final String ERROR_TAG = "SYSTEM_ERR";

    public HCJFRuntimeException(String message, Object... params) {
        this(message, null, params);
    }

    public HCJFRuntimeException(String message, Throwable cause, Object... params) {
        super(getTag(cause), message, cause, params);
    }

    protected static String getTag(Throwable cause) {
        String tag = SystemProperties.get(SystemProperties.HCJF_DEFAULT_EXCEPTION_MESSAGE_TAG);
        if(cause != null) {
            if(cause instanceof Error) {
                tag = ERROR_TAG;
            } else if(cause instanceof Exception) {
                tag = EXCEPTION_TAG;
            }
        }
        return tag;
    }
}
