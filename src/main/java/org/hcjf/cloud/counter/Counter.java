package org.hcjf.cloud.counter;

/**
 * Distributed atomic counter.
 * @author javaito
 */
public interface Counter {

    /**
     * This method return the last number and add one unit to the counter
     * in one atomic operation over the cloud.
     * @return Return the last value of the counter before increase.
     */
    public Long getAndAdd();

    /**
     * This method return the last number and add the counter
     * in one atomic operation over the cloud.
     * @param offset Offset to increment the counter value.
     * @return Return the last value of the counter before increase.
     */
    public Long getAndAdd(Long offset);

}
