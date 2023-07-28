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

    @Test
    public void testTtlAndLru() throws InterruptedException {
        TtlMap<String, String> superMap = new TtlMap<>(new LruMap<>(3), 5000L);

        superMap.put("1°","1°");
        Thread.sleep(10);

        superMap.put("2°","2°");
        Thread.sleep(10);

        superMap.put("3°","3°");
        Thread.sleep(10);

        superMap.put("4°","4°");

        Assert.assertTrue(!superMap.containsKey("1°"));
        Assert.assertTrue(superMap.containsKey("2°"));
        Assert.assertTrue(superMap.containsKey("3°"));
        Assert.assertTrue(superMap.containsKey("4°"));

        Thread.sleep(5500);

        Assert.assertTrue(superMap.isEmpty());

        superMap.put("3°","3°");
        Thread.sleep(10);

        Assert.assertTrue(superMap.containsKey("3°"));
    }
}
