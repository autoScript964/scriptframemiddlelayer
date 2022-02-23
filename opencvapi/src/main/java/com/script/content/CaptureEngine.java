package com.script.content;

import android.util.Log;

import com.script.AtModule2;
import com.script.opencvapi.LtLog;
import com.script.opencvapi.ScreenInfo;
import com.script.opencvapi.module.ScreenInfoModule;
import com.script.opencvapi.module.TypeModule;
import com.script.utils.Utils;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by daiepngfei on 2020-08-11
 */
public class CaptureEngine {
    private static final String TAG = ScProxy.TAG + "#" + "CaptureEngine";
    private ExecutorService mExecutorService;
    private boolean stopped;
    private static final long DEFAULT_EXPECTED_FRAME_TIME_INTERVAL = 500;
    private volatile long expectedFrameTimeInterval = DEFAULT_EXPECTED_FRAME_TIME_INTERVAL;
    private volatile long loopStartTime;

    /**
     * 1 - 10
     * 默认等级为2
     * 1-5级为实用等级
     * todo：'或'因为设备性能缘故，目前6-10 有边际效应现象存在收益递减。后续优化
     * @param level
     */
    void setEngineMaxSpeedLevel(int level) {
        level = Math.min(Math.max(level, 1), 10);
        this.expectedFrameTimeInterval = 1000 / level;
    }

    private Runnable watcher = new Runnable() {
        @Override
        public void run() {
            if (loopStartTime != 0 && System.currentTimeMillis() - loopStartTime > 5000) {
                if (currentSocket != null) {
                    if (currentSocket.isConnected()) {
                        try {
                            currentSocket.close();
                            Thread.sleep(2000);
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, "err -> $ watch dog : foce-close socket: " + loopStartTime);
                    }

                } else {
                    Log.d(TAG, "err -> $ watch dog : foce-stop thread & re-start");
                    stop();
                    start();
                }

            }

        }
    };

    CaptureEngine(ScProxy scProxy) {

    }

    void start() {
        synchronized (this) {
            stopped = false;
            if (mExecutorService == null || mExecutorService.isShutdown() || mExecutorService.isTerminated()) {
                mExecutorService = Executors.newSingleThreadExecutor();
                WatchDog.ins().registerObserverable(watcher);
                mExecutorService.submit(mWorkingRunnable);
            }

        }
    }

    void stop() {
        synchronized (this) {
            WatchDog.ins().unregisterObserverable(watcher);
            stopped = true;
            if (mExecutorService != null) {
                mExecutorService.shutdown();
                mExecutorService = null;
            }
        }
    }

    boolean isStopped() {
        return stopped;
    }

    private Runnable mWorkingRunnable = new Runnable() {

        @Override
        public void run() {

            while (!stopped) {
                final long now = System.currentTimeMillis();
                if (Thread.currentThread().isInterrupted()) {
                    Log.d(TAG, "err -> $ interrupt or so is disconnect: " + Thread.currentThread().isInterrupted());
                    break;
                }
                // connect socket
                try {
                    Log.d(TAG, "running -> start connecting socket");
                    connect(5000);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "err -> connecting socket error", e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    continue;
                }

                final short fRequestImage = 0x0010;
                final short fResponseImage = 0x0011;
                final TypeModule typeModule = new TypeModule(fRequestImage);
                final byte[] bufferData = typeModule.toDataWithLen().array();

                // capture
//                while (true) {
                loopStartTime = now;
                final Socket so = currentSocket;
                if (Thread.currentThread().isInterrupted() || so == null || !so.isConnected()) {
                    Log.d(TAG, "err -> interrupt or so is disconnect: " + Thread.currentThread().isInterrupted());
                    break;
                }

                OutputStream outputStream = null;
                InputStream inputStream = null;
                try {
                    // write - request
                    outputStream = so.getOutputStream();
                    if (outputStream == null) {
                        Log.d(TAG, "err -> no socket output stream ");
                        continue;
                    }
                    Log.d(TAG, "running -> start request img / writing data into socket ");
                    outputStream.write(bufferData);

                    // read - response
                    inputStream = so.getInputStream();
                    if (inputStream == null) {
                        Log.d(TAG, "err -> no socket input stream ");
                        continue;
                    }
                    Log.d(TAG, "running -> start to read data size ");
                    final byte[] arrayOfSize = new byte[4];
                    final int readSizeResult = inputStream.read(arrayOfSize);
                    if (readSizeResult == -1) {
                        Log.d(TAG, "err -> read data size error -1 ");
                        continue;
                    }
                    Log.d(TAG, "running -> start to parse data size ");
                    final int size = Utils.bytesToInt(arrayOfSize, 0);
                    if (size <= 0) {
                        Log.d(TAG, "err -> parse data size error -1 ");
                        continue;
                    }
                    Log.d(TAG, "running -> start to read img data size : " + size);
                    final byte[] data = new byte[size];
                    int sum = 0;
                    while (true) {
                        final int left = size - sum;
                        if (left <= 0) {
                            break;
                        }
                        final int len = inputStream.read(data, sum, Math.min(left, 8092));
                        if (len == -1) {
                            Log.d(TAG, "err -> reading actual data len -1 sum: " + sum + "/" + left);
                            break;
                        }
                        if (Thread.currentThread().isInterrupted()) {
                            Log.d(TAG, "err -> reading(interruption) actual data len -1 sum: " + sum);
                            break;
                        }
                        sum += len;
                    }
                    if (sum != size) {
                        Log.d(TAG, "err -> read data error -1 ");
                        continue;
                    }

                    Log.d(TAG, "running -> start to parse img data ");
                    // --o
                    int len = data.length;
                    int offset = 0;
                    final short type = Utils.bytesToShort(data, offset);
                    offset += AtModule2.SIZE_OF_TYPE;
                    len -= AtModule2.SIZE_OF_TYPE;
                    if (type != fResponseImage) {
                        Log.d(TAG, "err -> read data type error " + type);
                        continue;
                    }
                    final ScreenInfoModule module = new ScreenInfoModule(data, offset, len);
                    final ScreenInfo screenInfo = new ScreenInfo();
                    if (module.img != null) {
                        screenInfo.width = module.width;
                        screenInfo.height = module.height;
                        screenInfo.raw = module.img.array();
                        screenInfo.timestamp = module.timestamp;

                        Mat mat = new Mat(screenInfo.height, screenInfo.width, CvType.CV_8UC4);
                        mat.put(0, 0, screenInfo.raw);
                        Mat screenMat = new Mat(screenInfo.height, screenInfo.width, CvType.CV_8UC3);
                        Imgproc.cvtColor(mat, screenMat, Imgproc.COLOR_RGBA2BGR);
                        mat.release();

                        currentScreenInfo = screenInfo;
//                        currentScreenInfo.raw = null;

                        if (currentScreenMat == null) {
                            synchronized (lock) {
                                LtLog.e("CaptureEngine-currentScreen LOCK(ENTRY) ");
                                currentScreenMat = screenMat;
                                lock.notifyAll();
                                LtLog.e("CaptureEngine-currentScreen LOCK(EXIT) ");
                            }
                        } else {
                            final Mat m = currentScreenMat;
                            currentScreenMat = screenMat;
                            //m.release();
                        }

                        final long delta = System.currentTimeMillis() - now;
                        final long sleepTime = delta >= expectedFrameTimeInterval ? 0 : expectedFrameTimeInterval - delta;
                        System.out.println(TAG + "::looping： " + (1000 / delta) + "/ time: " + sleepTime);

                        try {
                            if (sleepTime > 0) {
                                Thread.sleep(sleepTime);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "img null");
                    }

                    so.close();

                } catch (Exception e) {
                    Log.e(TAG, "", e);
                    loopStartTime = 0;
                    e.printStackTrace();
                }
//                }
            }
        }
    };

    private volatile Socket currentSocket;
    private volatile ScreenInfo currentScreenInfo;
    private Mat currentScreenMat;


    public ScreenInfo getCurrentScreenInfo() {
        if(currentScreenInfo == null) {
            ScProxy.captureEngine().getCurrentScreenMat();
        }
        return currentScreenInfo;
    }

    private final Object lock = new Object();

    public Mat getCurrentScreenMat() {
        if(currentScreenMat == null) {
            while (true) {
                synchronized (lock) {
                    try {
                        if (currentScreenMat == null) {
                            LtLog.e("CaptureEngine-currentScreen WAIT ");
                            lock.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    LtLog.e("CaptureEngine-currentScreen UNLOCK ");
                    break;
                }
            }
        }
        return currentScreenMat;
    }

    private void connect(int timeout) throws IOException {
        if (currentSocket != null) {
            currentSocket.close();
        }

        final int fPort = 11413;
        final String fIp = "screen_server";
        final String ip = System.getProperty(fIp, "127.0.0.1");
        Socket socket = new Socket();
        InetSocketAddress address = new InetSocketAddress(ip, fPort);
        final long startTime = System.currentTimeMillis();
        while (true) {
            socket.connect(address, 1000);
            if (socket.isConnected()) {
                break;
            }
            if (System.currentTimeMillis() - startTime > timeout) {
                break;
            }
        }

        if (socket.isConnected()) {
            socket.setSoTimeout(10*1000);
            currentSocket = socket;
        } else {
            throw new SocketTimeoutException();
        }
    }

    public void destroy() {
        stop();
    }
}
