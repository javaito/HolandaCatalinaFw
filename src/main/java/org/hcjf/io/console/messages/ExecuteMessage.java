package org.hcjf.io.console.messages;

import org.hcjf.io.net.messages.Message;

import java.util.List;

/**
 * This message contains the command information.
 * @author javaito
 */
public class ExecuteMessage extends Message {

    private String commandName;
    private List<Object> parameters;

    /**
     * Returns the command name.
     * @return Command name.
     */
    public String getCommandName() {
        return commandName;
    }

    /**
     * Set the command name.
     * @param commandName Command name.
     */
    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    /**
     * Returns the command parameters.
     * @return Command parameters.
     */
    public List<Object> getParameters() {
        return parameters;
    }

    /**
     * Set the command parameters.
     * @param parameters Command parameters.
     */
    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }
}
