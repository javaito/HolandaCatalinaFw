package org.hcjf.log;

/**
 * Static class that contains the funcionality in order to
 * maintain and organize a log file with the same records format
 * The log behavior is affected by the following system properties
 * <br><b>hcfj_log_path</b>: work directory of the log, by default app work directory
 * <br><b>hcfj_log_file_prefix</b>: all the log files start with this prefix, by default hcjf
 * <br><b>hcfj_log_error_file</b>: if the property is true then log create a particular file for error tag only, by default false
 * <br><b>hcfj_log_warning_file</b>: if the property is true then log create a particular file for warning tag only, by default false
 * <br><b>hcfj_log_info_file</b>: if the property is true then log create a particular file for info tag only, by default false
 * <br><b>hcfj_log_debug_file</b>: if the property is true then log create a particular file for debug tag only, by default false
 * <br><b>hcfj_log_level</b>: min level to write file, by default "I"
 * @author javaito
 * @mail javaito@gmail.com
 */
public final class Log {

    private static final String LOG_PATH = "log_path";
    private static final String LOG_FILE_PREFIX = "log_file_prefix";
    private static final String LOG_ERROR_FILE = "log_error_file";
    private static final String LOG_WARNING_FILE = "log_warning_file";
    private static final String LOG_INFO_FILE = "log_info_file";
    private static final String LOG_DEBUG_FILE = "log_debug_file";
    private static final String LOG_LEVEL= "log_level";

    /**
     * Private constructor
     */
    private Log() {
    }

    /**
     * Create a record with debug tag ("[D]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param message Message to the record.
     * @param params Parameters for the places in the message.
     */
    public static void d(String message, Object... params) {

    }

    /**
     * Create a record with info tag ("[I]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param message Message to the record.
     * @param params Parameters for the places in the message.
     */
    public static void i(String message, Object... params) {

    }

    /**
     * Create a record with warning tag ("[W]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param message Message to the record.
     * @param params Parameters for the places in the message.
     */
    public static void w(String message, Object... params) {

    }

    /**
     * Create a record with warning tag ("[W]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param message Message to the record.
     * @param throwable Throwable whose message will be printed as part of record
     * @param params Parameters for the places in the message.
     */
    public static void w(String message, Throwable throwable, Object... params) {

    }

    /**
     * Create a record with error tag ("[E]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param message Message to the record.
     * @param params Parameters for the places in the message.
     */
    public static void e(String message, Object... params) {

    }

    /**
     * Create a record with error tag ("[E]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param message Message to the record.
     * @param throwable Throwable whose message will be printed as part of record
     * @param params Parameters for the places in the message.
     */
    public static void e(String message, Throwable throwable, Object... params) {

    }

    private static class LogRecord {

    }

    /**
     * This enum contains all the possible tags for the records
     */
    private static enum LogTag {

        DEBUG("D", 0),

        INFO("I", 1),

        WARNING("W", 2),

        ERROR("E", 3);

        private Integer order;
        private String tag;

        LogTag(String tag, Integer order) {
            this.order = order;
            this.tag = tag;
        }

        /**
         * Return the tag order.
         * @return Tag order.
         */
        public Integer getOrder() {
            return order;
        }

        /**
         * Return the tag label.
         * @return Tag label.
         */
        public String getTag() {
            return tag;
        }
    }
}
