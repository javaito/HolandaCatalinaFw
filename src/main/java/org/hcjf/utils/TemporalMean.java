package org.hcjf.utils;

import com.google.common.util.concurrent.AtomicDouble;

public class TemporalMean {

    private final Long startingPeriod;
    private final AtomicDouble accumulator;

    public TemporalMean() {
        startingPeriod = System.currentTimeMillis();
        accumulator = new AtomicDouble();
    }

    public final void add(long delta) {
        accumulator.addAndGet(delta);
    }

    public final void add(int delta) {
        accumulator.addAndGet(delta);
    }

    public final void add(double delta) {
        accumulator.addAndGet(delta);
    }

    public final double getMeanByMilisecond() {
        return accumulator.get() / (System.currentTimeMillis() - startingPeriod);
    }

}
