package org.hcjf.service.security;

import java.util.HashMap;
import java.util.Map;

/**
 * @author javaito.
 */
public class SecurityPermissions {

    private static final String ID_CONCAT = "@";
    private static final Map<String,SecurityPermission> permissions;

    static {
        permissions = new HashMap<>();
    }

    private static String createPermissionId(String className, String permissionName) {
        return className + ID_CONCAT + permissionName;
    }

    public static SecurityPermission publishPermission(Class targetClass, String permissionName) {
        return createPermission(targetClass.getName(), permissionName);
    }

    private static SecurityPermission createPermission(String className, String permissionName) {
        String permissionId = createPermissionId(className, permissionName);
        SecurityPermission permission = new SecurityPermission(permissionId, className, permissionName);
        permissions.put(permissionId, permission);

        Grants.publishGrant(permission);
        return permission;
    }

    public static void checkPermission(Class targetClass, String permissionName) {
        System.getSecurityManager().checkPermission(
                SecurityPermissions.getPermission(
                        targetClass.getName(), permissionName
                ));
    }

    public static void checkPermission(Class targetClass, String permissionName, GrantedAction action) {
        try {
            System.getSecurityManager().checkPermission(
                    SecurityPermissions.getPermission(
                            targetClass.getName(), permissionName));
            action.onAction();
        } catch (SecurityException ex) {}
    }

    private static SecurityPermission getPermission(String className, String permissionName) {
        return permissions.get(createPermissionId(className, permissionName));
    }

    public static final class SecurityPermission extends java.security.Permission {

        private final String targetClassName;
        private final String permissionName;

        private SecurityPermission(String name, String targetClassName, String permissionName) {
            super(name);
            this.targetClassName = targetClassName;
            this.permissionName = permissionName;
        }

        public String getTargetClassName() {
            return targetClassName;
        }

        public String getPermissionName() {
            return permissionName;
        }

        @Override
        public boolean implies(java.security.Permission permission) {
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            boolean result = false;
            if(obj instanceof SecurityPermission) {
                result = getName().equals(((SecurityPermission)obj).getName());
            }
            return result;
        }

        @Override
        public int hashCode() {
            return getName().hashCode();
        }

        @Override
        public String getActions() {
            return null;
        }

    }

    public interface GrantedAction {

        void onAction();

    }
}
