package org.hcjf.io.net.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLEngine;

public abstract class NioSSLProvider extends SSLProvider {

   private final ByteBuffer buffer = ByteBuffer.allocate(32 * 1024);
   private final SelectionKey key;

   public NioSSLProvider(SelectionKey key, SSLEngine engine, int bufferSize, Executor ioWorker, Executor taskWorkers) {
      super(engine, bufferSize, ioWorker, taskWorkers);
      this.key = key;
   }
   
   @Override
   public void onOutput(ByteBuffer encrypted) {
      try {
         ((WritableByteChannel) this.key.channel()).write(encrypted);
      }
      catch (IOException exc) {
         throw new IllegalStateException(exc);
      }
   }

   public boolean processInput() {
	  buffer.clear();
      int bytes;
      try {
         bytes = ((ReadableByteChannel) this.key.channel()).read(buffer);
      }
      catch (IOException ex) {
         bytes = -1;
      }
      if (bytes == -1) {
         return false;
      }
      buffer.flip();
      ByteBuffer copy = ByteBuffer.allocate(bytes);
      copy.put(buffer);
      copy.flip();
      this.notify(copy);
      return true;
   }
}