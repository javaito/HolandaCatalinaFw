package org.hcjf.service.security;

import org.hcjf.layers.Layer;
import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.Layers;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * @author javaito.
 */
public class LayerSecurityTest {

    private static ServiceSession serviceSession;

    @BeforeClass
    public static void setup() {
        System.setSecurityManager(new ServiceSecurityManager());
        Layers.publishLayer(SecurityTestLayer.class);
    }

    @Before
    public void setupSession() {
        serviceSession = new ServiceSession(UUID.randomUUID());
    }

    @Test
    public void permissionTest() {
        try {
            Service.run(() -> {
                SecurityTestLayerInterface li = Layers.get(SecurityTestLayerInterface.class, "test");
                li.invoke(0);
            }, ServiceSession.getGuestSession(), true, Long.MAX_VALUE);
            Assert.fail();
        } catch (Exception ex) {
            Assert.assertTrue(true);
        }

        serviceSession.addGrant(Grants.getGrant(SecurityTestLayer.class,
                SecurityTestLayer.INVOKE_PERMISSION_NAME));
        try {
            Service.run(() -> {
                SecurityTestLayerInterface li = Layers.get(SecurityTestLayerInterface.class, "test");
                li.invoke(0);
            }, serviceSession, true, Long.MAX_VALUE);
            Assert.fail();
        } catch (Exception ex) {
            Assert.assertTrue(true);
        }

        serviceSession.addGrant(Grants.getGrant(SecurityTestLayer.class,
                SecurityTestLayer.INVOKE_2_PERMISSION_NAME));
        try {
            Service.run(() -> {
                SecurityTestLayerInterface li = Layers.get(SecurityTestLayerInterface.class, "test");
                int i = li.invoke(0);
                Assert.assertEquals(i, 50);
            }, serviceSession, true, Long.MAX_VALUE);
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail();
        }
    }

    @Test
    public void lazyPermissionTest() {
        serviceSession.addGrant(Grants.getGrant(SecurityTestLayer.class,
                SecurityTestLayer.INVOKE_PERMISSION_NAME));
        serviceSession.addGrant(Grants.getGrant(SecurityTestLayer.class,
                SecurityTestLayer.INVOKE_2_PERMISSION_NAME));
        try {
            Service.run(() -> {
                serviceSession.addGrant(Grants.getGrant(SecurityTestLayer.class,
                        SecurityTestLayer.INVOKE_LAZY_PERMISSION_NAME));
                SecurityTestLayerInterface li = Layers.get(SecurityTestLayerInterface.class, "test");
                int i = li.invoke(0);
                Assert.assertEquals(i, 50);
                i = li.invoke(1);
                Assert.assertEquals(i, 101);
                serviceSession.addGrant(Grants.getGrant(SecurityTestLayer.class,
                        SecurityTestLayer.INVOKE_ACTION_LAZY_PERMISSION_NAME));
                i = li.invoke(0);
                Assert.assertEquals(i, 250);
                i = li.invoke(1);
                Assert.assertEquals(i, 301);
            }, serviceSession, true, Long.MAX_VALUE);
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.fail();
        }
    }

    public interface SecurityTestLayerInterface extends LayerInterface {

        int invoke(int i);

    }

    public static class SecurityTestLayer extends Layer implements SecurityTestLayerInterface {

        public static final String INVOKE_PERMISSION_NAME = "invoke";
        public static final String INVOKE_2_PERMISSION_NAME = "invoke2";
        public static final String INVOKE_LAZY_PERMISSION_NAME = "invoke_lazy";
        public static final String INVOKE_ACTION_LAZY_PERMISSION_NAME = "invokeAction_lazy";

        @Override
        public String getImplName() {
            return "test";
        }

        @Override
        @Permission(INVOKE_PERMISSION_NAME)
        @Permission(INVOKE_2_PERMISSION_NAME)
        @LazyPermission(INVOKE_LAZY_PERMISSION_NAME)
        @LazyPermission(INVOKE_ACTION_LAZY_PERMISSION_NAME)
        public int invoke(int i) {
            System.out.println("Method invoked!!!");
            AtomicInteger integer = new AtomicInteger(i);

            if(i > 0) {
                SecurityPermissions.checkPermission(getClass(),INVOKE_LAZY_PERMISSION_NAME);
                integer.addAndGet(100);
            } else {
                integer.addAndGet(50);
            }

            Integer j = 0;
            SecurityPermissions.checkPermission(getClass(),INVOKE_ACTION_LAZY_PERMISSION_NAME,
                    ()-> integer.addAndGet(200));

            return integer.get();
        }
    }

}
