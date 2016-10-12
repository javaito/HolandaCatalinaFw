package org.hcjf.utils;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public final class Strings {

    private static final String DEFAULT_PADDING_VALUE = " ";

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

    public static String leftPad(String value, int paddingSize) {
        return String.format("%1$" + paddingSize + "s", value);
    }

    public static String leftPad(String value, String paddingValue, int paddingSize) {
        return leftPad(value, paddingSize).replace(DEFAULT_PADDING_VALUE, paddingValue);
    }

    public static String rightPad(String value, int paddingSize) {
        return String.format("%1$-" + paddingSize + "s", value);
    }

    public static String rightPad(String value, String paddingValue, int paddingSize) {
        return rightPad(value, paddingSize).replace(DEFAULT_PADDING_VALUE, paddingValue);
    }
}
