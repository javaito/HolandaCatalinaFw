package org.hcjf.errors;

import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Strings;

public class TaggedException extends RuntimeException {

    public TaggedException(String tag, String message, Object... params) {
        this(tag, message, null, params);
    }

    public TaggedException(String tag, String message, Throwable cause, Object... params) {
        super(Strings.createTaggedMessage(String.format(message, params), tag, getServiceTag()), cause);
    }

    protected static String getServiceTag() {
        return SystemProperties.get(SystemProperties.Net.SERVICE_NAME).toUpperCase();
    }
}
