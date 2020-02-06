package org.hcjf.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SetNullTest {

    @Test
    public void test() {
        Map<String,Object> model = Introspection.createAndPut(1, "fields.integer");
        model = Introspection.put(model, "lalala", "fields.string");
        Integer integerValue = Introspection.resolve(model, "fields.integer");
        Assert.assertEquals(integerValue, Integer.valueOf(1));
        model = Introspection.put(model, null, "fields.integer");
        Assert.assertNull(Introspection.resolve(model, "fields.integer"));
    }

}
