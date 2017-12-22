package org.hcjf.log.debug;

/**
 * This class represents an agent that collect and store data from different
 * points of the platform in order to monitoring and debug the instance status
 * @author javaito
 */
public class Agent {

    private final String packageName;
    private final String name;

    public Agent(String name, String packageName) {
        this.packageName = packageName;
        this.name = name;
    }

    /**
     * Returns the package name of the agent.
     * @return Package name of the agent.
     */
    public final String getPackageName() {
        return packageName;
    }

    /**
     * Returns the name if the agent.
     * @return Name of the agent.
     */
    public final String getName() {
        return name;
    }
}
