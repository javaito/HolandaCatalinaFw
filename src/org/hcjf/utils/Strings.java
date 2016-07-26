package org.hcjf.utils;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public final class Strings {

    /**
     *
     * @param value
     * @return
     */
    public static String capitalize(String value) {
        String result = value;
        if(value != null && !value.trim().isEmpty()) {
            char[] chars = value.toCharArray();
            chars[0] = Character.toUpperCase(chars[0]);
            result = new String(chars);
        }
        return result;
    }

    /**
     *
     * @param value
     * @return
     */
    public static String uncapitalize(String value) {
        String result = value;
        if(value != null && !value.trim().isEmpty()) {
            char[] chars = value.toCharArray();
            chars[0] = Character.toLowerCase(chars[0]);
            result = new String(chars);
        }
        return result;
    }
}
