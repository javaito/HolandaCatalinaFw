package org.hcjf.io.fs;

import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

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
    private final Map<Path, WatchKey> keys;
    private final Map<WatchKey, List<FileSystemWatcherConsumer>> consumers;

    private FileSystemWatcherService() {
        super(FILE_SYSTEM_WATCHER_SERVICE_NAME);
        keys = new HashMap<>();
        consumers = new HashMap<>();

        if(System.getProperties().containsKey(SystemProperties.HCJF_DEFAULT_PROPERTIES_FILE_PATH)) {
            registerConsumer(new SystemPropertiesConsumer());
        }
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
            synchronized (this) {
                Path absolutePath = consumer.getBasePath().toAbsolutePath();
                if(!keys.containsKey(absolutePath)) {
                    keys.put(absolutePath, consumer.getBasePath().register(watcher, consumer.getEventKinds()));
                }
                WatchKey key = keys.get(absolutePath);
                if(!consumers.containsKey(key)) {
                    consumers.put(key, new ArrayList<>());
                }
                consumers.get(key).add(consumer);
            }
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
        //Init the main watcher, if the watcher init fail
        //this service doesn't start.
        try {
            watcher = FileSystems.getDefault().newWatchService();
            synchronized (FileSystemWatcherService.this) {
                FileSystemWatcherService.this.notifyAll();
            }
        } catch (Throwable ex) {
            Log.d(FILE_SYSTEM_WATCHER_SERVICE_LOG_TAG, "File System Watcher init fail", ex);
        }

        thread = new FileSystemWatcherThread();
        thread.start();
    }

    /**
     * This thread run all the time and is interrupted
     * by the shutdown call.
     */
    private class FileSystemWatcherThread extends Thread {

        @Override
        public void run() {
            if(watcher != null) {
                while (!isInterrupted()) {
                    WatchKey key;
                    try {
                        key = watcher.take();
                    } catch (InterruptedException x) {
                        continue;
                    }

                    //Find the consumer by key.
                    for(FileSystemWatcherConsumer consumer : consumers.get(key)) {
                        key.pollEvents().stream().filter(event -> {
                            boolean result = false;
                            try {
                                result = event.count() <= 1 && (
                                        consumer.getFileName() == null ||
                                                Files.isSameFile(consumer.getPath(),
                                                        consumer.getBasePath().resolve((Path) event.context())));
                            } catch (IOException ex) {
                                //This exception could be throw when some temporary files are deleted by operating system
                            }
                            return result;
                        }).forEach(event -> {

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
                        });

                        boolean valid = key.reset();
                        if (!valid) {
                            Log.d(FILE_SYSTEM_WATCHER_SERVICE_LOG_TAG,
                                    "Inaccessible path '$1', consumer unregistered", consumer.getBasePath());
                        }
                    }
                }
            }
        }
    }

    /**
     * This private implementation watch the changes over the properties file
     * associated to the instance.
     */
    private class SystemPropertiesConsumer extends FileSystemWatcherConsumer {

        public final boolean xmlFile;

        /**
         * Constructor
         */
        public SystemPropertiesConsumer() {
            super(Paths.get(SystemProperties.get(
                    SystemProperties.HCJF_DEFAULT_PROPERTIES_FILE_PATH)),
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
            this.xmlFile = SystemProperties.getBoolean(
                    SystemProperties.HCJF_DEFAULT_PROPERTIES_FILE_XML);
        }

        /**
         * This method must be implemented if you want to
         * listen the create event over the base path.
         * @param event Event information.
         */
        protected void create(WatchEvent<Path> event) {
            load(getPath().toFile());
        }

        /**
         * This method must be implemented if you want to
         * listen the update event over the base path.
         * @param event Event information.
         */
        protected void update(WatchEvent<Path> event) {
            load(getPath().toFile());
        }

        /**
         * This method check if the expected file is an xml file or properties file and
         * load the content of the file in the system properties.
         * @param file Watcher file
         */
        private void load(File file) {
            try {
                if(xmlFile) {
                    System.getProperties().loadFromXML(
                            new FileInputStream(file));
                } else {
                    System.getProperties().load(
                            new FileInputStream(file));
                }
            } catch (IOException ex) {
                Log.w(FILE_SYSTEM_WATCHER_SERVICE_LOG_TAG, "Unable to load properties file", ex);
            }
            Log.d(FILE_SYSTEM_WATCHER_SERVICE_LOG_TAG, "Properties reloaded");
        }
    }
}
