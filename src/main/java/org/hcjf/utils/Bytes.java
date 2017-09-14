package org.hcjf.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author javaito
 */
public class Bytes {

    /**
     * Returns all the index into the array where start the key.
     * @param value Array to find the indexes.
     * @param key Array that is the key to find the index.
     * @param startIndex Index to start the search.
     * @param resultSize Max size of the result, when the algorithm found this amount of indexes break the execution.
     * @return List with all the indexes.
     */
    public static List<Integer> allIndexOf(byte[] value, byte[] key, int startIndex, int resultSize) {
        if(startIndex < 0) {
            throw new IllegalArgumentException("The start index can not be smaller than 1");
        }

        if(startIndex > value.length) {
            throw new IllegalArgumentException("The start index can not be bigger than the value");
        }

        if(key.length > value.length) {
            throw new IllegalArgumentException("The array can not contain something larger than itself");
        }

        List<Integer> result = new ArrayList<>();
        boolean accumulate = false;
        int keyIndex = 0;
        int startKey = 0;
        for (int i = 0; i < value.length; i++) {
            if(!accumulate) {
                if(value[i] == key[keyIndex]) {
                    accumulate = true;
                    startKey = i;
                    keyIndex++;
                }
            } else {
                if(value[i] == key[keyIndex]) {
                    keyIndex++;
                    if(keyIndex == key.length) {
                        result.add(startKey);
                        if(result.size() == resultSize) {
                            break;
                        }
                        startKey = 0;
                        accumulate = false;
                        keyIndex = 0;
                    }
                } else {
                    startKey = 0;
                    accumulate = false;
                    keyIndex = 0;
                }
            }
        }

        return result;
    }

    /**
     * Returns all the index into the array where start the key.
     * @param value Array to find the indexes.
     * @param key Array that is the key to find the index.
     * @return List with all the indexes.
     */
    public static List<Integer> allIndexOf(byte[] value, byte[] key) {
        return allIndexOf(value, key, 0, Integer.MAX_VALUE);
    }

    /**
     * Returns the first index into the array starting in the first element of the source array.
     * @param value Source array.
     * @param key Search key.
     * @return First index into the source array.
     */
    public static int indexOf(byte[] value, byte[] key) {
        List<Integer> indexes = allIndexOf(value, key, 0, 1);
        int result = -1;
        if(indexes.size() > 0) {
            result = indexes.get(0);
        }
        return result;
    }

    /**
     * Returns the first index into the array starting in the element indicated by the 'startIndex'
     * parameter of the source array.
     * @param value Source array.
     * @param key Search key.
     * @param startIndex Element into the source array to start the search.
     * @return First index into the source array.
     */
    public static int indexOf(byte[] value, byte[] key, int startIndex) {
        List<Integer> indexes = allIndexOf(value, key, startIndex, 1);
        int result = -1;
        if(indexes.size() > 0) {
            result = indexes.get(0);
        }
        return result;
    }

    /**
     * Splits the byte array using the split key, and returns a list with
     * the sub arrays products of the split.
     * @param value Byte array to split.
     * @param splitKey Byte array with the pattern to split.
     * @return List with all the sub arrays.
     */
    public static List<byte[]> split(byte[] value, byte[] splitKey) {
        List<byte[]> result = new ArrayList<>();

        List<Integer> indexes = allIndexOf(value, splitKey, 0, Integer.MAX_VALUE);
        byte[] body;
        Integer startIndex = 0;
        for(Integer index : indexes){
            body = new byte[index - startIndex];
            if(body.length > 0) {
                System.arraycopy(value, startIndex, body, 0, body.length);
            }
            result.add(body);
            startIndex = index + splitKey.length;
        }

        body = new byte[value.length - startIndex];
        System.arraycopy(value, startIndex, body, 0, body.length);
        result.add(body);

        return result;
    }
}
