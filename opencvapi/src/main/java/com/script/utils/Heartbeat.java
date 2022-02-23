package com.script.utils;

import com.script.opencvapi.LtLog;
import com.script.opencvapi.AtFairyConfig;
import com.script.opencvapi.module.HeartBeatModule;
import com.script.opencvapi.module.HeartBeatOverModule;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by     on 2018/9/30.
 */
public class Heartbeat {
    private Timer mTimer;
    private HeartBeatTask mTask;
    private byte[] mHeartBeatData;
    private byte[] mHeartBeatOverData;
    private byte[] mRecvData;
    private byte[] mRecvOverData;
    private int mPort;
    private String mIp = "127.0.0.1";

    public Heartbeat(String packageName, String serviceName) {
        LtLog.i("heart beat package name:" + packageName + " service name:" + serviceName);
        mTimer = new Timer();
        mTask = new HeartBeatTask();
        HeartBeatModule module = new HeartBeatModule();
        module.packageName = packageName;
        module.serviceName = serviceName;
        mHeartBeatData = module.toDataWithLength().array();
        HeartBeatOverModule overModule = new HeartBeatOverModule();
        overModule.packageName = packageName;
        overModule.serviceName = serviceName;
        mHeartBeatOverData = overModule.toDataWithLength().array();
        mRecvData = new byte[10];
        mRecvOverData = new byte[10];
        try {
            mPort = Integer.parseInt(AtFairyConfig.getASPort());
        } catch (Exception e) {
        }
    }

    public void start() {
        if (mPort > 0) {
            mTimer.schedule(mTask, 0, 5000);
        } else {
            LtLog.e("start heart beat error port :" + mPort);
        }
    }

    private void sendHeart() {
        LtLog.i("send heart ......");
        try {
            Socket socket = new Socket(mIp, mPort);
            socket.getOutputStream().write(mHeartBeatData);
            socket.getInputStream().read(mRecvData);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendHeartOver() {
        LtLog.i("send heart ......");
        try {
            Socket socket = new Socket(mIp, mPort);
            socket.getOutputStream().write(mHeartBeatOverData);
            socket.getInputStream().read(mRecvOverData);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void destory() {
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    class HeartBeatTask extends TimerTask {

        @Override
        public void run() {
            sendHeart();
        }
    }

    public void cancel() {
        if (mTimer != null) {
            mTimer.cancel();
            new Thread(this::sendHeartOver).start();
        }
    }
}
