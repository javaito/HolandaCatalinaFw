package org.hcjf.utils;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author Andres Medina
 */
public class CryptographyTestSuite {

    private static final String message = "Las máquinas me sorprenden con mucha frecuencia";
    private static final String encryptedMessage = "6fa42b468b601a015ab71c2413ae533525a71d5be0418335ea8b14545aed84aed2b7db05ba9ece60cc8bc33a2fd42ef59db662a8d03dc9c5c58c512b5bf79e7d";
    private static final String key = "71324dccdb58966a04507b0fe2008632940b87c6dc5cea5f4bdf0d0089524c8e";
    byte[] test;


    @Test
    public void encrypt() {
        Cryptography crypt = new Cryptography(96,"HolandaCatalina","AES","GCM","PKCS5Padding",128);
        crypt.setKey(Strings.hexToBytes(key));
        test = crypt.encrypt(message.getBytes());
        System.out.println(Strings.bytesToHex(test));

        String messageResult = new String(crypt.decrypt(test));
        Assert.assertEquals(message,messageResult);
    }

    @Test
    public void decrypt() {
        Cryptography crypt = new Cryptography(96,"HolandaCatalina","AES","GCM","PKCS5Padding",128);
        crypt.setKey(Strings.hexToBytes(key));
        String messageResult = new String(crypt.decrypt(Strings.hexToBytes(encryptedMessage)));
        Assert.assertEquals(message,messageResult);
    }
}
