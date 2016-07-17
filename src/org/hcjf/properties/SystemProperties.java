package org.hcjf.properties;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hcjf.log.Log;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class overrides the system properties default implementation adding
 * some default values and properties definitions for the service-oriented platforms
 * works.
 * @author javaito
 * @email javaito@gmail.com
 */
public final class SystemProperties extends Properties {

    private static final String HCJF_DEFAULT_DATE_FORMAT = "hcjf.default.date.format";
    private static final String HCJF_DEFAULT_NUMBER_FORMAT = "hcjf.default.number.format";
    private static final String HCJF_DEFAULT_DECIMAL_SEPARATOR = "hcjf.default.decimal.separator";
    private static final String HCJF_DEFAULT_GROUPING_SEPARATOR = "hcjf.default.grouping.separator";
    private static final String HCJF_DEFAULT_LOCALE = "hcjf.default.locale";

    public static final String SERVICE_THREAD_POOL_MAX_SIZE = "hcfj.service.thread.pool.max.size";
    public static final String SERVICE_THREAD_POOL_KEEP_ALIVE_TIME = "hcfj.service.thread.pool.keep.alive.time";

    public static final String LOG_FILE_PREFIX = "hcfj.log.file.prefix";
    public static final String LOG_ERROR_FILE = "hcfj.log.error.file";
    public static final String LOG_WARNING_FILE = "hcfj.log.warning.file";
    public static final String LOG_INFO_FILE = "hcfj.log.info.file";
    public static final String LOG_DEBUG_FILE = "hcfj.log.debug.file";
    public static final String LOG_LEVEL= "hcfj.log.level";
    public static final String LOG_DATE_FORMAT = "hcfj.log.date.format";
    public static final String LOG_CONSUMERS = "hcjf.log.consumers";
    public static final String LOG_SYSTEM_OUT_ENABLED = "hcjf.log.system.out.enabled";
    public static final String LOG_QUEUE_INITIAL_SIZE = "hcjf.log.queue.initial.size";

    public static final String NET_INPUT_BUFFER_SIZE = "hcfj.net.inpt.buffer.size";
    public static final String NET_OUTPUT_BUFFER_SIZE = "hcfj.net.output.buffer.size";
    public static final String NET_DISCONNECT_AND_REMOVE = "hcfj.net.disconnect.and.remove";
    public static final String NET_CONNECTION_TIMEOUT_AVAILABLE = "hcfj.net.connection.timeout.available";
    public static final String NET_CONNECTION_TIMEOUT = "hcfj.net.connection.timeout";
    public static final String NET_WRITE_TIMEOUT = "hcjf.net.write.timeout";

    public static final String HTTP_SERVER_NAME = "hcjf.http.server.name";
    public static final String HTTP_RESPONSE_DATE_HEADER_FORMAT_VALUE = "hcjf.http.response.date.header.format.value";
    public static final String HTTP_INPUT_LOG_BODY_MAX_LENGTH = "hcjf.http.input.log.body.max.length";
    public static final String HTTP_OUTPUT_LOG_BODY_MAX_LENGTH = "hcjf.http.output.log.body.max.length";

    public static final String REST_DEFAULT_MIME_TYPE = "hcjf.rest.default.mime.type";
    public static final String REST_DEFAULT_ENCODING_IMPL = "hcjf.rest.default.encoding.impl";
    public static final String REST_QUERY_PATH = "hcjf.rest.query.path";
    public static final String REST_QUERY_PARAMETER_PATH = "hcjf.rest.query.parameter.path";

    public static final String QUERY_DEFAULT_LIMIT = "hcjf.query.default.limit";
    public static final String QUERY_DEFAULT_DESC_ORDER = "hcjf.query.default.desc.order";

    public static final String CLOUD_IMPL = "hcjf.cloud.impl";

    //Java property names
    public static final String FILE_ENCODING = "file.encoding";

    private static final SystemProperties instance;

    static {
        instance = new SystemProperties();
    }

    private final Map<String, Object> instancesCache;
    private final JsonParser jsonParser;

    private SystemProperties() {
        super(new Properties());
        instancesCache = new HashMap<>();
        jsonParser = new JsonParser();

        defaults.put(HCJF_DEFAULT_DATE_FORMAT, "yyyy-MM-dd HH:mm:ss");
        defaults.put(HCJF_DEFAULT_NUMBER_FORMAT, "0.000");
        defaults.put(HCJF_DEFAULT_DECIMAL_SEPARATOR, ".");
        defaults.put(HCJF_DEFAULT_GROUPING_SEPARATOR, ",");
        defaults.put(HCJF_DEFAULT_LOCALE, "EN");

        defaults.put(SERVICE_THREAD_POOL_MAX_SIZE, Integer.toString(Integer.MAX_VALUE));
        defaults.put(SERVICE_THREAD_POOL_KEEP_ALIVE_TIME, "10");

        defaults.put(LOG_FILE_PREFIX, "hcfj");
        defaults.put(LOG_ERROR_FILE, "false");
        defaults.put(LOG_WARNING_FILE, "false");
        defaults.put(LOG_INFO_FILE, "false");
        defaults.put(LOG_DEBUG_FILE, "false");
        defaults.put(LOG_LEVEL, "1");
        defaults.put(LOG_DATE_FORMAT, "yyyy-MM-dd HH:mm:ss");
        defaults.put(LOG_CONSUMERS, "[]");
        defaults.put(LOG_SYSTEM_OUT_ENABLED, "true");
        defaults.put(LOG_QUEUE_INITIAL_SIZE, "10000");

        defaults.put(NET_INPUT_BUFFER_SIZE, "1024");
        defaults.put(NET_OUTPUT_BUFFER_SIZE, "1024");
        defaults.put(NET_CONNECTION_TIMEOUT_AVAILABLE, "true");
        defaults.put(NET_CONNECTION_TIMEOUT, "10000");
        defaults.put(NET_DISCONNECT_AND_REMOVE, "true");
        defaults.put(NET_WRITE_TIMEOUT, "10000");

        defaults.put(HTTP_SERVER_NAME, "HCJF Web Server");
        defaults.put(HTTP_RESPONSE_DATE_HEADER_FORMAT_VALUE, "EEE, dd MMM yyyy HH:mm:ss z");
        defaults.put(HTTP_INPUT_LOG_BODY_MAX_LENGTH, "1024");
        defaults.put(HTTP_OUTPUT_LOG_BODY_MAX_LENGTH, "1024");

        defaults.put(REST_DEFAULT_MIME_TYPE, "application/json");
        defaults.put(REST_DEFAULT_ENCODING_IMPL, "hcjf");
        defaults.put(REST_QUERY_PATH, "query");
        defaults.put(REST_QUERY_PARAMETER_PATH, "q");

        defaults.put(QUERY_DEFAULT_LIMIT, "1000");
        defaults.put(QUERY_DEFAULT_DESC_ORDER, "false");

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

        synchronized (instancesCache) {
            instancesCache.remove(key);
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
        try {
            if (propertyValue != null) {
                result = Boolean.valueOf(propertyValue);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("The property value has not a boolean valid format: '"
                    + propertyName + ":" + propertyValue + "'", ex);
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
        try {
            if(propertyValue != null) {
                result = Integer.decode(propertyValue);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("The property value has not a integer valid format: '"
                    + propertyName + ":" + propertyValue + "'", ex);
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
        try {
            if (propertyValue != null) {
                result = Long.decode(propertyValue);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("The property value has not a long valid format: '"
                    + propertyName + ":" + propertyValue + "'", ex);
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
        try {
            if (propertyValue != null) {
                result = Double.valueOf(propertyValue);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("The property value has not a double valid format: '"
                    + propertyName + ":" + propertyValue + "'", ex);
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

    /**
     * This method return the value of the property as Locale instance.
     * The instance returned will be stored on near cache and will be removed when the
     * value of the property has been updated.
     * @param propertyName Name of the property that contains locale representation.
     * @return Locale instance.
     */
    public static Locale getLocale(String propertyName) {
        Locale result;
        synchronized (instance.instancesCache) {
            result = (Locale) instance.instancesCache.get(propertyName);
            if(result == null) {
                String propertyValue = get(propertyName);
                try {
                    result = Locale.forLanguageTag(propertyValue);
                    instance.instancesCache.put(propertyName, result);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("The property value has not a locale tag valid format: '"
                            + propertyName + ":" + propertyValue + "'", ex);
                }
            }
        }
        return result;
    }

    /**
     * This method return the valuo of the property called 'hcjf.default.locale' as a locale instance.
     * The instance returned will be stored on near cache and will be removed when the
     * value of the property has been updated.
     * @return Locale instance.
     */
    public static Locale getLocale() {
        return getLocale(HCJF_DEFAULT_LOCALE);
    }

    /**
     * This method return the value of the property as a DecimalFormat instnace.
     * The instance returned will be stored on near cache and will be removed when the
     * value of the property has been updated.
     * @param propertyName Name of the property that contains decimal pattern.
     * @return DecimalFormat instance.
     */
    public static DecimalFormat getDecimalFormat(String propertyName) {
        DecimalFormat result;
        synchronized (instance.instancesCache) {
            result = (DecimalFormat) instance.instancesCache.get(propertyName);
            if(result == null) {
                String propertyValue = get(propertyName);
                try {
                    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                    symbols.setDecimalSeparator(get(HCJF_DEFAULT_DECIMAL_SEPARATOR).charAt(0));
                    symbols.setGroupingSeparator(get(HCJF_DEFAULT_GROUPING_SEPARATOR).charAt(0));
                    result = new DecimalFormat(propertyValue, symbols);
                    instance.instancesCache.put(propertyName, result);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("The property value has not a decimal pattern valid format: '"
                            + propertyName + ":" + propertyValue + "'", ex);
                }
            }
        }
        return result;
    }

    /**
     * This method return the value of the property as a SimpleDateFormat instance.
     * The instance returned will be stored on near cache and will be removed when the
     * value of the property has been updated.
     * @param propertyName Name of the property that contains date representation.
     * @return Simple date format instance.
     */
    public static SimpleDateFormat getDateFormat(String propertyName) {
        SimpleDateFormat result;
        synchronized (instance.instancesCache) {
            result = (SimpleDateFormat) instance.instancesCache.get(propertyName);
            if(result == null) {
                String propertyValue = get(propertyName);
                try {
                    result = new SimpleDateFormat(get(propertyName));
                    instance.instancesCache.put(propertyName, result);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("The property value has not a date pattern valid format: '"
                            + propertyName + ":" + propertyValue + "'", ex);
                }
            }
        }
        return result;
    }

    /**
     * This method return the value of the property as instance of list.
     * @param propertyName Name of the property that contains the json array representation.
     * @return List instance.
     */
    public static List<String> getList(String propertyName) {
        String propertyValue = get(propertyName);
        List<String> result = new ArrayList<>();
        if(instance.instancesCache.containsKey(propertyName)) {
            result.addAll((List<? extends String>) instance.instancesCache.get(propertyName));
        } else {
            try {
                Gson gson = new Gson();
                JsonArray array = (JsonArray) instance.jsonParser.parse(propertyValue);
                array.forEach(A -> result.add(A.getAsString()));
                List<String> cachedResult = new ArrayList<>();
                cachedResult.addAll(result);
                instance.instancesCache.put(propertyName, cachedResult);
            } catch (Exception ex) {
                throw new IllegalArgumentException("The property value has not a json array valid format: '"
                        + propertyName + ":" + propertyValue + "'", ex);
            }
        }
        return result;
    }

    /**
     * This method return the value of the property as instance of map.
     * @param propertyName The name of the property that contains the json object representation.
     * @return Map instance.
     */
    public static Map<String, String> getMap(String propertyName) {
        String propertyValue = get(propertyName);
        Map<String, String> result = new HashMap<>();
        if(instance.instancesCache.containsKey(propertyName)) {
            result.putAll((Map<String, String>) instance.instancesCache.get(propertyName));
        } else {
            try {
                Gson gson = new Gson();
                JsonObject object = (JsonObject) instance.jsonParser.parse(propertyValue);
                object.entrySet().forEach(S -> result.put(S.getKey(), object.get(S.getKey()).getAsString()));
                Map<String, String> cachedResult = new HashMap<>();
                cachedResult.putAll(result);
                instance.instancesCache.put(propertyName, cachedResult);
            } catch (Exception ex) {
                throw new IllegalArgumentException("The property value has not a json object valid format: '"
                        + propertyName + ":" + propertyValue + "'", ex);
            }
        }
        return result;
    }
}
