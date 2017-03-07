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
    public static final String START_SUB_GROUP = "[";
    public static final String END_SUB_GROUP = "]";
    public static final String REPLACEABLE_GROUP = "$";
    public static final String EMPTY_STRING = "";
    public static final String WHITE_SPACE = " ";
    public static final String CLASS_SEPARATOR = ".";
    public static final String CASE_INSENSITIVE_REGEX_FLAG = "(?i)";

    /**
     * Replace the first character for his upper case representation.
     * @param value Value to replace.
     * @return Replaced value.
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
     * Replace the first character for his lower case representation.
     * @param value Value to replace.
     * @return Replaced value.
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
     * Complete the left side of the value with n instance of the default padding value (' ').
     * @param value Value to be completed.
     * @param paddingSize Number of instance to pad the value.
     * @return New string with the left side padding.
     */
    public static String leftPad(String value, int paddingSize) {
        return String.format("%1$" + paddingSize + "s", value);
    }

    /**
     * Complete the left side of the value with n instance of the padding value.
     * @param value Value to be completed.
     * @param paddingValue Padding value.
     * @param paddingSize Number of instance to pad the value.
     * @return New string with the left side padding.
     */
    public static String leftPad(String value, String paddingValue, int paddingSize) {
        return leftPad(value, paddingSize).replace(DEFAULT_PADDING_VALUE, paddingValue);
    }

    /**
     * Complete the right side of the value with n instance of the default padding value (' ').
     * @param value Value to be completed.
     * @param paddingSize Number of instance to pad the value.
     * @return New string with the right side padding.
     */
    public static String rightPad(String value, int paddingSize) {
        return String.format("%1$-" + paddingSize + "s", value);
    }

    /**
     * Complete the right side of the value with n instance of the padding value.
     * @param value Value to be completed.
     * @param paddingValue Padding value.
     * @param paddingSize Number of instance to pad the value.
     * @return New string with the right side padding.
     */
    public static String rightPad(String value, String paddingValue, int paddingSize) {
        return rightPad(value, paddingSize).replace(DEFAULT_PADDING_VALUE, paddingValue);
    }

    /**
     * Return all the index into the string value where found the specific founded value.
     * @param value Value to found the index.
     * @param foundedValue Founded value into the string.
     * @return Return a list with the indexes of the places where found value.
     */
    public static Set<Integer> allIndexOf(String value, String foundedValue) {
        return allIndexOf(value, foundedValue, false);
    }

    /**
     * Return all the index into the string value where found the specific founded value.
     * @param value Value to found the index.
     * @param foundedValue Founded value into the string.
     * @param desc If this parameter is true then the first index is the smaller else if the parameter
     * s false then the first index is bigger.
     * @return Return a list with the indexes of the places where found value.
     */
    public static Set<Integer> allIndexOf(String value, String foundedValue, boolean desc) {
        TreeSet<Integer> result = new TreeSet<>((o1, o2) -> (o1 - o2) * (desc ? 1 : -1));

        int index = value.indexOf(foundedValue);
        while(index >= 0) {
            result.add(index);
            index = value.indexOf(foundedValue, index + 1);
        }

        return result;
    }

    /**
     * Return the list with all the groups and sub groups of the value.
     * A group is the char sequence between the start group character '('
     * and the end group character ')'
     * @param value
     * @return
     */
    public static List<String> group(String value) {
        Set<Integer> startIndexes = allIndexOf(value, START_GROUP);
        Set<Integer> endIndexes = allIndexOf(value, END_GROUP);

        if(startIndexes.size() != endIndexes.size()) {
            throw new IllegalArgumentException("");
        }

        return group(value, startIndexes, endIndexes);
    }

    /**
     * Return the list of groups into the value, using the ordered sets of
     * start indexes and end indexes.
     * @param value Value to group.
     * @param startIndexes Set with all the start indexes.
     * @param endIndexes Set with all the end indexes.
     * @return List with all the groups.
     */
    private static List<String> group(String value, Set<Integer> startIndexes, Set<Integer> endIndexes) {
        List<String> result = new ArrayList<>();
        Integer start = null;
        Integer end = null;
        Integer candidate = null;
        Iterator<Integer> startIterator = startIndexes.iterator();
        while(!startIndexes.isEmpty()) {
            start = startIndexes.iterator().next();
            Iterator<Integer> endIterator = endIndexes.iterator();
            end = Integer.MAX_VALUE;
            while(endIterator.hasNext()) {
                candidate = endIterator.next();
                if(start < candidate && candidate < end) {
                    end = candidate;
                }
            }

            if(!endIndexes.remove(end)) {
                throw new IllegalArgumentException("");
            }
            if(!startIndexes.remove(start)) {
                throw new IllegalArgumentException("");
            }

            result.add(value.substring(start + 1, end));
        }

        return result;
    }

    /**
     * Return a list with all groups and sub groups in ascendant order with replacement
     * places that refer some index into the same list.
     * e.g. "Hello (world)" -> ["world", "Hello $0"]
     * @param value String to group.
     * @return List with groups.
     */
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

    /**
     * Return the first replaceable index founded.
     * @param value Value to analyse
     * @return Replaceable index.
     */
    public static String getGroupIndex(String value) {
        String result = null;
        Integer startIndex = value.indexOf(REPLACEABLE_GROUP);
        StringBuilder resultBuilder = new StringBuilder();
        char current;
        for (int i = startIndex; i < value.length(); i++) {
            current = value.charAt(i);
            if(Character.isDigit(current) || current == Strings.REPLACEABLE_GROUP.charAt(0)) {
                resultBuilder.append(current);
            } else {
                break;
            }
        }
        result = resultBuilder.toString();
        return result;
    }

    /**
     * Return a hexadecimal representation of the byte array.
     * @param bytes Byte array to represents.
     * @return Hexadecimal representation.
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for(byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * This method creates a uuid from a hash code of the string object.
     * @param value String to create a uuid. The hash code of this
     * value is used to create the least significant bits of the uuid and the
     * most significant bits are 0.
     * @return UUID instance generated using the nex constructor new UUID(0, value.hashCode());
     */
    public static UUID createUUIDFromStringHash(String value) {
        return createUUIDFromStringHash(null, value);
    }

    /**
     * This method creates a uuid from a hash code of the string object.
     * @param value1 First string to create a uuid. The hash code of this
     * value is used to create the most significant bits of the uuid. This value con be null.
     * @param value2 Second string to create a uuid. The hash code of this
     * value is used to create the least significant bits of the uuid
     * @return UUID instance generated using the nex constructor new UUID(0, value.hashCode());
     */
    public static UUID createUUIDFromStringHash(String value1, String value2) {
        return new UUID(value1 == null ? 0 : value1.hashCode(), value2.hashCode());
    }
}
