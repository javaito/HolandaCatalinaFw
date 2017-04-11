package org.hcjf.log;

import org.hcjf.utils.Messages;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public final class LogMessages extends Messages {

    private static final LogMessages instance;

    static {
        instance = new LogMessages();
    }

    private LogMessages() {
    }

    /**
     * Returnt he message associated to the log code.
     * @param logCode Log code.
     * @param params Parameters to complete the message.
     * @return Message complete and translated.
     */
    public static String getMessage(String logCode, Object... params) {
        return instance.getInternalMessage(logCode, params);
    }

    /**
     * Add the default value associated to log code.
     * @param logCode Log code.
     * @param defaultMessage Default message.
     */
    public static void addDefault(String logCode, String defaultMessage) {
        instance.addInternalDefault(logCode, defaultMessage);
    }


}
