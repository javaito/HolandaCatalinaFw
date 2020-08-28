package org.hcjf.service.security;

import java.util.HashMap;
import java.util.List;
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

    /**
     * Creates a identifier using the name of the class that publish the permission
     * and the name of permission.
     * @param className Class that publish the permission.
     * @param permissionName Name of the permission.
     * @return Returns the identifier created.
     */
    private static String createPermissionId(String className, String permissionName) {
        return className + ID_CONCAT + permissionName;
    }

    /**
     * Publish a new permission.
     * @param targetClass Class that publish the permission.
     * @param permissionName Name of the permission.
     * @return Returns the instance of the new permission created.
     */
    public static SecurityPermission publishPermission(Class targetClass, String permissionName, String title, String description, List<String> tags) {
        return createPermission(targetClass.getName(), permissionName, title, description, tags);
    }

    /**
     * Creates the permission instance.
     * @param className Name of the class that publish the permission.
     * @param permissionName Name of the permission.
     * @return Returns the instance of the permission created.
     */
    private static SecurityPermission createPermission(String className, String permissionName, String title, String description, List<String> tags) {
        String permissionId = createPermissionId(className, permissionName);
        SecurityPermission permission = new SecurityPermission(permissionId, className, title, permissionName, description, tags);
        permissions.put(permissionId, permission);

        Grants.publishGrant(permission);
        return permission;
    }

    /**
     * Checks if the current identity contains the grants for the specific permission.
     * @param targetClass Class that publish the permission.
     * @param permissionName Name of the permission.
     */
    public static void checkPermission(Class targetClass, String permissionName) {
        System.getSecurityManager().checkPermission(
                SecurityPermissions.getPermission(
                        targetClass.getName(), permissionName
                ));
    }

    /**
     * Checks if the current identity contains the grants for the specific permission, if the
     * permission is granted then is executed the attached action.
     * @param targetClass Class that publish the permission.
     * @param permissionName Name of the permission.
     * @param action Action to execute if the permission is granted.
     */
    public static void checkPermission(Class targetClass, String permissionName, GrantedAction action) {
        try {
            System.getSecurityManager().checkPermission(
                    SecurityPermissions.getPermission(
                            targetClass.getName(), permissionName));
            action.onAction();
        } catch (SecurityException ex) {}
    }

    /**
     * Returns the instance of a permission indexed by the id created for the class name and the permission name.
     * @param className Name of the class that publish the permission.
     * @param permissionName Name of the permission.
     * @return Returns the permission instance.
     */
    private static SecurityPermission getPermission(String className, String permissionName) {
        return permissions.get(createPermissionId(className, permissionName));
    }

    /**
     * Class that represents a permission.
     */
    public static final class SecurityPermission extends java.security.Permission {

        private final String targetClassName;
        private final String permissionName;
        private final String title;
        private final String description;
        private final List<String> tags;

        private SecurityPermission(String name, String targetClassName, String permissionName,
                                   String title, String description, List<String> tags) {
            super(name);
            this.targetClassName = targetClassName;
            this.permissionName = permissionName;
            this.title = title;
            this.description = description;
            this.tags = tags;
        }

        /**
         * Returns the name of the target class.
         * @return Name of the target class.
         */
        public String getTargetClassName() {
            return targetClassName;
        }

        /**
         * Returns the permission's name.
         * @return Permission's name.
         */
        public String getPermissionName() {
            return permissionName;
        }

        /**
         * Returns the title of the permission.
         * @return Title
         */
        public String getTitle() {
            return title;
        }

        /**
         * Returns the description of the permission.
         * @return Permission description.
         */
        public String getDescription() {
            return description;
        }

        /**
         * Returns the list of tags.
         * @return List of tags
         */
        public List<String> getTags() {
            return tags;
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
