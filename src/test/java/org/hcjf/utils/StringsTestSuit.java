package org.hcjf.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author javaito
 */
public class StringsTestSuit {

    @Test
    public void testGroup() {

    }

    @Test
    public void testGroupRichText() {
        String value = "Hello 'world'";
        List<String> richTexts = Strings.groupRichText(value);
        Assert.assertEquals(richTexts.size(), 2);
        Assert.assertEquals(richTexts.get(0), "world");
        Assert.assertEquals(richTexts.get(1), "Hello '&0'");

        value = "SELECT *  FROM holder WHERE nombre LIKE '%MKR%' OR dominio LIKE " +
                "'%MKR%' AND activo = '1' AND holderid IN " +
                "(92928,124291,11278,119441,104341,45460,111255,15513,15001,3358,12447,22047," +
                "88740,15528,21033,3755,115506,57397,123447,10427,120639,120638,120641,120640," +
                "120642,30533,58697,17483,40395,106188,7246,43598,23889,23891,81366,15321,23130," +
                "5594,93018,81628,3041,20065,103654,78824,49257,19050,99818,87530,71405,39792,18930," +
                "3827,16246,43261,47230) LIMIT 20";
        richTexts = Strings.groupRichText(value);
        Assert.assertEquals(richTexts.size(), 4);
        Assert.assertEquals(richTexts.get(0), "%MKR%");
        Assert.assertEquals(richTexts.get(1), "%MKR%");
        Assert.assertEquals(richTexts.get(2), "1");
        Assert.assertEquals(richTexts.get(3), "SELECT *  FROM holder WHERE nombre LIKE '&0' OR dominio LIKE " +
                "'&1' AND activo = '&2' AND holderid IN " +
                "(92928,124291,11278,119441,104341,45460,111255,15513,15001,3358,12447,22047," +
                "88740,15528,21033,3755,115506,57397,123447,10427,120639,120638,120641,120640," +
                "120642,30533,58697,17483,40395,106188,7246,43598,23889,23891,81366,15321,23130," +
                "5594,93018,81628,3041,20065,103654,78824,49257,19050,99818,87530,71405,39792,18930," +
                "3827,16246,43261,47230) LIMIT 20");

        value = "This is an example of rich text: 'example of \\'rich text\\''";
        richTexts = Strings.groupRichText(value);
        Assert.assertEquals(richTexts.size(), 2);
        Assert.assertEquals(richTexts.get(0), "example of \\'rich text\\'");
        Assert.assertEquals(richTexts.get(1), "This is an example of rich text: '&0'");

        value = "'Start with' rich text. '[]\\';;./,;;\\|||^!@#$%&*()_+~~~```'";
        richTexts = Strings.groupRichText(value);
        Assert.assertEquals(richTexts.size(), 3);
        Assert.assertEquals(richTexts.get(0), "Start with");
        Assert.assertEquals(richTexts.get(1), "[]\\';;./,;;\\|||^!@#$%&*()_+~~~```");
        Assert.assertEquals(richTexts.get(2), "'&0' rich text. '&1'");

        value = "in 'the' middle";
        richTexts = Strings.groupRichText(value);
        Assert.assertEquals(richTexts.size(), 2);
        Assert.assertEquals(richTexts.get(0), "the");

        value = "There aren\\'t any rich text here";
        richTexts = Strings.groupRichText(value);
        Assert.assertEquals(richTexts.size(), 1);
        Assert.assertEquals(richTexts.get(0), value);

        value = "There aren\\'t any rich text here";
        String[] fragments = Strings.splitByLength(value, 2);
        Assert.assertEquals(fragments[fragments.length-1], "re");
        Assert.assertEquals(fragments[0], "Th");
        fragments = Strings.splitByLength(value, 3);
        Assert.assertEquals(fragments[fragments.length-1], "re");
        Assert.assertEquals(fragments[0], "The");
        fragments = Strings.splitByLength(value, 5);
        Assert.assertEquals(fragments[fragments.length-1], "re");
        Assert.assertEquals(fragments[0], "There");

        value = "AndresMedina";
        value = Strings.splitInWord(value, "-");
        Assert.assertEquals(value, "Andres-Medina");
    }

    @Test
    public void testJoin() {
        List<String> values = List.of("one", "two", "three");
        String joined = Strings.join(values, Strings.ARGUMENT_SEPARATOR);
        Assert.assertEquals(joined, "one,two,three");

        joined = Strings.join(values, Strings.RICH_TEXT_SEPARATOR, Strings.RICH_TEXT_SEPARATOR, Strings.ARGUMENT_SEPARATOR);
        Assert.assertEquals(joined, "'one','two','three'");
    }

    @Test
    public void testJoinWords() {
        String value = "hello-world";
        value = Strings.joinWords(value, "-");
        Assert.assertEquals(value, "HelloWorld");
    }

    @Test
    public void testTrim() {
        String value = "%hello world%";
        value = Strings.trim(value, "%");
        Assert.assertEquals(value, "hello world");

        value = "%hello world&";
        value = Strings.trim(value, "%", "&");
        Assert.assertEquals(value, "hello world");

        value = "%";
        value = Strings.trim(value, "%");
        Assert.assertEquals(value, "");

        value = "hello world";
        value = Strings.trim(value, "%", "&");
        Assert.assertEquals(value, "hello world");
    }

    @Test
    public void testTaggedMessages() {
        String message = "Hello world!!";

        String taggedMessage = Strings.createTaggedMessage(message, "tag1", "tag2");
        Map<String,String> tags = Strings.getTagsFromMessage(taggedMessage);

        Assert.assertEquals(tags.get("tag1"), message);
        Assert.assertEquals(tags.get("tag2"), message);
    }
}
