package org.hcjf.cloud.impl.messages;

import org.hcjf.io.net.messages.Message;

/**
 * @author javaito
 */
public class PublishPluginMessage extends Message {

    private byte[] jarFile;

    public byte[] getJarFile() {
        return jarFile;
    }

    public void setJarFile(byte[] jarFile) {
        this.jarFile = jarFile;
    }
}
