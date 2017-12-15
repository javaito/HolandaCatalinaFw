package org.hcjf.service.security;

import org.hcjf.layers.Layers;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;
import org.hcjf.utils.Introspection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

/**
 * @author Javier Quiroga.
 * @email javier.quiroga@sitrack.com
 */
public class IntrospectionSecurityTest {

    private static ServiceSession serviceSession;

    @BeforeClass
    public static void setup() {
        System.setSecurityManager(new ServiceSecurityManager());
        Layers.publishLayer(LayerSecurityTest.SecurityTestLayer.class);

        Introspection.getGetters(SecurityBeanTest.class);
        Introspection.getSetters(SecurityBeanTest.class);
    }

    @Before
    public void setupSession() {
        serviceSession = new ServiceSession(UUID.randomUUID());
    }

    @Test
    public void test(){
        SecurityBeanTest bean = new SecurityBeanTest();

        Service.run(() -> {
            Introspection.Getter getter = Introspection.getGetters(SecurityBeanTest.class).get("name");
            try {
                getter.get(bean);
                Assert.fail();
            } catch (InvocationTargetException e) {
            } catch (IllegalAccessException e) {
            } catch (SecurityException ex) {
                Assert.assertTrue(true);
            }
        }, ServiceSession.getGuestSession(), true, Long.MAX_VALUE);

        serviceSession.addGrant(Grants.getGrant(SecurityBeanTest.class, SecurityBeanTest.SET_NAME_PERMISSION));
        serviceSession.addGrant(Grants.getGrant(SecurityBeanTest.class, SecurityBeanTest.GET_NAME_PERMISSION));
        Service.run(() -> {
            Introspection.Getter getter = Introspection.getGetters(SecurityBeanTest.class).get("name");
            Introspection.Setter setter = Introspection.getSetters(SecurityBeanTest.class).get("name");
            try {
                String name = "testName";
                setter.set(bean, name);
                Assert.assertEquals(getter.get(bean), name);
            } catch (InvocationTargetException e) {
            } catch (IllegalAccessException e) {
            } catch (SecurityException ex) {
                Assert.fail();
            }
        }, serviceSession, true, Long.MAX_VALUE);
    }

    public static class SecurityBeanTest {

        public static final String GET_NAME_PERMISSION = "getName";
        public static final String SET_NAME_PERMISSION = "setName";

        private String name;

        @Permission(GET_NAME_PERMISSION)
        public String getName() {
            return name;
        }

        @Permission(SET_NAME_PERMISSION)
        public void setName(String name) {
            this.name = name;
        }
    }
}
