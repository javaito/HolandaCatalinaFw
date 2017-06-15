package org.hcjf.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author javaito
 */
public class NamedUuidTest {

    @Test
    public void testNamedUudi() {
        String name = getClass().getName();
        Integer hashCode = name.hashCode();

        NamedUuid namedUuid = NamedUuid.create(name);
        Assert.assertEquals(namedUuid.getHash(), hashCode);

        NamedUuid namedUuidFromUuid = NamedUuid.create(namedUuid.getId());
        Assert.assertEquals(namedUuidFromUuid.getHash(), hashCode);
    }

}
