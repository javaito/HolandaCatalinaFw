package org.hcjf.utils;

import java.util.*;

/**
 * This class contains utils methods to work with strings.
 * @author javaito
 * @mail javaito@gmail.com
 */
public final class Strings {

    public static final String DEFAULT_PADDING_VALUE = " ";
    public static final String START_GROUP = "(";
    public static final String END_GROUP = ")";
    public static final String REPLACEABLE_GROUP = "$";
    public static final String EMPTY_STRING = "";
    public static final String WHITE_SPACE = " ";

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

    public static final List<String> replaceableGroup(String value) {
        List<String> groups = Strings.group(value);
        List<Integer> withSubGroups = new ArrayList<>();
        Iterator<Integer> iterator;
        String replacedValue = value;
        Integer groupIndex;
        String group;
        String groupCopy;
        for (int j = groups.size() -1; j >= 0; j--) {
            group = groups.get(j);
            replacedValue = replacedValue.replace(START_GROUP+group+END_GROUP, REPLACEABLE_GROUP+j);
            iterator = withSubGroups.iterator();
            while(iterator.hasNext()) {
                groupIndex = iterator.next();
                groupCopy = groups.remove(groupIndex.intValue()).replace(START_GROUP+group+END_GROUP, REPLACEABLE_GROUP+j);
                groups.add(groupIndex, groupCopy);
                if(!groupCopy.contains(Strings.END_GROUP)) {
                    iterator.remove();
                }
            }

            if(group.contains(Strings.END_GROUP)) {
                withSubGroups.add(j);
            }
        }
        groups.add(replacedValue);
        return groups;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for(byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
