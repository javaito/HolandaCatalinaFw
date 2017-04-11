package org.hcjf.io.net.http;

import org.hcjf.io.net.NetPackage;
import org.hcjf.properties.SystemProperties;

import java.util.UUID;

/**
 * This class is a factory in order to implements a method to create and organize
 * the http sessions.
 * @author javaito
 * @email javaito@gmail.com
 */
public interface HttpSessionManager {

    public HttpSessionManager DEFAULT = new DefaultHttpSessionManager();

    /**
     * Starts with the http session creation.
     * @param server Http server associated to the factory.
     * @param netPackage Net package before to be decoded to a request object.
     * @return Return a http session instance.
     */
    public HttpSession createSession(HttpServer server, NetPackage netPackage);

    /**
     * This method must update the session with the information into the request.
     * @param session Net session.
     * @param request Request object.
     * @return Http session updated.
     */
    public HttpSession checkSession(HttpSession session, HttpRequest request);

    /**
     * This method must update the session object when the session is destroyed.
     * @param session Net session.
     * @return Http session updated.
     */
    public HttpSession destroySession(HttpSession session);

    /**
     * This class is a default session factory implementation.
     */
    public static class DefaultHttpSessionManager implements HttpSessionManager {

        private DefaultHttpSessionManager() {
        }

        /**
         * Default http session factory creation.
         * @param server Http server associated to the factory.
         * @param netPackage Net package before to be decoded to a request object.
         * @return Default http session factory.
         */
        @Override
        public HttpSession createSession(HttpServer server, NetPackage netPackage) {
            HttpSession result = new HttpSession(UUID.randomUUID(), server);
            result.setSessionName(SystemProperties.get(SystemProperties.Service.GUEST_SESSION_NAME));
            return result;
        }

        /**
         * This method must update the session with the information into the request.
         * @param session Net session.
         * @param request Request object.
         * @return Http session updated.
         */
        @Override
        public HttpSession checkSession(HttpSession session, HttpRequest request) {
            return session;
        }

        /**
         * This method must update the session object when the session is destroyed.
         * @param session Net session.
         * @return Http session updated.
         */
        @Override
        public HttpSession destroySession(HttpSession session) {
            return session;
        }
    }

}
