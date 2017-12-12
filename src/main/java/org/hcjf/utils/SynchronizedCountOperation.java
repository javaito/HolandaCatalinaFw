package org.hcjf.utils;

/**
 * This class execute some operation periodically with some accumulated value.
 * The period of execution is evaluated for each input but there aren't any timer
 * or thread running for each counter instance.
 * @author javaito.
 */
public final class SynchronizedCountOperation {

    private static final Operation meanOperation = new Mean();
    private static final Operation harmonicMeanOperation = new HarmonicMean();

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
        accumulator = operation.accumulate(accumulator, value);
        if(counter >= maxCount || System.currentTimeMillis() - lastExecution > maxTime) {
            currentValue = operation.execute(currentValue, accumulator, counter, lastExecution);
            counter = 0;
            accumulator = 0;
            lastExecution = System.currentTimeMillis();
        }
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public static Operation getMeanOperation() {
        return meanOperation;
    }

    public static Operation getHarmonicMeanOperation() {
        return harmonicMeanOperation;
    }

    public interface Operation {

        double accumulate(double accumulator, double value);

        double execute(double currentValue, double accumulator, int counter, long lastExecution);
    }

    private static class Mean implements Operation {

        @Override
        public double accumulate(double accumulator, double value) {
            return accumulator + value;
        }

        @Override
        public double execute(double currentValue, double accumulator, int counter, long lastExecution) {
            return currentValue == 0 ? accumulator / counter : ((accumulator / counter) + currentValue) / 2;
        }
    }

    private static class HarmonicMean implements Operation {

        @Override
        public double accumulate(double accumulator, double value) {
            return accumulator + (value == 0 ? 0 : 1 / value);
        }

        @Override
        public double execute(double currentValue, double accumulator, int counter, long lastExecution) {
            return currentValue == 0 ? counter / accumulator : ((counter / accumulator) + currentValue) / 2;
        }
    }
}
