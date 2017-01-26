package org.hcjf.io.net.ssl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;

public class NioSSLExample
{
   public static void main(String[] args) throws Exception
   {
      InetSocketAddress address = new InetSocketAddress("www.amazon.com", 443);
      Selector selector = Selector.open();
      SocketChannel channel = SocketChannel.open();
      channel.connect(address);
      channel.configureBlocking(false);
      int ops = SelectionKey.OP_CONNECT | SelectionKey.OP_READ;

      SelectionKey key =  channel.register(selector, ops);
      
      // create the worker threads
      final Executor ioWorker = Executors.newSingleThreadExecutor();
      final Executor taskWorkers = Executors.newFixedThreadPool(2);

      // create the SSLEngine
      final SSLEngine engine = SSLContext.getDefault().createSSLEngine();
      engine.setUseClientMode(true);
      engine.beginHandshake();
      final int ioBufferSize = 32 * 1024;
      final NioSSLProvider ssl = new NioSSLProvider(key, engine, ioBufferSize, ioWorker, taskWorkers)
      {
         @Override
         public void onFailure(Exception ex)
         {
            System.out.println("handshake failure");
            ex.printStackTrace();
         }

         @Override
         public void onSuccess()
         {
            System.out.println("handshake success");
            SSLSession session = engine.getSession();
            try
            {
               System.out.println("local principal: " + session.getLocalPrincipal());
               System.out.println("remote principal: " + session.getPeerPrincipal());
               System.out.println("cipher: " + session.getCipherSuite());
            }
            catch (Exception exc)
            {
               exc.printStackTrace();
            }

            //HTTP request
            StringBuilder http = new StringBuilder();
            http.append("GET / HTTP/1.0\r\n");
            http.append("Connection: close\r\n");
            http.append("\r\n");
            byte[] data = http.toString().getBytes();
            ByteBuffer send = ByteBuffer.wrap(data);
            this.sendAsync(send);
         }

         private AtomicInteger counter = new AtomicInteger(0);

         @Override
         public void onInput(ByteBuffer decrypted)
         {
            byte[] dst = new byte[decrypted.remaining()];
            decrypted.get(dst);
            String response = new String(dst);
            System.out.print(response);
            System.out.flush();
         }

         @Override
         public void onClosed()
         {
            System.out.println("ssl session closed");
         }
      };

      // NIO selector
      while (true)
      {
         key.selector().select();
         Iterator<SelectionKey> keys = key.selector().selectedKeys().iterator();
         while (keys.hasNext())
         {
            keys.next();
            keys.remove();
            ssl.processInput();
         }
      }
   }
}
