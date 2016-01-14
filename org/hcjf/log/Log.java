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

    /**
     * Private constructor
     */
    private Log() {
    }

    /**
     * Create a record with info tag ("[I]").
     * @param message
     * @param params
     */
    public static void i(String message, Object... params) {

    }
}
