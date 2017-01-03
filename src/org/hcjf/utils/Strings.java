package org.hcjf.utils;

import java.util.*;

/**
 * This class contains utils methods to work with strings.
 * @author javaito
 * @mail javaito@gmail.com
 */
public final class Strings {

    private static final String DEFAULT_PADDING_VALUE = " ";
    private static final String START_GROUP = "(";
    private static final String END_GROUP = ")";

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

    /**
     *
     * @param value
     * @param paddingSize
     * @return
     */
    public static String leftPad(String value, int paddingSize) {
        return String.format("%1$" + paddingSize + "s", value);
    }

    /**
     *
     * @param value
     * @param paddingValue
     * @param paddingSize
     * @return
     */
    public static String leftPad(String value, String paddingValue, int paddingSize) {
        return leftPad(value, paddingSize).replace(DEFAULT_PADDING_VALUE, paddingValue);
    }

    /**
     *
     * @param value
     * @param paddingSize
     * @return
     */
    public static String rightPad(String value, int paddingSize) {
        return String.format("%1$-" + paddingSize + "s", value);
    }

    /**
     *
     * @param value
     * @param paddingValue
     * @param paddingSize
     * @return
     */
    public static String rightPad(String value, String paddingValue, int paddingSize) {
        return rightPad(value, paddingSize).replace(DEFAULT_PADDING_VALUE, paddingValue);
    }

    /**
     *
     * @param value
     * @param foundedValue
     * @return
     */
    public static Set<Integer> allIndexOf(String value, String foundedValue) {
        return allIndexOf(value, foundedValue, false);
    }

    /**
     *
     * @param value
     * @param foundedValue
     * @param desc
     * @return
     */
    public static Set<Integer> allIndexOf(String value, String foundedValue, boolean desc) {
        TreeSet<Integer> result = new TreeSet<>((o1, o2) -> (01 - 02) * (desc ? 1 : -1));

        int index = value.indexOf(foundedValue);
        while(index >= 0) {
            result.add(index);
            index = value.indexOf(foundedValue, index + 1);
        }

        return result;
    }

    public static List<String> group(String value) {
        Set<Integer> startIndexes = allIndexOf(value, START_GROUP, true);
        Set<Integer> endIndexes = allIndexOf(value, END_GROUP);

        if(startIndexes.size() != endIndexes.size()) {
            throw new IllegalArgumentException("");
        }

        return group(value, new ArrayList<>(), startIndexes, endIndexes);
    }

    private static List<String> group(String value, List<String> groups, Set<Integer> startIndexes, Set<Integer> endIndexes) {
        List<String> result = new ArrayList<>();
        Integer start = null;
        Integer end = null;
        Iterator<Integer> startIterator = startIndexes.iterator();
        while(startIterator.hasNext()) {
            start = startIterator.next();
            Iterator<Integer> endIterator = endIndexes.iterator();
            while(endIterator.hasNext()) {
                end = endIterator.next();
                if(start < end) {
                    endIterator.remove();
                    startIterator.remove();
                    break;
                }
                end = null;
            }
            if(end == null) {
                throw new IllegalArgumentException("");
            }

            result.add(value.substring(start + 1, end));
        }

        return result;
    }

    public static void main(String[] args) {
        String s = "SELECT * FROM holder WHERE holderid IN (bal or (a & b)) AND (SELECT (hola) asdljfh (chau))";
        System.out.println(allIndexOf(s, "(", true));
        System.out.println(allIndexOf(s, ")"));

        System.out.println(group(s));
    }
}
