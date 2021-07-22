package org.hcjf.io.fs;

import org.hcjf.service.ServiceConsumer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * This class must be implemented if tou want to
 * consume the file system watcher service.
 * @author javaito
 */
public abstract class FileSystemWatcherConsumer implements ServiceConsumer {

    private final Path path;
    private final String fileName;
    private final Path basePath;
    private final WatchEvent.Kind[] eventKinds;
    private final TriggerType triggerType;

    /**
     * Constructor
     * @param path Base path, all the paths bellow of the base are wached by the consumer
     * @param eventKinds Kinds of events that this consumer are listening
     */
    public FileSystemWatcherConsumer(Path path, WatchEvent.Kind... eventKinds) {
        this(path, TriggerType.WATCHER, eventKinds);
    }

    /**
     * Constructor
     * @param path Base path, all the paths bellow of the base are wached by the consumer
     * @param triggerType Trigger type.
     * @param eventKinds Kinds of events that this consumer are listening
     */
    public FileSystemWatcherConsumer(Path path, TriggerType triggerType, WatchEvent.Kind... eventKinds) {
        this.path = path;
        this.triggerType = triggerType;
        if(!Files.isDirectory(path)) {
            basePath = path.getParent();
            fileName = path.getFileName().toString();
        } else {
            basePath = path;
            fileName = null;
        }

        this.eventKinds = eventKinds;
    }

    /**
     * Return complete path. If the consumer is watching a directory then path and base path are the same.
     * @return Complete path.
     */
    public Path getPath() {
        return path;
    }

    /**
     * Returns the trigger type for this consumer.
     * @return Trigger type.
     */
    public final TriggerType getTriggerType() {
        return triggerType;
    }

    /**
     * Return the base path.
     * @return Base path.
     */
    public final Path getBasePath() {
        return basePath;
    }

    /**
     * Return the file name associated to the consumer or null if the consumer is watching a directory
     * @return File name or null.
     */
    public final String getFileName() {
        return fileName;
    }

    /**
     * Return an array of the event kinds.
     * @return Event kinds.
     */
    public final WatchEvent.Kind[] getEventKinds() {
        return eventKinds;
    }

    /**
     * This method must be implemented if you want to
     * listen the create event over the base path.
     * @param event Event information.
     */
    protected void create(WatchEvent<Path> event) {}

    /**
     * This method must be implemented if you want to
     * listen the update event over the base path.
     * @param event Event information.
     */
    protected void update(WatchEvent<Path> event) {}

    /**
     * This method must be implemented if you want to
     * listen the delete event over the base path.
     * @param event Event information.
     */
    protected void delete(WatchEvent<Path> event) {}

    /**
     * This method must be implemented if you want to
     * listen the overflow event over the base path.
     * @param event Event information.
     */
    protected void overflow(WatchEvent<Path> event) {}

    /**
     * Enums the kind of trigger to verify the file system.
     */
    public enum TriggerType {
        WATCHER,
        POLLING
    }
}
