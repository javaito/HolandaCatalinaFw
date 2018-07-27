package org.hcjf.utils;

import java.util.*;
import java.util.stream.IntStream;

/**
 * This class contains utils methods to work with strings.
 * @author javaito
 *
 */
public final class Strings {

    public static final class StandardOutput {

        public static final String RESET = "\033[0m";
        public static final String CLEAN_LINES = "\033[1J";
        public static final String CURSOR_TO_HOME = "\033[H";

        // Regular Colors
        public static final String BLACK = "\033[0;30m";   // BLACK
        public static final String RED = "\033[0;31m";     // RED
        public static final String GREEN = "\033[0;32m";   // GREEN
        public static final String YELLOW = "\033[0;33m";  // YELLOW
        public static final String BLUE = "\033[0;34m";    // BLUE
        public static final String PURPLE = "\033[0;35m";  // PURPLE
        public static final String CYAN = "\033[0;36m";    // CYAN
        public static final String WHITE = "\033[0;37m";   // WHITE

        // Bold
        public static final String BLACK_BOLD = "\033[1;30m";  // BLACK
        public static final String RED_BOLD = "\033[1;31m";    // RED
        public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
        public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
        public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
        public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
        public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
        public static final String WHITE_BOLD = "\033[1;37m";  // WHITE

        // Underline
        public static final String BLACK_UNDERLINED = "\033[4;30m";  // BLACK
        public static final String RED_UNDERLINED = "\033[4;31m";    // RED
        public static final String GREEN_UNDERLINED = "\033[4;32m";  // GREEN
        public static final String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
        public static final String BLUE_UNDERLINED = "\033[4;34m";   // BLUE
        public static final String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
        public static final String CYAN_UNDERLINED = "\033[4;36m";   // CYAN
        public static final String WHITE_UNDERLINED = "\033[4;37m";  // WHITE

        // Background
        public static final String BLACK_BACKGROUND = "\033[40m";  // BLACK
        public static final String RED_BACKGROUND = "\033[41m";    // RED
        public static final String GREEN_BACKGROUND = "\033[42m";  // GREEN
        public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
        public static final String BLUE_BACKGROUND = "\033[44m";   // BLUE
        public static final String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
        public static final String CYAN_BACKGROUND = "\033[46m";   // CYAN
        public static final String WHITE_BACKGROUND = "\033[47m";  // WHITE

        // High Intensity
        public static final String BLACK_BRIGHT = "\033[0;90m";  // BLACK
        public static final String RED_BRIGHT = "\033[0;91m";    // RED
        public static final String GREEN_BRIGHT = "\033[0;92m";  // GREEN
        public static final String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
        public static final String BLUE_BRIGHT = "\033[0;94m";   // BLUE
        public static final String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
        public static final String CYAN_BRIGHT = "\033[0;96m";   // CYAN
        public static final String WHITE_BRIGHT = "\033[0;97m";  // WHITE

        // Bold High Intensity
        public static final String BLACK_BOLD_BRIGHT = "\033[1;90m"; // BLACK
        public static final String RED_BOLD_BRIGHT = "\033[1;91m";   // RED
        public static final String GREEN_BOLD_BRIGHT = "\033[1;92m"; // GREEN
        public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m";// YELLOW
        public static final String BLUE_BOLD_BRIGHT = "\033[1;94m";  // BLUE
        public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m";// PURPLE
        public static final String CYAN_BOLD_BRIGHT = "\033[1;96m";  // CYAN
        public static final String WHITE_BOLD_BRIGHT = "\033[1;97m"; // WHITE

        // High Intensity backgrounds
        public static final String BLACK_BACKGROUND_BRIGHT = "\033[0;100m";// BLACK
        public static final String RED_BACKGROUND_BRIGHT = "\033[0;101m";// RED
        public static final String GREEN_BACKGROUND_BRIGHT = "\033[0;102m";// GREEN
        public static final String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m";// YELLOW
        public static final String BLUE_BACKGROUND_BRIGHT = "\033[0;104m";// BLUE
        public static final String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m"; // PURPLE
        public static final String CYAN_BACKGROUND_BRIGHT = "\033[0;106m";  // CYAN
        public static final String WHITE_BACKGROUND_BRIGHT = "\033[0;107m";   // WHITE
    }

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
    public static final String ARGUMENT_SEPARATOR = ",";
    public static final String ARGUMENT_SEPARATOR_2 = ";";
    public static final String ASSIGNATION = "=";
    public static final String REPLACEABLE_RICH_TEXT = "&";
    public static final String RICH_TEXT_SEPARATOR = "'";
    public static final String RICH_TEXT_SKIP_CHARACTER = "\\";
    public static final String CARRIAGE_RETURN_AND_LINE_SEPARATOR = "\r\n";
    public static final String CARRIAGE_RETURN = "\r";
    public static final String LINE_SEPARATOR = "\n";
    public static final String TAB = "\t";

    public static final String SPLIT_BY_LENGTH_REGEX = "(?<=\\G.{%d})";

    /**
     * This method replace the combination of character \r\n and the character \n for
     * white space character.
     * @param value String to remove the lines.
     * @return String value without lines.
     */
    public static String removeLines(String value) {
        return value.replace(CARRIAGE_RETURN_AND_LINE_SEPARATOR, WHITE_SPACE).replace(LINE_SEPARATOR, WHITE_SPACE);
    }

    /**
     * Return the string that result of join all the values separated by the
     * separated value specified.
     * @param values Values to join.
     * @param separator Separator value.
     * @return Result of the join operation.
     */
    public static String join(Collection<String> values, String separator) {
        Builder builder = new Builder();
        values.stream().filter(S -> !S.isEmpty()).forEach(S -> builder.append(S, separator));
        return builder.toString();
    }

    /**
     * Return the string that result of join all the values separated by the
     * separated value specified and wrapped by the start and and value.
     * @param values Values to join.
     * @param separator Separator value.
     * @param endValue Wrapped start value.
     * @param separator Wrapped end value.
     * @return Result of the join operation.
     */
    public static String join(Collection<String> values, String startValue, String endValue, String separator) {
        Builder builder = new Builder();
        values.stream().filter(S -> !S.isEmpty()).forEach(S -> builder.append(startValue).append(S).append(endValue, separator));
        return builder.toString();
    }

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
     * This method search all the upper case characters into the
     * value parameter and separates the value using this upper case
     * characters like limit adding between words the separator value.
     * @param value Value to split.
     * @param separator Separator value.
     * @return String with separated words.
     */
    public static String splitInWord(String value, String separator) {
        StringBuilder result = new StringBuilder();
        for(char character : value.toCharArray()) {
            if(Character.isUpperCase(character) && result.length() > 0) {
                result.append(separator);
            }
            result.append(character);
        }
        return result.toString();
    }

    /**
     * This method join the words into the value identifying each of word for the uppercase character.
     * @param value Value to join.
     * @param separator String to delimits the words.
     * @return Joined value.
     */
    public static String joinWords(String value, String separator) {
        StringBuilder result = new StringBuilder();
        String[] words = value.split(separator);
        for(String word : words) {
            result.append(capitalize(word));
        }
        return result.toString();
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
     * Returns all the rich texts contained into the value and the value (in the last place)
     * with all the replaceable places for each rich text.
     * @param value Value to get the rich texts.
     * @return List with the rich text and the original value.
     */
    public static List<String> groupRichText(String value) {
        List<String> result = new ArrayList<>();
        Set<Integer> indexes = allIndexOf(value, RICH_TEXT_SEPARATOR, true);
        Integer counter = 0;
        Integer startIndex = -1;
        Integer endIndex = 0;
        StringBuilder newValue = new StringBuilder();
        String richText;
        for (Integer index : indexes) {
            if (index == 0 || value.charAt(index - 1) != RICH_TEXT_SKIP_CHARACTER.charAt(0)) {
                if (startIndex == -1) {
                    startIndex = index;
                } else {
                    richText = value.substring(startIndex + 1, index);
                    newValue.append(value.substring(endIndex, startIndex));
                    newValue.append(RICH_TEXT_SEPARATOR).append(REPLACEABLE_RICH_TEXT).append(counter++).append(RICH_TEXT_SEPARATOR);
                    result.add(richText);
                    endIndex = index + 1;
                    startIndex = -1;
                }
            }
        }

        if(endIndex < value.length()) {
            newValue.append(value.substring(endIndex));
        }

        if(result.isEmpty()) {
            result.add(value);
        } else {
            result.add(newValue.toString());
        }
        return result;
    }

    /**
     * Return the list with all the groups and sub groups of the value.
     * A group is the char sequence between the start group character '('
     * and the end group character ')'
     * @param value Groupable value.
     * @return List with all the groups.
     */
    public static List<String> group(String value) {
        Set<Integer> startIndexes = allIndexOf(value, START_GROUP);
        Set<Integer> endIndexes = allIndexOf(value, END_GROUP);

        if(startIndexes.size() != endIndexes.size()) {
            throw new IllegalArgumentException("Expected the same amount of start and end group delimiter");
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
        Integer start;
        Integer end;
        Integer candidate;
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
     * e.g. "Hello (world)" - ["world", "Hello $0"]
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
        for (int i = startIndex; i < value.length() && i >= 0; i++) {
            current = value.charAt(i);
            if(Character.isDigit(current) || current == Strings.REPLACEABLE_GROUP.charAt(0)) {
                resultBuilder.append(current);
            } else {
                break;
            }
        }
        if(resultBuilder.length() > 0) {
            result = resultBuilder.toString();
        }
        return result;
    }

    /**
     * Reverts the grouping action over the specific value.
     * @param value Value to revert.
     * @param groups Group lists.
     * @return Reverted value.
     */
    public static String reverseGrouping(String value, List<String> groups) {
        String result = value;
        String groupIndex = Strings.getGroupIndex(result);
        Integer index;
        while(groupIndex != null) {
            index = Integer.parseInt(groupIndex.replace(Strings.REPLACEABLE_GROUP,Strings.EMPTY_STRING));
            result = result.replace(groupIndex,
                    START_GROUP + groups.get(index) + END_GROUP);
            groupIndex = Strings.getGroupIndex(result);
        }
        return result;
    }

    /**
     * Returns a hexadecimal representation of the byte array.
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
     * Returns a byte array based on the hexadecimal representation.
     * @param hex Hexadecimal representation.
     * @return Byte array.
     */
    public static byte[] hexToBytes(String hex) {
       if ((hex.length() % 2) != 0) {
            throw new IllegalArgumentException("Input string must contain an even number of characters");
        }
        int len = hex.length();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            result[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return result;
    }

    /**
     * This method splits the string value in n substring with the
     * same length except for the last substring that could be smaller than the
     * rest of the substrings.
     * @param value String value to split.
     * @param length Length of the substrings.
     * @return Substrings array.
     */
    public static String[] splitByLength(String value, int length) {
        return value.split(String.format(SPLIT_BY_LENGTH_REGEX, length));
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

    /**
     * This class is a StringBuilder wrapper that add a way to append objects
     * with a buffer to be used in the next append or discard the buffer is there are not
     * a other append operation
     */
    public static final class Builder {

        private final StringBuilder builder;
        private String[] buffer;

        public Builder() {
            this.builder = new StringBuilder();
        }

        /**
         * This method set a null value to the internal buffer.
         * @return Return this instance.
         */
        public Builder cleanBuffer() {
            buffer = null;
            return this;
        }

        /**
         * This method check if the instance buffer is not null, then
         * put the buffer into the internal builder and set null value to
         * the append buffer.
         */
        private void checkBuffer() {
            if(buffer != null) {
                for(String bufferElement : buffer) {
                    builder.append(bufferElement);
                }
                buffer = null;
            }
        }

        public Builder append(Object obj) {
            checkBuffer();
            builder.append(obj);
            return this;
        }

        public Builder append(Object obj, String... buffer) {
            checkBuffer();
            builder.append(obj);
            this.buffer = buffer;
            return this;
        }

        public Builder append(String str) {
            checkBuffer();
            builder.append(str);
            return this;
        }

        public Builder append(String str, String... buffer) {
            checkBuffer();
            builder.append(str);
            this.buffer = buffer;
            return this;
        }

        public Builder append(StringBuffer sb) {
            checkBuffer();
            builder.append(sb);
            return this;
        }

        public Builder append(StringBuffer sb, String... buffer) {
            checkBuffer();
            builder.append(sb);
            this.buffer = buffer;
            return this;
        }

        public Builder append(CharSequence s) {
            checkBuffer();
            builder.append(s);
            return this;
        }

        public Builder append(CharSequence s, String... buffer) {
            checkBuffer();
            builder.append(s);
            this.buffer = buffer;
            return this;
        }

        public Builder append(CharSequence s, int start, int end) {
            checkBuffer();
            builder.append(s, start, end);
            return this;
        }

        public Builder append(CharSequence s, int start, int end, String... buffer) {
            checkBuffer();
            builder.append(s, start, end);
            this.buffer = buffer;
            return this;
        }

        public Builder append(char[] str) {
            checkBuffer();
            builder.append(str);
            return this;
        }

        public Builder append(char[] str, String... buffer) {
            checkBuffer();
            builder.append(str);
            this.buffer = buffer;
            return this;
        }

        public Builder append(char[] str, int offset, int len) {
            checkBuffer();
            builder.append(str, offset, len);
            return this;
        }

        public Builder append(char[] str, int offset, int len, String... buffer) {
            checkBuffer();
            builder.append(str, offset, len);
            this.buffer = buffer;
            return this;
        }

        public Builder append(boolean b) {
            checkBuffer();
            builder.append(b);
            return this;
        }

        public Builder append(boolean b, String... buffer) {
            checkBuffer();
            builder.append(b);
            this.buffer = buffer;
            return this;
        }

        public Builder append(char c) {
            checkBuffer();
            builder.append(c);
            return this;
        }

        public Builder append(char c, String... buffer) {
            checkBuffer();
            builder.append(c);
            this.buffer = buffer;
            return this;
        }

        public Builder append(int i) {
            checkBuffer();
            builder.append(i);
            return this;
        }

        public Builder append(int i, String... buffer) {
            checkBuffer();
            builder.append(i);
            this.buffer = buffer;
            return this;
        }

        public Builder append(long lng) {
            checkBuffer();
            builder.append(lng);
            return this;
        }

        public Builder append(long lng, String... buffer) {
            checkBuffer();
            builder.append(lng);
            this.buffer = buffer;
            return this;
        }

        public Builder append(float f) {
            checkBuffer();
            builder.append(f);
            return this;
        }

        public Builder append(float f, String... buffer) {
            checkBuffer();
            builder.append(f);
            this.buffer = buffer;
            return this;
        }

        public Builder append(double d) {
            checkBuffer();
            builder.append(d);
            return this;
        }

        public Builder append(double d, String... buffer) {
            checkBuffer();
            builder.append(d);
            this.buffer = buffer;
            return this;
        }

        public Builder appendCodePoint(int codePoint) {
            builder.appendCodePoint(codePoint);
            return this;
        }

        public Builder delete(int start, int end) {
            builder.delete(start, end);
            return this;
        }

        public Builder deleteCharAt(int index) {
            builder.deleteCharAt(index);
            return this;
        }

        public Builder replace(int start, int end, String str) {
            builder.replace(start, end, str);
            return this;
        }

        public Builder insert(int index, char[] str, int offset, int len) {
            builder.insert(index, str, offset, len);
            return this;
        }

        public Builder insert(int offset, Object obj) {
            builder.insert(offset, obj);
            return this;
        }

        public Builder insert(int offset, String str) {
            builder.insert(offset, str);
            return this;
        }

        public Builder insert(int offset, char[] str) {
            builder.insert(offset, str);
            return this;
        }

        public Builder insert(int dstOffset, CharSequence s) {
            builder.insert(dstOffset, s);
            return this;
        }

        public Builder insert(int dstOffset, CharSequence s, int start, int end) {
            builder.insert(dstOffset, s, start, end);
            return this;
        }

        public Builder insert(int offset, boolean b) {
            builder.insert(offset, b);
            return this;
        }

        public Builder insert(int offset, char c) {
            builder.insert(offset, c);
            return this;
        }

        public Builder insert(int offset, int i) {
            builder.insert(offset, i);
            return this;
        }

        public Builder insert(int offset, long l) {
            builder.insert(offset, l);
            return this;
        }

        public Builder insert(int offset, float f) {
            builder.insert(offset, f);
            return this;
        }

        public Builder insert(int offset, double d) {
            builder.insert(offset, d);
            return this;
        }

        public int indexOf(String str) {
            return builder.indexOf(str);
        }

        public int indexOf(String str, int fromIndex) {
            return builder.indexOf(str, fromIndex);
        }

        public int lastIndexOf(String str) {
            return builder.lastIndexOf(str);
        }

        public int lastIndexOf(String str, int fromIndex) {
            return builder.lastIndexOf(str, fromIndex);
        }

        public Builder reverse() {
            builder.reverse();
            return this;
        }

        @Override
        public String toString() {
            return builder.toString();
        }

        public int length() {
            return builder.length();
        }

        public int capacity() {
            return builder.capacity();
        }

        public void ensureCapacity(int minimumCapacity) {
            builder.ensureCapacity(minimumCapacity);
        }

        public void trimToSize() {
            builder.trimToSize();
        }

        public void setLength(int newLength) {
            builder.setLength(newLength);
        }

        public char charAt(int index) {
            return builder.charAt(index);
        }

        public int codePointAt(int index) {
            return builder.codePointAt(index);
        }

        public int codePointBefore(int index) {
            return builder.codePointBefore(index);
        }

        public int codePointCount(int beginIndex, int endIndex) {
            return builder.codePointCount(beginIndex, endIndex);
        }

        public int offsetByCodePoints(int index, int codePointOffset) {
            return builder.offsetByCodePoints(index, codePointOffset);
        }

        public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
            builder.getChars(srcBegin, srcEnd, dst, dstBegin);
        }

        public void setCharAt(int index, char ch) {
            builder.setCharAt(index, ch);
        }

        public String substring(int start) {
            return builder.substring(start);
        }

        public CharSequence subSequence(int start, int end) {
            return builder.subSequence(start, end);
        }

        public String substring(int start, int end) {
            return builder.substring(start, end);
        }

        public IntStream chars() {
            return builder.chars();
        }

        public IntStream codePoints() {
            return builder.codePoints();
        }
    }
}
