package org.hcjf.errors;

import org.hcjf.utils.Messages;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public final class Errors extends Messages {

    private static final Errors instance;

    public static final String ORG_HCJF_ENCODING_1 = "org.hcjf.encoding@1";
    public static final String ORG_HCJF_ENCODING_2 = "org.hcjf.encoding@2";
    public static final String ORG_HCJF_ENCODING_3 = "org.hcjf.encoding@3";
    public static final String ORG_HCJF_ENCODING_4 = "org.hcjf.encoding@4";
    public static final String ORG_HCJF_ENCODING_5 = "org.hcjf.encoding@5";
    public static final String ORG_HCJF_ENCODING_6 = "org.hcjf.encoding@6";
    public static final String ORG_HCJF_ENCODING_7 = "org.hcjf.encoding@7";
    public static final String ORG_HCJF_ENCODING_8 = "org.hcjf.encoding@8";
    public static final String ORG_HCJF_ENCODING_9 = "org.hcjf.encoding@9";
    public static final String ORG_HCJF_ENCODING_10 = "org.hcjf.encoding@10";
    public static final String ORG_HCJF_ENCODING_11 = "org.hcjf.encoding@11";
    public static final String ORG_HCJF_ENCODING_12 = "org.hcjf.encoding@12";
    public static final String ORG_HCJF_ENCODING_13 = "org.hcjf.encoding@13";
    public static final String ORG_HCJF_ENCODING_14 = "org.hcjf.encoding@14";
    public static final String ORG_HCJF_ENCODING_15 = "org.hcjf.encoding@15";
    public static final String ORG_HCJF_ENCODING_16 = "org.hcjf.encoding@16";
    public static final String ORG_HCJF_ENCODING_17 = "org.hcjf.encoding@17";
    public static final String ORG_HCJF_ENCODING_18 = "org.hcjf.encoding@18";
    public static final String ORG_HCJF_ENCODING_19 = "org.hcjf.encoding@19";
    public static final String ORG_HCJF_ENCODING_20 = "org.hcjf.encoding@20";
    public static final String ORG_HCJF_ENCODING_21 = "org.hcjf.encoding@21";
    public static final String ORG_HCJF_ENCODING_22 = "org.hcjf.encoding@22";

    public static final String ORG_HCJF_IO_FS_1 = "org.hcjf.io.fs@1";

    public static final String ORG_HCJF_IO_NET_HTTP_1 = "org.hcjf.io.net.http@1";
    public static final String ORG_HCJF_IO_NET_HTTP_2 = "org.hcjf.io.net.http@2";
    public static final String ORG_HCJF_IO_NET_HTTP_3 = "org.hcjf.io.net.http@3";
    public static final String ORG_HCJF_IO_NET_HTTP_4 = "org.hcjf.io.net.http@4";
    public static final String ORG_HCJF_IO_NET_HTTP_5 = "org.hcjf.io.net.http@5";
    public static final String ORG_HCJF_IO_NET_HTTP_6 = "org.hcjf.io.net.http@6";
    public static final String ORG_HCJF_IO_NET_HTTP_7 = "org.hcjf.io.net.http@7";
    public static final String ORG_HCJF_IO_NET_HTTP_8 = "org.hcjf.io.net.http@8";
    public static final String ORG_HCJF_IO_NET_HTTP_9 = "org.hcjf.io.net.http@9";
    public static final String ORG_HCJF_IO_NET_HTTP_END_POINT_NOT_FOUND = "org.hcjf.io.net.http.resource.not.found";

    public static final String ORG_HCJF_IO_NET_HTTP_LAYERED_1 = "org.hcjf.io.net.http.layered@1";

    public static final String ORG_HCJF_IO_NET_HTTP_PROXY_1 = "org.hcjf.io.net.http.proxy@1";
    public static final String ORG_HCJF_IO_NET_HTTP_PROXY_2 = "org.hcjf.io.net.http.proxy@2";

    public static final String ORG_HCJF_IO_NET_HTTP_REST_1 = "org.hcjf.io.net.http.rest@1";
    public static final String ORG_HCJF_IO_NET_HTTP_REST_2 = "org.hcjf.io.net.http.rest@2";
    public static final String ORG_HCJF_IO_NET_HTTP_REST_3 = "org.hcjf.io.net.http.rest@3";
    public static final String ORG_HCJF_IO_NET_HTTP_REST_4 = "org.hcjf.io.net.http.rest@4";

    public static final String ORG_HCJF_EVENTS_1 = "org.hcjf.events@1";

    static {
        instance = new Errors();

        addDefault(ORG_HCJF_ENCODING_1, "Parameters map can't be null");
        addDefault(ORG_HCJF_ENCODING_2, "EncodingService implementation not found: %s@%s");
        addDefault(ORG_HCJF_ENCODING_3, "Only support crud packages");
        addDefault(ORG_HCJF_ENCODING_4, "Byte buffer type is not supported for 'HCJF' json encoding");
        addDefault(ORG_HCJF_ENCODING_5, "The HCJF json implementation expected a json object like data");
        addDefault(ORG_HCJF_ENCODING_6, "The json field %s must be json object or json array");
        addDefault(ORG_HCJF_ENCODING_7, "Unable to create instance of %s because body field not found in json object");
        addDefault(ORG_HCJF_ENCODING_8, "The evaluator action json object must has field 'a' as string");
        addDefault(ORG_HCJF_ENCODING_9, "The evaluator action json object must has field 'f' as string");
        addDefault(ORG_HCJF_ENCODING_10, "The evaluator action json object must has field 'v' as typed object");
        addDefault(ORG_HCJF_ENCODING_11, "Not implemented evaluation action: %s");
        addDefault(ORG_HCJF_ENCODING_12, "The HCJF json implementation expected parameter values as json object or as json primitive");
        addDefault(ORG_HCJF_ENCODING_13, "The HCJF json implementation expected %s field as json object");
        addDefault(ORG_HCJF_ENCODING_14, "Unable to create instance of %s");
        addDefault(ORG_HCJF_ENCODING_15, "Unable to add field %s");
        addDefault(ORG_HCJF_ENCODING_16, "The HCJF json implementation expected %s field as json object");
        addDefault(ORG_HCJF_ENCODING_17, "Unsupported encoding type for field %s");
        addDefault(ORG_HCJF_ENCODING_18, "The field %s expected as json array of json object, with internal format '{t:typeByte,v:value}'");
        addDefault(ORG_HCJF_ENCODING_19, "The field %s expected as json array");
        addDefault(ORG_HCJF_ENCODING_20, "The field %s expected as json object");
        addDefault(ORG_HCJF_ENCODING_21, "The field %s expected as %s");
        addDefault(ORG_HCJF_ENCODING_22, "Byte buffer type is not supported for 'HCJF' json encoding");

        addDefault(ORG_HCJF_IO_FS_1, "File system consumer null");

        addDefault(ORG_HCJF_IO_NET_HTTP_1, "Folder location can't be null");
        addDefault(ORG_HCJF_IO_NET_HTTP_2, "The base folder doesn't exist");
        addDefault(ORG_HCJF_IO_NET_HTTP_3, "Forbidden path (%s):%s");
        addDefault(ORG_HCJF_IO_NET_HTTP_4, "Unable to read file: %s");
        addDefault(ORG_HCJF_IO_NET_HTTP_5, "File not found: %s");
        addDefault(ORG_HCJF_IO_NET_HTTP_6, "Unsupported ssl engine");
        addDefault(ORG_HCJF_IO_NET_HTTP_7, "Parameter 'groupName' can't be null");
        addDefault(ORG_HCJF_IO_NET_HTTP_8, "Parameter 'parameterName' can't be null");
        addDefault(ORG_HCJF_IO_NET_HTTP_9, "File checksum algorithm not found");
        addDefault(ORG_HCJF_IO_NET_HTTP_END_POINT_NOT_FOUND, "End point not found: %s");

        addDefault(ORG_HCJF_IO_NET_HTTP_LAYERED_1, "Resource name can't be null");

        addDefault(ORG_HCJF_IO_NET_HTTP_PROXY_1, "Null http proxy rule");
        addDefault(ORG_HCJF_IO_NET_HTTP_PROXY_2, "This kind of http server not support add custom context");

        addDefault(ORG_HCJF_IO_NET_HTTP_REST_1, "Resource name parameter not found");
        addDefault(ORG_HCJF_IO_NET_HTTP_REST_2, "Resource query parameter not found.");
        addDefault(ORG_HCJF_IO_NET_HTTP_REST_3, "The resources can't be created using a query like a parameter.");
        addDefault(ORG_HCJF_IO_NET_HTTP_REST_4, "%s method is not implemented on the REST interface");

        addDefault(ORG_HCJF_EVENTS_1, "Consumer null");
    }

    private Errors() {
    }

    /**
     * Returnt he message associated to the error code.
     * @param errorCode Error code.
     * @param params Parameters to complete the message.
     * @return Message complete and translated.
     */
    public static String getMessage(String errorCode, Object... params) {
        return instance.getInternalMessage(errorCode, params);
    }

    /**
     * Add the default value associated to error code.
     * @param errorCode Error code.
     * @param defaultMessage Default message.
     */
    public static void addDefault(String errorCode, String defaultMessage) {
        instance.addInternalDefault(errorCode, defaultMessage);
    }

}
