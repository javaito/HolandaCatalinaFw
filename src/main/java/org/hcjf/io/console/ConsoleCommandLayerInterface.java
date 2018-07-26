package org.hcjf.io.console;

import org.hcjf.layers.LayerInterface;
import org.hcjf.layers.distributed.DistributedLayerInterface;

import java.util.List;

/**
 * @author javaito
 */
public interface ConsoleCommandLayerInterface extends LayerInterface, DistributedLayerInterface {

    /**
     * Command implementation.
     * @param parameters Parameters to execute the command.
     * @return Result of the command execution.
     */
    Object execute(List<Object> parameters);

}
