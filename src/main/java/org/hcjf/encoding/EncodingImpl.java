package org.hcjf.encoding;

import org.hcjf.service.ServiceConsumer;

import java.util.Map;

/**
 * Encoding implementation base class.
 * @author javaito
 * @mail javaito@gmail.com
 */
public abstract class EncodingImpl<P extends DecodedPackage> implements ServiceConsumer {

    private final MimeType mimeType;
    private final String implementationName;

    public EncodingImpl(MimeType mimeType, String implementationName) {
        this.mimeType = mimeType;
        this.implementationName = implementationName;
    }

    /**
     * Return the mime type associated to the encoding.
     * @return Mime type.
     */
    public MimeType getMimeType() {
        return mimeType;
    }

    /**
     * Return the name of the implementation.
     * @return Implementation name.
     */
    public String getImplementationName() {
        return implementationName;
    }

    /**
     * The implementation of this method must encode
     * some kind of decoded package.
     * @param decodedPackage Decoded package.
     * @return Return the byte array that represents
     * the decoded package's encoding.
     */
    public abstract byte[] encode(P decodedPackage);

    /**
     * Decode a byte array in order to generate an instance of the some
     * decode package.
     * @param objectClass
     * @param data
     * @return
     */
    public abstract P decode(Class objectClass, byte[] data, Map<String, Object> parameters);

    /**
     *
     * @param data
     * @return
     */
    public abstract P decode(byte[] data, Map<String, Object> parameters);

}
