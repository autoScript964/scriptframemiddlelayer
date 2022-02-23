package com.script.opencvapi;


import android.util.Log;

import com.script.network.ServerNio2;
import com.script.network.StickPackageForNio2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by     on 14-12-30.
 */
public class LtLog {

    public static final String TAG = "at" ;
    private static String logFile;
    private static OutputStream out ;
    private static int maxSize ;
    private static int currentSize ;
    private static String logPrefix = "autoTaskfairy-->";
    public static void setLogFile(String file, int maxSize){
        LtLog.logFile = file ;
        LtLog.maxSize = maxSize ;
        File f = new File(file) ;
        if(!f.exists()){
            try {
                if(!f.getParentFile().exists()){
                    f.getParentFile().mkdirs() ;
                    f.getParentFile().setReadable(true, false) ;
                    f.getParentFile().setWritable(true, false) ;
                }
                f.createNewFile() ;
                f.setReadable(true, false) ;
                f.setWritable(true, false) ;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        currentSize = (int) f.length();
        if(out != null){
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            out = new FileOutputStream(f,true) ;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void saveToFile(String info){
        if(out != null){
            currentSize += info.length() ;
            if(currentSize > maxSize){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    out = new FileOutputStream(logFile) ;
                    currentSize = 0 ;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            try {
                out.write(info.getBytes());
                out.write("\n".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void setLogprefix(String prefix){
        logPrefix = prefix ;
    }
    public static String packFairyLog(String info){
        String time = new SimpleDateFormat("MM/dd HH:mm:ss").format(new Date()) ;
        String log = time +" "+logPrefix  + AtFairy2.VERSION+":"+info ;
        return  log ;
    }
    public static void i(String info){
        String log = packFairyLog(info) ;
        Log.i(TAG,log);
        saveToFile(log);
    }

    public static void d(String info){
        String log = packFairyLog(info) ;
        Log.d(TAG,log);
        saveToFile(info);
    }
    public static void e(String info){
        String log = packFairyLog(info) ;
        Log.w(TAG,log);
        saveToFile(info);
    }
    public static void w(String info){
        String log = packFairyLog(info) ;
        Log.w(TAG,log);
        saveToFile(info);
    }
    public static void i(String tag, String info){
        i(tag+":"+info) ;
    }
    public static void e(String tag, String info){
        e(tag+":"+info) ;
    }
    public static void d(String tag, String info){
        d(tag+":"+info) ;
    }
    public static void w(String tag, String info){
        w(tag+":"+info) ;
    }

}

class SendThread implements Runnable{

    public static final int MAX_LINE = 50 ;
    private static ByteBuffer netBuffer = ByteBuffer.allocate(1024*10) ;
    private static Queue<String> msgList ;

    private Thread mThread ;
    private boolean mStarted ;
    private Object mLock ;
    private boolean mExit ;
    private StickPackageForNio2 mServer ;
    private ServerNio2.ServerNioObject mClient ;
    public SendThread(){
        msgList = new LinkedList<>() ;
        mLock = new Object() ;
        mStarted = false ;
        mExit = false ;
        mThread = new Thread(this) ;
    }
    public void start(StickPackageForNio2 server, ServerNio2.ServerNioObject client){
        mServer = server ;
        mClient = client ;
        synchronized (mLock){
            if(!mStarted){
                mStarted = true ;
                mThread.start();
            }
        }
    }
    public void stop(){
        synchronized (mLock){
            if(mStarted){
                mStarted = false ;
                System.out.println("stop.............") ;
            }
        }

    }
    public void sendMsg(String msg){
        synchronized (msgList){
            msgList.add(msg) ;
            if(msgList.size() > MAX_LINE){
                msgList.poll() ;
            }
            if(mStarted) {
                msgList.notify();
            }
        }
    }
    private void sendTonet(String info){
        synchronized (msgList) {
            if (info.length() + 1 < netBuffer.capacity()) {
                    netBuffer.clear();
                    netBuffer.put((info + "\n").getBytes());
                    mClient.sendBuffer = netBuffer;
                    mServer.sendMessage(mClient);
            }
        }
    }
    @Override
    public void run() {
        while (!mExit){
            String info = null  ;
            synchronized (msgList){
                while(msgList.size() <= 0 || !mStarted){
                    try {
                        msgList.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(msgList.size() >=0 ) {
                    info = msgList.poll();
                }
            }
            if(info != null){
                sendTonet(info);
            }
        }
        System.out.println("thread exit.....") ;
    }
}
