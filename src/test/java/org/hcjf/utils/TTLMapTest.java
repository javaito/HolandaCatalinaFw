package org.hcjf.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

/**
 * @author javaito
 */
public class TTLMapTest {

    @Test
    public void testTtlMap() {
        TtlMap<String, String> ttlMap = new TtlMap<>(new HashMap<>(), 1000L);
        ttlMap.put("hello", "world");

        Assert.assertNotNull(ttlMap.get("hello"));

        try {
            Thread.sleep(1001);
        } catch (InterruptedException e) {
        }

        Assert.assertNull(ttlMap.get("hello"));
    }

}
