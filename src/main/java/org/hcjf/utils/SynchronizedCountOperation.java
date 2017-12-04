package org.hcjf.utils;

/**
 * This class execute some operation periodically with some accumulated value.
 * The period of execution is evaluated for each input but there aren't any timer
 * or thread running for each counter instance.
 * @author javaito.
 */
public final class SynchronizedCountOperation {

    private final int maxCount;
    private final long maxTime;
    private final Operation operation;
    private int counter;
    private long lastExecution;
    private double accumulator;
    private double currentValue;

    public SynchronizedCountOperation(Operation operation, int maxCount, long maxTime) {
        this.operation = operation;
        this.maxCount = maxCount;
        this.maxTime = maxTime;
    }

    public SynchronizedCountOperation(Operation operation, int maxCount) {
        this(operation, maxCount, Long.MAX_VALUE);
    }

    public SynchronizedCountOperation(Operation operation, long maxTime) {
        this(operation, Integer.MAX_VALUE, maxTime);
    }

    public synchronized void add(double value) {
        counter++;
        accumulator += value;
        if(maxCount >= counter || System.currentTimeMillis() - lastExecution > maxTime) {
            currentValue = operation.execute(currentValue, accumulator, counter, lastExecution);
            counter = 0;
            accumulator = 0;
            lastExecution = System.currentTimeMillis();
        }
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public interface Operation {
        double execute(double currentValue, double accumulator, int counter, long lastExecution);
    }
}
