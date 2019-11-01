package org.hcjf.service;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.UUID;

public class ServiceSessionTest {

    @Test
    public void testIdentity() {
        UUID id1 = UUID.randomUUID();
        ServiceSession serviceSession1 = new ServiceSession(id1);

        UUID id2 = UUID.randomUUID();
        ServiceSession serviceSession2 = new ServiceSession(id2);

        UUID id3 = UUID.randomUUID();
        ServiceSession serviceSession3 = new ServiceSession(id3);

        Service.run(() -> {
            Assert.assertEquals(ServiceSession.getCurrentIdentity(), serviceSession1);
            Assert.assertEquals(ServiceSession.getCurrentSession(), serviceSession1);

            ServiceSession.runAs(() -> {
                Assert.assertEquals(ServiceSession.getCurrentIdentity(), serviceSession2);
                Assert.assertEquals(ServiceSession.getCurrentSession(), serviceSession1);
                ServiceSession.runAs(() -> {
                    Assert.assertEquals(ServiceSession.getCurrentIdentity(), serviceSession3);
                    Assert.assertEquals(ServiceSession.getCurrentSession(), serviceSession1);
                }, serviceSession3);
            }, serviceSession2);

            Assert.assertEquals(ServiceSession.getCurrentIdentity(), ServiceSession.getCurrentSession());
        }, serviceSession1);
    }

    @Test
    public void testBody() {
        UUID id1 = UUID.randomUUID();
        ServiceSession serviceSession1 = new ServiceSession(id1) {
            @Override
            public Map<String, Object> getBody() {
                return Map.of("field", "test");
            }
        };

        UUID id2 = UUID.randomUUID();
        ServiceSession serviceSession2 = new ServiceSession(id2);

        UUID id3 = UUID.randomUUID();
        ServiceSession serviceSession3 = new ServiceSession(id3);

        Service.run(() -> {
            Assert.assertEquals(ServiceSession.getCurrentIdentity(), serviceSession1);
            Assert.assertEquals(ServiceSession.getCurrentSession(), serviceSession1);

            ServiceSession.runAs(() -> {
                Assert.assertEquals(ServiceSession.getCurrentIdentity(), serviceSession2);
                Assert.assertEquals(ServiceSession.getCurrentSession(), serviceSession1);
                Assert.assertTrue(!ServiceSession.getCurrentIdentity().getBody().isEmpty());
            }, serviceSession2);

            Assert.assertEquals(ServiceSession.getCurrentIdentity(), serviceSession1);
        }, serviceSession1);

        Service.run(() -> {
            ServiceSession.runAs(() -> {
                Assert.assertTrue(!ServiceSession.getCurrentIdentity().getBody().isEmpty());
            }, serviceSession1);
        }, serviceSession2);

        Service.run(() -> {
            ServiceSession.runAs(() -> {
                ServiceSession.runAs(() -> {
                    Assert.assertTrue(!ServiceSession.getCurrentIdentity().getBody().isEmpty());
                }, serviceSession3);
            }, serviceSession1);
        }, serviceSession2);
    }
}
