package org.hcjf.log;

import org.hcjf.properties.SystemProperties;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Stream;

/**
 * Static class that contains the funcionality in order to
 * maintain and organize a log file with the same records format
 * The log behavior is affected by the following system properties
 * <br><b>hcfj_log_path</b>: work directory of the log, by default app work directory
 * <br><b>hcfj_log_initial_queue_size</b>: initial size of the internal queue, by default 10000;
 * <br><b>hcfj_log_file_prefix</b>: all the log files start with this prefix, by default hcjf
 * <br><b>hcfj_log_error_file</b>: if the property is true then log create a particular file for error tag only, by default false
 * <br><b>hcfj_log_warning_file</b>: if the property is true then log create a particular file for warning tag only, by default false
 * <br><b>hcfj_log_info_file</b>: if the property is true then log create a particular file for info tag only, by default false
 * <br><b>hcfj_log_debug_file</b>: if the property is true then log create a particular file for debug tag only, by default false
 * <br><b>hcfj_log_level</b>: min level to write file, by default "I"
 * <br><b>hcfj_log_date_format</b>: date format to show in the log file, by default "yyyy-mm-dd hh:mm:ss"
 * @author javaito
 * @email javaito@gmail.com
 */
public final class Log {

    private static final Integer QUEUE_INITIAL_SIZE = 10000;

    private static final Log instance;

    static {
        instance = new Log();
    }

    private final List<LogPrinter> printers;
    private final Queue<LogRecord> queue;
    private final Thread thread;

    /**
     * Private constructor
     */
    private Log() {
        this.printers = new ArrayList<>();
        this.queue = new PriorityBlockingQueue<>(QUEUE_INITIAL_SIZE, new Comparator<LogRecord>() {

            @Override
            public int compare(LogRecord o1, LogRecord o2) {
                return (int)((o1.getDate().getTime() / 1000) - (o2.getDate().getTime() / 1000));
            }

        });
        this.thread = new LogThread();
        this.thread.start();
    }

    /**
     * Add the record to the queue and notify the consumer thread.
     * @param record Record to add.
     */
    private void addRecord(LogRecord record) {
        if(instance.queue.add(record)) {
            synchronized (this.thread) {
                this.thread.notify();
            }
        }
    }

    /**
     * Create a record with debug tag ("[D]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param message Message to the record. This message use the syntax for class
     *                {@link java.util.Formatter}.
     * @param params Parameters for the places in the message.
     */
    public static void d(String message, Object... params) {
        instance.addRecord(new LogRecord(LogTag.DEBUG, message, params));
    }

    /**
     * Create a record with debug tag ("[D]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param message Message to the record. This message use the syntax for class
     *                {@link java.util.Formatter}.
     * @param throwable Throwable whose message will be printed as part of record
     * @param params Parameters for the places in the message.
     */
    public static void d(String message, Throwable throwable, Object... params) {
        instance.addRecord(new LogRecord(LogTag.DEBUG, message, throwable, params));
    }

    /**
     * Create a record with info tag ("[I]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param message Message to the record. This message use the syntax for class
     *                {@link java.util.Formatter}.
     * @param params Parameters for the places in the message.
     */
    public static void i(String message, Object... params) {
        instance.addRecord(new LogRecord(LogTag.INFO, message, params));
    }

    /**
     * Create a record with warning tag ("[W]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param message Message to the record. This message use the syntax for class
     *                {@link java.util.Formatter}.
     * @param params Parameters for the places in the message.
     */
    public static void w(String message, Object... params) {
        instance.addRecord(new LogRecord(LogTag.WARNING, message, params));
    }

    /**
     * Create a record with warning tag ("[W]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param message Message to the record. This message use the syntax for class
     *                {@link java.util.Formatter}.
     * @param throwable Throwable whose message will be printed as part of record
     * @param params Parameters for the places in the message.
     */
    public static void w(String message, Throwable throwable, Object... params) {
        instance.addRecord(new LogRecord(LogTag.WARNING, message, throwable, params));
    }

    /**
     * Create a record with error tag ("[E]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param message Message to the record. This message use the syntax for class
     *                {@link java.util.Formatter}.
     * @param params Parameters for the places in the message.
     */
    public static void e(String message, Object... params) {
        instance.addRecord(new LogRecord(LogTag.ERROR, message, params));
    }

    /**
     * Create a record with error tag ("[E]"). All the places in the messages
     * are replaced for each param in the natural order.
     * @param message Message to the record. This message use the syntax for class
     *                {@link java.util.Formatter}.
     * @param throwable Throwable whose message will be printed as part of record
     * @param params Parameters for the places in the message.
     */
    public static void e(String message, Throwable throwable, Object... params) {
        instance.addRecord(new LogRecord(LogTag.ERROR, message, throwable, params));
    }

    private class LogThread extends Thread {

        private static final String LOG_THREAD_NAME = "LogThread";

        public LogThread() {
            super(LOG_THREAD_NAME);
        }

        /**
         * Wait to found a recor to print.
         */
        @Override
        public void run() {
            while(!isInterrupted()) {
                if(instance.queue.isEmpty()) {
                    synchronized (LogThread.this) {
                        try {
                            wait();
                        } catch (InterruptedException e) {}
                    }
                }

                try {
                    writeRecord(instance.queue.remove());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        /**
         * Print the record in all the printers registered and the
         * syste out printer.
         * @param record Record to print.
         */
        private void writeRecord(LogRecord record) {
            if(record.getTag() != LogTag.DEBUG) {
                printers.stream().forEach(printer -> printer.print(record));
            }
            System.out.println(record.toString());
        }
    }

    /**
     * This class contains all the information to write a record in the log.
     * The instances of this class will be queued sorted chronologically waiting for
     * be write
     */
    public static final class LogRecord {

        private final Date date;
        private final LogTag tag;
        private final String message;
        private final SimpleDateFormat dateFormat;

        /**
         * Constructor
         * @param tag Taf for the record.
         * @param message Message with wildcard for the parameters.
         * @param throwable The error object, could be null
         * @param params Values that will be put in the each places of the message.
         */
        private LogRecord(LogTag tag, String message, Throwable throwable, Object... params) {
            this.date = new Date();
            this.tag = tag;
            this.dateFormat = new SimpleDateFormat(SystemProperties.get(SystemProperties.LOG_DATE_FORMAT));
            this.message = createMessage(message, throwable, params);
        }

        /**
         * Constructor
         * @param tag Taf for the record.
         * @param message Message with wildcard for the parameters.
         * @param params Values that will be put in the each places of the message.
         */
        private LogRecord(LogTag tag, String message, Object... params) {
            this(tag, message, null, params);
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

            printWriter.print(dateFormat.format(getDate()));
            printWriter.print(" [");
            printWriter.print(getTag().getTag());
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
         * Return the record tag.
         * @return Record tag.
         */
        public LogTag getTag() {
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
     * This enum contains all the possible tags for the records
     */
    private static enum LogTag {

        DEBUG("D", 0),

        INFO("I", 1),

        WARNING("W", 2),

        ERROR("E", 3);

        private Integer order;
        private String tag;

        LogTag(String tag, Integer order) {
            this.order = order;
            this.tag = tag;
        }

        /**
         * Return the tag order.
         * @return Tag order.
         */
        public Integer getOrder() {
            return order;
        }

        /**
         * Return the tag label.
         * @return Tag label.
         */
        public String getTag() {
            return tag;
        }

        /**
         * Return the tag instance that correspond with the parameter.
         * @param tagString String tag
         * @return Tag instance founded or null if the instance is not exist.
         */
        public LogTag valueOfByTag(String tagString) {
            LogTag result = null;

            try {
                result = Stream.of(values()).
                        filter(r -> r.getTag().equals(tagString)).
                        findFirst().get();
            } catch (NoSuchElementException ex) {}

            return result;
        }
    }
}
