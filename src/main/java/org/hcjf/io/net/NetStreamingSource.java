package org.hcjf.io.net;

import org.hcjf.log.Log;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;

/**
 * This class create a bond from a data source to a client
 * using a pipe.
 * @author javaito
 */
public abstract class NetStreamingSource implements Runnable {

    private final long resourceSize;
    private long totalWrite;
    private final ByteBuffer buffer;
    private SocketChannel channel;
    private NetPackage netPackage;
    private NetService service;

    public NetStreamingSource(long resourceSize, int bufferSize) {
        this.resourceSize = resourceSize;
        this.buffer = ByteBuffer.allocate(bufferSize);
    }

    /**
     * Starts the streaming process.
     * @param service Net service.
     * @param channel Represents the client side of the pipe.
     * @param netPackage Net Package.
     */
    public final void init(NetService service, SocketChannel channel, NetPackage netPackage){
        this.service = service;
        this.channel = channel;
        this.netPackage = netPackage;
    }

    /**
     * Base logic of the streaming process.
     */
    @Override
    public void run() {
        try (ReadableByteChannel resourceIn = getSource();){

            int writeSize;
            int readSize = 0;
            try {
                readSize = resourceIn.read(buffer);
            } catch(AsynchronousCloseException ex){}
            while(readSize > 0 && channel.isConnected() && (totalWrite < resourceSize || resourceSize < 0)){
                buffer.flip();

                writeSize = 0;
                while(writeSize < readSize && channel.isConnected()){
                    //Stores the number of written byte
                    writeSize += channel.write(buffer);
                }

                totalWrite += writeSize;

                buffer.rewind();
                buffer.clear();

                readSize = 0;
                try {
                    readSize = resourceIn.read(buffer);
                } catch(AsynchronousCloseException ex){}
            }

            if(!channel.isConnected()) {
                clientConnectionClosed();
            }
        } catch(Exception ex){
            clientConnectionClosed();
            Log.w(NetService.NET_SERVICE_LOG_TAG, "The streaming writing finish unexpectedly", ex);
        }

        done();
    }

    /**
     * This method will be called when the streaming
     * process is finished
     */
    private void done() {
        service.streamingDone(netPackage);
        resourceDone(totalWrite);
    }

    /**
    * This method will called when the streaming process done.
    * @param totalWrite Quantity of the bytes written over the channel.
    */
    public abstract void resourceDone(long totalWrite);

    /**
    * The implementation must provides the data source to the streaming process.
    * @throws java.io.IOException Throws if the source provide fail.
    */
    public abstract ReadableByteChannel getSource() throws Exception;

    /**
    * When the streaming client close the channel this method will be called.
    */
    public void clientConnectionClosed() {}
}
