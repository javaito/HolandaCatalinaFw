package org.hcjf.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;


/**
 * @author Andres Medina
 */
public class CryptographyTestSuite {

    private static final String message = "Las máquinas me sorprenden con mucha frecuencia";
    private static final String encryptedMessage = "b72159963a9a6a877f276a6fd08dfd920e1a532d3570b853a6103d0db8f28c100001fd776a28e9419eab7050c9cc2a56a9134f48fff2969643627cdc0a6f36c49826f27fb9834ddff8445346ca6b57c601edf583db62a1369864dfd539f81b0f46f672282ac36df0efde06845b6adef64393fb517009784fa2ca493e88b16feb713e9d24bca4ef163c64f3b82cc4f48e9a95b3c4f8d030a0448fa7ac8f8c99d3";
    byte[] test;

    @Test(timeout = 20000)
    public void encrypt() {
        Cryptography crypt = new Cryptography();//(key,"RandomIVTestService","HolandaCatalinaCrypt","AES","GCM","PKCS5Padding",128);
        for (int i = 0; i < 1000000; i++) {
            crypt.encrypt((message + i).getBytes());
        }
    }

    @Test
    public void decrypt() {
        Cryptography crypt = new Cryptography();//(key,"RandomIVTestService","HolandaCatalinaCrypt","AES","GCM","PKCS5Padding",128);
        String messageResult = new String(crypt.decrypt(Strings.hexToBytes(encryptedMessage)));
        Assert.assertEquals(message,messageResult);
    }

}
