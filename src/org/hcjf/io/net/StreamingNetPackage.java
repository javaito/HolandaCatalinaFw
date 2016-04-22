package org.hcjf.io.net;

/**
 * This package extension contains a streaming source.
 * @author javaito
 * @email javaito@gmail.com
 */
public class StreamingNetPackage extends NetPackage {

    private final NetStreamingSource source;

    public StreamingNetPackage(String remoteHost, String remoteAddress, int remotePort,
                               int localPort, byte[] payload, NetStreamingSource source) {
        super(remoteHost, remoteAddress, remotePort, localPort, payload, ActionEvent.STREAMING);
        this.source = source;
    }

    /**
     * Return the data source of the streaming.
     * @return Data source.
     */
    public NetStreamingSource getSource() {
        return source;
    }
}
