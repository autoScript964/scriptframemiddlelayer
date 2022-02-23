package com.script.opencvapi.utils;

import android.content.Context;
import android.content.Intent;

import com.script.AtModule2;
import com.script.network.ClientNio;
import com.script.opencvapi.LtLog;
import com.script.opencvapi.ScreenInfo;
import com.script.opencvapi.module.ScreenInfoModule;
import com.script.opencvapi.module.TypeModule;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 * Created by     on 2018/7/19.
 */
public class AtScreencap2 extends AtScreencap {
    public static final short TYPE_REQUEST_IMAGE = 0x0010;
    public static final short TYPE_IAMGE = 0x0011;

    public static final byte ATTR_IMG = 0X01;
    public static final byte ATTR_WIDTH = 0X02;
    public static final byte ATTR_HEIGHT = 0X03;
    public static final byte ATTR_TIMESTAMP = 0x04;
    private static final int SCREENCAP_PORT = 11413;
    public static final String KEY_SCREENSERVER = "screen_server";
    private ClientNio mNetWork;
    private Context mContext;

    public AtScreencap2(final Context context, Intent intentResult) {
        super(context, intentResult);
        mContext = context;
        mNetWork = new ClientNio(5000);
        connectService(context);
    }

    private void connectService(final Context context) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                for (int i = 0; i < 5; ++i) {
                    success = mNetWork.connect(System.getProperty(KEY_SCREENSERVER, "127.0.0.1"), SCREENCAP_PORT);
                    LtLog.i("connect ypservice :" + success);
                    if (success) {
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ScreenInfo screencap(ScreenInfo screenInfo) {
        synchronized (this) {
            TypeModule typeModule = new TypeModule(TYPE_REQUEST_IMAGE);
            try {
                int ret = mNetWork.send(typeModule.toDataWithLen());
                if (ret == -1) {
                    LtLog.i("screencap send error reconnect....");
                    mNetWork.disconnect();
                    connectService(mContext);
                    return null;
                }
                ByteBuffer byteBuffer = mNetWork.readPackage();
                if (byteBuffer != null) {
                    byte[] data = byteBuffer.array();
                    int len = data.length;
                    int offset = 0;
                    short type = Utils.bytesToShort(data, offset);
                    offset += AtModule2.SIZE_OF_TYPE;
                    len -= AtModule2.SIZE_OF_TYPE;

                    switch (type) {
                        case TYPE_IAMGE:
                            ScreenInfoModule module = new ScreenInfoModule(data, offset, len);
                            screenInfo.width = module.width;
                            screenInfo.height = module.height;
                            screenInfo.raw = module.img.array();
                            screenInfo.timestamp = module.timestamp;
                            break;
                        default:
                            LtLog.i("screencap package type error :" + type);
                            break;

                    }
                } else {
                    LtLog.i("screencap read package error reconnect");
                    mNetWork.disconnect();
                    connectService(mContext);
                }
            } catch (TimeoutException e) {
                LtLog.i("screencap read package timeout reconnect");
                e.printStackTrace();
                mNetWork.disconnect();
                connectService(mContext);
            } catch (AtModule2.YpModuleException e) {
                e.printStackTrace();
                LtLog.i("screencap read package exception:" + e.getMessage());
                mNetWork.disconnect();
                connectService(mContext);
            }
        }
        return screenInfo;
    }

    private ExecutorService captureService = Executors.newSingleThreadExecutor();

    public ScreenInfo captureRawScreen(final ScreenInfo screenInfo) {
        if (Utils.isInMainThread()) {
            final Object obj = new Object();
            captureService.submit(
                    new Runnable() {
                        @Override
                        public void run() {
                            screencap(screenInfo);
                            synchronized (obj) {
                                obj.notify();
                            }
                        }
                    });
            synchronized (obj) {
                try {
                    obj.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return screenInfo;
        } else {
            return screencap(screenInfo);
        }
    }


}
