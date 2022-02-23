package com.script.content;

/**
 * Created by daiepngfei on 2020-08-14
 */
public class CUtils {

    public static void sleep(long time) throws InterruptedException {
        if(time > 0) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e){
                throw e;
            }
            if(LocalInterruptThread.currentLocalInterrupted()){
                throw new InterruptedException();
            }
        }
    }
}
