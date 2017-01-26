package org.hcjf.io.net.ssl;


import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public abstract class SSLProvider implements Runnable {

   final SSLEngine engine;
   final Executor ioWorker, taskWorkers;
   final ByteBuffer clientWrap, clientUnwrap;
   final ByteBuffer serverWrap, serverUnwrap;

   public SSLProvider(SSLEngine engine, int capacity, Executor ioWorker, Executor taskWorkers) {
      this.clientWrap = ByteBuffer.allocate(capacity);
      this.serverWrap = ByteBuffer.allocate(capacity);
      this.clientUnwrap = ByteBuffer.allocate(capacity);
      this.serverUnwrap = ByteBuffer.allocate(capacity);
      this.clientUnwrap.limit(0);
      this.engine = engine;
      this.ioWorker = ioWorker;
      this.taskWorkers = taskWorkers;
      this.ioWorker.execute(this);
   }

   public abstract void onInput(ByteBuffer decrypted);
   public abstract void onOutput(ByteBuffer encrypted);
   public abstract void onFailure(Exception ex);
   public abstract void onSuccess();
   public abstract void onClosed();

   public void sendAsync(final ByteBuffer data) {
      this.ioWorker.execute(new Runnable() {
         @Override
         public void run() {
            clientWrap.put(data);

            SSLProvider.this.run();
         }
      });
   }

   public void notify(final ByteBuffer data) {
      this.ioWorker.execute(new Runnable() {
         @Override
         public void run() {
            clientUnwrap.put(data);
            SSLProvider.this.run();
         }
      });
   }

   public void run() {
      // executes non-blocking tasks on the IO-Worker
      while (this.isHandShaking()) {
         continue;
      }
   }

   private synchronized boolean isHandShaking() {
      switch (engine.getHandshakeStatus()) {
         case NOT_HANDSHAKING: {
            boolean occupied = false;

            if (clientWrap.position() > 0) {
               occupied |= this.wrap();
            }

            if (clientUnwrap.position() > 0) {
               occupied |= this.unwrap();
            }

            return occupied;
         }
         case NEED_WRAP: {
            if (!this.wrap()) {
               return false;
            }
            break;
         }
         case NEED_UNWRAP: {
            if (!this.unwrap()) {
               return false;
            }
            break;
         }
         case NEED_TASK: {
            final Runnable sslTask = engine.getDelegatedTask();
            Runnable wrappedTask = new Runnable() {
               @Override
               public void run() {
                  sslTask.run();
                  ioWorker.execute(SSLProvider.this);
               }
            };
            taskWorkers.execute(wrappedTask);
            return false;
         }
         case FINISHED: {
            throw new IllegalStateException("FINISHED");
         }
      }

      return true;
   }

   private boolean wrap() {
      SSLEngineResult wrapResult;

      try {
         clientWrap.flip();
         wrapResult = engine.wrap(clientWrap, serverWrap);
         clientWrap.compact();
      } catch (SSLException exc) {
         this.onFailure(exc);
         return false;
      }

      switch (wrapResult.getStatus()) {
         case OK: {
            if (serverWrap.position() > 0) {
               serverWrap.flip();
               this.onOutput(serverWrap);
               serverWrap.compact();
            }
            break;
         }
         case BUFFER_UNDERFLOW: {
            // try again later
            break;
         }
         case BUFFER_OVERFLOW: {
            throw new IllegalStateException("failed to wrap");
         }
         case CLOSED: {
            this.onClosed();
            return false;
         }
      }

      return true;
   }

   private boolean unwrap() {
      SSLEngineResult unwrapResult;

      try {
         clientUnwrap.flip();
         unwrapResult = engine.unwrap(clientUnwrap, serverUnwrap);
         clientUnwrap.compact();
      }
      catch (SSLException ex) {
         this.onFailure(ex);
         return false;
      }

      switch (unwrapResult.getStatus()) {
         case OK: {
            if (serverUnwrap.position() > 0) {
               serverUnwrap.flip();
               this.onInput(serverUnwrap);
               serverUnwrap.compact();
            }
            break;
         }
         case CLOSED: {
            this.onClosed();
            return false;
         }
         case BUFFER_OVERFLOW: {
            throw new IllegalStateException("failed to unwrap");
         }
         case BUFFER_UNDERFLOW: {
            return false;
         }
      }

      if (unwrapResult.getHandshakeStatus() == HandshakeStatus.FINISHED) {
            this.onSuccess();
            return false;
      }

      return true;
   }
}
