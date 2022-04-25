package org.hcjf.service.security;

import org.hcjf.io.net.http.HttpResponseCode;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;
import org.hcjf.service.ServiceThread;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

/**
 * @author javaito
 */
public class ServiceSecurityManager extends SecurityManager {

    private static final String GRANT_NOT_FOUND_TEMPLATE = "$@{USER}Grant not found %s into service session %s";
    private static final String SECURITY_EXCEPTION_MESSAGE = "Security exception";
    private static final String TAG_RESPONSE_CODE = "$@{RESPONSE_CODE}";
    @Override
    public void checkPermission(Permission perm) {
        if(perm instanceof SecurityPermissions.SecurityPermission) {
            ServiceSession serviceSession;
            try {
                serviceSession = ServiceSession.getCurrentIdentity();
            } catch (Exception ex) {
                throw new SecurityException(SECURITY_EXCEPTION_MESSAGE, ex);
            }
            if (!serviceSession.isSystemSession() &&
                    !serviceSession.containsGrant(perm.getName())) {
                String message = String.format(GRANT_NOT_FOUND_TEMPLATE,
                        perm.getName(), serviceSession.getId());
                Throwable throwable = new Throwable(message, new Throwable(TAG_RESPONSE_CODE.concat(String.valueOf(HttpResponseCode.FORBIDDEN))));
                throw new SecurityException(throwable);
            }
        }
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
    }

    @Override
    public void checkCreateClassLoader() {
    }

    @Override
    public void checkAccess(Thread thread) {
        if(Thread.currentThread() instanceof ServiceThread) {
            ServiceSession session = ServiceSession.getCurrentIdentity();
            if(session != null) {
                ServiceSession.LayerStackElement element = session.getCurrentLayer();
                if (element != null) {
                    if (element.isPlugin()) {
                        if(!(thread instanceof Service.StaticServiceThread)) {
                            throw new SecurityException("Unable to manipulate a thread into a plugin layer");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void checkAccess(ThreadGroup g) {
    }

    @Override
    public void checkExit(int status) {
    }

    @Override
    public void checkExec(String cmd) {
    }

    @Override
    public void checkLink(String lib) {

    }

    @Override
    public void checkRead(FileDescriptor fd) {
    }

    @Override
    public void checkRead(String file) {
    }

    @Override
    public void checkRead(String file, Object context) {
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
    }

    @Override
    public void checkWrite(String file) {
    }

    @Override
    public void checkDelete(String file) {
    }

    @Override
    public void checkConnect(String host, int port) {
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
    }

    @Override
    public void checkListen(int port) {
    }

    @Override
    public void checkAccept(String host, int port) {
    }

    @Override
    public void checkMulticast(InetAddress maddr) {
    }

    @Override
    public void checkPropertiesAccess() {
    }

    @Override
    public void checkPropertyAccess(String key) {
    }

    @Override
    public void checkPrintJobAccess() {
    }

    @Override
    public void checkPackageAccess(String pkg) {
    }

    @Override
    public void checkPackageDefinition(String pkg) {
    }

    @Override
    public void checkSecurityAccess(String target) {
    }
}
