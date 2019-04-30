package org.hcjf.layers;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.crud.ReadRowsLayerInterface;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;

/**
 * @author Javier Quiroga.
 * @email javier.quiroga@sitrack.com
 */
public class LayerSearchTest {

    @BeforeClass
    public static void config() {
        Layers.publishLayer(LayerA.class);
        Layers.publishLayer(LayerB.class);
    }

    @Test
    public void testSearchByName() {
        ReadRowsLayerInterface readRowsLayerInterface = Layers.get(ReadRowsLayerInterface.class, "impl-layer-a");
        Assert.assertEquals(readRowsLayerInterface.getImplName(), "impl-layer-a");
    }

    @Test
    public void testSearchByMatch() {
        ReadRowsLayerInterface readRowsLayerInterface = Layers.get(ReadRowsLayerInterface.class,
                layer -> layer.getImplName().contains("-"));
        Assert.assertTrue(readRowsLayerInterface.getImplName().contains("-"));

        readRowsLayerInterface = Layers.get(ReadRowsLayerInterface.class,
                layer -> layer.getImplName().contains("-a"));
        Assert.assertEquals(readRowsLayerInterface.getImplName(), "impl-layer-a");

        readRowsLayerInterface = Layers.get(ReadRowsLayerInterface.class,
                layer -> layer.getImplName().contains("-b"));
        Assert.assertEquals(readRowsLayerInterface.getImplName(), "impl-layer-b");

        try {
            readRowsLayerInterface = Layers.get(ReadRowsLayerInterface.class,
                    layer -> layer.getImplName().contains("-c"));
            Assert.fail();
        } catch (Exception ex){
            Assert.assertTrue(ex instanceof HCJFRuntimeException);
        }
    }

    @Test
    public void testSearchAllByMatch() {
        Set<ReadRowsLayerInterface> readRowsLayerInterfaces = Layers.getAll(ReadRowsLayerInterface.class,
                layer -> layer.getImplName().contains("-"));
        Assert.assertEquals(readRowsLayerInterfaces.size(), 2);

        readRowsLayerInterfaces = Layers.getAll(ReadRowsLayerInterface.class,
                layer -> layer.getImplName().contains("-a"));
        Assert.assertEquals(readRowsLayerInterfaces.size(), 1);
        Assert.assertEquals(readRowsLayerInterfaces.iterator().next().getImplName(), "impl-layer-a");

        readRowsLayerInterfaces = Layers.getAll(ReadRowsLayerInterface.class,
                layer -> layer.getImplName().contains("-b"));
        Assert.assertEquals(readRowsLayerInterfaces.size(), 1);
        Assert.assertEquals(readRowsLayerInterfaces.iterator().next().getImplName(), "impl-layer-b");
    }

    public static class LayerA extends Layer implements ReadRowsLayerInterface {

        public LayerA() {
            super("impl-layer-a");
        }

    }

    public static class LayerB extends Layer implements ReadRowsLayerInterface {

        public LayerB() {
            super("impl-layer-b");
        }

    }
}
