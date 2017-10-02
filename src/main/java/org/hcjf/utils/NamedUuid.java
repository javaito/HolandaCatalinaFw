package org.hcjf.utils;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This king of uuid contains a hash code for some string in his internal structure.
 * @author javaito
 */
public final class NamedUuid {

    private static final Map<Integer, String> names;

    static {
        names = new HashMap<>();
    }

    private final UUID id;
    private final Integer hash;

    private NamedUuid(UUID id, Integer hash) {
        this.id = id;
        this.hash = hash;
    }

    /**
     * Register the hash code for the specific name.
     * @param name Specific name.
     */
    public static void registerName(String name) {
        names.put(name.hashCode(), name);
    }

    /**
     * Returns the name registered for the hash stored into the uuid instance.
     * @param uuid Uuid instance.
     * @return Registered name.
     * @throws IllegalArgumentException id the uuid instance is not a uuid version 5.
     */
    public static String getName(UUID uuid) {
        if(uuid.version() == 5) {
            return names.get((int)uuid.getLeastSignificantBits());
        } else {
            throw new IllegalArgumentException("Only for uuid version 5");
        }
    }

    /**
     * Returns the uuid instance.
     * @return UUID instance.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the hash code of the creation name.
     * @return Hash code of the name.
     */
    public Integer getHash() {
        return hash;
    }

    /**
     * Creates an instance of named uuid from a uuid instance.
     * @param uuid UUID instance.
     * @return Named uuid instance.
     */
    public static NamedUuid create(UUID uuid) {
        return new NamedUuid(uuid, (int)uuid.getLeastSignificantBits());
    }

    /**
     * Creates an instance of named uuid from a name.
     * @param name Name
     * @return Named uuid instance.
     */
    public static NamedUuid create(String name) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException("SHA-1 not supported");
        }

        //Put a random uuid and the class name into the digest algorithm to create the base uuid.
        md.update(toBytes(UUID.randomUUID()));
        md.update(name.getBytes());
        byte[] sha1Bytes = md.digest();

        //Clear the uuid version and set the version number 5
        sha1Bytes[6] &= 0x0f;
        sha1Bytes[6] |= 0x50;

        //Clear the variant and set to IETF variant.
        sha1Bytes[8] &= 0x3f;
        sha1Bytes[8] |= 0x80;

        //Create a hash code for the class name and put this code into the least significant bits
        //of the uuid instance.
        int hashCode = name.hashCode();
        ByteBuffer hashBuffer = ByteBuffer.allocate(4);
        hashBuffer.putInt(hashCode);
        hashBuffer.rewind();

        sha1Bytes[12] = hashBuffer.get();
        sha1Bytes[13] = hashBuffer.get();
        sha1Bytes[14] = hashBuffer.get();
        sha1Bytes[15] = hashBuffer.get();
        return new NamedUuid(fromBytes(sha1Bytes), hashCode);
    }

    /**
     * Create an instance of uuid using a byte array as seed.
     * @param data Byte array.
     * @return UUID instance.
     */
    private static UUID fromBytes(byte[] data) {
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

    /**
     * Creates a byte array using the long values into the uuid instance.
     * @param uuid UUID instance.
     * @return Byte array.
     */
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

    /**
     * Returns the string representation of internal id.
     * @return String representation of the internal id.
     */
    @Override
    public String toString() {
        return id.toString();
    }
}
