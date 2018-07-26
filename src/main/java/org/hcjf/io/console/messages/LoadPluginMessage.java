package org.hcjf.io.console.messages;

import org.hcjf.io.net.messages.Message;

/**
 * This message contains the plugin file.
 * @author javaito
 */
public class LoadPluginMessage extends Message {

    private byte[] pluginFile;

    /**
     * Returns the plugin file
     * @return Plugin file.
     */
    public byte[] getPluginFile() {
        return pluginFile;
    }

    /**
     * Set the plugin file.
     * @param pluginFile Plugin file.
     */
    public void setPluginFile(byte[] pluginFile) {
        this.pluginFile = pluginFile;
    }
}
