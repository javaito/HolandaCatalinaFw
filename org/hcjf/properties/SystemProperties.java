package org.hcjf.properties;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is an interface with some utilities in order to
 * be easy the access to java system properties.
 * @author javaito
 * @email javaito@gmail.com
 */
public final class SystemProperties {

    public static final String LOG_PATH = "hcfj_log_path";
    public static final String LOG_FILE_PREFIX = "hcfj_log_file_prefix";
    public static final String LOG_ERROR_FILE = "hcfj_log_error_file";
    public static final String LOG_WARNING_FILE = "hcfj_log_warning_file";
    public static final String LOG_INFO_FILE = "hcfj_log_info_file";
    public static final String LOG_DEBUG_FILE = "hcfj_log_debug_file";
    public static final String LOG_LEVEL= "hcfj_log_level";
    public static final String LOG_DATE_FORMAT = "hcfj_log_date_format";

    private static final SystemProperties instance;

    static {
        instance = new SystemProperties();
    }

    private final Map<String, String> defaultValues;

    private SystemProperties() {
        defaultValues = new HashMap<>();

        defaultValues.put(LOG_FILE_PREFIX, "hcfj");
        defaultValues.put(LOG_ERROR_FILE, "false");
        defaultValues.put(LOG_WARNING_FILE, "false");
        defaultValues.put(LOG_INFO_FILE, "false");
        defaultValues.put(LOG_DEBUG_FILE, "false");
        defaultValues.put(LOG_LEVEL, "I");
        defaultValues.put(LOG_DATE_FORMAT, "yyyy-mm-dd HH:MM:ss");

    }

    /**
     * This method return the string value of the system property
     * named like the parameter.
     * @param propertyName Name of the find property.
     * @return Return the value of the property or null if the property is no defined.
     */
    public static String get(String propertyName) {
        String defaultValue = instance.defaultValues.get(propertyName);

        return defaultValue == null ?
                System.getProperty(propertyName) :
                System.getProperty(propertyName, defaultValue);
    }
}
