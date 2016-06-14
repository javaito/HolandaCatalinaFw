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

    private static final String FILE_SYSTEM_WATCHER_SERVICE_NAME = "File System Watcher Service";
    public static final String FILE_SYSTEM_WATCHER_SERVICE_LOG_TAG = "FILE_SYSTEM_WATCHER_SERVICE";

    private static final FileSystemWatcherService instance;

    static {
        instance = new FileSystemWatcherService();
    }

    private WatchService watcher;
    private FileSystemWatcherThread thread;
    private final Map<WatchKey, FileSystemWatcherConsumer> consumers;

    private FileSystemWatcherService() {
        super(FILE_SYSTEM_WATCHER_SERVICE_NAME);
        consumers = new HashMap<>();
    }

    /**
     * Return the instance of the singleton.
     * @return Instance of the singleton.
     */
    public static final FileSystemWatcherService getInstance() {
        return instance;
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

        synchronized (this) {
            while(watcher == null) {
                try {
                    Log.d(FILE_SYSTEM_WATCHER_SERVICE_LOG_TAG, "Waiting file system watcher init");
                    wait(5000);
                } catch (InterruptedException e) {}
            }
        }

        try {
            WatchKey key = consumer.getBasePath().register(watcher, consumer.getEventKinds());
            consumers.put(key, consumer);
        } catch (IOException ex) {
            Log.d(FILE_SYSTEM_WATCHER_SERVICE_LOG_TAG,
                    "Unable to register file system watcher consumer, '$1'", ex, consumer.getBasePath());
        }
    }

    /**
     * Star thre main thread of the service.
     */
    @Override
    protected void init() {
        thread = new FileSystemWatcherThread();
        thread.start();
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            Log.w(FILE_SYSTEM_WATCHER_SERVICE_LOG_TAG, "Unable to start file system watcher service");
        }
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
                synchronized (FileSystemWatcherService.this) {
                    FileSystemWatcherService.this.notifyAll();
                }
            } catch (IOException ex) {
                Log.d(FILE_SYSTEM_WATCHER_SERVICE_LOG_TAG, "File System Watcher init fail", ex);
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
                                consumer.overflow((WatchEvent<Path>) event);
                            } else if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                consumer.create((WatchEvent<Path>) event);
                            } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                consumer.update((WatchEvent<Path>) event);
                            } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                consumer.delete((WatchEvent<Path>) event);
                            }
                        }

                        boolean valid = key.reset();
                        if (!valid) {
                            Log.d(FILE_SYSTEM_WATCHER_SERVICE_LOG_TAG,
                                    "Inaccessible path '$1', consumer unregistered", consumer.getBasePath());
                        }
                    } else {
                        Log.d(FILE_SYSTEM_WATCHER_SERVICE_LOG_TAG, "Consumer null");
                    }
                }
            }
        }
    }
}
