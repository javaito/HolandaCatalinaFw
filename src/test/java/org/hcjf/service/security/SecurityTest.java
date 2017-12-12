package org.hcjf.service.security;

import org.hcjf.layers.Layer;
import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.Layers;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

/**
 * @author javaito.
 */
public class SecurityTest {

    @Test
    public void securityTest() {
        System.setSecurityManager(new ServiceSecurityManager());

        Layers.publishLayer(SecurityTestLayer.class);

        System.out.println(Grants.getGrants());

        try {
            Service.run(() -> {
                SecurityTestLayerInterface li = Layers.get(SecurityTestLayerInterface.class, "test");
                li.invoke(0);
            }, ServiceSession.getGuestSession(), true, Long.MAX_VALUE);
            Assert.fail();
        } catch (Exception ex) {
            Assert.assertTrue(true);
        }

        ServiceSession serviceSession = new ServiceSession(UUID.randomUUID());
        for(Grant grant : Grants.getGrants()) {
            if(!grant.getGrantId().endsWith("lazyInvoke")) {
                serviceSession.addGrant(grant);
            }
        }
        System.out.println("Grants into session: " + serviceSession.getGrants());
        Service.run(() -> {
            SecurityTestLayerInterface li = Layers.get(SecurityTestLayerInterface.class, "test");
            li.invoke(0);
        }, serviceSession, true, Long.MAX_VALUE);
        Assert.assertTrue(true);

        try {
            Service.run(() -> {
                SecurityTestLayerInterface li = Layers.get(SecurityTestLayerInterface.class, "test");
                li.invoke(1);
            }, serviceSession, true, Long.MAX_VALUE);
            Assert.fail();
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.assertTrue(true);
        }

        for(Grant grant : Grants.getGrants()) {
            if(grant.getGrantId().endsWith("lazyInvoke")) {
                serviceSession.addGrant(grant);
            }
        }
        System.out.println("Grants into session: " + serviceSession.getGrants());
        Service.run(() -> {
            SecurityTestLayerInterface li = Layers.get(SecurityTestLayerInterface.class, "test");
            li.invoke(1);
        }, serviceSession, true, Long.MAX_VALUE);
        Assert.assertTrue(true);

        Service.run(() -> {
            SecurityTestLayerInterface li = Layers.get(SecurityTestLayerInterface.class, "test");
            li.invoke(0);
        }, ServiceSession.getSystemSession(), true, Long.MAX_VALUE);
        Assert.assertTrue(true);
    }

    public interface SecurityTestLayerInterface extends LayerInterface {

        void invoke(int i);

    }

    public static class SecurityTestLayer extends Layer implements SecurityTestLayerInterface {

        @Override
        public String getImplName() {
            return "test";
        }

        @Override
        @Permission("invoke")
        @LazyPermission("lazyInvoke")
        public void invoke(int i) {
            System.out.println("Method invoked!!!");

            if(i > 0) {
                checkPermission("lazyInvoke");
            }
        }
    }

}
