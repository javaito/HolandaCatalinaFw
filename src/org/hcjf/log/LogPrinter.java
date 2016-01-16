package org.hcjf.log;

/**
 * This class porvide the interface to write the records of
 * the log in different places.
 * @author javaito
 * @email javaito@gmail.com
 */
public interface LogPrinter {

    /**
     * This method must be the implementation in order to
     * print the record in the destiny.
     * @param record Rocord to print.
     */
    public void print(Log.LogRecord record);

}
