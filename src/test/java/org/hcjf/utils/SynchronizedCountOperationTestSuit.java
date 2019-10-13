package org.hcjf.utils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author javaito.
 */
public class SynchronizedCountOperationTestSuit {

    private static Random random;
    private static List<Double> values;

    @BeforeClass
    public static void createSet() {
        random = new Random();
        values = new ArrayList<>();

        double value;
        for (int i = 0; i < 100; i++) {
            value = random.nextDouble() * 100;
            values.add(value);
        }

        List<Double> sortedValues = new ArrayList<>();
        sortedValues.addAll(values);
        Collections.sort(sortedValues);
        System.out.println("Values: " + sortedValues.toString());
        System.out.println("Max value: " + sortedValues.get(values.size()-1));
        System.out.println("Min value: " + sortedValues.get(0));
    }

    @Test
    public void meanFilterBySize() {
        SynchronizedCountOperation operation = new SynchronizedCountOperation(
                SynchronizedCountOperation.getMeanOperation(), 100);
        testOperation(operation, 10, 10);
    }

    @Test
    public void meanFilterByTime() {
        SynchronizedCountOperation operation = new SynchronizedCountOperation(
                SynchronizedCountOperation.getMeanOperation(), 10L);
        testOperation(operation, 10, 10);
    }

    @Test
    public void harmonicMeanFilterBySize() {
        SynchronizedCountOperation operation = new SynchronizedCountOperation(
                SynchronizedCountOperation.getHarmonicMeanOperation(), 100);
        testOperation(operation, 10, 10);
    }

    @Test
    public void harmonicMeanFilterByTime() {
        SynchronizedCountOperation operation = new SynchronizedCountOperation(
                SynchronizedCountOperation.getHarmonicMeanOperation(), 10L);
        testOperation(operation, 10, 10);
    }

    private void testOperation(SynchronizedCountOperation operation, int threadAmount, int maxWarning) {
        try {
            List<TestThread> threads = new ArrayList<>();
            for (int i = 0; i < threadAmount; i++) {
                threads.add(new TestThread(operation, "Thread" + i));
            }

            threads.forEach(T -> T.start());
            threads.forEach(T -> {
                try {
                    T.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            int count = 0;
            for (TestThread thread : threads) {
                count += thread.getWarningCount();
            }

            System.out.println("Result: " + operation.getCurrentValue());
        } catch (Throwable throwable) {
            Assert.fail(throwable.getMessage());
            throw throwable;
        }
    }

    public class TestThread extends Thread {

        private final SynchronizedCountOperation operation;
        private final String name;
        private int warningCount;

        public TestThread(SynchronizedCountOperation operation, String name) {
            this.operation = operation;
            this.name = name;
        }

        @Override
        public void run() {
            long time;
            for(Double value : values) {
                time = System.currentTimeMillis();
                operation.add(value);
                time = System.currentTimeMillis() - time;
                if(time > 15) {
                    System.out.println(String.format("!WARNING Value [name=%s][i=%f][time=%d]: %f", name, value, time, operation.getCurrentValue()));
                    warningCount++;
                } else {
//                    System.out.println(String.format("Value [name=%s][i=%d][time=%d]: %f", name, i, time, operation.getCurrentValue()));
                }
                try {
                    Thread.sleep((long) (random.nextDouble() * 100));
                } catch (Exception ex){}
            }
        }

        public int getWarningCount() {
            return warningCount;
        }
    }
}
