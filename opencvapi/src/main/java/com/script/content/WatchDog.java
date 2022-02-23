package com.script.content;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daiepngfei on 2020-08-12
 */
class WatchDog implements Runnable {
    private Thread watchingThread;
    private boolean isRunning;

    private WatchDog() {
    }

    public void unregisterObserverable(Runnable watcher) {
        synchronized (this) {
            runnables.remove(watcher);
        }
    }

    private static class F {
        private static final WatchDog sIns = new WatchDog();
    }

    static WatchDog ins() {
        return F.sIns;
    }

    private List<Runnable> runnables = new ArrayList<>();

    public void registerObserverable(Runnable runnable) {
        synchronized (this) {
            if (runnables != null) {
                runnables.add(runnable);
            }
        }
    }

    public void start() {
        synchronized (this) {
            stop();
            if (watchingThread == null) {
                watchingThread = new Thread(this);
                // watchingThread.setDaemon(true);
                watchingThread.start();
            }
        }
    }

    public void stop(){
        synchronized (this){
            if(watchingThread != null && watchingThread.isAlive()) {
                isRunning = false;
                watchingThread.interrupt();
                watchingThread = null;
            }
        }
    }

    @Override
    public void run() {
        while (isRunning && !Thread.currentThread().isInterrupted()) {
            for (Runnable runnable : runnables) {
                runnable.run();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
