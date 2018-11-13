package org.hcjf.log.debug;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * Class with some static useful methods to register and search information
 * about agents.
 * @author javaito
 */
public class Agents {

    public static final String OBJECT_NAME_TEMPLATE = "%s:type=%s";

    /**
     * Registers an agent into the MBean server of the instance of jvm.
     * @param agent Agent to register.
     */
    public static void register(Agent agent) {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName(String.format(OBJECT_NAME_TEMPLATE,
                    agent.getPackageName(), agent.getName()));
            mbs.registerMBean(agent, name);
        } catch (Exception ex){}
    }

}
