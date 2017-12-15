package org.hcjf.service.security;

import java.util.*;

/**
 * @author javaito
 */
public final class Grants {

    private static final Map<String, Map<String,Grant>> grants;

    static {
        grants = new HashMap<>();
    }

    public synchronized static void publishGrant(SecurityPermissions.SecurityPermission permission) {
        Grant grant = new Grant(permission.getName(), permission.getTargetClassName(), permission.getPermissionName());
        Map<String, Grant> grantsByClass = grants.get(permission.getTargetClassName());
        if(grantsByClass == null) {
            grantsByClass = new HashMap<>();
            grants.put(permission.getTargetClassName(), grantsByClass);
        }
        grantsByClass.put(grant.getGrantName(), grant);
    }

    public static Grant getGrant(Class targetClass, String permissionName) {
        Grant result = null;
        Map<String, Grant> grantsByClass = grants.get(targetClass.getName());
        if(grantsByClass != null) {
            result = grantsByClass.get(permissionName);
        }
        return result;
    }

    public static Collection<Grant> getGrants() {
        Collection<Grant> result = new ArrayList<>();
        for(Map<String, Grant> grantsByClass : grants.values()) {
            result.addAll(grantsByClass.values());
        }
        return result;
    }

    /**
     * System grant representation.
     * @author javaito
     */
    public static final class Grant {

        private final String permissionId;
        private final String targetClassName;
        private final String grantName;

        private Grant(String permissionId, String targetClassName, String grantName) {
            this.permissionId = permissionId;
            this.targetClassName = targetClassName;
            this.grantName = grantName;
        }

        /**
         * Returns the id of the grant.
         * @return Id of the grant.
         */
        public String getPermissionId() {
            return permissionId;
        }

        /**
         * Returns the target class name.
         * @return Target class name.
         */
        public String getTargetClassName() {
            return targetClassName;
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
            return getPermissionId();
        }

        @Override
        public boolean equals(Object obj) {
            boolean result = false;
            if(obj instanceof Grant) {
                result = super.equals(obj);
            } else if(obj instanceof String) {
                result = Objects.equals(getPermissionId(), obj);
            }
            return result;
        }
    }
}
