package org.hcjf.service.security;

import java.util.*;

/**
 * @author javaito
 */
public final class Grants {

    private static final Map<String,Grant> grants;

    static {
        grants = new HashMap<>();
    }

    public static void publishGrant(Class targetClass, String permissionName) {
        Objects.requireNonNull(targetClass, "Null target class");
        Objects.requireNonNull(permissionName, "Null permission name");

        Grant grant = new Grant(targetClass, permissionName);
        grants.put(grant.getGrantId(), grant);
    }

    public static Grant getGrant(String grantId) {
        return grants.get(grantId);
    }

    public static Collection<Grant> getGrants() {
        return Collections.unmodifiableCollection(grants.values());
    }
}
