package org.hcjf.io.net.http;

import org.hcjf.properties.SystemProperties;

import java.util.UUID;

/**
 * This class is a factory in order to implements a method to create and organize
 * the http sessions.
 * @author javaito
 * @email javaito@gmail.com
 */
public interface HttpSessionFactory {

    public HttpSessionFactory DEFAULT = new DefaultHttpSessionFactory();

    /**
     * This method must create an instance of the http session based on
     * the payload and the package.
     * @param server Http server associated to the factory.
     * @param request Net package that represents a http request.
     * @return Return a http session instance.
     */
    public HttpSession createSession(HttpServer server, HttpRequest request);

    /**
     * This class is a default session factory implementation.
     */
    public static class DefaultHttpSessionFactory implements HttpSessionFactory {

        private DefaultHttpSessionFactory () {
        }

        /**
         * Default http session factory creation.
         * @param server Http server associated to the factory.
         * @param httpPackage Net package that represents a http request.
         * @return Default http session factory.
         */
        @Override
        public HttpSession createSession(HttpServer server, HttpRequest httpPackage) {
            return new HttpSession(UUID.randomUUID(),
                    SystemProperties.get(SystemProperties.SERVICE_GUEST_SESSION_NAME),
                    server, httpPackage);
        }
    }

}
