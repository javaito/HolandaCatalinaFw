package org.hcjf.service.grants;

import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.ServiceSession;

import java.util.*;

/**
 * System grant representation.
 * @author javaito
 */
public final class Grant {

    private static final Map<String,Set<Grant>> grantsByClassName;
    private static final Map<String,Grant> grantsById;

    static {
        grantsByClassName = new HashMap<>();
        grantsById = new HashMap<>();
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
     * @param grantName Grant name.
     * @return Returns the instance of the new grant or null if the grant exist.
     */
    public static synchronized Grant publishGrant(String grantName) {
        Grant grant = null;

        //Get the previous class that was called before this method.
        StackTraceElement previosCall = Thread.currentThread().getStackTrace()[2];

        String className = previosCall.getClassName();
        String grantId = createGrantId(className, grantName);

        if(!grantsById.containsKey(grantId)) {
            grant = new Grant(grantId, className, grantName);
            Set<Grant> grants = grantsByClassName.get(className);
            if(grants == null) {
                //Is the first grant for this class name
                grants = new HashSet<>();
                grantsByClassName.put(className, grants);
            }
            grants.add(grant);
            grantsById.put(grantId, grant);
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
        return className + "@" + grantName;
    }
}
