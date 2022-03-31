package org.hcjf.layers.crud.command;

import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.Layers;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Executes commands to update a resource through the REST interface.<br/>
 * When a resource layer implements this interface, it is able to receive PUT requests
 * with the <code>_command</code> or <code>_commands</code> field, specifying an action
 * and a payload.<br/>
 * The default implementation looks for a layer with implementation name <code>Resource::command</code>.
 * To provide such a layer, extend the ResourceCommandLayer class. Each of these layers should implement
 * a single command and return the corresponding result.<br/>
 * Methods in this interface can be overridden to provide a different command handling algorithm.
 */
public interface CommandUpdateLayerInterface extends LayerInterface {

    /**
     * Processes the <code>_command</code> field in a PUT request. Content should be an object
     * complying with <code>CommandRequestModel</code>
     * @param command the parsed content of the command
     * @return the command's result
     */
    default Map<String, Object> executeUpdateCommand(CommandRequestModel command) {
        String resourceName = getImplName();
        ResourceCommandLayerInterface resourceCommand = Layers.get(ResourceCommandLayerInterface.class,
                String.format("%s::%s", resourceName, command.getCommand()));
        return resourceCommand.execute(command.getPayload());
    }

    /**
     * Processes de <code>_commands</code> field in a PUT request. Content should be an array
     * of objects complying with <code>CommandRequestModel</code>
     * @param commands the parsed array of commands
     * @return a list of results, in the same order as the commands were specified in the request
     */
    default Collection<Map<String, Object>> executeUpdateCommands(Collection<CommandRequestModel> commands) {
        return commands.stream().map(this::executeUpdateCommand).collect(Collectors.toList());
    }

}
