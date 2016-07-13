package org.hcjf.encoding;

import org.hcjf.service.ServiceConsumer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class EncodingImpl implements ServiceConsumer {

    protected static final String CHARSET_PARAMETER_NAME = "charset";

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

        BYTE_BUFFER((byte)11);

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
    }

}
