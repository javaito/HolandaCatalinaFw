package org.hcjf.cloud.impl;

import org.hcjf.cloud.impl.network.CloudOrchestrator;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author javaito
 */
public final class LockImpl implements Lock {

    private static final String LOCK_NAME = "__lock__";

    private final String name;

    public LockImpl(String name) {
        this.name = name;
        CloudOrchestrator.getInstance().publishPath(Lock.class.getName(), name);
    }

    @Override
    public void lock() {
        CloudOrchestrator.getInstance().lock(Lock.class.getName(), name, LOCK_NAME);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        CloudOrchestrator.getInstance().unlock(Lock.class.getName(), name, LOCK_NAME);
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    public final static class DistributedCondition implements Condition {

        private final String name;

        private DistributedCondition(String name) {
            this.name = name;
        }

        @Override
        public void await() throws InterruptedException {

        }

        @Override
        public void awaitUninterruptibly() {

        }

        @Override
        public long awaitNanos(long nanosTimeout) throws InterruptedException {
            return 0;
        }

        @Override
        public boolean await(long time, TimeUnit unit) throws InterruptedException {
            return false;
        }

        @Override
        public boolean awaitUntil(Date deadline) throws InterruptedException {
            return false;
        }

        @Override
        public void signal() {

        }

        @Override
        public void signalAll() {

        }
    }
}
