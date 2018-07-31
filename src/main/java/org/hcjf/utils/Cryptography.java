package org.hcjf.utils;

import org.hcjf.properties.SystemProperties;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

/**
 * @author Andres Medina
 */
public class Cryptography {

    private AlgorithmParameterSpec spec;
    private Cipher cipher;
    private byte[] iv;
    private byte[] aadData;
    private SecureRandom secureRandom;
    private String operationMode;
    private final static String TR_PATTERN = "%s/%s/%s";
    private SecretKey secretKey;
    private int tagBigLength;

    public Cryptography() {
        this(SystemProperties.get(SystemProperties.Cryptography.KEY),
                SystemProperties.getInteger(SystemProperties.Cryptography.Random.IV_SIZE),
                SystemProperties.get(SystemProperties.Cryptography.AAD),
                SystemProperties.get(SystemProperties.Cryptography.ALGORITHM),
                SystemProperties.get(SystemProperties.Cryptography.OPERATION_MODE),
                SystemProperties.get(SystemProperties.Cryptography.PADDING_SCHEME),
                SystemProperties.getInteger(SystemProperties.Cryptography.GCM.TAG_BIT_LENGTH));
    }

    public Cryptography(String key, int ivSize,String aad, String algorithm, String operationMode, String paddingScheme, int tagBigLength) {

        iv = new byte[ivSize];
        aadData = aad.getBytes();
        secureRandom = new SecureRandom();
        this.tagBigLength = tagBigLength;
        this.secretKey = new SecretKeySpec(Strings.hexToBytes(key), "AES");

        String transformation = String.format(TR_PATTERN,algorithm,operationMode,paddingScheme);
        this.operationMode = operationMode;

        try {
            cipher = Cipher.getInstance(transformation);
        } catch(NoSuchAlgorithmException noSuchAlgoExc) {
            System.out.println("Exception while encrypting. Algorithm being requested is not available in this environment " + noSuchAlgoExc);
        } catch(NoSuchPaddingException noSuchPaddingExc) {
            System.out.println("Exception while encrypting. Padding Scheme being requested is not available this environment " + noSuchPaddingExc);
        }

    }

    public byte[] encrypt(byte[] message) {
        secureRandom.nextBytes(iv);
        initParameterSpec();
        byte[] encryptedMessage = this.convert(Cipher.ENCRYPT_MODE, message);
        byte[] result = new byte[encryptedMessage.length + iv.length];
        System.arraycopy(iv,0,result,0,iv.length);
        System.arraycopy(encryptedMessage,0,result,iv.length,encryptedMessage.length);
        return result;
    }

    public byte[] decrypt(byte[] message) {
        byte[] messageFragment = new byte[message.length - iv.length];
        System.arraycopy(message,0,iv,0,iv.length);
        System.arraycopy(message,iv.length,messageFragment,0,messageFragment.length);
        initParameterSpec();
        return this.convert(Cipher.DECRYPT_MODE, messageFragment);
    }

    private void initParameterSpec() {
        if(operationMode.equals("GCM")) {
            this.spec = new GCMParameterSpec(tagBigLength, iv);
        }
    }

    private byte[] convert(int encryptMode, byte[] message) {

        byte[] result = null;

        try {
            cipher.init(encryptMode, secretKey, spec, new SecureRandom());
        } catch(InvalidKeyException invalidKeyExc) {
            System.out.println("Exception while encrypting. Key being used is not valid. It could be due to invalid encoding, wrong length or uninitialized " + invalidKeyExc);
        } catch(InvalidAlgorithmParameterException invalidAlgoParamExc) {
            System.out.println("Exception while encrypting. Algorithm parameters being specified are not valid " + invalidAlgoParamExc);
        }

        try {
            cipher.updateAAD(aadData); // add AAD tag data before encrypting
        } catch(IllegalArgumentException illegalArgumentExc) {
            System.out.println("Exception thrown while encrypting. Byte array might be null " + illegalArgumentExc );
        } catch(IllegalStateException illegalStateExc) {
            System.out.println("Exception thrown while encrypting. CIpher is in an illegal state " + illegalStateExc);
        } catch(UnsupportedOperationException unsupportedExc) {
            System.out.println("Exception thrown while encrypting. Provider might not be supporting this method " + unsupportedExc);
        }

        try {
            result = cipher.doFinal(message) ;
        } catch(IllegalBlockSizeException illegalBlockSizeExc) {
            System.out.println("Exception while encrypting, due to block size " + illegalBlockSizeExc) ;
        } catch(BadPaddingException badPaddingExc) {
            System.out.println("Exception while encrypting, due to padding scheme " + badPaddingExc) ;
        }

        return result;
    }

    public void setKey(byte[] key) {
        this.secretKey = new SecretKeySpec(key, "AES");
    }
}
