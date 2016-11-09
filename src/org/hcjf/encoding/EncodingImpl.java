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
public abstract class EncodingImpl<P extends DecodedPackage> implements ServiceConsumer {

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
    public abstract byte[] encode(P decodedPackage);

    /**
     *
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
