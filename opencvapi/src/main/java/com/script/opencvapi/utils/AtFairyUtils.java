package com.script.opencvapi.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Debug;
import android.view.inputmethod.EditorInfo;

import com.script.opencvapi.AtFairyConfig;

import okhttp3.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by     on 2018/3/16.
 */
public class AtFairyUtils {
    private static int INPUT_PORT = 13600;
    private static final String REPORT_TASK_STATUS = "ReportTaskStatus";

    private static String imagePath = null ;

    public static final void setImagePath(String path){
        imagePath = path ;
    }

    private static AtFairyUtils sInstance;
    private Context mContext;

    public static AtFairyUtils getInstance() {
        return sInstance;
    }

    public AtFairyUtils(Context context) {
        sInstance = this;
        mContext = context;
    }


    public byte[] getTemplateData(String name) {
        InputStream in = null;
        try {
            if (imagePath == null) {
                AssetManager assetManager = mContext.getAssets();
                in = assetManager.open(name);
            } else {
                String file = imagePath + "/" + name;
                in = new FileInputStream(file);
            }
            int len = in.available();
            byte[] b = new byte[len];
            in.read(b);
            return b;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public long getUsrGameMem() {
        String packageName = AtFairyConfig.getGamePackage();
        return getPackageMem(packageName);
    }

    public long getPackageMem(String packageName) {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        long memSize = 0;
        if (list != null && packageName != null) {
            for (int i = 0; i < list.size(); ++i) {
                ActivityManager.RunningAppProcessInfo processInfo = list.get(i);
                if (processInfo.processName.equals(packageName)) {
                    int[] memoryPid = new int[]{processInfo.pid};
                    Debug.MemoryInfo[] memoryInfo = activityManager
                            .getProcessMemoryInfo(memoryPid);
                    memSize = memoryInfo[0].nativePrivateDirty + memoryInfo[0].dalvikPrivateDirty;
                    break;
                }
            }
        }
        return memSize;
    }

    public long getPackageNativeMem(String packageName) {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        long memSize = 0;
        if (list != null && packageName != null) {
            for (int i = 0; i < list.size(); ++i) {
                ActivityManager.RunningAppProcessInfo processInfo = list.get(i);
                if (processInfo.processName.equals(packageName)) {
                    int[] memoryPid = new int[]{processInfo.pid};
                    Debug.MemoryInfo[] memoryInfo = activityManager
                            .getProcessMemoryInfo(memoryPid);
                    memSize = memoryInfo[0].nativePrivateDirty;
                    break;
                }
            }
        }
        return memSize;
    }

    public long getPackageJvmMem(String packageName) {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        long memSize = 0;
        if (list != null && packageName != null) {
            for (int i = 0; i < list.size(); ++i) {
                ActivityManager.RunningAppProcessInfo processInfo = list.get(i);
                if (processInfo.processName.equals(packageName)) {
                    int[] memoryPid = new int[]{processInfo.pid};
                    Debug.MemoryInfo[] memoryInfo = activityManager
                            .getProcessMemoryInfo(memoryPid);
                    memSize = memoryInfo[0].dalvikPrivateDirty;
                    break;
                }
            }
        }
        return memSize;
    }

    public long getFreeMem() {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();

        activityManager.getMemoryInfo(info);
        return info.availMem >> 10;
    }

    public static void inputText(final String text) {
        final RemoteInput remoteInput = new RemoteInput("127.0.0.1", INPUT_PORT);
        remoteInput.start(new RemoteInput.RemoteCallback() {
            @Override
            public void onConnected() {
                remoteInput.onText(text);
                remoteInput.stop();
            }

            @Override
            public void onStartInputView(EditorInfo info, boolean restarting) {

            }

            @Override
            public void onFinishedInput() {

            }
        });
    }

    /**
     * 上报脚本状态
     */
    public String postState(int type, String taskId, int state) {
        String result = null;
        try {
            FormBody.Builder builder = new FormBody.Builder();
            builder.add("cn_id", AtFairyConfig.getCNID());
            builder.add("as_id", AtFairyConfig.getASID());
            builder.add("game_id", AtFairyConfig.getGameID());
            builder.add("channel_id", AtFairyConfig.getChannelID());
            if (taskId != null) {
                builder.add("task_id", taskId);
            }
            builder.add("task_type", type + "");
            builder.add("task_state", state + "");
            RequestBody formBody = builder.build();
            Request request = new Request.Builder()
                    .url(AtFairyConfig.getServerUrl() + REPORT_TASK_STATUS)
                    .post(formBody)
                    .build();
            OkHttpClient client = new OkHttpClient();
            Response response = null;
            try {
                response = client.newCall(request).execute();
                result = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    public byte[] getTemplateDataFromAssert(String name){
        InputStream in  = null;
        try {
            in = mContext.getAssets().open(name) ;
            int len = in.available();
            byte[] b = new byte[len];
            in.read(b);
            return b;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return  null ;
    }

}
