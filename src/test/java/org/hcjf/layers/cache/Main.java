package org.hcjf.layers.cache;

import org.hcjf.layers.Layers;
import org.hcjf.properties.SystemProperties;

public class Main {

    public static void main(String[] args) {

        System.setProperty(SystemProperties.Log.SYSTEM_OUT_ENABLED, "false");

        Layers.publishLayer(MapCache.class);
        Layers.publishLayer(SubLayerCache.class);
        Layers.publishLayer(SubLayerCache2.class);

        CacheServiceConsumer cacheServiceConsumer = new CacheServiceConsumer("subLayer");
        CacheService.getInstance().registerConsumer(cacheServiceConsumer);
        CacheServiceConsumer cacheServiceConsumer2 = new CacheServiceConsumer("subLayer2");
        CacheService.getInstance().registerConsumer(cacheServiceConsumer2);

        System.out.println(cacheServiceConsumer.get().toString());
        System.out.println(cacheServiceConsumer2.get().toString());

        MapCache.Source.getInstance().getSource().put("Key", "value");
        System.out.println(cacheServiceConsumer.get().toString());
        System.out.println(cacheServiceConsumer2.get().toString());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(cacheServiceConsumer.get().toString());
        System.out.println(cacheServiceConsumer2.get().toString());

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(cacheServiceConsumer.get().toString());
        System.out.println(cacheServiceConsumer2.get().toString());
    }

}
