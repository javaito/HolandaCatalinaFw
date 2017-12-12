package org.hcjf.service.security;

import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.resources.Resource;
import org.hcjf.utils.Version;

import java.security.Permission;
import java.util.Objects;

/**
 * System grant representation.
 * @author javaito
 */
public final class Grant {

    private final String grantId;
    private final String grantName;

    public Grant(Class targetClass, String grantName) {
        this.grantId = SecurityPermission.createPermissionId(targetClass.getName(), grantName);
        this.grantName = grantName;
    }

    /**
     * Returns the id of the grant.
     * @return Id of the grant.
     */
    public String getGrantId() {
        return grantId;
    }

    /**
     * Returns the grant name.
     * @return Grant name.
     */
    public String getGrantName() {
        return grantName;
    }

    @Override
    public String toString() {
        return getGrantId();
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if(obj instanceof Grant) {
            result = super.equals(obj);
        } else if(obj instanceof String) {
            result = Objects.equals(getGrantId(), obj);
        }
        return result;
    }
}
