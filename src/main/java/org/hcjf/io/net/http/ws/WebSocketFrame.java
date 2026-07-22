package org.hcjf.io.net.http.ws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Codec de frames WebSocket según RFC 6455.
 *
 * Formato de frame (simplificado):
 *   Byte 0: FIN(1) | RSV(3) | Opcode(4)
 *   Byte 1: MASK(1) | PayloadLen(7)
 *   [2 bytes si len=126] [8 bytes si len=127]
 *   [4 bytes máscara si MASK=1]
 *   Payload (desenmascarado si es cliente→servidor)
 *
 * @author javaito
 */
public final class WebSocketFrame {

    private static final String WS_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    /**
     * Opcodes definidos por RFC 6455.
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

    // ── Decodificación ────────────────────────────────────────────────────

    /**
     * Decodifica un frame recibido del cliente.
     * Los frames cliente→servidor siempre vienen enmascarados (RFC 6455 §5.3).
     * @param data Bytes crudos del frame.
     * @return Frame decodificado.
     */
    public static WebSocketFrame decode(byte[] data) {
        if (data == null || data.length < 2) {
            throw new IllegalArgumentException("WebSocket frame demasiado corto");
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

    // ── Encoding estático (servidor→cliente, sin máscara) ─────────────────

    /**
     * Codifica un mensaje de texto como frame WebSocket TEXT.
     * @param message Texto a enviar.
     * @return Bytes del frame codificado.
     */
    public static byte[] encodeText(String message) {
        return encode(Opcode.TEXT, message.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Codifica datos binarios como frame WebSocket BINARY.
     * @param data Datos a enviar.
     * @return Bytes del frame codificado.
     */
    public static byte[] encodeBinary(byte[] data) {
        return encode(Opcode.BINARY, data);
    }

    /**
     * Codifica un frame PONG con el payload del PING correspondiente.
     * @param payload Payload del PING recibido.
     * @return Bytes del frame PONG.
     */
    public static byte[] encodePong(byte[] payload) {
        return encode(Opcode.PONG, payload);
    }

    /**
     * Codifica un frame CLOSE para cerrar la conexión normalmente.
     * @return Bytes del frame CLOSE.
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
            throw new RuntimeException("Error codificando frame WebSocket", e);
        }
    }

    // ── Handshake ─────────────────────────────────────────────────────────

    /**
     * Calcula el valor de Sec-WebSocket-Accept a partir del Sec-WebSocket-Key del cliente.
     * SHA-1(key + GUID) codificado en Base64.
     * @param clientKey Valor del header Sec-WebSocket-Key.
     * @return Valor para el header Sec-WebSocket-Accept.
     */
    public static String computeAcceptKey(String clientKey) {
        try {
            String raw = clientKey + WS_GUID;
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hash = sha1.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 no disponible", e);
        }
    }

    // ── Accesores ─────────────────────────────────────────────────────────

    public Opcode getOpcode()  { return opcode; }
    public byte[] getPayload() { return payload; }
    public boolean isFin()     { return fin; }

    /**
     * Retorna el payload decodificado como texto UTF-8.
     * Solo válido para frames TEXT.
     */
    public String getText() {
        return new String(payload, StandardCharsets.UTF_8);
    }
}
