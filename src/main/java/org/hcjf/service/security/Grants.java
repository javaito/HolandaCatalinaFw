package org.hcjf.service.security;

import java.util.*;

/**
 * @author javaito
 */
public final class Grants {

    private static final Map<String, Grant> grantsById;
    private static final Map<String, Map<String,Grant>> grants;

    static {
        grantsById = new HashMap<>();
        grants = new HashMap<>();
    }

    /**
     * Publish the new grant associated to the permission as parameter.
     * @param permission Permission instance as parameter.
     */
    public synchronized static void publishGrant(SecurityPermissions.SecurityPermission permission) {
        Grant grant = new Grant(permission.getName(), permission.getTargetClassName(), permission.getPermissionName());
        Map<String, Grant> grantsByClass = grants.get(permission.getTargetClassName());
        if(grantsByClass == null) {
            grantsByClass = new HashMap<>();
            grants.put(permission.getTargetClassName(), grantsByClass);
        }
        grantsByClass.put(grant.getGrantName(), grant);
        grantsById.put(grant.permissionId, grant);
    }

    /**
     * Returns the grant instance for the specific target class and permission name.
     * @param targetClass Target class.
     * @param permissionName Permission name.
     * @return Returns the grant instance.
     */
    public static Grant getGrant(Class targetClass, String permissionName) {
        Grant result = null;
        Map<String, Grant> grantsByClass = grants.get(targetClass.getName());
        if(grantsByClass != null) {
            result = grantsByClass.get(permissionName);
        }
        return result;
    }

    /**
     * Returns the grant instance for the specific permission id.
     * @param permissionId Permission id.
     * @return Grant instance.
     */
    public static Grant getGrant(String permissionId) {
        return grantsById.get(permissionId);
    }

    /**
     * Returns all the grants stored into the framework instance.
     * @return Collection of grants.
     */
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

        /**
         * Returns the string representation of the grant instance.
         * @return String representation of the grant instance.
         */
        @Override
        public String toString() {
            return getPermissionId();
        }

        /**
         * Verify if the instance if equals to other grant instance.
         * @param obj Objet to compare.
         * @return If the object to compare is instance of the grant call the super
         * implementation of the method, but if the object to compare is instance of
         * string then compare the object with the id of the grant.
         */
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
