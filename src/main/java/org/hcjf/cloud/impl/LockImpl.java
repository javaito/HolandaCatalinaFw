package org.hcjf.cloud.impl;

import org.hcjf.cloud.impl.network.CloudOrchestrator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author javaito
 */
public final class LockImpl implements Lock {

    private static final String LOCK_NAME = "__lock__";
    private static final String DEFAULT_CONDITION_NAME = "__default_condition_name__";

    private final String name;
    private final Map<String, ConditionImpl> conditionMap;
    private long lockedId;

    public LockImpl(String name) {
        this.name = name;
        this.conditionMap = new HashMap<>();
        CloudOrchestrator.getInstance().publishPath(Lock.class.getName(), name);
    }

    @Override
    public void lock() {
        CloudOrchestrator.getInstance().lock(Lock.class.getName(), name, LOCK_NAME);
        lockedId = Thread.currentThread().getId();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unlock() {
        lockedId = -1;
        CloudOrchestrator.getInstance().unlock(Lock.class.getName(), name, LOCK_NAME);
    }

    @Override
    public Condition newCondition() {
        return newCondition(DEFAULT_CONDITION_NAME);
    }

    public synchronized Condition newCondition(String conditionName) {
        ConditionImpl result = conditionMap.get(conditionName);
        if(result == null) {
            result = new ConditionImpl(this, conditionName);
            conditionMap.put(conditionName, result);
        }
        return result;
    }

    public final static class ConditionImpl implements Condition {

        private final LockImpl lock;
        private final String name;

        private ConditionImpl(LockImpl lock, String name) {
            this.lock = lock;
            this.name = name;
        }

        @Override
        public void await() throws InterruptedException {
            if(Thread.currentThread().getId() == lock.lockedId) {
                CloudOrchestrator.getInstance().unlock(Lock.class.getName(), lock.name, LOCK_NAME);
                synchronized (this) {
                    wait();
                }
                CloudOrchestrator.getInstance().lock(Lock.class.getName(), lock.name, LOCK_NAME);
            } else {
                throw new IllegalMonitorStateException();
            }
        }

        @Override
        public void awaitUninterruptibly() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long awaitNanos(long nanosTimeout) throws InterruptedException {
            await(nanosTimeout, TimeUnit.NANOSECONDS);
            return nanosTimeout;
        }

        @Override
        public boolean await(long time, TimeUnit unit) throws InterruptedException {
            boolean result;
            if(Thread.currentThread().getId() == lock.lockedId) {
                CloudOrchestrator.getInstance().unlock(Lock.class.getName(), lock.name, LOCK_NAME);
                long startTime = System.currentTimeMillis();
                synchronized (this) {
                    unit.timedWait(this, time);
                }
                result = (System.currentTimeMillis() - startTime) >= unit.toMillis(time);
                CloudOrchestrator.getInstance().lock(Lock.class.getName(), lock.name, LOCK_NAME);
            } else {
                throw new IllegalMonitorStateException();
            }
            return result;
        }

        @Override
        public boolean awaitUntil(Date deadline) throws InterruptedException {
            boolean result = false;
            long time = deadline.getTime() - System.currentTimeMillis();
            if(time > 0) {
                result = await(time, TimeUnit.MILLISECONDS);
            }
            return result;
        }

        @Override
        public void signal() {
            synchronized(this) {
                notify();
            }
            CloudOrchestrator.getInstance().signal(lock.name, name);
        }

        @Override
        public void signalAll() {
            synchronized(this) {
                notifyAll();
            }
            CloudOrchestrator.getInstance().signalAll(lock.name, name);
        }

        public void distributedSignal() {
            synchronized(this) {
                notify();
            }
        }

        public void distributedSignalAll() {
            synchronized(this) {
                notifyAll();
            }
        }
    }
}
