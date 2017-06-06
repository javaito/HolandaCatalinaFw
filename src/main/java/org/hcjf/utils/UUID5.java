package org.hcjf.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.UUID;

/**
 * @author javaito
 */
public class UUID5 {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    public static final UUID NAMESPACE_DNS = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
    public static final UUID NAMESPACE_URL = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");
    public static final UUID NAMESPACE_OID = UUID.fromString("6ba7b812-9dad-11d1-80b4-00c04fd430c8");
    public static final UUID NAMESPACE_X500 = UUID.fromString("6ba7b814-9dad-11d1-80b4-00c04fd430c8");

    public static UUID nameUUIDFromNamespaceAndString(UUID namespace, String name) {
        return nameUUIDFromNamespaceAndBytes(namespace, Objects.requireNonNull(name, "name == null").getBytes(UTF8));
    }

    public static UUID nameUUIDFromNamespaceAndBytes(UUID namespace, byte[] name) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException nsae) {
            throw new InternalError("SHA-1 not supported");
        }
        md.update(toBytes(Objects.requireNonNull(namespace, "namespace is null")));
        md.update(Objects.requireNonNull(name, "name is null"));
        byte[] sha1Bytes = md.digest();
        sha1Bytes[6] &= 0x0f;  /* clear version        */
        sha1Bytes[6] |= 0x50;  /* set to version 5     */
        sha1Bytes[8] &= 0x3f;  /* clear variant        */
        sha1Bytes[8] |= 0x80;  /* set to IETF variant  */

        int hashCode = UUID.class.hashCode();
        System.out.println(hashCode);
        System.out.println(Integer.toBinaryString(hashCode));
        ByteBuffer hashBuffer = ByteBuffer.allocate(4);
        hashBuffer.putInt(hashCode);
        hashBuffer.rewind();

        sha1Bytes[12] = hashBuffer.get();
        sha1Bytes[13] = hashBuffer.get();
        sha1Bytes[14] = hashBuffer.get();
        sha1Bytes[15] = hashBuffer.get();
        return fromBytes(sha1Bytes);
    }

    private static UUID fromBytes(byte[] data) {
        // Based on the private UUID(bytes[]) constructor
        long msb = 0;
        long lsb = 0;
        assert data.length >= 16;
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (data[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (data[i] & 0xff);
        }
        return new UUID(msb, lsb);
    }

    private static byte[] toBytes(UUID uuid) {
        // inverted logic of fromBytes()
        byte[] out = new byte[16];
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        for (int i = 0; i < 8; i++) {
            out[i] = (byte) ((msb >> ((7 - i) * 8)) & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            out[i] = (byte) ((lsb >> ((15 - i) * 8)) & 0xff);
        }
        return out;
    }

    public static void main(String[] args) {
        UUID hola = UUID5.nameUUIDFromNamespaceAndString(NAMESPACE_DNS, "hola");
        UUID chau = UUID5.nameUUIDFromNamespaceAndString(NAMESPACE_DNS, "chau");

        System.out.println(hola);
        System.out.println(hola.version());
        System.out.println(hola.getMostSignificantBits());
        System.out.println(hola.getLeastSignificantBits());
        System.out.println((int)hola.getLeastSignificantBits());
        System.out.println(Long.toBinaryString(hola.getMostSignificantBits()));
        System.out.println(Long.toBinaryString(hola.getLeastSignificantBits()));
        System.out.println(chau);
        System.out.println(chau.version());
        System.out.println(chau.getMostSignificantBits());
        System.out.println(chau.getLeastSignificantBits());
        System.out.println((int)chau.getLeastSignificantBits());
        System.out.println(Long.toBinaryString(chau.getMostSignificantBits()));
        System.out.println(Long.toBinaryString(chau.getLeastSignificantBits()));
    }
}
