package org.hcjf.cloud.timer;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public abstract class CloudTimerTask implements Runnable {

    private final String name;

    public CloudTimerTask(String name) {
        this.name = name;
    }

    @Override
    public void run() {

    }

    protected abstract Long getDelay();

    protected abstract void onRun();
}
