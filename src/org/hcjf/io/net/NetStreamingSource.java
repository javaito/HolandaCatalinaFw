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
 * @email javaito@gmail.com
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
        try (ReadableByteChannel resourceIn = getResourceChannel();){

            int writeSize;
            int last;
            long lastTime;
            int readSize = 0;
            try {
                readSize = resourceIn.read(buffer);
            } catch(AsynchronousCloseException ex){}
            while(readSize > 0 && channel.isConnected() && (totalWrite < resourceSize || resourceSize < 0)){
                buffer.flip();

                writeSize = 0;
                last = 0;
                lastTime = System.currentTimeMillis();
                while(writeSize < readSize && channel.isConnected()){
                    //Stores the number of written byte
                    last = channel.write(buffer);
                    writeSize += last;

                    //If the number of bytes is greater than cero then
                    //stores the system timestamp to know tha last write of the cycle.
                    if(last > 0){
                        lastTime = System.currentTimeMillis();
                    }
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
    * Este metodo sera llamado cuando se termine la escritura de datos, ya sea
    * normalmente o por un error.
    * @param totalWrite Cantidad de bytes escritos sobre el canal.
    */
    public abstract void resourceDone(long totalWrite);

    /**
    * La implementacion debe proveer el canal de donde se sacaran los datos
    * que se enviaran por streaming al cliente.
    * @return Canal de donde se sacaran los datos.
    * @throws java.io.IOException
    */
    public abstract ReadableByteChannel getResourceChannel() throws Exception;

    /**
    * Este metodo sera llamado cuando la conexion con el cliente se cierre por
    * algun motivo, este metodo puede ser implementado por ejemplo para
    * notificar al productor del streaming que el cliente ya no esta recibiendo
    * los datos.
    */
    public void clientConnectionClosed() {}
}
