package org.hcjf.utils;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author Andres Medina
 */
public class CryptographyTestSuite {

    private static final String message = "Las máquinas me sorprenden con mucha frecuencia";
    private static final String encryptedMessage = "6b44b12578cf682eca96bb74f9fcef699cc6e37cdddcdff48e64ff8c511e04521ba3fdb5f009c57023319cb50b3d5938564bdb3eda58d59da2dc4c227b9d7bd7";
    byte[] test;

    @Test
    public void encrypt() {
        Cryptography crypt = new Cryptography();//(key,"RandomIVTestService","HolandaCatalinaCrypt","AES","GCM","PKCS5Padding",128);
        test = crypt.encrypt(message.getBytes());
        System.out.println(Strings.bytesToHex(test));
    }

    @Test
    public void decrypt() {
        Cryptography crypt = new Cryptography();//(key,"RandomIVTestService","HolandaCatalinaCrypt","AES","GCM","PKCS5Padding",128);
        String messageResult = new String(crypt.decrypt(Strings.hexToBytes(encryptedMessage)));
        Assert.assertEquals(message,messageResult);
    }
}
