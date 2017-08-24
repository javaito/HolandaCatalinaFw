package org.hcjf.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author javaito
 * @email javaito@gmail.com
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
        Assert.assertEquals(richTexts.get(2), "'&0'' rich text. '&1'");

        value = "in 'the' middle";
        richTexts = Strings.groupRichText(value);
        Assert.assertEquals(richTexts.size(), 2);
        Assert.assertEquals(richTexts.get(0), "the");

        value = "There aren\\'t any rich text here";
        richTexts = Strings.groupRichText(value);
        Assert.assertEquals(richTexts.size(), 1);
        Assert.assertEquals(richTexts.get(0), value);
    }

}
