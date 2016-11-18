package org.hcjf.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class Messages {

    protected static final Pattern MESSAGE_CODE_PATTERN =
            Pattern.compile("^((([A-Z]|[a-z])*)(\\.(([A-Z]|[a-z])*))*)*@[1-9].*");

    private final Map<String, String> defaultMessages;

    protected Messages() {
        defaultMessages = new HashMap<>();
    }

    /**
     * Returnt he message associated to the error code.
     * @param errorCode Error code.
     * @param params Parameters to complete the message.
     * @return Message complete and translated.
     */
    protected String getInternalMessage(String errorCode, Object... params) {
        String result = defaultMessages.get(errorCode);

        if(result == null) {
            result = errorCode;
        } else {
            //TODO: Translate message
        }

        return String.format(result, params);
    }

    /**
     * Add the default value associated to error code.
     * @param errorCode Error code.
     * @param defaultMessage Default message.
     */
    protected void addInternalDefault(String errorCode, String defaultMessage) {
        if(MESSAGE_CODE_PATTERN.matcher(errorCode).matches()) {
            defaultMessages.put(errorCode, defaultMessage);
        }
    }

}
