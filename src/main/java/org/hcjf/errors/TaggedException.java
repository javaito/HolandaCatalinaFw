package org.hcjf.errors;

import org.hcjf.utils.Strings;

public class TaggedException extends RuntimeException {

    public TaggedException(String tag, String message, Object... params) {
        this(tag, message, null, params);
    }

    public TaggedException(String tag, String message, Throwable cause, Object... params) {
        super(Strings.createTaggedMessage(String.format(message, params), tag), cause);
    }

}
