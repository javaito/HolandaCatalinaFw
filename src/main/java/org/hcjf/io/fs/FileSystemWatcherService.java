package org.hcjf.io.fs;

import org.hcjf.errors.Errors;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.utils.Strings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * This class provide an interface in order to register
 * a watcher for a file or directory.
 * @author javaito
 */
public final class FileSystemWatcherService extends Service<FileSystemWatcherConsumer> {

    private static final FileSystemWatcherService instance;

    static {
        instance = new FileSystemWatcherService();
    }

    private WatchService watcher;
    private final Map<Path, WatchKey> keys;
    private final Map<WatchKey, List<FileSystemWatcherConsumer>> consumersForWatch;
    private final Map<Path, Map<Path,String>> lastChecksum;
    private final Map<Path, List<FileSystemWatcherConsumer>> consumersForPolling;
    private Future watcherFuture;
    private Future pollingFuture;

    private FileSystemWatcherService() {
        super(SystemProperties.get(SystemProperties.FileSystem.SERVICE_NAME),
                SystemProperties.getInteger(SystemProperties.FileSystem.SERVICE_PRIORITY));
        keys = new HashMap<>();
        consumersForWatch = new HashMap<>();
        lastChecksum = new HashMap<>();
        consumersForPolling = new HashMap<>();

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
            throw new NullPointerException(Errors.getMessage(Errors.ORG_HCJF_IO_FS_1));
        }

        synchronized (this) {
            while(watcher == null) {
                try {
                    Log.d(SystemProperties.get(SystemProperties.FileSystem.LOG_TAG), "Waiting file system watcher init");
                    wait(5000);
                } catch (InterruptedException e) {}
            }
        }

        try {
            synchronized (this) {
                Path absolutePath = consumer.getBasePath().toAbsolutePath();
                if(consumer.getTriggerType() == FileSystemWatcherConsumer.TriggerType.WATCHER) {
                    if (!keys.containsKey(absolutePath)) {
                        keys.put(absolutePath, consumer.getBasePath().register(watcher, consumer.getEventKinds()));
                    }
                    WatchKey key = keys.get(absolutePath);
                    if (!consumersForWatch.containsKey(key)) {
                        consumersForWatch.put(key, new ArrayList<>());
                    }
                    consumersForWatch.get(key).add(consumer);
                    Log.i(SystemProperties.get(SystemProperties.FileSystem.LOG_TAG), "File system watcher registered %s", absolutePath);
                } else if(consumer.getTriggerType() == FileSystemWatcherConsumer.TriggerType.POLLING) {
                    if(!consumersForPolling.containsKey(absolutePath)) {
                        lastChecksum.put(absolutePath, loadChecksumMap(absolutePath, new HashMap<>()));
                        consumersForPolling.put(absolutePath, new ArrayList<>());
                    }
                    consumersForPolling.get(absolutePath).add(consumer);
                }
            }
        } catch (IOException ex) {
            Log.e(SystemProperties.get(SystemProperties.FileSystem.LOG_TAG),
                    "Unable to register file system watcher consumer, '%s'", ex, consumer.getBasePath());
        }
    }

    private Map<Path,String> loadChecksumMap(Path currentPath, Map<Path,String> checksumMap) {
        return verifyNewChecksumMap(currentPath, null, checksumMap);
    }

    private Map<Path,String> verifyNewChecksumMap(Path currentPath, Map<Path,String> checksumMap, Map<Path,String> newChecksumMap) {
        if(currentPath.toFile().isDirectory()) {
            for(File file : currentPath.toFile().listFiles()) {
                newChecksumMap = verifyNewChecksumMap(file.toPath(), checksumMap, newChecksumMap);
            }
        } else {
            try {
                if(checksumMap == null || !checksumMap.containsKey(currentPath)) {
                    newChecksumMap.put(currentPath, Strings.checksum(Files.readAllBytes(currentPath)));
                }
            } catch (IOException ex) {
                Log.e(SystemProperties.get(SystemProperties.FileSystem.LOG_TAG),
                        "Unable to calculate checksum for file: %s", ex, currentPath.toString());
            }
        }
        return newChecksumMap;
    }

    @Override
    public void unregisterConsumer(FileSystemWatcherConsumer consumer) {
        //TODO
    }

    /**
     * Start the main thread of the service.
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
            Log.d(SystemProperties.get(SystemProperties.FileSystem.LOG_TAG), "File System Watcher init fail", ex);
        }

        watcherFuture = fork(new FileSystemWatcherTask());
        pollingFuture = fork(new FileSystemPollingTask());
    }

    /**
     * Shutdown implementation for the file system watcher service.
     */
    @Override
    protected void shutdown(ShutdownStage stage) {
        watcherFuture.cancel(true);
    }

    private class FileSystemPollingTask implements Runnable {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(SystemProperties.getLong(SystemProperties.FileSystem.POLLING_WAIT_TIME));
                } catch (Exception ex) {
                    Log.e(SystemProperties.get(SystemProperties.FileSystem.LOG_TAG),
                            "Polling file system loop fail, closing...", ex);
                    break;
                }
                try {
                    for (Path path : consumersForPolling.keySet()) {
                        try {

                            //Verify file changes.
                            List<Path> deletePaths = new ArrayList<>();
                            List<Path> updatePaths = new ArrayList<>();
                            List<Path> createPaths = new ArrayList<>();
                            Map<Path, String> lastChecksumMap = lastChecksum.get(path);
                            for(Path checksumPath : lastChecksumMap.keySet()) {
                                String checksum = lastChecksumMap.get(checksumPath);
                                if(checksumPath.toFile().exists()) {
                                    String currentChecksum = Strings.checksum(Files.readAllBytes(checksumPath));
                                    if (!checksum.equals(currentChecksum)) {
                                        updatePaths.add(checksumPath);
                                        lastChecksumMap.put(checksumPath, currentChecksum);
                                    }
                                } else {
                                    deletePaths.add(checksumPath);
                                    lastChecksumMap.remove(checksumPath);
                                }
                            }
                            Map<Path, String> newChecksumMap = verifyNewChecksumMap(path, lastChecksumMap, new HashMap<>());
                            lastChecksumMap.putAll(newChecksumMap);
                            createPaths.addAll(newChecksumMap.keySet());

                            //Notify consumers
                            notifyConsumer(path, createPaths, StandardWatchEventKinds.ENTRY_CREATE);
                            notifyConsumer(path, updatePaths, StandardWatchEventKinds.ENTRY_MODIFY);
                            notifyConsumer(path, deletePaths, StandardWatchEventKinds.ENTRY_DELETE);
                        } catch (Exception ex){
                            Log.w(SystemProperties.get(SystemProperties.FileSystem.LOG_TAG),
                                    "Polling path fail: %s", path.toString());
                        }
                    }
                } catch (Exception ex) {
                    Log.w(SystemProperties.get(SystemProperties.FileSystem.LOG_TAG),
                            "Polling file system fail", ex);
                }
            }
        }

        private void notifyConsumer(Path path, List<Path> paths, WatchEvent.Kind<Path> kind) {
            for(FileSystemWatcherConsumer consumer : consumersForPolling.get(path)) {
                fork(() -> {
                    for(Path changedPath : paths) {
                        synchronized (consumer) {
                            WatchEvent<Path> event = new WatchEvent<Path>() {
                                @Override
                                public Kind<Path> kind() {
                                    return kind;
                                }

                                @Override
                                public int count() {
                                    return 1;
                                }

                                @Override
                                public Path context() {
                                    return changedPath;
                                }
                            };
                            if(kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                                consumer.create(event);
                            } else if(kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                                consumer.update(event);
                            } else if(kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                                consumer.delete(event);
                            }
                        }
                    }
                });
            }
        }

    }

    /**
     * This thread run all the time and is interrupted
     * by the shutdown call.
     */
    private class FileSystemWatcherTask implements Runnable {

        @Override
        public void run() {
            if(watcher != null) {
                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key;
                    try {
                        key = watcher.take();
                    } catch (InterruptedException x) {
                        break;
                    }

                    //Find the consumer by key.
                    for(FileSystemWatcherConsumer consumer : consumersForWatch.get(key)) {
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

                            fork(() -> {
                                synchronized (consumer) {
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
                            });
                        });

                        boolean valid = key.reset();
                        if (!valid) {
                            Log.d(SystemProperties.get(SystemProperties.FileSystem.LOG_TAG),
                                    "Inaccessible path '$1', consumer unregistered", consumer.getBasePath());
                        }
                    }
                }

                try {
                    watcher.close();
                } catch (Exception ex){}

                Log.d(SystemProperties.get(SystemProperties.FileSystem.LOG_TAG), "File system watcher service stopped");
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
                    TriggerType.POLLING,
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
                Log.w(SystemProperties.get(SystemProperties.FileSystem.LOG_TAG), "Unable to load properties file", ex);
            }
            Log.d(SystemProperties.get(SystemProperties.FileSystem.LOG_TAG), "Properties reloaded");
        }
    }
}
