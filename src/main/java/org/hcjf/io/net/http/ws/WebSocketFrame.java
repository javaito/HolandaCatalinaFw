package org.hcjf.io.net.http.ws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * WebSocket frame codec according to RFC 6455.
 *
 * Frame format (simplified):
 *   Byte 0: FIN(1) | RSV(3) | Opcode(4)
 *   Byte 1: MASK(1) | PayloadLen(7)
 *   [2 bytes if len=126] [8 bytes if len=127]
 *   [4 bytes mask if MASK=1]
 *   Payload (unmasked if client→server)
 *
 * @author javaito
 */
public final class WebSocketFrame {

    private static final String WS_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    /**
     * Opcodes defined by RFC 6455.
     */
    public enum Opcode {
        CONTINUATION(0x0),
        TEXT(0x1),
        BINARY(0x2),
        CLOSE(0x8),
        PING(0x9),
        PONG(0xA);

        private final int code;

        Opcode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Opcode fromCode(int code) {
            for (Opcode op : values()) {
                if (op.code == code) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Unknown WebSocket opcode: 0x" + Integer.toHexString(code));
        }
    }

    private final boolean fin;
    private final Opcode opcode;
    private final byte[] payload;

    private WebSocketFrame(boolean fin, Opcode opcode, byte[] payload) {
        this.fin = fin;
        this.opcode = opcode;
        this.payload = payload;
    }

    // ── Decoding ────────────────────────────────────────────────────────────

    /**
     * Decodes a frame received from the client.
     * Client→server frames are always masked (RFC 6455 §5.3).
     * @param data Raw frame bytes.
     * @return Decoded frame.
     */
    public static WebSocketFrame decode(byte[] data) {
        if (data == null || data.length < 2) {
            throw new IllegalArgumentException("WebSocket frame too short");
        }

        boolean fin    = (data[0] & 0x80) != 0;
        int opcodeCode = (data[0] & 0x0F);
        boolean masked = (data[1] & 0x80) != 0;
        int rawLen     = (data[1] & 0x7F);

        int offset = 2;
        int length;

        if (rawLen == 126) {
            length = ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
            offset = 4;
        } else if (rawLen == 127) {
            length = (int) ByteBuffer.wrap(data, 2, 8).getLong();
            offset = 10;
        } else {
            length = rawLen;
        }

        byte[] payload;
        if (masked) {
            byte[] mask = {data[offset], data[offset + 1], data[offset + 2], data[offset + 3]};
            offset += 4;
            payload = new byte[length];
            for (int i = 0; i < length; i++) {
                payload[i] = (byte) (data[offset + i] ^ mask[i % 4]);
            }
        } else {
            payload = new byte[length];
            System.arraycopy(data, offset, payload, 0, length);
        }

        return new WebSocketFrame(fin, Opcode.fromCode(opcodeCode), payload);
    }

    // ── Static encoding (server→client, unmasked) ────────────────────────────

    /**
     * Encodes a text message as a WebSocket TEXT frame.
     * @param message Text to send.
     * @return Encoded frame bytes.
     */
    public static byte[] encodeText(String message) {
        return encode(Opcode.TEXT, message.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encodes binary data as a WebSocket BINARY frame.
     * @param data Data to send.
     * @return Encoded frame bytes.
     */
    public static byte[] encodeBinary(byte[] data) {
        return encode(Opcode.BINARY, data);
    }

    /**
     * Encodes a PONG frame with the payload of the corresponding PING.
     * @param payload Payload of the received PING.
     * @return PONG frame bytes.
     */
    public static byte[] encodePong(byte[] payload) {
        return encode(Opcode.PONG, payload);
    }

    /**
     * Encodes a CLOSE frame to close the connection normally.
     * @return CLOSE frame bytes.
     */
    public static byte[] encodeClose() {
        return encode(Opcode.CLOSE, new byte[0]);
    }

    private static byte[] encode(Opcode opcode, byte[] payload) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            out.write(0x80 | opcode.code);  // FIN=1 + opcode

            int len = payload.length;
            if (len <= 125) {
                out.write(len);             // MASK=0 + length
            } else if (len <= 65535) {
                out.write(126);
                out.write((len >> 8) & 0xFF);
                out.write(len & 0xFF);
            } else {
                out.write(127);
                out.write(ByteBuffer.allocate(8).putLong(len).array());
            }

            out.write(payload);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error encoding WebSocket frame", e);
        }
    }

    // ── Handshake ─────────────────────────────────────────────────────────

    /**
     * Computes the Sec-WebSocket-Accept value from the client's Sec-WebSocket-Key.
     * SHA-1(key + GUID) encoded in Base64.
     * @param clientKey Value of the Sec-WebSocket-Key header.
     * @return Value for the Sec-WebSocket-Accept header.
     */
    public static String computeAcceptKey(String clientKey) {
        try {
            String raw = clientKey + WS_GUID;
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hash = sha1.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 not available", e);
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────

    public Opcode getOpcode()  { return opcode; }
    public byte[] getPayload() { return payload; }
    public boolean isFin()     { return fin; }

    /**
     * Returns the decoded payload as UTF-8 text.
     * Only valid for TEXT frames.
     */
    public String getText() {
        return new String(payload, StandardCharsets.UTF_8);
    }
}
