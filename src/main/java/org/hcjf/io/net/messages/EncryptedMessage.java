package org.hcjf.io.net.messages;

/**
 * This kind of message are only a wrapper that contains the original message encrypted.
 * @author javaito
 */
public class EncryptedMessage extends Message {

    private byte[] encrypedData;

    /**
     * Returns the encrypted data.
     * @return Encrypted data.
     */
    public byte[] getEncrypedData() {
        return encrypedData;
    }

    /**
     * Set the encrypted data.
     * @param encrypedData Encrypted data.
     */
    public void setEncrypedData(byte[] encrypedData) {
        this.encrypedData = encrypedData;
    }
}
