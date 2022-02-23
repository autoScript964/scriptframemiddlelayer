package com.script.content;

import android.util.Log;

public class LocalInterruptThread extends Thread {
    private boolean isLocalInterrupted;
    private final Object lock = new Object();
    public static boolean isCurrentLocalInterrupted(){
        final Thread current = Thread.currentThread();
        final boolean isLocalInterrupted =  (current instanceof LocalInterruptThread && ((LocalInterruptThread) current).isLocalInterrupted());
        Log.d("ProcessCenter#out2#", "{isCurrentLocalInterrupted}=> thread(" + current.getName() + ";" + current.hashCode() + ")" + isLocalInterrupted + "/" + current.isInterrupted());
        return isLocalInterrupted || current.isInterrupted();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean currentLocalInterrupted(){
        final Thread current = Thread.currentThread();
        final boolean interrupted = isCurrentLocalInterrupted();
        if(current instanceof LocalInterruptThread){
            ((LocalInterruptThread) current).isLocalInterrupted = false;
        }
        Thread.interrupted();
        return interrupted;
    }

    @Override
    public void interrupt() {
        synchronized (lock) {
            super.interrupt();
            isLocalInterrupted = true;
        }
    }

    @Override
    public boolean isInterrupted() {
        return super.isInterrupted();
    }

    public boolean isLocalInterrupted() {
        synchronized (lock) {
            return isLocalInterrupted;
        }
    }


}
