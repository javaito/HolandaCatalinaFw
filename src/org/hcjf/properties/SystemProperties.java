package org.hcjf.properties;

import org.hcjf.log.Log;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class is an interface with some utilities in order to
 * be easy the access to java system properties.
 * @author javaito
 * @email javaito@gmail.com
 */
public final class SystemProperties extends Properties {

    private static final String SYSTEM_PROPERTIES_LOG_TAG = "SYSTEM_PROPERTIES";

    public static final String SERVICE_THREAD_POOL_MAX_SIZE = "hcfj.service.thread.pool.max.size";
    public static final String SERVICE_THREAD_POOL_KEEP_ALIVE_TIME = "hcfj.service.thread.pool.keep.alive.time";

    public static final String LOG_PATH = "hcfj.log.path";
    public static final String LOG_FILE_PREFIX = "hcfj.log.file.prefix";
    public static final String LOG_ERROR_FILE = "hcfj.log.error.file";
    public static final String LOG_WARNING_FILE = "hcfj.log.warning.file";
    public static final String LOG_INFO_FILE = "hcfj.log.info.file";
    public static final String LOG_DEBUG_FILE = "hcfj.log.debug.file";
    public static final String LOG_LEVEL= "hcfj.log.level";
    public static final String LOG_DATE_FORMAT = "hcfj.log.date.format";

    public static final String NET_INPUT_BUFFER_SIZE = "hcfj.net.input.buffer.size";
    public static final String NET_OUTPUT_BUFFER_SIZE = "hcfj.net.output.buffer.size";
    public static final String NET_DISCONNECT_AND_REMOVE = "hcfj.net.disconnect.and.remove";
    public static final String NET_CONNECTION_TIMEOUT_AVAILABLE = "hcfj.net.connection.timeout.available";
    public static final String NET_CONNECTION_TIMEOUT = "hcfj.net.connection.timeout";
    public static final String NET_WRITE_TIMEOUT = "hcjf.net.write.timeout";

    public static final String HTTP_SERVER_NAME = "hcjf.http.server.name";
    public static final String HTTP_RESPONSE_DATE_HEADER_FORMAT_VALUE = "hcjf.http.response.date.header.format.value";

    public static final String SHARED_MEMORY_IMPL = "hcjf.shared.memory.impl";
    public static final String SHARED_MEMORY_HAZELCAST_IMPL_NAME = "Hazelcast";

    //Java property names
    public static final String FILE_ENCODING = "file.encoding";

    private static final SystemProperties instance;

    static {
        instance = new SystemProperties();
    }

    private final Map<String, SimpleDateFormat> dateFormaters;

    private SystemProperties() {
        super(new Properties());
        dateFormaters = new HashMap<>();

        defaults.put(SERVICE_THREAD_POOL_MAX_SIZE, Integer.toString(Integer.MAX_VALUE));
        defaults.put(SERVICE_THREAD_POOL_KEEP_ALIVE_TIME, "10");

        defaults.put(LOG_FILE_PREFIX, "hcfj");
        defaults.put(LOG_ERROR_FILE, "false");
        defaults.put(LOG_WARNING_FILE, "false");
        defaults.put(LOG_INFO_FILE, "false");
        defaults.put(LOG_DEBUG_FILE, "false");
        defaults.put(LOG_LEVEL, "I");
        defaults.put(LOG_DATE_FORMAT, "yyyy-MM-dd HH:mm:ss");

        defaults.put(NET_INPUT_BUFFER_SIZE, "1024");
        defaults.put(NET_OUTPUT_BUFFER_SIZE, "1024");
        defaults.put(NET_CONNECTION_TIMEOUT_AVAILABLE, "true");
        defaults.put(NET_CONNECTION_TIMEOUT, "10000");
        defaults.put(NET_DISCONNECT_AND_REMOVE, "true");
        defaults.put(NET_WRITE_TIMEOUT, "10000");

        defaults.put(HTTP_SERVER_NAME, "HCJF Web Server");
        defaults.put(HTTP_RESPONSE_DATE_HEADER_FORMAT_VALUE, "EEE, dd MMM yyyy HH:mm:ss z");

        defaults.put(SHARED_MEMORY_IMPL, SHARED_MEMORY_HAZELCAST_IMPL_NAME);

        Properties system = System.getProperties();
        putAll(system);
        System.setProperties(this);
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

        instance.defaults.put(propertyName, defaultValue);
    }

    /**
     * Calls the <tt>Hashtable</tt> method {@code put}. Provided for
     * parallelism with the <tt>getProperty</tt> method. Enforces use of
     * strings for property keys and values. The value returned is the
     * result of the <tt>Hashtable</tt> call to {@code put}.
     *
     * @param key   the key to be placed into this property list.
     * @param value the value corresponding to <tt>key</tt>.
     * @return the previous value of the specified key in this property
     * list, or {@code null} if it did not have one.
     * @see #getProperty
     * @since 1.2
     */
    @Override
    public synchronized Object setProperty(String key, String value) {
        Object result = super.setProperty(key, value);

        synchronized (dateFormaters) {
            dateFormaters.remove(key);
        }

        try {
            //TODO: Create listeners
        } catch (Exception ex){}

        return result;
    }

    /**
     * This method return the string value of the system property
     * named like the parameter.
     * @param propertyName Name of the find property.
     * @param validator
     * @return Return the value of the property or null if the property is no defined.
     */
    public static String get(String propertyName, PropertyValueValidator<String> validator) {
        String result = System.getProperty(propertyName);

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
            result = Integer.decode(propertyValue);
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
            result = Long.decode(propertyValue);
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
            result = Double.valueOf(propertyValue);
        }

        return result;
    }

    /**
     * Return the default charset of the JVM instance.
     * @return Default charset.
     */
    public static String getDefaultCharset() {
        return System.getProperty(FILE_ENCODING);
    }

    private static SimpleDateFormat getDateFormat(String propertyName) {
        SimpleDateFormat result;
        synchronized (instance.dateFormaters) {
            result = instance.dateFormaters.get(propertyName);
            if(result == null) {
                result = new SimpleDateFormat(get(propertyName));
                instance.dateFormaters.put(propertyName, result);
            }
        }
        return result;
    }

    /**
     *
     * @param propertyName
     * @param value
     * @return
     */
    public static String getFormattedDate(String propertyName, Date value) {
        return getDateFormat(propertyName).format(value);
    }
}
