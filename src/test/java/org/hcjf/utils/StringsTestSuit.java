package org.hcjf.utils;

import org.hcjf.bson.BsonArray;
import org.hcjf.bson.BsonDecoder;
import org.hcjf.bson.BsonDocument;
import org.hcjf.bson.BsonEncoder;
import org.hcjf.layers.query.Query;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * @author javaito
 */
public class StringsTestSuit {

    @Test
    public void testGroupRichText() {
        String value = "Hello 'world'";
        List<String> richTexts = Strings.groupRichText(value);
        Assert.assertEquals(richTexts.size(), 2);
        Assert.assertEquals(richTexts.get(0), "world");
        Assert.assertEquals(richTexts.get(1), "Hello '¡0·'");

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
        Assert.assertEquals(richTexts.get(3), "SELECT *  FROM holder WHERE nombre LIKE '¡0·' OR dominio LIKE " +
                "'¡1·' AND activo = '¡2·' AND holderid IN " +
                "(92928,124291,11278,119441,104341,45460,111255,15513,15001,3358,12447,22047," +
                "88740,15528,21033,3755,115506,57397,123447,10427,120639,120638,120641,120640," +
                "120642,30533,58697,17483,40395,106188,7246,43598,23889,23891,81366,15321,23130," +
                "5594,93018,81628,3041,20065,103654,78824,49257,19050,99818,87530,71405,39792,18930," +
                "3827,16246,43261,47230) LIMIT 20");

        value = "This is an example of rich text: 'example of \\'rich text\\''";
        richTexts = Strings.groupRichText(value);
        Assert.assertEquals(richTexts.size(), 2);
        Assert.assertEquals(richTexts.get(0), "example of \\'rich text\\'");
        Assert.assertEquals(richTexts.get(1), "This is an example of rich text: '¡0·'");

        value = "'Start with' rich text. '[]\\';;./,;;\\|||^!@#$%&*()_+~~~```'";
        richTexts = Strings.groupRichText(value);
        Assert.assertEquals(richTexts.size(), 3);
        Assert.assertEquals(richTexts.get(0), "Start with");
        Assert.assertEquals(richTexts.get(1), "[]\\';;./,;;\\|||^!@#$%&*()_+~~~```");
        Assert.assertEquals(richTexts.get(2), "'¡0·' rich text. '¡1·'");

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
    public void testSplitWord() {
        String splitResult = Strings.splitInWord("JavierQuiroga", " ");
        Assert.assertEquals(splitResult, "Javier Quiroga");
        splitResult = Strings.splitInWord("JavierQUiroga", " ");
        Assert.assertEquals(splitResult, "Javier QUiroga");
        splitResult = Strings.splitInWord("JAVIERQUIROGA", " ");
        Assert.assertEquals(splitResult, "JAVIERQUIROGA");
        splitResult = Strings.splitInWord("JAVIER QUIRoGA", " ");
        Assert.assertEquals(splitResult, "JAVIER QUIRo GA");
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

    @Test
    public void testDeductBoolean() {
        String value = "true";
        Object deductedValue = Strings.deductInstance(value);
        Assert.assertEquals(deductedValue.getClass(), Boolean.class);

        value = "false";
        deductedValue = Strings.deductInstance(value);
        Assert.assertEquals(deductedValue.getClass(), Boolean.class);
    }

    @Test
    public void replaceFirst() {
        String value = "Javier Roman Quiroga, Javier 8652314978kdjf!%$&$%&/(&(=/=()¡";
        String valueReplaced1 = Strings.replaceFirst(value, "Javier", "javaito");
        Assert.assertEquals("javaito Roman Quiroga, Javier 8652314978kdjf!%$&$%&/(&(=/=()¡", valueReplaced1);
        String valueReplaced2 = Strings.replaceFirst(valueReplaced1, "Javier", "javaito");
        Assert.assertEquals("javaito Roman Quiroga, javaito 8652314978kdjf!%$&$%&/(&(=/=()¡", valueReplaced2);
        String valueReplaced3 = Strings.replaceFirst(valueReplaced2, "=()¡", "~");
        Assert.assertEquals("javaito Roman Quiroga, javaito 8652314978kdjf!%$&$%&/(&(=/~", valueReplaced3);

        System.out.println();
    }

    @Test
    public void testDeductDate() {
        String value = "2030-02-19 00:00:00";
        Object deductedValue = Strings.deductInstance(value);
        Assert.assertEquals(deductedValue.getClass(), Date.class);

        value = "'2030-02-19 00:00:00'";
        deductedValue = Strings.deductInstance(value);
        Assert.assertEquals(deductedValue.getClass(), Date.class);
    }

    @Test
    public void testDeductDouble() {
        String value = "2.0000000020559128E-6";
        Object deductedValue = Strings.deductInstance(value);
        Assert.assertEquals(deductedValue.getClass(), Double.class);
    }

    @Test
    public void testNearFrom() {
        String value = "Holanda Catalina";
        String nearOffValue = Strings.getNearFrom(value, 4, 2);
        Assert.assertEquals(nearOffValue, "land");

        nearOffValue = Strings.getNearFrom(value, 4, 50);
        Assert.assertEquals(nearOffValue, value);

        nearOffValue = Strings.getNearFrom(value, 0, 4);
        Assert.assertEquals(nearOffValue, "Hola");

        nearOffValue = Strings.getNearFrom(value, value.length(), 4);
        Assert.assertEquals(nearOffValue, "lina");
    }

    @Test
    public void testGroup() {
        String s = "(lalalal) / la";
        List<String> groups = Strings.replaceableGroup(s);
        System.out.println();

        s = "(100 - 20) / 50";
        groups = Strings.replaceableGroup(s);
        System.out.println();
    }

    public static void main(String[] args) {
        /**
         * 1560880035309
         * },
         * {
         * "startTimestamp": 1560880040918
         * },
         * {
         * "startTimestamp": 1560881049657
         * },
         * {
         * "startTimestamp": 1560881051237
         * },
         * {
         * "startTimestamp": 1560881056146
         * },
         * {
         * "startTimestamp": 1560881061012
         * },
         * {
         * "startTimestamp": 1560880045960
         * },
         * {
         * "startTimestamp": 1560880051667
         * },
         * {
         * "startTimestamp": 1560880989097
         * },
         * {
         * "startTimestamp": 1560880992709
         * },
         * {
         * "startTimestamp": 1560880995050
         * },
         * {
         * "startTimestamp": 1560881001090
         * },
         * {
         * "startTimestamp": 1560881006090
         * },
         * {
         * "startTimestamp": 1560881009805
         */
        Long l1 = 1560880035309L;
        Long l2 = 1560880040918L;
        Long l3 = 1560881049657L;
        Long l4 = 1560881051237L;
        Long l5 = 1560881056146L;
        Long l6 = 1560881061012L;
        Long l7 = 1560880045960L;
        Long l8 = 1560880051667L;
        Long l9 = 1560880989097L;
        Long l10 = 1560880992709L;
        Long l11 = 1560880995050L;
        Long l12 = 1560881001090L;
        Long l13 = 1560881006090L;
        Long l14 = 1560881009805L;

        Collection<Map<String,Object>> t = new ArrayList<>();
        t.add(Map.of("id", 1, "value", l1));
        t.add(Map.of("id", 2, "value", l2));
        t.add(Map.of("id", 3, "value", l3));
        t.add(Map.of("id", 4, "value", l4));
        t.add(Map.of("id", 5, "value", l5));
        t.add(Map.of("id", 6, "value", l6));
        t.add(Map.of("id", 7, "value", l7));
        t.add(Map.of("id", 8, "value", l8));
        t.add(Map.of("id", 9, "value", l9));
        t.add(Map.of("id", 10, "value", l10));
        t.add(Map.of("id", 11, "value", l11));
        t.add(Map.of("id", 12, "value", l12));
        t.add(Map.of("id", 13, "value", l13));
        t.add(Map.of("id", 14, "value", l14));


        Collection<Map<String,Object>> resultSet = Query.compile("SELECT * FROM A ORDER BY value").evaluate(t);
        BsonDocument document = new BsonDocument();
        document.put("resultSet", resultSet);
        byte[] body = BsonEncoder.encode(document);

        BsonDocument document1 = BsonDecoder.decode(body);
        Collection resultSet1 = ((BsonArray) document1.get("resultSet")).toList();

        System.out.println(resultSet);
        System.out.println(((BsonArray) document1.get("resultSet")).toJsonString());
    }

}
