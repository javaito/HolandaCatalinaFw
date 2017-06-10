package org.hcjf.service.grants;

import org.hcjf.service.ServiceSession;

/**
 * @author javaito
 */
public final class SessionNoGrantException extends RuntimeException {

    private final ServiceSession serviceSession;
    private final Grant grant;

    public SessionNoGrantException(ServiceSession serviceSession, Grant grant) {
        this.serviceSession = serviceSession;
        this.grant = grant;
    }

    /**
     * Returns the session that not have the grant.
     * @return Service session.
     */
    public ServiceSession getServiceSession() {
        return serviceSession;
    }

    /**
     * Returns the grant that is not into the session.
     * @return Grant instance.
     */
    public Grant getGrant() {
        return grant;
    }
}
