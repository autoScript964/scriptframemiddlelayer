package com.script.content;

import com.script.opencvapi.LtLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by daiepngfei on 2020-08-26
 */
public class DProfiler {

    private CMUseageProfiler profiler = new CMUseageProfiler();
    private volatile ExecutorService service = null;

    private ScProxy proxy = null;

    DProfiler(ScProxy proxy){
        this.proxy = proxy;
    }

    void start(){
        if(service == null || service.isTerminated() || service.isShutdown()){
            synchronized (this){
                if(service == null || service.isTerminated() || service.isShutdown()){
                    service = Executors.newSingleThreadExecutor();
                    service.submit(profiler);
                }
            }
        }

    }

    void stop() {
        profiler.setStop(true);
        if(service == null || service.isTerminated() || service.isShutdown()) {
            synchronized (this) {
                if(service == null || service.isTerminated() || service.isShutdown()){
                    return;
                }
                service.shutdown();
            }
        } else {
            service.shutdown();
        }

    }


    public void startWithUserTag(String userTag) {
        this.profiler.setUserTag(userTag);
        start();
    }

    public void setPackageName(String packageName) {
        this.profiler.packageName = packageName;
    }

    /**
     *
     */
    static class CMUseageProfiler implements Runnable {

        private String packageName = "";
        private String tag = "CMUP: ";
        private static final String TAG = "CMUP: ";
        private boolean stop = false;

        void setUserTag(String userTag) {
            tag = TAG + "[" + (userTag == null ? "null" : userTag) + "] ";
        }


        public void setStop(boolean stop) {
            synchronized (this) {
                this.stop = stop;
            }
        }

        @Override
        public void run() {

            try {
                Process process = Runtime.getRuntime().exec("top");
                try(BufferedReader infoReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String headerLine = "";
                    while (!stop) {
                        if(Thread.currentThread().isInterrupted()){
                            break;
                        }

                        final String infoLine = infoReader.readLine();

                        if(infoLine != null ){

                            if(infoLine.toLowerCase().contains("cpu")){
                                headerLine = infoLine;
                                continue;
                            }

                            if(infoLine.contains(packageName)){
                                LtLog.d(tag, headerLine);
                                LtLog.d(tag, infoLine);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
