package org.hcjf.service.grants;

import org.hcjf.cloud.Cloud;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.ServiceSession;
import org.hcjf.utils.Introspection;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * System grant representation.
 * @author javaito
 */
public final class Grant {

    private static final Integer STACK_TRACE_START_ELEMENT = 2;
    private static final String ID_CONCAT = "@";

    private static GrantsMap grantsMap;

    static {
        if(SystemProperties.getBoolean(SystemProperties.Grant.CLOUD_DEPLOYMENT)) {
            grantsMap = new CloudMap();
        } else {
            grantsMap = new LocalMap();
        }
    }

    private final String grantId;
    private final String grantClass;
    private final String grantName;

    private Grant(String grantId, String grantClass, String grantName) {
        this.grantId = grantId;
        this.grantClass = grantClass;
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
     * Returns the class name that publish the grant.
     * @return Class name.
     */
    public String getGrantClass() {
        return grantClass;
    }

    /**
     * Returns the grant name.
     * @return Grant name.
     */
    public String getGrantName() {
        return grantName;
    }

    /**
     * Verify if the grant is into the current session or the current session
     * is the instance of system session.
     * @param grant Grant instance to verify.
     */
    public static void validateGrant(Grant grant) {
        grant = Objects.requireNonNull(grant, "Grant null");
        if(!ServiceSession.getCurrentIdentity().isSystemSession() &&
                !ServiceSession.getCurrentIdentity().getGrants().contains(grant)){
            throw new SessionNoGrantException(ServiceSession.getCurrentIdentity(), grant);
        }
    }

    /**
     * Publish a new grant into the envelope of the caller class.
     * The grant gonna be published into the first non abstract class of the stack trace.
     * @param grantName Grant name.
     * @return Returns the instance of the new grant or null if the grant exist.
     */
    public static synchronized Grant publishGrant(String grantName) {
        Grant grant = null;

        //Get the previous class that was called before this method.
        Integer index = STACK_TRACE_START_ELEMENT;
        StackTraceElement previousCall;
        Class clazz = null;
        do {
            previousCall = Thread.currentThread().getStackTrace()[index++];
            try {
                clazz = Class.forName(previousCall.getClassName());
            } catch (ClassNotFoundException e) {
            }
        } while(clazz != null && Modifier.isAbstract(clazz.getModifiers()));

        String className = previousCall.getClassName();
        String grantId = createGrantId(className, grantName);

        if(!grantsMap.contains(grantId)) {
            grant = new Grant(grantId, className, grantName);
            grantsMap.put(grant);
        } else {
            Log.w(SystemProperties.get(SystemProperties.get(SystemProperties.Grant.LOG_TAG)), "Duplicate grant %s", grantId);
        }

        return grant;
    }

    /**
     * Creates the id of the grant.
     * @param className Class name.
     * @param grantName Grant name.
     * @return Returns the grant id.
     */
    private static String createGrantId(String className, String grantName) {
        return className + ID_CONCAT + grantName;
    }

    /**
     * This interface groups the local and cloud implementation of map.
     */
    private interface GrantsMap {

        /**
         * Add a grant into the map.
         * @param grant Grant instance.
         */
        void put(Grant grant);

        /**
         * Returns the the grant instance indexed by the specific id.
         * @param grantId Grant id.
         * @return Grant instance.
         */
        Grant get(String grantId);

        /**
         * Verify if the grant with the specific id exist into the map.
         * @param grantId Specific id.
         * @return Returns true if the id exist into the map.
         */
        boolean contains(String grantId);

    }

    /**
     * Map implementation for a local solution.
     */
    private static class LocalMap implements GrantsMap {

        private final Map<String, Grant> grants;

        public LocalMap() {
            grants = new HashMap<>();
        }

        /**
         * Add a grant into the map.
         * @param grant Grant instance.
         */
        @Override
        public void put(Grant grant) {
            grants.put(grant.getGrantId(), grant);
        }

        /**
         * Returns the the grant instance indexed by the specific id.
         * @param grantId Grant id.
         * @return Grant instance.
         */
        @Override
        public Grant get(String grantId) {
            return grants.get(grantId);
        }

        /**
         * Verify if the grant with the specific id exist into the map.
         * @param grantId Specific id.
         * @return Returns true if the id exist into the map.
         */
        @Override
        public boolean contains(String grantId) {
            return grants.containsKey(grantId);
        }
    }

    /**
     * Map implementation for a cloud solution.
     */
    private static class CloudMap implements GrantsMap {

        private final Map<String, Map<String, Object>> grants;

        public CloudMap() {
            grants = Cloud.getMap(SystemProperties.get(SystemProperties.Grant.CLOUD_MAP_NAME));
        }

        /**
         * Add a grant into the map.
         * @param grant Grant instance.
         */
        @Override
        public void put(Grant grant) {
            grants.put(grant.getGrantId(), Introspection.toMap(grant));
        }

        /**
         * Returns the the grant instance indexed by the specific id.
         * @param grantId Grant id.
         * @return Grant instance.
         */
        @Override
        public Grant get(String grantId) {
            Grant result = null;
            try {
                result = Introspection.toInstance(grants.get(grantId), Grant.class);
            } catch (Exception e) {}
            return result;
        }

        /**
         * Verify if the grant with the specific id exist into the map.
         * @param grantId Specific id.
         * @return Returns true if the id exist into the map.
         */
        @Override
        public boolean contains(String grantId) {
            return grants.containsKey(grantId);
        }
    }
}
