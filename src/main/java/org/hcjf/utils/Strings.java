package org.hcjf.utils;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.properties.SystemProperties;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Matcher;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

    public static final class TaggedMessages {
        public static final String START_TAGGED_MESSAGE = "$@{";
        public static final String TAGGED_MESSAGE_PATTERN = "$@{%s}%s";
    }

    public static final String DEFAULT_PADDING_VALUE = " ";
    public static final String START_GROUP = "(";
    public static final String END_GROUP = ")";
    public static final String START_SUB_GROUP = "[";
    public static final String END_SUB_GROUP = "]";
    public static final String START_OBJECT = "{";
    public static final String END_OBJECT = "}";
    public static final String START_TAG = "<";
    public static final String END_TAG = ">";
    public static final String OBJECT_FIELD_SEPARATOR = ":";
    public static final String QUESTION = "?";
    public static final String REPLACEABLE_GROUP = "¿";
    public static final String END_GROUP_NAME = "·";
    public static final String EMPTY_STRING = "";
    public static final String WHITE_SPACE = " ";
    public static final String CLASS_SEPARATOR = ".";
    public static final String CLASS_SEPARATOR_WITH_SCAPE_CHARACTER = "\\.";
    public static final String UNDERSCORE = "_";
    public static final String CASE_INSENSITIVE_REGEX_FLAG = "(?i)";
    public static final String ARGUMENT_SEPARATOR = ",";
    public static final String ARGUMENT_SEPARATOR_2 = ";";
    public static final String ASSIGNATION = "=";
    public static final String REPLACEABLE_RICH_TEXT = "¡";
    public static final String RICH_TEXT_SEPARATOR = "'";
    public static final String RICH_TEXT_SKIP_CHARACTER = "\\";
    public static final String CARRIAGE_RETURN_AND_LINE_SEPARATOR = "\r\n";
    public static final String CARRIAGE_RETURN = "\r";
    public static final String LINE_SEPARATOR = "\n";
    public static final String TAB = "\t";
    public static final String SLASH = "/";
    public static final String AT = "@";
    public static final String ALL = "*";
    public static final String ARGUMENT_IDENTIFIER = "$";
    public static final String NULL = "null";

    public static final String SPLIT_BY_LENGTH_REGEX = "(?<=\\G.{%d})";
    public static final String REPLACEABLE_EXPRESSION_REGEX = "¿[0-9]*·{1,}";

    /**
     * Creates a hexadecimal string as checksum for the byte array. This method use the algorithm indicated into the
     * file properties or MD% as default.
     * @param bytes Bytes to create the checksum.
     * @return Checksum string.
     */
    public static String checksum(byte[] bytes) {
        return checksum(SystemProperties.get(SystemProperties.HCJF_CHECKSUM_ALGORITHM), bytes);
    }

    /**
     * Creates a hexadecimal string as checksum for the byte array.
     * @param algorithm Algorithm to create the checksum.
     * @param bytes Bytes to create the checksum.
     * @return Checksum string.
     */
    public static String checksum(String algorithm, byte[] bytes) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(bytes);
            return bytesToHex(messageDigest.digest());
        } catch (NoSuchAlgorithmException ex) {
            throw new HCJFRuntimeException("Checksum fail", ex);
        }
    }

    /**
     * This method trim the first and the last value if this values are equals that the parameter.
     * @param value Value to trim.
     * @param limitStrings Value to compare with the start and end of the value.
     * @return Returns the value trimmed
     */
    public static String trim(String value, String limitStrings) {
        return trim(value, limitStrings, limitStrings);
    }

    /**
     * This method trim the first and the last value if this values are equals that the parameters.
     * @param value Value to trim.
     * @param startingString Value to compare with the start of the value.
     * @param endingString Value to compare with the end of the value.
     * @return Returns the value trimmed.
     */
    public static String trim(String value, String startingString, String endingString) {
        String result = value;
        boolean trim = false;
        int start = 0;
        int end = value.length();
        if(value.startsWith(startingString)) {
            start = startingString.length();
            trim = true;
        }
        if(value.endsWith(endingString)) {
            end = end - endingString.length();
            trim = true;
        }
        if(trim) {
            if (start < end) {
                result = value.substring(start, end);
            } else {
                result = EMPTY_STRING;
            }
        }
        return result;
    }

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
        return join(values.stream(), separator);
    }

    /**
     * Return the string that result of join all the values separated by the
     * separated value specified.
     * @param values Values to join.
     * @param separator Separator value.
     * @return Result of the join operation.
     */
    public static String join(Stream<String> values, String separator) {
        Builder builder = new Builder();
        values.filter(S -> !S.isEmpty()).forEach(S -> builder.append(S, separator));
        return builder.toString();
    }

    /**
     * Return the string that result of join all the values separated by the
     * separated value specified and wrapped by the start and and value.
     * @param values Values to join.
     * @param startValue Wrapped start value.
     * @param endValue Wrapped end value.
     * @param separator Separator value.
     * @return Result of the join operation.
     */
    public static String join(Collection<String> values, String startValue, String endValue, String separator) {
        return join(values.stream(), startValue, endValue, separator);
    }

    /**
     * Return the string that result of join all the values separated by the
     * separated value specified and wrapped by the start and and value.
     * @param values Values to join.
     * @param startValue Wrapped start value.
     * @param endValue Wrapped end value.
     * @param separator Separator value.
     * @return Result of the join operation.
     */
    public static String join(Stream<String> values, String startValue, String endValue, String separator) {
        Builder builder = new Builder();
        values.filter(S -> !S.isEmpty()).forEach(S -> builder.append(startValue).append(S).append(endValue, separator));
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
        Character previousCharacter = null;
        for(char character : value.toCharArray()) {
            if(previousCharacter != null && Character.isLowerCase(previousCharacter) && Character.isUpperCase(character)) {
                result.append(separator);
            }
            result.append(character);
            previousCharacter = character;
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
     * This method wrap the value with the wrapper value.
     * @param value Value to wrap.
     * @param wrapper Wrapper instance.
     * @return Wrapper value.
     */
    public static String wrap(String value, String wrapper) {
        return wrap(value, wrapper, wrapper);
    }

    /**
     * This method wrap the value with the start and end wrapper.
     * @param value Value to wrap.
     * @param startWrapper Start value of the wrapper.
     * @param endWrapper End value of the wrapper.
     * @return Wrapper value.
     */
    public static String wrap(String value, String startWrapper, String endWrapper) {
        return startWrapper + value + endWrapper;
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
        return groupRichText(value, RICH_TEXT_SEPARATOR);
    }

    /**
     * Returns all the rich texts contained into the value and the value (in the last place)
     * with all the replaceable places for each rich text.
     * @param value Value to get the rich texts.
     * @return List with the rich text and the original value.
     */
    public static List<String> groupRichText(String value, String separator) {

        if(separator.length() != 1) {
            throw new HCJFRuntimeException("Text grouping function need to a simple character as separator.");
        }

        List<String> result = new ArrayList<>();
        Set<Integer> indexes = allIndexOf(value, separator, true);
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
                    newValue.append(value, endIndex, startIndex);
                    newValue.append(separator).append(REPLACEABLE_RICH_TEXT).
                            append(counter++).append(END_GROUP_NAME).append(separator);
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
        return group(value, START_GROUP, END_GROUP);
    }

    /**
     * Return the list with all the groups and sub groups of the value.
     * A group is the char sequence between the start group and the end group values.
     * @param value Groupable value.
     * @param startGroupCharacter Starting character
     * @param endGroupCharacter Ending character
     * @return List with all the groups.
     */
    public static List<String> group(String value, String startGroupCharacter, String endGroupCharacter) {
        return group(value, startGroupCharacter, endGroupCharacter, true, true);
    }

    /**
     * Return the list with all the groups and sub groups if it is considered.
     * A group is the char sequence between the start group and the end group values.
     * @param value Groupable value.
     * @param startGroupCharacter Starting character
     * @param endGroupCharacter Ending character
     * @param considerSubGroups This argument must be true if you need to consider subgroups.
     * @param skipText This argument must be true if you need to skip the text into the value, the text is considered
     *                 all the text between ''.
     * @return List with all the groups.
     */
    public static List<String> group(String value, String startGroupCharacter, String endGroupCharacter, Boolean skipText, Boolean considerSubGroups) {
        List<String> result;
        if(skipText) {
            List<String> richTexts = Strings.groupRichText(value);
            String safetyValue = richTexts.get(richTexts.size() - 1);
            //Using the las value of the rich text list we assure that the start and end characters into the strings
            //be discarded of the group calculation.
            Set<Integer> startIndexes = allIndexOf(safetyValue, startGroupCharacter);
            Set<Integer> endIndexes = allIndexOf(safetyValue, endGroupCharacter);

            if (startIndexes.size() != endIndexes.size()) {
                throw new IllegalArgumentException("Expected the same amount of start and end group delimiter");
            }

            List<String> groups = group(safetyValue, startIndexes, startGroupCharacter.length(), endIndexes, considerSubGroups);
            result = new ArrayList<>();
            groups.stream().forEach(G -> result.add(reverseRichTextGrouping(G, richTexts)));
        } else {
            //Using the las value of the rich text list we assure that the start and end characters into the strings
            //be discarded of the group calculation.
            Set<Integer> startIndexes = allIndexOf(value, startGroupCharacter);
            Set<Integer> endIndexes = allIndexOf(value, endGroupCharacter);

            if (startIndexes.size() != endIndexes.size()) {
                throw new IllegalArgumentException("Expected the same amount of start and end group delimiter");
            }

            result = group(value, startIndexes, startGroupCharacter.length(), endIndexes, considerSubGroups);
        }
        return result;
    }

    /**
     * Return the list of groups into the value, using the ordered sets of
     * start indexes and end indexes.
     * @param value Value to group.
     * @param startIndexes Set with all the start indexes.
     * @param endIndexes Set with all the end indexes.
     * @return List with all the groups.
     */
    private static List<String> group(String value, Set<Integer> startIndexes, Integer startElementSize, Set<Integer> endIndexes, Boolean considerSubGroups) {
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
                    if(!considerSubGroups) {
                        break;
                    }
                }
            }

            if(!endIndexes.remove(end)) {
                throw new IllegalArgumentException("");
            }
            if(!startIndexes.remove(start)) {
                throw new IllegalArgumentException("");
            }

            result.add(value.substring(start + startElementSize, end));
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
    public static List<String> replaceableGroup(String value) {
        return replaceableGroup(value, START_GROUP, END_GROUP);
    }

    /**
     * Return a list with all groups and sub groups in ascendant order with replacement
     * places that refer some index into the same list.
     * e.g. "Hello (world)" - ["world", "Hello $0"]
     * @param value String to group.
     * @param startGroup String to delimit the group starts
     * @param endGroup String to delimit the group ends
     * @return List with groups.
     */
    public static List<String> replaceableGroup(String value, String startGroup, String endGroup) {
        List<String> groups = Strings.group(value, startGroup, endGroup);
        String replacedValue = value;
        String group;
        String nextGroup;
        Integer occurrence;
        String newSegment;
        for (int j = 0; j < groups.size(); j++) {
            occurrence = 0;
            group = groups.get(j);
            newSegment = startGroup + group + endGroup;
            replacedValue = replaceLast(replacedValue,newSegment, REPLACEABLE_GROUP + j + END_GROUP_NAME);
            for(int k = j + 1; k < groups.size(); k++) {
                nextGroup = groups.get(k);
                if(nextGroup.equals(group)) {
                    occurrence += 1;
                } else if(nextGroup.contains(startGroup)) {
                    if(occurrence < occurrenceSize(nextGroup, newSegment)) {
                        nextGroup = replaceLast(nextGroup, newSegment, REPLACEABLE_GROUP + j + END_GROUP_NAME);
                        groups.set(k, nextGroup);
                    }
                }
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
    public static String getNextGroupIndex(String value, String groupIndicator) {
        String result = null;
        Integer startIndex = value.indexOf(groupIndicator);
        StringBuilder resultBuilder = new StringBuilder();
        char current;
        for (int i = startIndex; i < value.length() && i >= 0; i++) {
            current = value.charAt(i);
            if(Character.isDigit(current) || current == groupIndicator.charAt(0)) {
                resultBuilder.append(current);
            } else if(current == END_GROUP_NAME.charAt(0)) {
                resultBuilder.append(current);
                break;
            }
        }
        if(resultBuilder.length() > 0) {
            result = resultBuilder.toString();
        }
        return result;
    }

    public static Integer getGroupIndexAsNumber(String value, String groupIndicator) {
        return Integer.parseInt(value.replace(groupIndicator, Strings.EMPTY_STRING).
                replace(Strings.END_GROUP_NAME, Strings.EMPTY_STRING));
    }

    /**
     * Reverts the grouping action over the specific value.
     * @param value Value to revert.
     * @param richTextGroups Group lists.
     * @return Reverted value.
     */
    public static String reverseRichTextGrouping(String value, List<String> richTextGroups) {
        return reverseRichTextGrouping(value, richTextGroups, RICH_TEXT_SEPARATOR);
    }

    /**
     * Reverts the grouping action over the specific value.
     * @param value Value to revert.
     * @param richTextGroups Group lists.
     * @return Reverted value.
     */
    public static String reverseRichTextGrouping(String value, List<String> richTextGroups, String separator) {
        String result = value;
        String copy = value;
        String groupIndex = Strings.getNextGroupIndex(copy, REPLACEABLE_RICH_TEXT);
        Integer index;
        while(groupIndex != null) {
            index = getGroupIndexAsNumber(groupIndex, REPLACEABLE_RICH_TEXT);
            result = result.replace(wrap(groupIndex,separator), wrap(richTextGroups.get(index), separator));
            copy = copy.replace(wrap(groupIndex,separator), Strings.EMPTY_STRING);
            groupIndex = Strings.getNextGroupIndex(copy, REPLACEABLE_RICH_TEXT);
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
        return reverseGrouping(value, groups, START_GROUP, END_GROUP);
    }

    /**
     * Reverts the grouping action over the specific value.
     * @param value Value to revert.
     * @param groups Group lists.
     * @param startGroupCharacter Starting character.
     * @param endGroupCharacter Ending character.
     * @return Reverted value.
     */
    public static String reverseGrouping(String value, List<String> groups, String startGroupCharacter, String endGroupCharacter) {
        String result = value;
        String groupIndex = Strings.getNextGroupIndex(result, REPLACEABLE_GROUP);
        Integer index;
        while(groupIndex != null) {
            index = Integer.parseInt(groupIndex.replace(REPLACEABLE_GROUP, EMPTY_STRING).replace(END_GROUP_NAME, EMPTY_STRING));
            result = result.replace(groupIndex,
                    startGroupCharacter + groups.get(index) + endGroupCharacter);
            groupIndex = Strings.getNextGroupIndex(result, REPLACEABLE_GROUP);
        }
        return result;
    }

    /**
     * This method count the number of occurrence of founded segment into the value.
     * @param value Value to count.
     * @param foundedSegment Segment of string.
     * @return Returns the number of occurrences.
     */
    public static Integer occurrenceSize(String value, String foundedSegment) {
        Integer result = 0;
        Integer index = 0;
        do {
            index = value.indexOf(foundedSegment, index);
            if(index >= 0) {
                result += 1;
                index += 1;
            }
        } while (index >= 0);
        return result;
    }

    /**
     * Replace only the first occurrence of the replace segment value and returns the value with the new segment into
     * the original string value. If the replace segment value is not founded into the original value then the return
     * value is the same that the original.
     * @param value Value that contains the segment to be replaced.
     * @param replaceSegment Replace segment value.
     * @param newSegment New segment.
     * @return Value replaced.
     */
    public static String replaceFirst(String value, String replaceSegment, String newSegment) {
        String result = value;
        int indexOf = value.indexOf(replaceSegment);
        if(indexOf >= 0) {
            String firstValue = value.substring(0, indexOf);
            String lastValue = value.substring(indexOf + replaceSegment.length());
            result = Strings.join(List.of(firstValue, newSegment, lastValue), Strings.EMPTY_STRING);
        }
        return result;
    }

    /**
     * Replace only the last occurrence of the replace segment value and returns the value with the new segment into
     * the original string value. If the replace segment value is not founded into the original value then the return
     * value is the same that the original.
     * @param value Value that contains the segment to be replaced.
     * @param replaceSegment Replace segment value.
     * @param newSegment New segment.
     * @return Value replaced.
     */
    public static String replaceLast(String value, String replaceSegment, String newSegment) {
        String result = value;
        int indexOf = value.lastIndexOf(replaceSegment);
        if(indexOf >= 0) {
            String firstValue = value.substring(0, indexOf);
            String lastValue = value.substring(indexOf + replaceSegment.length());
            result = Strings.join(List.of(firstValue, newSegment, lastValue), Strings.EMPTY_STRING);
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
        return hexToBytes(hex, false);
    }

    /**
     * Returns a byte array based on the hexadecimal representation.
     * @param hex Hexadecimal representation.
     * @param littleEndian
     * @return Byte array.
     */
    public static byte[] hexToBytes(String hex, Boolean littleEndian) {
        if ((hex.length() % 2) != 0) {
            throw new IllegalArgumentException("Input string must contain an even number of characters");
        }
        int len = hex.length();
        byte[] result = new byte[len / 2];
        if(littleEndian) {
            for (int i = len - 1; i >= 0; i -= 2) {
                result[(len - 1 - i) / 2] = (byte) ((Character.digit(hex.charAt(i-1), 16) << 4)
                        + Character.digit(hex.charAt(i), 16));
            }
        } else {
            for (int i = 0; i < len; i += 2) {
                result[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                        + Character.digit(hex.charAt(i + 1), 16));
            }
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
     * Returns the instance of the class that is deducted from the string value.
     * @param value String value.
     * @return Instance deducted.
     */
    public static Object deductInstance(String value) {
        Object result = null;
        if(value != null) {
            String trimmedStringValue = value.trim();
            if (trimmedStringValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.NULL))) {
                result = null;
            } else if (trimmedStringValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.TRUE))) {
                result = true;
            } else if (trimmedStringValue.equalsIgnoreCase(SystemProperties.get(SystemProperties.Query.ReservedWord.FALSE))) {
                result = false;
            } else if (trimmedStringValue.startsWith(SystemProperties.get(SystemProperties.Query.ReservedWord.STRING_DELIMITER)) &&
                    trimmedStringValue.endsWith(SystemProperties.get(SystemProperties.Query.ReservedWord.STRING_DELIMITER))) {
                trimmedStringValue = trimmedStringValue.substring(1, trimmedStringValue.length() - 1);

                try {
                    synchronized (SystemProperties.getDateFormat(SystemProperties.HCJF_DEFAULT_DATE_FORMAT)) {
                        result = SystemProperties.getDateFormat(SystemProperties.HCJF_DEFAULT_DATE_FORMAT).parse(trimmedStringValue);
                    }
                } catch (Exception ex) {
                    result = trimmedStringValue;
                }
            } else if (trimmedStringValue.matches(SystemProperties.get(SystemProperties.HCJF_UUID_REGEX))) {
                result = UUID.fromString(trimmedStringValue);
            } else if (trimmedStringValue.matches(SystemProperties.get(SystemProperties.HCJF_INTEGER_NUMBER_REGEX))) {
                try {
                    long longValue = Long.parseLong(trimmedStringValue);
                    if (longValue == (byte) longValue) {
                        result = (byte) longValue;
                    } else if (longValue == (short) longValue) {
                        result = (short) longValue;
                    } else if (longValue == (int) longValue) {
                        result = (int) longValue;
                    } else {
                        result = longValue;
                    }
                } catch (Exception ex) {
                    result = trimmedStringValue;
                }
            } else if (trimmedStringValue.matches(SystemProperties.get(SystemProperties.HCJF_DECIMAL_NUMBER_REGEX))) {
                    result = trimmedStringValue;
            } else if (trimmedStringValue.matches(SystemProperties.get(SystemProperties.HCJF_SCIENTIFIC_NUMBER_REGEX))) {
                try {
                    synchronized (SystemProperties.getDecimalFormat(SystemProperties.HCJF_DEFAULT_SCIENTIFIC_NUMBER_FORMAT)) {
                        result = SystemProperties.getDecimalFormat(SystemProperties.HCJF_DEFAULT_SCIENTIFIC_NUMBER_FORMAT).parse(trimmedStringValue);
                    }
                } catch (ParseException e) {
                    result = trimmedStringValue;
                }
            } else {
                try {
                    synchronized (SystemProperties.getDateFormat(SystemProperties.HCJF_DEFAULT_DATE_FORMAT)) {
                        //Verify again if the string is not a date
                        result = SystemProperties.getDateFormat(SystemProperties.HCJF_DEFAULT_DATE_FORMAT).parse(trimmedStringValue);
                    }
                } catch (Exception ex) {
                    result = trimmedStringValue;
                }
            }
        }
        return result;
    }

    /**
     * This method create a tagged message with the format $@{tag,tag,tag...}message
     * @param message Message to tag.
     * @param tags Array of tags.
     * @return Returns the tagged message.
     */
    public static final String createTaggedMessage(String message, String... tags) {
        String result;
        if(tags == null || tags.length == 0) {
            throw new IllegalArgumentException("Unable to create a tagged message without tags");
        } else {
            Builder builder = new Builder();
            for(String tag : tags) {
                if(!tag.contains(START_OBJECT) && !tag.contains(END_OBJECT) && !tag.contains(ARGUMENT_SEPARATOR)) {
                    builder.append(tag, ARGUMENT_SEPARATOR);
                } else {
                    throw new IllegalArgumentException("The tags can't contains the special characters '{', '}', ','");
                }
            }
            result = String.format(TaggedMessages.TAGGED_MESSAGE_PATTERN, builder.toString(), message);
        }
        return result;
    }

    /**
     * This method create a hash map indexing the same message with the different tags.
     * @param taggedMessage Tagged message.
     * @return Hash map with the tags.
     */
    public static final Map<String,String> getTagsFromMessage(String taggedMessage) {
        Map<String,String> result = new HashMap<>();
        if(taggedMessage.startsWith(TaggedMessages.START_TAGGED_MESSAGE)) {
            String tags = taggedMessage.substring(taggedMessage.indexOf(TaggedMessages.START_TAGGED_MESSAGE) +
                    TaggedMessages.START_TAGGED_MESSAGE.length(), taggedMessage.indexOf(END_OBJECT));
            String message = taggedMessage.substring(taggedMessage.indexOf(END_OBJECT) + END_OBJECT.length());
            for(String tag : tags.split(ARGUMENT_SEPARATOR)) {
                result.put(tag.trim(), message);
            }
        }
        return result;
    }

    /**
     * Returns the place where the regex is no matching with the value.
     * @param matcher Matcher instance.
     * @param value Value to found.
     * @return Returns the index into the value where the regex is not matching.
     */
    public static final int getNoMatchPlace(Matcher matcher, String value) {
        int result = 0;
        for (int i = value.length(); i > 0; --i) {
            Matcher region = matcher.region(0, i);
            if (region.matches() || region.hitEnd()) {
                result = i;
                break;
            }
        }
        return result;
    }

    /**
     * Returns a shorted string that the original centered into the position parameter and with
     * @param value
     * @param position
     * @param length
     * @return
     */
    public static final String getNearFrom(String value, int position, int length) {
        String result = null;
        if(value != null) {
            position = position < 0 ? 0 : position;
            position = position > value.length() ? value.length() : position;
            length = length == 0 ? 1 : length;
            int start = position - Math.abs(length);
            int end = position + Math.abs(length);
            start = start < 0 ? 0 : start;
            end = end > value.length() ? value.length() : end;
            result = value.substring(start, end);
        }
        return result;
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
