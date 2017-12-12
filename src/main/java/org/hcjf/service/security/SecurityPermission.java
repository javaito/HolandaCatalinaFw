package org.hcjf.service.security;

import java.security.Permission;
import java.util.HashMap;
import java.util.Map;

/**
 * @author javaito.
 */
public final class SecurityPermission extends Permission {

    private static final String ID_CONCAT = "@";
    private static final Map<String,SecurityPermission> permissions;

    static {
        permissions = new HashMap<>();
    }

    private SecurityPermission(String name) {
        super(name);
    }

    @Override
    public boolean implies(Permission permission) {
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

    public static String createPermissionId(String className, String permissionName) {
        return className + ID_CONCAT + permissionName;
    }

    public static SecurityPermission getPermission(String className, String permissionName) {
        SecurityPermission result;
        String permissionId = createPermissionId(className, permissionName);
        if(permissions.containsKey(permissionId)) {
            result = permissions.get(permissionId);
        } else {
            synchronized (permissions) {
                result = permissions.get(permissionId);
                if (result == null) {
                    result = new SecurityPermission(permissionId);
                    permissions.put(permissionId, result);
                }
            }
        }
        return result;
    }
}
