package org.hcjf.log;

import org.hcjf.service.ServiceConsumer;

/**
 * This class porvide the interface to write the records of
 * the log in different places.
 * @author javaito
 *
 */
public interface LogPrinter extends ServiceConsumer {

    /**
     * This method must be the implementation in order to
     * print the record in the destiny.
     * @param record Rocord to print.
     */
    public void print(Log.LogRecord record);

}
