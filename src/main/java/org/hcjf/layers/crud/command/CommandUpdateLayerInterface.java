package org.hcjf.layers.crud.command;

import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.Layers;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public interface CommandUpdateLayerInterface extends LayerInterface {

    default Map<String, Object> executeUpdateCommand(CommandRequestModel command) {
        String resourceName = getImplName();
        ResourceCommandLayerInterface resourceCommand = Layers.get(ResourceCommandLayerInterface.class,
                String.format("%s::%s", resourceName, command.getCommand()));
        return resourceCommand.execute(command.getPayload());
    }

    default Collection<Map<String, Object>> executeUpdateCommands(Collection<CommandRequestModel> commands) {
        return commands.stream().map(this::executeUpdateCommand).collect(Collectors.toList());
    }

}
