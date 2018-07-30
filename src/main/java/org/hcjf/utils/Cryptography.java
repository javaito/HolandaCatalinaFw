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
    private String algorithm;
    private final static String TR_PATTERN = "%s/%s/%s";
    private SecretKey secretKey;

    public Cryptography() {
        this(SystemProperties.getInteger(SystemProperties.Cryptography.Random.IV_SIZE),
                SystemProperties.get(SystemProperties.Cryptography.AAD),
                SystemProperties.get(SystemProperties.Cryptography.ALGORITHM),
                SystemProperties.get(SystemProperties.Cryptography.OPERATION_MODE),
                SystemProperties.get(SystemProperties.Cryptography.PADDING_SCHEME),
                SystemProperties.getInteger(SystemProperties.Cryptography.GCM.TAG_BIT_LENGTH));
    }

    public Cryptography(int ivsize,String aad, String algorithm, String operationMode, String paddingScheme, int tagBigLength) {

        //Initializing IV
        iv = new byte[ivsize];
        aadData = aad.getBytes();
        secureRandom = new SecureRandom() ;
        secureRandom.nextBytes(iv); // SecureRandom initialized using self-seeding


        String transformation = String.format(TR_PATTERN,algorithm,operationMode,paddingScheme);
        this.algorithm = algorithm;

        try {
            cipher = Cipher.getInstance(transformation);
        } catch(NoSuchAlgorithmException noSuchAlgoExc) {
            System.out.println("Exception while encrypting. Algorithm being requested is not available in this environment " + noSuchAlgoExc);
        } catch(NoSuchPaddingException noSuchPaddingExc) {
            System.out.println("Exception while encrypting. Padding Scheme being requested is not available this environment " + noSuchPaddingExc);
        }

        if(operationMode.equals("GCM")) {
            // Initialize GCM Parameters
            spec = new GCMParameterSpec(tagBigLength, iv);
        }

    }

    public byte[] encrypt(byte[] message) {
        return this.convert(Cipher.ENCRYPT_MODE, message);
    }

    public byte[] decrypt(byte[] message) {
        return this.convert(Cipher.DECRYPT_MODE, message);
    }

    private byte[] convert(int encryptMode, byte[] message) {


        byte[] result = new byte[0];
        try {

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(encryptMode, secretKey);


            result =  cipher.doFinal(message);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return result;


        /*

        byte[] result = null;
        SecretKey secretKey = new SecretKeySpec(key, 0, key.length, this.algorithm);

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

        //secureRandom.nextBytes(iv);

        return result;*/
    }

    public void setKey(byte[] key) {
        this.secretKey = new SecretKeySpec(key, "AES");
    }
}
