package org.hcjf.encoding;

import org.hcjf.service.ServiceConsumer;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class EncodingImpl implements ServiceConsumer {

    protected static final String CHARSET_PARAMETER_NAME = "charset";

    protected static final String PARAMETERS_JSON_FIELD = "params";
    protected static final String BODY_JSON_FIELD = "body";
    protected static final String QUERY_JSON_FIELD = "query";
    protected static final String QUERY_ID_FIELD = "id";
    protected static final String QUERY_LIMIT_FIELD = "limit";
    protected static final String QUERY_PAGE_START_FIELD = "pageStart";
    protected static final String QUERY_DESC_FIELD = "desc";
    protected static final String QUERY_ORDER_FIELDS_FIELD = "orderFields";
    protected static final String QUERY_EVALUATORS_FIELD = "evaluators";
    protected static final String EVALUATOR_ACTION_FIELD = "a";
    protected static final String EVALUATOR_FIELD_FIELD = "f";
    protected static final String EVALUATOR_VALUE_FIELD = "v";
    protected static final String EVALUATOR_DISTINCT = "distinct";
    protected static final String EVALUATOR_EQUALS = "equals";
    protected static final String EVALUATOR_GREATER_THAN = "greaterThan";
    protected static final String EVALUATOR_GREATER_THAN_OR_EQUALS = "greaterThanOrEquals";
    protected static final String EVALUATOR_IN = "in";
    protected static final String EVALUATOR_LIKE = "like";
    protected static final String EVALUATOR_NOT_IN = "notIn";
    protected static final String EVALUATOR_SMALLER_THAN = "smallerThan";
    protected static final String EVALUATOR_SMALLER_THAN_OR_EQUALS = "smallerThanOrEquals";
    protected static final String TYPE_PARAMETER_FIELD = "t";
    protected static final String VALUE_PARAMETER_FIELD = "v";

    private final MimeType mimeType;
    private final String implementationName;

    public EncodingImpl(MimeType mimeType, String implementationName) {
        this.mimeType = mimeType;
        this.implementationName = implementationName;
    }

    /**
     *
     * @return
     */
    public MimeType getMimeType() {
        return mimeType;
    }

    /**
     *
     * @return
     */
    public String getImplementationName() {
        return implementationName;
    }

    /**
     *
     * @param decodedPackage
     * @return
     */
    public abstract byte[] encode(DecodedPackage decodedPackage);

    /**
     *
     * @param objectClass
     * @param data
     * @return
     */
    public abstract DecodedPackage decode(Class objectClass, byte[] data, Map<String, Object> parameters);

    /**
     *
     * @param data
     * @return
     */
    public abstract DecodedPackage decode(byte[] data, Map<String, Object> parameters);

    /**
     *
     */
    protected enum EncodingType {

        LIST((byte)0),

        MAP((byte)1),

        BOOLEAN((byte)2),

        BYTE((byte)3),

        INTEGER((byte)4),

        SHORT((byte)5),

        LONG((byte)6),

        FLOAT((byte)7),

        DOUBLE((byte)8),

        DATE((byte)9),

        STRING((byte)10),

        BYTE_BUFFER((byte)11),

        UUID((byte)12),

        REGEX((byte)13);

        private final byte id;

        EncodingType(byte id) {
            this.id = id;
        }

        public byte getId() {
            return id;
        }

        public static EncodingType fromId(byte id) {
            EncodingType result = null;

            for(EncodingType type : values()) {
                if(type.getId() == id) {
                    result = type;
                    break;
                }
            }

            return result;
        }

        public static EncodingType fromClass(Class clazz) {
            EncodingType result = null;

            if(List.class.isAssignableFrom(clazz)) {
                result = LIST;
            } else if(Map.class.isAssignableFrom(clazz)) {
                result = MAP;
            } else if(Boolean.class.isAssignableFrom(clazz)) {
                result = BOOLEAN;
            } else if(Byte.class.isAssignableFrom(clazz)) {
                result = BYTE;
            } else if(Integer.class.isAssignableFrom(clazz)) {
                result = INTEGER;
            } else if(Short.class.isAssignableFrom(clazz)) {
                result = SHORT;
            } else if(Long.class.isAssignableFrom(clazz)) {
                result = LONG;
            } else if(Float.class.isAssignableFrom(clazz)) {
                result = FLOAT;
            } else if(Double.class.isAssignableFrom(clazz)) {
                result = DOUBLE;
            } else if(Date.class.isAssignableFrom(clazz)) {
                result = DATE;
            } else if(String.class.isAssignableFrom(clazz)) {
                result = STRING;
            } else if(ByteBuffer.class.isAssignableFrom(clazz)) {
                result = BYTE_BUFFER;
            } else if(java.util.UUID.class.isAssignableFrom(clazz)) {
                result = UUID;
            } else if(Pattern.class.isAssignableFrom(clazz)) {
                result = REGEX;
            }

            return result;
        }
    }

}
