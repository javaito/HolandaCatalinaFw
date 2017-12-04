package org.hcjf.log.debug;

import org.hcjf.utils.Strings;

/**
 * @author javaito
 */
public class Agent {

    private final String packageName;
    private final String name;

    public Agent(String name, String packageName) {
        this.packageName = packageName;
        this.name = name;
    }

    public final String getPackageName() {
        return packageName;
    }

    public final String getName() {
        return name;
    }
}
