package org.hcjf.io.fs;

import org.hcjf.log.Log;
import org.hcjf.service.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provide an interface in order to register
 * a watcher for a file or directory.
 * @author javaito
 * @email javaito@gmail.com
 */
public final class FileSystemWatcherService extends Service<FileSystemWatcherConsumer> {

    private WatchService watcher;
    private final FileSystemWatcherThread thread;
    private final Map<WatchKey, FileSystemWatcherConsumer> consumers;

    public FileSystemWatcherService(String serviceName) {
        super(serviceName);
        thread = new FileSystemWatcherThread();
        consumers = new HashMap<>();
    }

    /**
     * This method register the consumer in the service.
     * @param consumer Consumer to register.
     * @throws NullPointerException if the consumer is null.
     */
    @Override
    public void registerConsumer(FileSystemWatcherConsumer consumer) {
        if(consumer == null) {
            throw new NullPointerException("File system consumer null");
        }

        try {
            WatchKey key = consumer.getBasePath().register(watcher, consumer.getEvetKinds());
        } catch (IOException ex) {
            Log.d("Unable to register file system watcher consumer, '$1'", ex, consumer.getBasePath());
        }
    }

    /**
     * Star thre main thread of the service.
     */
    @Override
    protected void init() {
        thread.start();
    }

    /**
     * This thread run all the time and is interrupted
     * by the shutdown call.
     */
    private class FileSystemWatcherThread extends Thread {

        @Override
        public void run() {
            //Init the main watcher, if the watcher init fail
            //this service doesn't start.
            try {
                watcher = FileSystems.getDefault().newWatchService();
            } catch (IOException ex) {
                Log.d("File System Watcher init fail", ex);
            }

            if(watcher != null) {
                while (!isInterrupted()) {
                    WatchKey key;
                    try {
                        key = watcher.take();
                    } catch (InterruptedException x) {
                        continue;
                    }

                    //Find the consumer by key.
                    FileSystemWatcherConsumer consumer = consumers.get(key);

                    if(consumer != null) {
                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();

                            if (kind == StandardWatchEventKinds.OVERFLOW) {
                                consumer.overflow(event);
                            } else if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                consumer.create(event);
                            } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                consumer.update(event);
                            } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                consumer.delete(event);
                            }
                        }

                        boolean valid = key.reset();
                        if (!valid) {
                            Log.d("Inaccessible path '$1', consumer unregistered", consumer.getBasePath());
                        }
                    } else {
                        Log.d("Consumer null");
                    }
                }
            }
        }
    }
}
