package com.script.framework;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.script.content.LocalInterruptThread;
import com.script.utils.Consumer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by daiepngfei on 2021-05-06
 */
@SuppressWarnings({"JavaDoc", "unused"})
public class ServiceCore {
    private final HandlerThread processorThread;
    private final Handler processorHandler;

    private final Map<String, WorkerThread<?>> workerThreads = new HashMap<>();

    private static final int CMD_START = -1000;
    private static final int CMD_RESUME = -2000;
    private static final int CMD_PAUSE = -3000;
    private static final int CMD_STOP = -4000;
    private static final int CMD_FORCE_BREAK = -5000;
    private static final String MSG_KEY_NAME = "name";
    private static final String TAG = "ProcessCenter#";

    private static class F {
        private static final ServiceCore sInstance = new ServiceCore();
    }

    public static ServiceCore getInstance() {
        return F.sInstance;
    }

    private ServiceCore() {
        processorThread = new HandlerThread("(The-Fairy)-ProcessCenter-Thread") {
            @Override
            protected void onLooperPrepared() {
                ServiceCore.this.onLooperPrepared();
            }
        };
        processorThread.start();
        processorHandler = new Handler(processorThread.getLooper(), ServiceCore.this::handleMessage);
    }

    private void onLooperPrepared() {
        // TODO: nothing now
    }

    private boolean handleMessage(Message msg) {
        switch (msg.what) {
            case CMD_START:
                onHandleStartMessage(msg);
                break;
            case CMD_RESUME:
                onHandleResumeMessage(msg);
                break;
            case CMD_PAUSE:
                onHandlePauseMessage(msg);
                break;
            case CMD_STOP:
                onHandleStopMessage(msg);
                break;
            case CMD_FORCE_BREAK:
                onHandleForceBreakMessage(msg);
                break;
            default:
        }
        return false;
    }

    /**
     * @param name
     * @param worker
     * @return
     */
    @SuppressWarnings("UnusedReturnValue")
    private WorkerThread<?> getOrCreateAStartedThreadWithName(String name, Worker<?> worker) {
        if (Looper.myLooper() != processorThread.getLooper()) {
            throw new IllegalStateException("U can't run or start a thread from non cmd thread");
        }
        WorkerThread<?> thread = null;
        if (!TextUtils.isEmpty(name)) {
            thread = workerThreads.get(name);
            if (thread == null || !thread.isAlive()) {
                thread = new WorkerThread<>(worker);
                thread.setName(name);
                workerThreads.put(name, thread);
                thread.start();
            }
        }
        return thread;
    }


    /**
     * @param name
     * @param consumer
     */
    private void consumeWorkerThread(String name, Consumer<WorkerThread<?>> consumer) {
        if (Looper.myLooper() != processorThread.getLooper()) {
            throw new IllegalStateException("U can't run or start a thread from non cmd thread");
        }
        if (TextUtils.isEmpty(name)) {
            return;
        }
        final WorkerThread<?> thread = workerThreads.get(name);
        if (consumer != null && thread != null) {
            consumer.accept(thread);
        }
    }


    /**
     * @param msg
     */
    private void onHandleStartMessage(Message msg) {
        try {
            final String name = msg.getData().getString(MSG_KEY_NAME);
            final Worker<?> worker = (Worker<?>) msg.obj;
            getOrCreateAStartedThreadWithName(name, worker);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @param msg
     */
    private void onHandleResumeMessage(final Message msg) {
        consumeWorkerThread(msg.getData().getString(MSG_KEY_NAME), t -> t.put(msg.obj));
    }

    /**
     * @param msg
     */
    private void onHandlePauseMessage(Message msg) {
        consumeWorkerThread(msg.getData().getString(MSG_KEY_NAME), WorkerThread::pause);
    }


    /**
     * @param msg
     */
    private void onHandleStopMessage(Message msg) {
        final String name = msg.getData().getString(MSG_KEY_NAME);
        if(TextUtils.isEmpty(name)){
            for(WorkerThread<?> t : workerThreads.values()){
                t.finish();
            }
        } else {
            consumeWorkerThread(msg.getData().getString(MSG_KEY_NAME), WorkerThread::finish);
        }
    }

    /**
     * @param msg
     */
    private void onHandleForceBreakMessage(Message msg) {
        try {
            final String name = msg.getData().getString(MSG_KEY_NAME);
            final String worker = (String) msg.obj;
            Log.d(TAG, "{onHandleForceBreakMessage}=> " + name + ", obj: " + worker);
            consumeWorkerThread(name, thread -> thread.forceBreakCurrentAndWait(worker));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T> void createWorker(String name, Worker<T> worker) {
        Message message = processorHandler.obtainMessage();
        message.what = CMD_START;
        message.obj = worker;
        Bundle bundle = new Bundle();
        bundle.putString(MSG_KEY_NAME, name);
        message.setData(bundle);
        message.sendToTarget();
    }

    public <T> void resume(String name, T source) {
        Message message = processorHandler.obtainMessage();
        message.what = CMD_RESUME;
        message.obj = source;
        Bundle bundle = new Bundle();
        bundle.putString(MSG_KEY_NAME, name);
        message.setData(bundle);
        message.sendToTarget();
    }

    public void pause(String name) {
        Message message = processorHandler.obtainMessage();
        message.what = CMD_PAUSE;
        message.sendToTarget();
    }

    public <T> void restart(String name, T source) {
        Message message = processorHandler.obtainMessage();
        message.what = CMD_FORCE_BREAK;
        message.obj = source;
        Bundle bundle = new Bundle();
        bundle.putString(MSG_KEY_NAME, name);
        message.setData(bundle);
        message.sendToTarget();
    }

    public void destroyWorker(String name) {
        Message message = processorHandler.obtainMessage();
        message.what = CMD_STOP;
        Bundle bundle = new Bundle();
        bundle.putString(MSG_KEY_NAME, name);
        message.setData(bundle);
        message.sendToTarget();
    }

    public void destroy(){
        Message message = processorHandler.obtainMessage();
        message.what = CMD_STOP;
        message.sendToTarget();
    }


    private static class WorkerThread<T> extends LocalInterruptThread {
        private static final String TAG = "ProcessCenter#";
        private final BlockingQueue<T> queue = new LinkedBlockingQueue<>();
        private final Worker<T> worker;
        private boolean isOn = true;
        private boolean isPaused = false;
        private final Object workingLock = new Object();
        private boolean isWorking = false;

        WorkerThread(Worker<T> worker) {
            this.worker = worker;
        }

        @SuppressWarnings("unchecked")
        void put(Object t) {
            try {
                synchronized (this) {
                    if (isPaused) {
                        return;
                    }
                    if (t != null) {
                        T obj = (T) t;
                        queue.put(obj);
                    }
                }
            } catch (ClassCastException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        void forceBreakCurrentAndWait(Object t) {
            if (Thread.currentThread() == this) {
                throw new IllegalStateException();
            }
            logD( "{forceBreakCurrentAndWait}=> -------s ");
            synchronized (workingLock) {
                while (isWorking) {
                    try {
                        logD( "{forceBreakCurrentAndWait}=> pre-interrupt (" + this.hashCode() + ")" + this.isInterrupted());
                        this.interrupt();
                        logD( "{forceBreakCurrentAndWait}=> interrupt (" + this.hashCode() + ")" + this.isInterrupted());
                        logD( "{forceBreakCurrentAndWait}=> wait ");
                        workingLock.wait(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            logD( "{forceBreakCurrentAndWait}=> put ");
            put(t);
        }

        private void logD(String msg){
            Log.d(TAG, "[" + this.getName() + "]=> " + msg);
        }

        void pause() {
            synchronized (this) {
                this.isPaused = true;
                this.queue.clear();
                this.interrupt();
            }
        }

        void finish() {
            synchronized (this) {
                this.isOn = false;
                this.interrupt();
            }
        }


        @Override
        public void run() {
            logD( "{run}=> " + getName() + ": START. ");
            while (isOn) {

                logD( "{run}=> pre-Take");
                T t;
                try {
                    t = queue.take();
                    logD( "{run}=> taken");

                    synchronized (workingLock) {
                        this.isWorking = true;
                    }
                    logD( "{run}=> pre-Working");
                    if (!Thread.currentThread().isInterrupted()) {
                        if (worker != null) {
                            logD( "{run}=> Working");
                            worker.onWorking(t);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logD( "{run}=> pre-End");
                if (LocalInterruptThread.currentLocalInterrupted()) {
                    // make sure that current thread' interrupted state will be clear
                    logD( "{run}=> interrupted-pre-End");
                }
                synchronized (workingLock) {
                    logD( "{run}=> interrupted-notify-End");
                    this.isWorking = false;
                    workingLock.notifyAll();
                }
                logD( "{run}=> End");
            }

            logD( " END. ");
        }
    }

    public interface Worker<T> {
        void onWorking(T t) throws InterruptedException;
    }

}
