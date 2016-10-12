package org.hcjf.log;

import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.utils.Strings;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Stream;

/**
 * Static class that contains the funcionality in order to
 * maintain and organize a log file with the same records format
 * The log behavior is affected by the following system properties
 * <br><b>hcfj_log_path</b>: work directory of the log, by default app work directory
 * <br><b>hcfj_log_initial_queue_size</b>: initial size of the internal queue, by default 10000;
 * <br><b>hcfj_log_file_prefix</b>: all the log files start with this prefix, by default hcjf
 * <br><b>hcfj_log_error_file</b>: if the property is true then log create a particular file for error group only, by default false
 * <br><b>hcfj_log_warning_file</b>: if the property is true then log create a particular file for warning group only, by default false
 * <br><b>hcfj_log_info_file</b>: if the property is true then log create a particular file for info group only, by default false
 * <br><b>hcfj_log_debug_file</b>: if the property is true then log create a particular file for debug group only, by default false
 * <br><b>hcfj_log_level</b>: min level to write file, by default "I"
 * <br><b>hcfj_log_date_format</b>: date format to show in the log file, by default "yyyy-mm-dd hh:mm:ss"
 * @author javaito
 * @email javaito@gmail.com
 */
public final class Log extends Service<LogPrinter> {

    public static final String NAME = "LogService";

    private static final Log instance;

    static {
        instance = new Log();
    }

    private final List<LogPrinter> printers;
    private final Queue<LogRecord> queue;
    private final Object logMonitor;
    private Future future;

    /**
     * Private constructor
     */
    private Log() {
        super(NAME, 0);
        this.printers = new ArrayList<>();
        this.queue = new PriorityBlockingQueue<>(
                SystemProperties.getInteger(SystemProperties.LOG_QUEUE_INITIAL_SIZE),
                (o1, o2) -> (int)(o1.getDate().getTime() - o2.getDate().getTime()));
        this.logMonitor = new Object();
    }

    /**
     * Start the log thread.
     */
    @Override
    protected void init() {
        future = fork(new LogRunnable());
        List<String> logConsumers = SystemProperties.getList(SystemProperties.LOG_CONSUMERS);
        logConsumers.forEach(S -> {
            try {
                LogPrinter printer = (LogPrinter) Class.forName(S).newInstance();
                registerConsumer(printer);
            } catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }

    /**
     * Only valid with the stage is START
     * @param stage Shutdown stage.
     */
    @Override
    protected void shutdown(ShutdownStage stage) {
        switch (stage) {
            case START: {
                future.cancel(true);
                break;
            }
        }
    }

    /**
     * This method register a printer.
     * @param consumer Printer.
     * @throws NullPointerException If the printer is null.
     */
    @Override
    public void registerConsumer(LogPrinter consumer) {
        if(consumer == null) {
            throw new NullPointerException("Log printer null");
        }

        printers.add(consumer);
    }

    /**
     * Add the record to the queue and notify the consumer thread.
     * @param record Record to add.
     */
    private void addRecord(LogRecord record) {
        if(instance.queue.add(record)) {
            synchronized (this.logMonitor) {
                this.logMonitor.notify();
            }
        }
    }

    /**
     * This method register a printer.
     * @param printer Printer.
     * @throws NullPointerException If the printer is null.
     */
    public static void addPrinter(LogPrinter printer) {
        instance.registerConsumer(printer);
    }

    /**
     * Create a record with debug group ("[D]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param tag Tag of the record
     * @param message Message to the record. This message use the syntax for class
     *                {@link java.util.Formatter}.
     * @param params Parameters for the places in the message.
     */
    public static void d(String tag, String message, Object... params) {
        instance.addRecord(new LogRecord(LogGroup.DEBUG, tag, message, params));
    }

    /**
     * Create a record with debug group ("[D]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param tag Tag of the record
     * @param message Message to the record. This message use the syntax for class
     *                {@link java.util.Formatter}.
     * @param throwable Throwable whose message will be printed as part of record
     * @param params Parameters for the places in the message.
     */
    public static void d(String tag, String message, Throwable throwable, Object... params) {
        instance.addRecord(new LogRecord(LogGroup.DEBUG, tag, message, throwable, params));
    }

    /**
     * Create a record with info group ("[I]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param tag Tag of the record
     * @param message Message to the record. This message use the syntax for class
     *                {@link java.util.Formatter}.
     * @param params Parameters for the places in the message.
     */
    public static void i(String tag, String message, Object... params) {
        instance.addRecord(new LogRecord(LogGroup.INFO, tag, message, params));
    }

    /**
     * Create a record with info group ("[IN]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param tag Tag of the record
     * @param message Message to the record. This message use the syntax for class
     *                {@link java.util.Formatter}.
     * @param params Parameters for the places in the message.
     */
    public static void in(String tag, String message, Object... params) {
        instance.addRecord(new LogRecord(LogGroup.INPUT, tag, message, params));
    }

    /**
     * Create a record with info group ("[OUT]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param tag Tag of the record
     * @param message Message to the record. This message use the syntax for class
     *                {@link java.util.Formatter}.
     * @param params Parameters for the places in the message.
     */
    public static void out(String tag, String message, Object... params) {
        instance.addRecord(new LogRecord(LogGroup.OUTPUT, tag, message, params));
    }

    /**
     * Create a record with warning group ("[W]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param tag Tag of the record
     * @param message Message to the record. This message use the syntax for class
     *                {@link java.util.Formatter}.
     * @param params Parameters for the places in the message.
     */
    public static void w(String tag, String message, Object... params) {
        instance.addRecord(new LogRecord(LogGroup.WARNING, tag, message, params));
    }

    /**
     * Create a record with warning group ("[W]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param tag Tag of the record
     * @param message Message to the record. This message use the syntax for class
     *                {@link java.util.Formatter}.
     * @param throwable Throwable whose message will be printed as part of record
     * @param params Parameters for the places in the message.
     */
    public static void w(String tag, String message, Throwable throwable, Object... params) {
        instance.addRecord(new LogRecord(LogGroup.WARNING, tag, message, throwable, params));
    }

    /**
     * Create a record with error group ("[E]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param tag Tag of the record
     * @param message Message to the record. This message use the syntax for class
     *                {@link java.util.Formatter}.
     * @param params Parameters for the places in the message.
     */
    public static void e(String tag, String message, Object... params) {
        instance.addRecord(new LogRecord(LogGroup.ERROR, tag, message, params));
    }

    /**
     * Create a record with error group ("[E]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param tag Tag of the record
     * @param message Message to the record. This message use the syntax for class
     *                {@link java.util.Formatter}.
     * @param throwable Throwable whose message will be printed as part of record
     * @param params Parameters for the places in the message.
     */
    public static void e(String tag, String message, Throwable throwable, Object... params) {
        instance.addRecord(new LogRecord(LogGroup.ERROR, tag, message, throwable, params));
    }

    private class LogRunnable implements Runnable {

        /**
         * Wait to found a recor to print.
         */
        @Override
        public void run() {
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    if(instance.queue.isEmpty()) {
                        synchronized (Log.this.logMonitor) {
                            Log.this.logMonitor.wait();
                        }
                    }

                    try {
                        writeRecord(instance.queue.remove());
                    } catch (Exception ex) {}
                }
            } catch (InterruptedException e) {}
        }

        /**
         * Print the record in all the printers registered and the
         * syste out printer.
         * @param record Record to print.
         */
        private void writeRecord(LogRecord record) {
            if(record.getGroup().getOrder() >= SystemProperties.getInteger(SystemProperties.LOG_LEVEL)) {
                printers.forEach(printer -> printer.print(record));
            }

            if(SystemProperties.getBoolean(SystemProperties.LOG_SYSTEM_OUT_ENABLED)) {
                System.out.println(record.toString());
                System.out.flush();
            }
        }
    }

    /**
     * This class contains all the information to write a record in the log.
     * The instances of this class will be queued sorted chronologically waiting for
     * be write
     */
    public static final class LogRecord {

        private final Date date;
        private final LogGroup group;
        private final String tag;
        private final String message;
        private final SimpleDateFormat dateFormat;

        /**
         * Constructor
         * @param group Group of the record.
         * @param tag Tag for the record.
         * @param message Message with wildcard for the parameters.
         * @param throwable The error object, could be null
         * @param params Values that will be put in the each places of the message.
         */
        private LogRecord(LogGroup group, String tag, String message, Throwable throwable, Object... params) {
            this.date = new Date();
            this.group = group;
            this.tag = tag;
            this.dateFormat = new SimpleDateFormat(SystemProperties.get(SystemProperties.LOG_DATE_FORMAT));
            this.message = createMessage(message, throwable, params);
        }

        /**
         * Constructor
         * @param group Group of the record.
         * @param tag Tag for the record.
         * @param message Message with wildcard for the parameters.
         * @param params Values that will be put in the each places of the message.
         */
        private LogRecord(LogGroup group, String tag, String message, Object... params) {
            this(group, tag, message, null, params);
        }

        /**
         * Create a final version of string to print the record.
         * @param message Message to be format
         * @param throwable The error object, could be null.
         * @param params Parameters to format the message.
         * @return Return the last version of the message.
         */
        private String createMessage(String message, Throwable throwable, Object... params) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            String group = getGroup().getGroup();
            String tag = getTag();
            if(SystemProperties.getBoolean(SystemProperties.LOG_TRUNCATE_TAG)) {
                int truncateSize = SystemProperties.getInteger(SystemProperties.LOG_TRUNCATE_TAG_SIZE);
                if(truncateSize > tag.length()) {
                    tag = Strings.rightPad(tag, truncateSize);
                } else if(truncateSize < tag.length()) {
                    tag = tag.substring(0, truncateSize);
                }
            }

            printWriter.print(dateFormat.format(getDate()));
            printWriter.print(" [");
            printWriter.print(group);
            printWriter.print("][");
            printWriter.print(tag);
            printWriter.print("] ");
            printWriter.printf(message, params);

            if(throwable != null) {
                printWriter.print("\r\n");
                throwable.printStackTrace(printWriter);
            }

            printWriter.flush();
            printWriter.close();

            return stringWriter.toString();
        }

        /**
         * Return the record date
         * @return Record date.
         */
        public Date getDate() {
            return date;
        }

        /**
         * Return the record group.
         * @return Record group.
         */
        public LogGroup getGroup() {
            return group;
        }

        /**
         * Return the log record tag.
         * @return Log record tag.
         */
        public String getTag() {
            return tag;
        }

        /**
         * Return the last version of the message.
         * @return Record message.
         */
        public String getMessage() {
            return message;
        }

        /**
         * Return de format message.
         * @return Format message.
         */
        @Override
        public String toString() {
            return getMessage();
        }
    }

    /**
     * This enum contains all the possible groups for the records
     */
    private static enum LogGroup {

        DEBUG("D", 0),

        INPUT("A", 1),

        OUTPUT("O", 1),

        INFO("I", 1),

        WARNING("W", 2),

        ERROR("E", 3);

        private Integer order;
        private String group;

        LogGroup(String tag, Integer order) {
            this.order = order;
            this.group = tag;
        }

        /**
         * Return the group order.
         * @return Tag order.
         */
        public Integer getOrder() {
            return order;
        }

        /**
         * Return the group label.
         * @return Tag label.
         */
        public String getGroup() {
            return group;
        }

        /**
         * Return the group instance that correspond with the parameter.
         * @param tagString String group
         * @return Tag instance founded or null if the instance is not exist.
         */
        public LogGroup valueOfByTag(String tagString) {
            LogGroup result = null;

            try {
                result = Stream.of(values()).
                        filter(r -> r.getGroup().equals(tagString)).
                        findFirst().get();
            } catch (NoSuchElementException ex) {}

            return result;
        }
    }
}
