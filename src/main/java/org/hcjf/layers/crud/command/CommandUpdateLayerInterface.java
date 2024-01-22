package org.hcjf.layers.crud.command;

import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.Layers;
import org.hcjf.utils.Introspection;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Executes commands to update a resource through the REST interface.<br/>
 * When a resource layer implements this interface, it is able to receive PUT requests
 * with the _command or _commands field, specifying an action
 * and a payload.
 * The default implementation looks for a layer with implementation name Resource::command.
 * To provide such a layer, extend the ResourceCommandLayer class. Each of these layers should implement
 * a single command and return the corresponding result.
 * Methods in this interface can be overridden to provide a different command handling algorithm.
 */
public interface CommandUpdateLayerInterface extends LayerInterface {

    String INSTANCE_ID = "__instanceId__";

    /**
     * Processes the _command field in a PUT request. Content should be an object
     * complying with CommandRequestModel
     * @param command the parsed content of the command
     * @return the command's result
     */
    default Map<String, Object> executeUpdateCommand(CommandRequestModel command) {
        String resourceName = getImplName();
        ResourceCommandLayerInterface resourceCommand = Layers.get(ResourceCommandLayerInterface.class,
                String.format("%s::%s", resourceName, command.getCommand()));
        Map<String,Object> payload = command.getPayload();
        payload.put(INSTANCE_ID, command.getInstanceId());
        return resourceCommand.execute(command.getPayload());
    }

    /**
     * Returns the id of the instance to be modified.
     * @param payload Payload instance.
     * @return Instance id.
     * @param <O> Expected data type.
     */
    default <O> O getInstanceId(Map<String,Object> payload) {
        return Introspection.resolve(payload, INSTANCE_ID);
    }

    /**
     * Processes de _commands field in a PUT request. Content should be an array
     * of objects complying with CommandRequestModel
     * @param commands the parsed array of commands
     * @return a list of results, in the same order as the commands were specified in the request
     */
    default Collection<Map<String, Object>> executeUpdateCommands(Collection<CommandRequestModel> commands) {
        return commands.stream().map(this::executeUpdateCommand).collect(Collectors.toList());
    }

}
