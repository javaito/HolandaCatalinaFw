package org.hcjf.io.console.messages;

import org.hcjf.io.net.messages.Message;

import java.util.Map;

/**
 * This message contains the login information.
 * @author javaito
 */
public class LoginMessage extends Message {

    private Map<String,Object> parameters;

    /**
     * Returns the login parameters.
     * @return Login parameters.
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Set the login parameters.
     * @param parameters Login parameters.
     */
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
