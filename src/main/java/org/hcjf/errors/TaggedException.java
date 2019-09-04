package org.hcjf.errors;

import org.hcjf.utils.Strings;

public class TaggedException extends RuntimeException {

    public TaggedException(String tag, String message) {
        this(tag, message, null);
    }

    public TaggedException(String tag, String message, Throwable cause) {
        super(Strings.createTaggedMessage(message, tag), cause);
    }

}
