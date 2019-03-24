package org.hcjf.utils;

import org.junit.Assert;
import org.junit.Test;

public class LruMapTest {

    @Test
    public void deleteFirst() throws InterruptedException {
        LruMap<String,String> lruMap = new LruMap<>(3);

        lruMap.put("1°","1°");
        Thread.sleep(10);

        lruMap.put("2°","2°");
        Thread.sleep(10);

        lruMap.put("3°","3°");
        Thread.sleep(10);

        lruMap.put("4°","4°");

        Assert.assertTrue(!lruMap.containsKey("1°"));
        Assert.assertTrue(lruMap.containsKey("2°"));
        Assert.assertTrue(lruMap.containsKey("3°"));
        Assert.assertTrue(lruMap.containsKey("4°"));
    }

    @Test
    public void deleteSecond() throws InterruptedException {
        LruMap<String,String> lruMap = new LruMap<>(3);

        lruMap.put("1°","1°");
        Thread.sleep(10);

        lruMap.put("2°","2°");
        Thread.sleep(10);

        lruMap.put("3°","3°");
        Thread.sleep(10);
        lruMap.get("1°");

        lruMap.put("4°","4°");
        Thread.sleep(10);

        Assert.assertTrue(lruMap.containsKey("1°"));
        Assert.assertTrue(!lruMap.containsKey("2°"));
        Assert.assertTrue(lruMap.containsKey("3°"));
        Assert.assertTrue(lruMap.containsKey("4°"));
    }
}
