package org.hcjf.utils;

import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * @author javaito
 */
public class TTLMapTest {

    @Test
    public void testTtlMap() {
        TtlMap<String, String> ttlMap = new TtlMap<>(new HashMap<>(), 1000L);
        ttlMap.put("hello", "world");

        System.out.println(ttlMap.get("hello"));
        Assert.assertNotNull(ttlMap.get("hello"));

        try {
            Thread.sleep(1001);
        } catch (InterruptedException e) {
        }

        System.out.println(ttlMap.get("hello"));
        Assert.assertNull(ttlMap.get("hello"));
    }

}
