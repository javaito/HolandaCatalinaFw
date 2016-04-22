package org.hcjf.properties;

import org.hcjf.log.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is an interface with some utilities in order to
 * be easy the access to java system properties.
 * @author javaito
 * @email javaito@gmail.com
 */
public final class SystemProperties {

    private static final String SYSTEM_PROPERTIES_LOG_TAG = "SYSTEM_PROPERTIES";

    public static final String SERVICE_THREAD_POOL_MAX_SIZE = "hcfj_service_thread_pool_max_size";
    public static final String SERVICE_THREAD_POOL_KEEP_ALIVE_TIME = "hcfj_service_thread_pool_keep_alive_time";

    public static final String LOG_PATH = "hcfj_log_path";
    public static final String LOG_FILE_PREFIX = "hcfj_log_file_prefix";
    public static final String LOG_ERROR_FILE = "hcfj_log_error_file";
    public static final String LOG_WARNING_FILE = "hcfj_log_warning_file";
    public static final String LOG_INFO_FILE = "hcfj_log_info_file";
    public static final String LOG_DEBUG_FILE = "hcfj_log_debug_file";
    public static final String LOG_LEVEL= "hcfj_log_level";
    public static final String LOG_DATE_FORMAT = "hcfj_log_date_format";

    public static final String NET_INPUT_BUFFER_SIZE = "hcfj_net_input_buffer_size";
    public static final String NET_OUTPUT_BUFFER_SIZE = "hcfj_net_output_buffer_size";
    public static final String NET_DISCONNECT_AND_REMOVE = "hcfj_net_disconnect_and_remove";
    public static final String NET_CONNECTION_TIMEOUT_AVAILABLE = "hcfj_net_connection_timeout_available";
    public static final String NET_CONNECTION_TIMEOUT = "hcfj_net_connection_timeout";
    public static final String NET_WRITE_TIMEOUT = "hcjf_net_write_timeout";

    private static final SystemProperties instance;

    static {
        instance = new SystemProperties();
    }

    private final Map<String, String> defaultValues;

    private SystemProperties() {
        defaultValues = new HashMap<>();

        defaultValues.put(SERVICE_THREAD_POOL_MAX_SIZE, Integer.toString(Integer.MAX_VALUE));
        defaultValues.put(SERVICE_THREAD_POOL_KEEP_ALIVE_TIME, "10");

        defaultValues.put(LOG_FILE_PREFIX, "hcfj");
        defaultValues.put(LOG_ERROR_FILE, "false");
        defaultValues.put(LOG_WARNING_FILE, "false");
        defaultValues.put(LOG_INFO_FILE, "false");
        defaultValues.put(LOG_DEBUG_FILE, "false");
        defaultValues.put(LOG_LEVEL, "I");
        defaultValues.put(LOG_DATE_FORMAT, "yyyy-MM-dd HH:mm:ss");

        defaultValues.put(NET_INPUT_BUFFER_SIZE, "1024");
        defaultValues.put(NET_OUTPUT_BUFFER_SIZE, "1024");
        defaultValues.put(NET_CONNECTION_TIMEOUT_AVAILABLE, "true");
        defaultValues.put(NET_CONNECTION_TIMEOUT, "10000");
        defaultValues.put(NET_DISCONNECT_AND_REMOVE, "true");
        defaultValues.put(NET_WRITE_TIMEOUT, "10000");
    }

    /**
     * Put the default value for a property.
     * @param propertyName Property name.
     * @param defaultValue Property default value.
     * @throws NullPointerException Throw a {@link NullPointerException} when the
     * property name or default value are null.
     */
    public static void putDefaultValue(String propertyName, String defaultValue) {
        if(propertyName == null) {
            throw new NullPointerException("Invalid property name null");
        }

        if(defaultValue == null) {
            throw new NullPointerException("Invalid default value null");
        }

        instance.defaultValues.put(propertyName, defaultValue);
    }

    /**
     * This method return the string value of the system property
     * named like the parameter.
     * @param propertyName Name of the find property.
     * @param validator
     * @return Return the value of the property or null if the property is no defined.
     */
    public static String get(String propertyName, PropertyValueValidator<String> validator) {
        String defaultValue = instance.defaultValues.get(propertyName);

        String result = defaultValue == null ?
                System.getProperty(propertyName) :
                System.getProperty(propertyName, defaultValue);

        if(result == null) {
            Log.d("Property not found: $1",  propertyName);
        }

        if(validator != null) {
            if(!validator.validate(result)){
                throw new IllegalPropertyValueException(propertyName + "=" + result);
            }
        }

        return result;
    }

    /**
     * This method return the string value of the system property
     * named like the parameter.
     * @param propertyName Name of the find property.
     * @return Return the value of the property or null if the property is no defined.
     */
    public static String get(String propertyName) {
        return get(propertyName, null);
    }

    /**
     * This method return the value of the system property as boolean.
     * @param propertyName Name of the find property.
     * @return Value of the system property as boolean, or null if the property is not found.
     */
    public static Boolean getBoolean(String propertyName) {
        Boolean result = null;

        String propertyValue = get(propertyName);
        if(propertyValue != null){
            result = Boolean.valueOf(propertyValue);
        }

        return result;
    }

    /**
     * This method return the value of the system property as integer.
     * @param propertyName Name of the find property.
     * @return Value of the system property as integer, or null if the property is not found.
     */
    public static Integer getInteger(String propertyName) {
        Integer result = null;

        String propertyValue = get(propertyName);
        if(propertyValue != null) {
            try {
                result = Integer.decode(propertyValue);
            } catch (NumberFormatException ex) {
                Log.d(SYSTEM_PROPERTIES_LOG_TAG, "Number format exception for property $1", ex, propertyName);
                if (instance.defaultValues.containsKey(propertyName)) {
                    result = Integer.decode(instance.defaultValues.get(propertyName));
                }
            }
        }

        return result;
    }

    /**
     * This method return the value of the system property as long.
     * @param propertyName Name of the find property.
     * @return Value of the system property as long, or null if the property is not found.
     */
    public static Long getLong(String propertyName) {
        Long result = null;

        String propertyValue = get(propertyName);
        if(propertyValue != null) {
            try {
                result = Long.decode(propertyValue);
            } catch (NumberFormatException ex) {
                Log.d(SYSTEM_PROPERTIES_LOG_TAG, "Number format exception for property $1", ex, propertyName);
                if (instance.defaultValues.containsKey(propertyName)) {
                    result = Long.decode(instance.defaultValues.get(propertyName));
                }
            }
        }

        return result;
    }

    /**
     * This method return the value of the system property as double.
     * @param propertyName Name of the find property.
     * @return Value of the system property as double, or null if the property is not found.
     */
    public static Double getDouble(String propertyName) {
        Double result = null;

        String propertyValue = get(propertyName);
        if(propertyValue != null) {
            try {
                result = Double.valueOf(propertyValue);
            } catch (NumberFormatException ex) {
                Log.d(SYSTEM_PROPERTIES_LOG_TAG, "Number format exception for property $1", ex, propertyName);
                if (instance.defaultValues.containsKey(propertyName)) {
                    result = Double.valueOf(instance.defaultValues.get(propertyName));
                }
            }
        }

        return result;
    }
}
