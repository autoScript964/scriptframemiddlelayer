package com.script.framework;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.script.content.Assets;
import com.script.content.CUtils;
import com.script.content.DTimer;
import com.script.content.LocalInterruptThread;
import com.script.content.MatOpt;
import com.script.content.MatchEngine;
import com.script.content.ScProxy;
import com.script.fairy.content.CompatPicFinder;
import com.script.opencvapi.FindResult;
import com.script.opencvapi.LtLog;
import com.script.opencvapi.ScreenInfo;
import com.script.opencvapi.AtFairy2;
import com.script.opencvapi.AtFairyConfig;
import com.script.opencvapi.AtFairyService;
import com.script.opencvapi.utils.TemplateInfo;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Administrator on 2019/1/23 0023.
 */
@SuppressWarnings("all")
public class AtFairyImpl extends AtFairy2 {
    /**
     * arr?????????
     */
    public static final float VERSION = AtFairy2.VERSION;
    /**
     * ????????????
     */
    private Thread mTaskThread;
    /**
     * ????????????
     */
    private Thread mErroThread;
    /**
     * ????????????????????????
     */
    private boolean mTaskRunning = false;
    /**
     * ?????????????????????
     */
    private static final String GAME_TASK_MAINCLASS = "com.script.fairy.TaskMain";
    /**
     * ????????????????????????
     */
    private static final String GAME_TASK_MAINFUNC = "main";
    /**
     * ???????????????????????????
     */
    private static final String GAME_TASK_ERROCLASS = "com.script.fairy.Abnormal";

    /**
     * ??????????????????????????????
     */
    private static final String GAME_TASK_ERROFUNC = "erro";
    /**
     * ???????????????????????????
     */
    private Object mMainObj;
    private Method method;
    private Constructor c;
    private Object mMainObjErro;
    private Method methodErro;
    private Constructor cErro;

    /**
     * ???????????????
     */
    private int gameVersion = 1;
    /**
     * ????????????
     */
    private String gameName = "game";
    /**
     * ????????????
     */
    //public ScreenInfo screenInfo = new ScreenInfo();
    TessBaseAPI tessBaseAPI;


    static {
        System.out.println("================== loadLibrary  ");
    }

    public AtFairyImpl(Context context, Intent intentResult) {
        super(context, intentResult);
        ScProxy.init(context);
        ScProxy.assets().setMatPreProcesser(new Assets.IMatPreProcesser() {
            @Override
            public Mat onPreProcessing(@NonNull byte[] assetsFileData, @Nullable MatOpt preset) {
                final Mat buf = new MatOfByte(assetsFileData);
                Mat mat = Imgcodecs.imdecode(buf, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
                buf.release();
                /*
                 * ????????????
                 *
                 *  ????????????????????????????????????????????????
                 *  1 HLS???
                 *      1)?????????RGB??????;
                 *      2)????????????BGR2HLS, ??????BGR???????????????????????????HLS;
                 *      3)??????imwrite??????????????????????????????RB??????
                 *  2 HSV??????
                 *
                 *  ????????????
                 *  1 ?????????BGR????????????
                 *  2 ?????????BGR???
                 */
                if (preset.getMethod() == MatOpt.Method.BGR2HLS) {
                    // ????????????HLS???BGR(???????????????RGB)
                    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_HLS2BGR);
                    // ???RGB?????????HLS ????????????????????????RGB???BGR
                    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2HLS);

                } else if (preset.getMethod() == MatOpt.Method.BGR2HSV) {
                    // ????????????HSV???BGR(???????????????RGB)
                    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_HSV2BGR);
                    // ???RGB?????????HSV ????????????????????????RGB???BGR
                    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2HSV);
                }

                return mat.empty() ? null : mat;
            }
        });

        /*ADDED BY      2021/05/07*/
        initProcessCenter();
        /*ADDED END. */

        if (AtFairyService.isApkInDebug(getContext())) {
            keepalive(false);
        }





        LtLog.e("??????????????????" + VERSION);
    }




    /**
     * ??????????????????
     *
     * @param gameName ????????????
     */
    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    /**
     * ?????????????????????
     *
     * @param gameVersion ???????????????
     */
    public void setGameVersion(int gameVersion) {
        this.gameVersion = gameVersion;
    }

    /*ADD BY      2021/05/07 */
    private static final String TASK_THREAD_NAME = "TaskThread";
    private static final String ABNORMAL_THREAD_NAME = "AbnormalThread";

    private void initProcessCenter() {
        ServiceCore.getInstance().createWorker(TASK_THREAD_NAME, new ServiceCore.Worker<String>() {
            int count = 0;

            @Override
            public void onWorking(String s) throws InterruptedException {
                try {
                    dispatchCompatEvent(OnCompatFiaryEvent.TYPE_NEW_TASK_START0);
                    LtLog.d("auto", "next round start at " + (++count));

                    if (TextUtils.isEmpty(s)) {
                        return;
                    }

                    LtLog.d("auto", "round exec at " + count);
                    LtLog.d("auto", "task : " + s);
                    Class cls = Class.forName(GAME_TASK_MAINCLASS);
                    c = cls.getConstructor(AtFairyImpl.this.getClass());//??????????????????
                    mMainObj = c.newInstance(AtFairyImpl.this);
                    method = cls.getMethod(GAME_TASK_MAINFUNC);
                    dispatchCompatEvent(OnCompatFiaryEvent.TYPE_NEW_TASK_START1);
                    method.invoke(mMainObj);
                    LtLog.d("auto", " Loop start: print3");
                } catch (Exception e) {

                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw, true));
                    String str = sw.toString();


                    LtLog.e("auto", str);
                    if (
                            e instanceof InterruptedException ||
                            (e.getCause() != null && e.getCause() instanceof InterruptedException) ||
                            LocalInterruptThread.isCurrentLocalInterrupted()
                    ) {
                        Log.d("auto", "round finish at " + count);
                        e.printStackTrace();
                        throw new InterruptedException();
                    } else {
                        // re-run
                        Thread.sleep(100);
                        onWorking(s);
                    }
                }
            }
        });

        ServiceCore.getInstance().createWorker(ABNORMAL_THREAD_NAME, new ServiceCore.Worker<String>() {
            int count = 0;

            /**
             * The Time.
             */
            long time = 0;
            /**
             * The Now time 2.
             */
            String nowTime2 = "??????????????????";
            /**
             * The Now date 2.
             */
            String nowDate2 = "";
            /**
             * The Format 1.
             */
            SimpleDateFormat format1 = new SimpleDateFormat("HH:mm:ss");
            /**
             * The Df 1.
             */
            DateFormat df1 = DateFormat.getDateInstance();//???????????????????????????

            @Override
            public void onWorking(String s) throws InterruptedException {
                nowTime2 = format1.format(new Date());
                nowDate2 = df1.format(new Date());

                try {
                    Class cls1 = Class.forName(GAME_TASK_ERROCLASS);
                    cErro = cls1.getConstructor(AtFairyImpl.this.getClass());//??????????????????
                    mMainObjErro = cErro.newInstance(AtFairyImpl.this);
                    methodErro = cls1.getMethod(GAME_TASK_ERROFUNC);
                    while (true) {
                        if (TextUtils.isEmpty(s)) {
                            return;
                        }
                        if (System.currentTimeMillis() - time > 45000) {
                            LtLog.e("---------------" + gameName + "?????????--" + gameVersion + "--???????????????????????????===" + nowDate2 + ":" + nowTime2);
                            LtLog.e("---------------????????????===" + df1.format(new Date()) + ":" + format1.format(new Date()));
                            time = System.currentTimeMillis();
                        }
                        DTimer.ThreadLocalManager.newTimer("--abnormal--");
                        methodErro.invoke(mMainObjErro);
                        DTimer.ThreadLocalManager.dumpCurrent();
                    }
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw, true));
                    String str = sw.toString();
                    LtLog.e("auto", str);
                    if (
                            e instanceof InterruptedException ||
                                    (e.getCause() != null && e.getCause() instanceof InterruptedException) ||
                                    LocalInterruptThread.isCurrentLocalInterrupted()
                    ) {
                        LtLog.d("auto", "round finish at " + count);
                        e.printStackTrace();
                        throw new InterruptedException();
                    } else {
                        // re-run
                        Thread.sleep(100);
                        onWorking(s);
                    }
                }

            }
        });
    }

    public void switchTask(String taskConfigs) {
        ServiceCore.getInstance().restart(TASK_THREAD_NAME, taskConfigs);
        ServiceCore.getInstance().restart(ABNORMAL_THREAD_NAME, taskConfigs);
    }
    /*ADDED END. */


    /**
     * ???????????????????????????????????????????????????
     */
    @Override
    public void onStart() {
        if(isInCompatMode()) {
            switchOnComaptTask();
        }
    }

    public boolean isInCompatMode() {
        return !getArguments().getBoolean(AtFairyService.INTENT_KEY_CUSTOM_LOCAL_SERVICE);
    }

    private void switchOnComaptTask() {
        AtFairyConfig.setUseCustomOptionProxy(false);
        switchTask(AtFairyConfig.getUserTaskConfig());
    }

    /**
     * ??????????????????????????????????????????
     */
    @Override
    public void onRestart() {
        LtLog.e("------------+++--------- onRestart.....");
        Log.d("auto", " -----------------------onRestart....." + mTaskRunning);
        killUserGame();
        Log.d("auto", " -----------------------onRestart2....." + mTaskRunning);
    }

    /**
     * ???????????????????????????
     */
    @Override
    public void onStop() {
        if(isInCompatMode()) {
            switchTask("");
        }
    }

    /**
     * ???????????????????????????
     */
    @Override
    public void onResume() {
        if(isInCompatMode()) {
            switchOnComaptTask();
        }
    }

    /**
     * ???????????????????????????
     */
    @Override
    public void onPause() {
        if(isInCompatMode()) {
            switchTask("");
        }
    }

    @Override
    public void onChangeConfig() {

    }

    /**
     * ?????????????????????
     *
     * @return false ???????????????
     */
    @Override
    public boolean onMonitorState(int i) {
        LtLog.e("-------??????????????????i=" + i);
        return false;
    }

    @Override
    public void onCheckStop() {

    }

    @Override
    public void onCheckStart() {

    }

    @Override
    public void onExit() {
        LtLog.d("auto", "onExit-enter");
        LtLog.d("auto", "onExit-compatMode = " + isInCompatMode());
        if(!isInCompatMode()){
            onUserFinished();
            LtLog.d("auto", "onExit-compatMode");
        }
        ScProxy.destroy();
        ServiceCore.getInstance().destroy();
        LtLog.d("auto", "onExit-exit");
    }

    public void requestExit(){
        Intent intent = new Intent();
        intent.setAction(AtFairyService.ACTION_EXIT);
        getContext().sendBroadcast(intent);
    }

    /**
     * ?????????????????????
     *
     * @param taskId
     * @param state  99???????????????????????????
     */
    public void finish(String taskId, int state) throws Exception {
        LtLog.d("auto", "Finish# enter: isInCompatMode-" + isInCompatMode() + ", mFairyProxy!=null-" + (mFairyProxy != null));
        /* ADDED BY      on 2021-03-23. */
        if (mFairyProxy != null) {
            mFairyProxy.finish(taskId, state);
            return;
        }
        /* ADDED END. */
        super.finish(FAIRY_TYPE_TASK, taskId, state);
        onStop();
        CUtils.sleep(2000);
    }

    /**
     * @throws Exception ????????????1????????????????????????
     */
    public void restart() throws Exception {
        CUtils.sleep(60000);
        onPause();
        onResume();
        CUtils.sleep(2000);
    }


    public boolean condit() throws Exception {
        final Thread thread = Thread.currentThread();
        final boolean isLocalInterrupted =  LocalInterruptThread.isCurrentLocalInterrupted();
        return !isLocalInterrupted;
    }


    /**
     * ?????????????????????
     *
     * @param type   ????????????  ???????????????FAIRY_TYPE_TASK    ????????????FAIRY_TYPE_CHECK?????????
     * @param taskId
     * @param state  99???????????????????????????
     */
    @Override
    public void finish(int type, String taskId, int state) {
        super.finish(type, taskId, state);
        // stop();
    }


    /**
     * ??????????????????
     *
     * @param leftX   the left x
     * @param leftY   the left y
     * @param rightX  the right x
     * @param rightY  the right y
     * @param picName the pic name
     * @return the find result
     * @throws Exception the exception
     */
    private boolean isCaching;
    private long cachingTime;

    public FindResult findPic(int x_1, int y_1, int x_2, int y_2, String picName) throws Exception {
        return CompatPicFinder.findPicWithTempalteInfoInRegion(currentThreadDTimer(), MatchEngine.Region.create().startAt(x_1, y_1)
                .expandWith(x_2 - x_1, y_2 - y_1), picName);
    }


    /**
     * ?????????????????????(???????????????????????????)
     *
     * @param picName the pic name
     * @return the find result
     * @throws Exception the exception
     */
    public FindResult findPic(String picName) throws Exception {
        return CompatPicFinder.findPicWithTempalteInfo(currentThreadDTimer(), picName);
    }


    /**
     * ????????????????????????????????????????????????
     *
     * @param x_1     the x 1
     * @param y_1     the y 1
     * @param x_2     the x 2
     * @param y_2     the y 2
     * @param picName new String[]{"test.png","test1.png",.......}
     * @return the find result
     * @throws Exception the exception
     */
    public FindResult findPic(int x_1, int y_1, int x_2, int y_2, String[] picName) throws Exception {
        return CompatPicFinder.findPicWithTempalteInfoInRegion(
                currentThreadDTimer(), MatchEngine.Region.create().startAt(x_1, y_1)
                        .expandWith(x_2 - x_1, y_2 - y_1), picName);
    }

    /**
     * ???????????????????????????????????????
     *
     * @param x_1     the x 1
     * @param y_1     the y 1
     * @param x_2     the x 2
     * @param y_2     the y 2
     * @param sim     the sim
     * @param picName new String[]{"test.png","test1.png",.......}
     * @throws Exception the exception
     */
    public void findPic(int x_1, int y_1, int x_2, int y_2, float sim, String[] picName) throws Exception {
        FindResult result;
        for (int i = 0; i < picName.length; i++) {
            result = CompatPicFinder.findPicWithTempalteInfoInRegion(currentThreadDTimer(), MatchEngine.Region.create().startAt(x_1, y_1)
                    .expandWith(x_2 - x_1, y_2 - y_1), picName[i]);
            onTap(sim, result, picName[i], 1000);
        }
    }

    /**
     * ???????????????????????????????????????
     *
     * @param picName new String[]{"test.png","test1.png",.......}
     * @return the find result
     * @throws Exception the exception
     */
    public FindResult findPic(String[] picName) throws Exception {
        return CompatPicFinder.findPicWithTempalteInfo(picName);
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param sim     the sim
     * @param picName new String[]{"test.png","test1.png",.......}
     * @throws Exception the exception
     */
    public void findPic(float sim, String[] picName) throws Exception {
        FindResult result;
        for (int i = 0; i < picName.length; i++) {
            result = CompatPicFinder.findPicWithTempalteInfo(currentThreadDTimer(), picName[i]);
            onTap(sim, result, picName[i], 1000);
        }
    }

    /**
     * @param expansion
     * @param picName
     * @throws Exception
     */
    public void findPicWithExpansion(int expansion, String... picName) throws Exception {
        FindResult result;
        for (int i = 0; i < picName.length; i++) {
            result = CompatPicFinder.findPicWithTempalteInfoExpanded(currentThreadDTimer(), expansion, picName[i]);
            onTap(0.8f, result, picName[i], 1000);
        }
    }


    /**
     * ???????????????????????????????????????????????????
     *
     * @param sim    the sim
     * @param result the result
     * @param string ???????????????
     * @param time   ??????????????????????????????
     * @throws Exception the exception
     */
    public void onTap(float sim, FindResult result, String string, long time) throws Exception {
        if (result.sim >= sim) {
            int x = new Random().nextInt(result.width) + result.x;
            int y = new Random().nextInt(result.height) + result.y;
            this.tap(x, y);
            StackTraceElement ste = new Throwable().getStackTrace()[1];
            LtLog.e(ste.getFileName() + ": Line " + ste.getLineNumber() + ":sim=" + result.sim + ": IntX=" + x + ": IntY=" + y + ":String=" + string);
            CUtils.sleep(time);
            //getScreenMat();
        }
    }


    /**
     * ?????????????????????????????????????????????
     *
     * @param sim    the sim
     * @param result the result
     * @param leftX  the left x
     * @param leftY  the left y
     * @param rightX the right x
     * @param rightY the right y
     * @param string ???????????????
     * @param time   ??????????????????????????????
     * @throws Exception the exception
     */
    public void onTap(float sim, FindResult result, int leftX, int leftY, int rightX, int rightY, String string, long time) throws Exception {
        int x, y;
        if (result.sim >= sim) {
            if ((rightX - leftX) < 1 || (rightY - leftY) < 1) {
                x = leftX;
                y = leftY;
            } else {
                x = new Random().nextInt(rightX - leftX) + leftX;
                y = new Random().nextInt(rightY - leftY) + leftY;
            }
            this.tap(x, y);
            StackTraceElement ste = new Throwable().getStackTrace()[1];
            LtLog.e(ste.getFileName() + ": Line " + ste.getLineNumber() + ":sim=" + result.sim + ": IntX=" + x + ": IntY=" + y + ":String=" + string);
            CUtils.sleep(time);
            //getScreenMat();
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param leftX  the left x
     * @param leftY  the left y
     * @param rightX the right x
     * @param rightY the right y
     * @param string ???????????????
     * @param time   ??????????????????????????????
     * @throws Exception the exception
     */
    public void onTap(int leftX, int leftY, int rightX, int rightY, String string, long time) throws Exception {
        int x, y;
        if ((rightX - leftX) < 1 || (rightY - leftY) < 1) {
            x = leftX;
            y = leftY;
        } else {
            x = new Random().nextInt(rightX - leftX) + leftX;
            y = new Random().nextInt(rightY - leftY) + leftY;
        }
        this.tap(x, y);
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        LtLog.e(ste.getFileName() + ": Line " + ste.getLineNumber() + ": IntX=" + x + ": IntY=" + y + ":String=" + string);
        CUtils.sleep(time);

//        getScreenMat();
    }

    public String getLineInfo(float usim, FindResult result, String str) throws Exception {
        int x1, y1;
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        if (result.sim >= usim) {
            x1 = result.x;
            y1 = result.y;
            return ste.getFileName() + ": Line " + ste.getLineNumber() + ":sim=" + result.sim + ": IntX=" + x1 + ": IntY=" + y1 + ":img=" + str;
        }
        return "     ";
    }


    public String getLineInfo(String str) throws Exception {
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        return ste.getFileName() + ": Line " + ste.getLineNumber() + ":other==" + str;
    }

    /**
     * ?????????????????????????????????sim??????0.9 ???    ???????????????????????????  ?????????????????????
     *
     * @param x         the x
     * @param y         the y
     * @param x_1       the x 1
     * @param y_1       the y 1
     * @param strColor  the str color   "RGB"
     * @param simDouble the sim double
     * @return the color num
     * @throws Exception the exception
     *                   <p>
     */
    @Deprecated
    public int getColorNum(int x, int y, int x_1, int y_1, String strColor, double simDouble) throws Exception {

        final ScreenInfo screenInfo = ScProxy.captureEngine().getCurrentScreenInfo();
        if (screenInfo == null || screenInfo.height > 720) {
            return 0;
        }
        double[] match = new double[3];
        match[0] = Double.parseDouble(strColor.split(",")[2]);
        match[1] = Double.parseDouble(strColor.split(",")[1]);
        match[2] = Double.parseDouble(strColor.split(",")[0]);
        double simValue = 255 * (1 - simDouble);
        double min_r = match[0] - simValue;
        double min_g = match[1] - simValue;
        double min_b = match[2] - simValue;
        double max_r = match[0] + simValue;
        double max_g = match[1] + simValue;
        double max_b = match[2] + simValue;
        if (min_r < 0) {
            min_r = 0;
        }
        if (min_g < 0) {
            min_g = 0;
        }
        if (min_b < 0) {
            min_b = 0;
        }
        if (max_r > 255) {
            max_r = 255;
        }
        if (max_g > 255) {
            max_g = 255;
        }
        if (max_b > 255) {
            max_b = 255;
        }

        Mat mat;

        while (true) {
            mat = this.getScreenMat(x, y, x_1 - x, y_1 - y, 1, 0, 0, 1, screenInfo);
            if (mat != null) {
                break;
            }
        }
        Scalar minValues = new Scalar(min_r, min_g, min_b);
        Scalar maxValues = new Scalar(max_r, max_g, max_b);
//        System.out.println("------------" + min_r + "," + min_g + "," + min_b + "," + max_r + "," + max_g + "," + max_b + ",mat=" + mat);
        Core.inRange(mat, minValues, maxValues, mat);
        int number = Core.countNonZero(mat);
        mat.release();

        return number;
    }

    /**
     * ?????????????????????????????????sim??????0.9?????????????????????
     *
     * @param str   the str   X1???Y1???X2???Y2??????
     * @param color the color ??????RGB???
     * @param sim   the sim
     * @param type  the type ??????1?????????????????? ??????0??????????????????
     * @return the color num
     * @throws Exception the exception
     */
    @Deprecated
    public int getColorNum(String str, String color, double sim, int type) throws Exception {
        int Nownum = 0;
        byte[] rawpic = ScProxy.captureEngine().getCurrentScreenInfo().raw;
        //???????????????????????????
        String[] arr = str.split(",");
        String[] btt = color.split(",");
        String setr = btt[0];
        String setg = btt[1];
        String setb = btt[2];
        int setir = Integer.parseInt(setr);
        int setig = Integer.parseInt(setg);
        int setib = Integer.parseInt(setb);
        double maxrRange = (1 - sim) * setir;
        double maxgRange = (1 - sim) * setig;
        double maxbRange = (1 - sim) * setib;
        int minw = Integer.parseInt(arr[0]);
        int maxw = Integer.parseInt(arr[2]);
        int minh = Integer.parseInt(arr[1]);
        int maxh = Integer.parseInt(arr[3]);
        int Totalspot = (maxw - minw) * (maxh - minh);
        //  LtLog.e("????????????" + Totalspot);
        int w = 1280;
        if (type == 1) {
            w = 736;
        }
        int i = (w * 4) * Integer.parseInt(arr[1]) + Integer.parseInt(arr[0]) * 4;
        int proit = i;
        int eachrow = maxw - minw;
        int nowspot = 0;
        int begin = 0;
        do {
            nowspot = nowspot + 1;
            String sr = String.valueOf(rawpic[i] & 0xff);
            String sg = String.valueOf(rawpic[i + 1] & 0xff);
            String sb = String.valueOf(rawpic[i + 2] & 0xff);
//            LtLog.e("?????????"+nowspot+"??????");
//            LtLog.e("---------------------------R=======" + String.valueOf(rawpic[i] & 0xff));
//            LtLog.e("---------------------------G=======" + String.valueOf(rawpic[i + 1] & 0xff));
//            LtLog.e("---------------------------B=======" + String.valueOf(rawpic[i + 2] & 0xff));
            int ir = Integer.parseInt(sr);
            int ig = Integer.parseInt(sg);
            int ib = Integer.parseInt(sb);
            if (setir - maxrRange <= ir && ir <= setir + maxrRange && setig - maxgRange <= ig && ig <= setig + maxgRange && ib >= setib - maxbRange && ib <= setib + maxbRange) {
                Nownum = Nownum + 1;
            }
            begin = begin + 1;
            if (begin >= eachrow) {
                proit = w * 4 + proit;
                i = proit;
                begin = 0;
            } else {
                i = i + 4;
            }
        } while (nowspot < Totalspot);
        //  LtLog.e("????????????????????????" + Nownum);
        return Nownum;
    }


    /**
     * ??????
     *
     * @param x         ????????????
     * @param y         ????????????
     * @param x1        ????????????
     * @param y1        ????????????
     * @param moveSleep ??????????????????????????????
     * @param stopSleep ????????????????????????????????????
     * @throws Exception
     */
    @Deprecated
    public void ranSwipe(int x, int y, int x1, int y1, int moveSleep, long stopSleep) throws Exception {
        touchDown(0, x, y);
        touchMove(0, x1, y1, moveSleep);
        touchUp(0);
        CUtils.sleep(stopSleep);
        condit();
    }

    public void ranSwipe(int x, int y, int x1, int y1, int moveSleep, long stopSleep, int id) throws Exception {
        touchDown(id, x, y);
        touchMove(id, x1, y1, moveSleep);
        touchUp(id);
        CUtils.sleep(stopSleep);
        condit();
    }

    /**
     * ???????????????
     *
     * @param x     the x
     * @param y     the y
     * @param x1    the x 1
     * @param y1    the y 1
     * @param dir   the dir ir = 0?????????????????????dir = 1?????????????????????dir = 2?????????????????????dir = 3??????????????????
     * @param sleep the sleep ????????????
     * @throws Exception the exception
     *                   <p>
     *                   ?????????????????????????????????????????????
     */
    @Deprecated
    public void ranSwipe(int x, int y, int x1, int y1, int dir, int sleep, long sleep1) throws Exception {
        if (dir == 0) {
            int result = x + (int) (Math.random() * ((x1 - x) + 1));
            this.touchDown(result, y);
            this.touchMove(result, y1, sleep);
            this.touchUp();
        } else if (dir == 1) {
            int result = y + (int) (Math.random() * ((y1 - y) + 1));
            this.touchDown(x, result);
            this.touchMove(x1, result, sleep);
            this.touchUp();
        } else if (dir == 2) {
            int result = x + (int) (Math.random() * ((x1 - x) + 1));
            this.touchDown(result, y1);
            this.touchMove(result, y, sleep);
            this.touchUp();
        } else if (dir == 3) {
            int result = y + (int) (Math.random() * ((y1 - y) + 1));
            this.touchDown(x1, result);
            this.touchMove(x, result, sleep);
            this.touchUp();
        }
        CUtils.sleep(sleep1);
        condit();
    }

    /**
     * ??????????????????????????????????????????????????????
     *
     * @param x1 the x 1
     * @param y1 the y 1
     * @param x2 the x 2
     * @param y2 the y 2
     * @return the string  ??????????????????0???0  ??????????????????????????????
     * @throws Exception the exception
     */
    public String change(int x1, int y1, int x2, int y2) throws Exception {

        Core.MinMaxLocResult mmr;
        int width;
        int height;
        width = x2 - x1;
        height = y2 - y1;
        Mat mat1 = this.getScreenMat(x1, y1, width, height, 1, 0, 0, 1);
        for (int i = 0; i < 10; i++) {
            Mat mat2 = this.getScreenMat(x1, y1, width, height, 1, 0, 0, 1);
            Mat dst = new Mat();
            Core.absdiff(mat1, mat2, dst);
            Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGB2GRAY);
            Imgproc.threshold(dst, dst, 0, 0, Imgproc.THRESH_TOZERO);
            mmr = Core.minMaxLoc(dst);
            if (mmr.maxLoc.x > 0) {
                return ((int) mmr.maxLoc.x + x1) + "," + ((int) mmr.maxLoc.y + y1);
            }
        }
        return "0,0";

    }


    /**
     * ?????????????????????????????????????????????????????????0???????????????
     *
     * @param x_1    the x 1
     * @param y_1    the y 1
     * @param width  the width
     * @param height the height
     * @param sim    the sim
     * @return the long  ????????????
     * @throws Exception the exception
     */
    private Mat mat1, mat2;
    private long timex, time;

    @Deprecated
    public long mMatTime(int x_1, int y_1, int width, int height, double sim) throws Exception {
        /*
         ?????????????????????????????????
         */
        boolean matSim = false;

        ScreenInfo screenInfo = ScProxy.captureEngine().getCurrentScreenInfo();
        if (screenInfo == null || screenInfo.height > 720) {
            LtLog.e("----screenInfo error ---");
            return 0;
        }
        if (mat1 != null) {
            mat1.release();
        }
        mat1 = this.getScreenMat(x_1, y_1, width, height, 1, 0, 0, 1, screenInfo);
        if (mat2 != null && mat1 != null) {
            // LtLog.e(getLineInfo(  "----------------------------mat1.rows=>" + mat1.rows()  + ",mat2.rows="+ mat2.rows()));
            /* try {*/
            matSim = judgeMatAndMatChange(sim, mat1, mat2);

         /*   } catch (Exception e) {
//                LtLog.i(publicFunction.getLineInfo() + "----------------------------matSim>" + e.toString());
            }*/
            mat1.release();
            //???????????????????????????????????? sim ????????? true;
        }
        if (matSim) {
            // LtLog.e(getLineInfo("????????????????????????"));
//            LtLog.i(publicFunction.getLineInfo() + "----------------------------matSim>" + matSim + ",timex=" + timex + ",time=" + time);
        } else {
            //??????????????????????????? ????????????
//            LtLog.i(publicFunction.getLineInfo() + "----------------------------matSim>" + matSim );
            time = System.currentTimeMillis() / 1000;
            if (mat2 != null) {
                mat2.release();
            }
            mat2 = this.getScreenMat(x_1, y_1, width, height, 1, 0, 0, 1);
            return 0;
        }
        timex = System.currentTimeMillis() / 1000 - time;
        return timex;
    }


    public long mMatTime(int leftX, int leftY, int width, int height, float sim) throws Exception {
        FindResult result = new FindResult();
        result.sim = 0.1f;
        if (mat1 != null) {
            mat1.release();
        }
        mat1 = getScreenMat(leftX, leftY, width, height, 1, 0, 0, 1);
        if (mat2 != null && mat1 != null) {
            result = matchMat(leftX, leftY, mat1, mat2);
            mat1.release();
            //???????????????????????????????????? sim ????????? true;
        }
        if (result.sim >= sim) {
            // LtLog.e(getLineInfo("????????????????????????"));
//            LtLog.i(publicFunction.getLineInfo() + "----------------------------matSim>" + matSim + ",timex=" + timex + ",time=" + time);
        } else {
            //??????????????????????????? ????????????
//            LtLog.i(publicFunction.getLineInfo() + "----------------------------matSim>" + matSim );
            time = System.currentTimeMillis() / 1000;
            if (mat2 != null) {
                mat2.release();
            }
            mat2 = getScreenMat(leftX, leftY, width, height, 1, 0, 0, 1);
            return 0;
        }
        timex = System.currentTimeMillis() / 1000 - time;
        return timex;
    }

    /**
     * ?????????MatTime?????????
     */
    public void initMatTime() {
        if (mat2 != null) {
            mat2.release();
        }
        if (mat1 != null) {
            mat1.release();
        }
        mat2 = null;
        mat1 = null;
    }

    /**
     * ???????????????????????????????????? sim ????????? true;
     */
    private boolean judgeMatAndMatChange(double sim, Mat mat, Mat tempMat) throws Exception {
        //???????????????????????????????????? sim ????????? true;
        boolean state = false;
        Mat dstMat = new Mat(), dst1 = new Mat(), dst2 = new Mat();
        if (mat.channels() == 3 || tempMat.channels() == 3) {
            Imgproc.cvtColor(mat, dst1, Imgproc.COLOR_RGB2HLS);
            Imgproc.cvtColor(tempMat, dst2, Imgproc.COLOR_RGB2HLS);
        }
        Imgproc.matchTemplate(dst1, dst2, dstMat, Imgproc.TM_CCOEFF_NORMED);
        Core.MinMaxLocResult mmr;
        mmr = Core.minMaxLoc(dstMat);
        if (mmr.maxVal >= sim) {
            state = true;
        }
        dstMat.release();
        dst1.release();
        dst2.release();
        return state;
    }

    /**
     * ??????????????????????????????????????????????????? x,x_1,y,y_1   ????????????
     *
     * @param x         the x
     * @param y         the y
     * @param x_1       the x 1
     * @param y_1       the y 1
     * @param img       the img
     * @param sim       the sim
     * @param step      the step ??????  ????????????????????????
     * @param max       the max  ????????????????????????????????????
     * @param direction the direction 1??????x????????????  2??????Y??????
     * @return the find result
     * @throws Exception the exception
     */
    public FindResult findPic(int x, int y, int x_1, int y_1, String img, float sim, int step, int max, int direction) throws Exception {
        FindResult result;
        Mat mat = ScProxy.captureEngine().getCurrentScreenMat();
        Mat mat1 = getTemplateMat(img);
        TemplateInfo templateInfo = getTemplateInfo(img);
        int js_1 = 0;
        while (direction == 1) {
            //    LtLog.e("?????????=" + x+","+y+","+x_1+","+y_1);
            if ((x_1 - x) > templateInfo.width) {
                Rect rect = new Rect(0, 0, x_1 - x, y_1 - y);
                Mat mat2 = new Mat(mat, rect);
                result = matchMat(x, y, mat1, mat2);
                if (result.sim > sim) {
                    result.x = x + result.x;
                    result.y = y + result.y;
                    //     LtLog.e("result.w=" + result.width+",result.h"+result.height);
                    return result;
                }
            }
            x_1 = x_1 + step;
            if (x_1 >= 1280) {
                return null;
            }
            js_1++;
            if (js_1 > max) {
                return null;
            }
        }
        while (direction == 2) {
            //  LtLog.e("?????????=" + x+","+y+","+x_1+","+y_1);
            if ((y_1 - y) > templateInfo.height) {
                Rect rect = new Rect(x, y, x_1 - x, y_1 - y);
                Mat mat2 = new Mat(mat, rect);
                result = matchMat(0, 0, mat1, mat2);
                if (result.sim > sim) {
                    //    LtLog.e("result.w=" + result.width+",result.h"+result.height);
                    result.x = x + result.x;
                    result.y = y + result.y;
                    return result;
                }
            }
            y_1 = y_1 + step;
            if (y_1 >= 720) {
                return null;
            }
            js_1++;
            if (js_1 > max) {
                return null;
            }
        }
        return null;
    }

    /**
     * ????????????
     *
     * @return the int
     */
    public int week() {
        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.DAY_OF_WEEK) == 1) {
            return 7;
        } else {
            return (cal.get(Calendar.DAY_OF_WEEK)) - 1;
        }
    }

    /**
     * ????????????
     *
     * @return the int
     */
    public int dateHour() {
        SimpleDateFormat format1 = new SimpleDateFormat("HH:mm:ss");
        String nowTime1 = format1.format(new Date());
        int hour = Integer.parseInt(nowTime1.split(":")[0]);
        return hour;
    }

    /**
     * ????????????
     *
     * @return the int
     */
    public int dateMinute() {
        SimpleDateFormat format1 = new SimpleDateFormat("HH:mm:ss");
        String nowTime1 = format1.format(new Date());
        int minute = Integer.parseInt(nowTime1.split(":")[1]);
        return minute;
    }

    /**
     * ????????????
     *
     * @param time the time
     * @throws Exception the exception
     */
    public void sleep(long time) throws Exception {
        CUtils.sleep(time);
    }


    Map<Integer, Integer> mMap = new HashMap<>();

    /**
     * ????????????map  Map<Integer,Integer> mMap= new HashMap<>();?????????
     *
     * @param number the number  ??????key?????????  value?????????0
     * @throws Exception the exception
     */
    @Deprecated
    public void initmMap(int number) throws Exception {
        mMap.clear();
        for (int i = 1; i <= number; i++) {
            mMap.put(i, 0);
        }
    }

    /**
     * ????????????key???value, ?????????
     *
     * @param mkey the mkey
     * @param str  the str
     * @return the map value
     * @throws Exception the exception
     */
    @Deprecated
    public int getmMapValue(int mkey, String str) throws Exception {
        LtLog.e(str + "=" + mMap.get(mkey));
        return mMap.get(mkey);
    }

    /**
     * ????????????key???value,?????????
     *
     * @param mkey   the mkey
     * @param mvalue the mvalue
     * @param str    the str
     * @throws Exception the exception
     */
    @Deprecated
    public void setmMapValue(int mkey, int mvalue, String str) throws Exception {
        mMap.put(mkey, mvalue);
        LtLog.e(str + "=" + mMap.get(mkey));
    }


    private long fad = 0;
    private long fadTime = 0;
    private int numcolor1 = 0;

    /**
     * ?????????????????????????????????
     *
     * @param leftX
     * @param leftY
     * @param rightX
     * @param rightY
     * @param setSim
     * @param type   0???1???
     * @param color
     * @return
     * @throws Exception
     */
    public long dazeTime(int leftX, int leftY, int rightX, int rightY, double setSim, int type, String color) throws Exception {
        if (fad == 0) {
            numcolor1 = getColorNum(leftX, leftY, rightX, rightY, setSim, type, color);
            fadTime = System.currentTimeMillis() / 1000;
            fad = 1;
            return 0;
        } else {
            int numcolor2 = getColorNum(leftX, leftY, rightX, rightY, setSim, type, color);
            if (numcolor1 == numcolor2) {
                return (System.currentTimeMillis() / 1000) - fadTime;
            } else {
                fad = 0;
                return 0;
            }
        }
    }//????????????


    /**
     * ??????????????????????????????
     *
     * @throws Exception
     */
    public void initDaze() throws Exception {
        fad = 0;
        fadTime = 0;
        numcolor1 = 0;
    }


    /**
     * ?????????????????????????????????????????????
     *
     * @param err           ???taskCount??????err?????????
     * @param count         ??????????????????????????????????????????
     * @param initSlidCount ???????????????????????? ?????????0????????????
     * @param x             ????????????
     * @param y             ????????????
     * @param x1            ????????????
     * @param y1            ????????????
     * @param moveSleep     ???????????????
     * @param stopSleep     ???????????????????????????????????????
     * @throws Exception
     */
    @Deprecated
    public void taskSlid(int err, int[] count, int initSlidCount, int x, int y, int x1, int y1, int moveSleep, long stopSleep) throws Exception {
        if (initSlidCount != 0 && err == count[0]) {
            for (int i = 0; i < initSlidCount; i++) {
                LtLog.e(getLineInfo("taskSlid???????????????>>>"));
                ranSwipe(x1, y1, x, y, moveSleep, stopSleep);
            }
        }
        for (int i = 1; i < count.length; i++) {
            if (err == count[i]) {
                LtLog.e(getLineInfo("taskSlid????????????>>>"));
                ranSwipe(x, y, x1, y1, moveSleep, stopSleep);
                return;
            }
        }
    }

    public void taskSlid(int err, int[] count, int initSlidCount, int x, int y, int x1, int y1, int moveSleep, long stopSleep, int id) throws Exception {
        if (initSlidCount != 0 && err == count[0]) {
            for (int i = 0; i < initSlidCount; i++) {
                LtLog.e(getLineInfo("taskSlid???????????????>>>"));
                ranSwipe(x1, y1, x, y, moveSleep, stopSleep, id);
            }
        }
        for (int i = 1; i < count.length; i++) {
            if (err == count[i]) {
                LtLog.e(getLineInfo("taskSlid????????????>>>"));
                ranSwipe(x, y, x1, y1, moveSleep, stopSleep, id);
                return;
            }
        }
    }


    /**
     * ??????
     *
     * @param inStream
     * @return
     * @throws Exception
     */
    public static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }


    /**
     * ????????????post
     *
     * @param host
     * @param suiji
     * @param haoAinum
     * @return
     * @throws Exception
     */

    /**
     * ???????????????????????????????????????????????????????????????Base64????????????
     *
     * @param imgpath
     * @return
     */
    public String GetImageStr(String imgpath) {
        InputStream in = null;
        byte[] data = null;
        //????????????????????????
        try {
            in = new FileInputStream(imgpath);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //???????????????Base64??????
        return new String(android.util.Base64.encode(data, android.util.Base64.DEFAULT));//??????Base64?????????????????????????????????
    }

    /**
     * ??????TID
     *
     * @param host
     * @param TID
     * @param suiji
     * @return
     */
    public String TIDhttpPost(String host, String TID, String suiji) {
        System.out.println(TID);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", TID)
                .addFormDataPart("r", suiji)
                .build();
        Request request = new Request.Builder()
                .url("http://" + host + "/GetAnswer.aspx")
                .post(requestBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String result = response.body().string();
            try {
                CUtils.sleep(3000);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error";
    }


    /**
     * ???????????????????????????
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @param minValue
     * @param maxValue
     * @return
     * @throws Exception ?????? getNumber(363, 393, 167, 35, new Scalar(0, 0, 100), new Scalar(100, 100, 150));
     */
    public String getNumber(int x, int y, int width, int height, Scalar minValue, Scalar maxValue) throws Exception {
        if (ScProxy.captureEngine().getCurrentScreenInfo().height > 720) {
            System.out.println("error Screen height >720");
            return null;
        }
        Mat mat = getScreenMat(x, y, width, height, 1, 0, 0, 1);

        Scalar minValues = minValue;
        Scalar maxValues = maxValue;
        Core.inRange(mat, minValues, maxValues, mat);
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(mat, bitmap);
        mat.release();
        tessBaseAPI.setImage(bitmap);
        String number = tessBaseAPI.getUTF8Text();
        tessBaseAPI.clear();
        return number;
    }


    /**
     * ????????????
     *
     * @param x
     * @param y
     * @param type
     * @return
     * @throws Exception
     */
    @Deprecated
    public int getIndex(int x, int y, int type) throws Exception {
        int index = -1;

        if (type == 0) {
            if (x < 0 || y < 0 || x > 1279 || y > 719) {
                return index;
            }
            index = x + (y * 1280);
        } else {
            if (x < 0 || y < 0 || x > 719 || y > 1279) {
                return index;
            }
            index = x + (y * 720);
        }
        return index;
    }

    /**
     * ??????????????????x
     *
     * @param index
     * @param type
     * @return
     * @throws Exception
     */
    @Deprecated
    public int IndexX(int index, int type) throws Exception {
        int x;
        if (type == 0) {
            x = index % 1280;
        } else {
            x = index % 720;
        }
        return x;
    }

    /**
     * ??????????????????y
     *
     * @param index
     * @param type
     * @return
     * @throws Exception
     */
    @Deprecated
    public int IndexY(int index, int type) throws Exception {
        int y;
        if (type == 0) {
            y = index / 1280;
        } else {
            y = index / 720;
        }
        return y;
    }

    /**
     * ????????????
     *
     * @param leftX  ??????
     * @param leftY  ??????
     * @param rightX ??????
     * @param rightY ??????
     * @param setSim ??????????????????
     * @param type   0????????? 1?????????
     * @param color  ??????
     * @return
     * @throws Exception ??????   findMultiColor(0,0,1280,720,0.9f,0,new String[]{"92,140,215","29|12|95,171,214","49|-21|83,83,204"});
     */
    @Deprecated
    public FindResult findMultiColor(int leftX, int leftY, int rightX, int rightY, float setSim, int type, String[] color) throws Exception {
        byte[] pic;
        FindResult result = new FindResult();
        result.sim = 0.1f;
        pic = ScProxy.captureEngine().getCurrentScreenInfo().raw;
        rightX = rightX - 1;
        rightY = rightY - 1;
        int colorNum = color.length;
        int[] anArrayX = new int[colorNum];
        int[] anArrayY = new int[colorNum];
        int[] anArrayR = new int[colorNum];
        int[] anArrayG = new int[colorNum];
        int[] anArrayB = new int[colorNum];
        for (int i = 0; i < colorNum; i++) {
            String[] oneColorarr = color[i].split("\\|");
            if (i == 0) {
                String[] setColorRGB = oneColorarr[0].split(",");
                anArrayX[0] = 0;
                anArrayY[0] = 0;
                anArrayR[0] = Integer.parseInt(setColorRGB[0]);
                anArrayG[0] = Integer.parseInt(setColorRGB[1]);
                anArrayB[0] = Integer.parseInt(setColorRGB[2]);
            } else {
                String[] setColorRGB = oneColorarr[2].split(",");
                anArrayX[i] = Integer.parseInt(oneColorarr[0]);
                anArrayY[i] = Integer.parseInt(oneColorarr[1]);
                anArrayR[i] = Integer.parseInt(setColorRGB[0]);
                anArrayG[i] = Integer.parseInt(setColorRGB[1]);
                anArrayB[i] = Integer.parseInt(setColorRGB[2]);
            }
        }
        int startIndex = getIndex(leftX, leftY, type) * 4;
        int endIndex = getIndex(rightX, rightY, type) * 4;
        int R, G, B, oneR = anArrayR[0], oneB = anArrayB[0], oneG = anArrayG[0];
        if (endIndex >= 3686400) {
            endIndex = 3686396;
        }
        float sim;
        for (int i = startIndex; i < endIndex; i += 4) {
            R = (pic[i] & 0xff);
            G = (pic[i + 1] & 0xff);
            B = (pic[i + 2] & 0xff);
            sim = (float) (765 - (Math.abs(R - oneR) + Math.abs(G - oneG) + Math.abs(B - oneB))) / 765;
            if (sim >= setSim) {
                int oneX = IndexX(i / 4, type);
                int oneY = IndexY(i / 4, type);
                int next = 1;
                do {
                    int nextClolorIndex = getIndex(oneX + anArrayX[next], oneY + anArrayY[next], type) * 4;
                    if (nextClolorIndex < 0) {
                        break;
                    }
                    System.out.println();
                    R = (pic[nextClolorIndex] & 0xff);
                    G = (pic[nextClolorIndex + 1] & 0xff);
                    B = (pic[nextClolorIndex + 2] & 0xff);
                    sim = (float) (765 - (Math.abs(R - anArrayR[next]) + Math.abs(G - anArrayG[next]) + Math.abs(B - anArrayB[next]))) / 765;
                    if (sim >= setSim) {
                    } else {
                        break;
                    }
                    next++;
                    if (next == colorNum) {
                        result.sim = setSim;
                        result.x = oneX;
                        result.y = oneY;
                        return result;
                    }
                } while (next < colorNum);
            }
        }
        return result;
    }


    /**
     * ????????????
     *
     * @param leftX
     * @param leftY
     * @param rightX
     * @param rightY
     * @param setSim
     * @param colorStr_start ????????????
     * @param colorStr_sub   ????????????
     * @return
     * @throws Exception
     */
    public FindResult findMultiColor(int leftX, int leftY, int rightX, int rightY, double setSim, String colorStr_start, String colorStr_sub) throws Exception {
        int[] xy = new int[2];
        Mat mat = ScProxy.captureEngine().getCurrentScreenMat();
        Mat m = new Mat();
        Imgproc.cvtColor(mat, m, Imgproc.COLOR_BGR2RGB);
        multipointFindColorEx(leftX, leftY, rightX, rightY, null, m.getNativeObjAddr(), colorStr_start, colorStr_sub, setSim, xy);
        FindResult result = new FindResult();
        result.sim = 0.1f;
        if (xy[0] != -1) {
            result.sim = (float) setSim;
            result.x = xy[0];
            result.y = xy[1];
            result.width = 5;
            result.height = 5;
        }
        return result;
    }

    /**
     * ???????????????????????????
     *
     * @param leftX
     * @param leftY
     * @param rightX
     * @param rightY
     * @param setSim
     * @param type   0???1???
     * @param color
     * @return
     * @throws Exception
     */
    public int getColorNum(int leftX, int leftY, int rightX, int rightY, double setSim, int type, String color) throws Exception {
        byte[] pic;
        int nowNum = 0;
        pic = ScProxy.captureEngine().getCurrentScreenInfo().raw;
        String[] arr = color.split(",");
        int setr = Integer.parseInt(arr[0]);
        int setg = Integer.parseInt(arr[1]);
        int setb = Integer.parseInt(arr[2]);
        int width = rightX - leftX;
        int height = rightY - leftY;
        int setx = leftX, sety = leftY;
        for (int i = 0; i <= height; i++) {
            for (int j = 0; j <= width; j++) {
                int startIndex = getIndex(setx, sety, type) * 4;
                int R = (pic[startIndex] & 0xff);
                int G = (pic[startIndex + 1] & 0xff);
                int B = (pic[startIndex + 2] & 0xff);
                float sim = (float) (765 - (Math.abs(R - setr) + Math.abs(G - setg) + Math.abs(B - setb))) / 765;
                if (sim >= setSim) {
                    nowNum++;
                }
                setx = setx + 1;
            }
            setx = leftX;
            sety = sety + 1;
        }
        return nowNum;
    }

    @Deprecated
    public native void multipointFindColorEx(int x1, int y1, int x2, int y2, byte[] imgByte, long mat, String colorStr_start, String colorStr_sub, double sim, int[] xy);

    public List findPic(int leftX, int leftY, int rightX, int rightY, float sim, String picName) throws Exception {
        FindResult result = new FindResult();
        result.sim = 0.1f;
        List<FindResult> list = new ArrayList<>();
        TemplateInfo mInfo = getTemplateInfo(picName);
        Mat mat = captureMat();
        Mat rectMat = getScreenMat(leftX, leftY, rightX - leftX, rightY - leftY, mInfo.flag, mInfo.thresh, mInfo.maxval, mInfo.type, mat);
        Mat mat1 = getTemplateMat(picName);
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < mInfo.width * 3; i++) {
            double d = 0 + Math.random() * 255;
            data.add(d);
        }
        Double[] data1 = new Double[data.size()];
        data.toArray(data1);
        int length = data1.length;
        double[] dest = new double[length];
        for (int i = 0; i < length; i++) {
            dest[i] = data1[i];
        }
        for (int i = 0; i < 20; i++) {
            result = matchMat(0, 0, mat1, rectMat);
            if (result.sim > sim) {
                for (int j = 0; j < mInfo.height; j++) {
                    rectMat.put(result.y + j, result.x, dest);
                }
                result.x = result.x + leftX;
                result.y = result.y + leftY;
                list.add(result);
            } else {
                break;
            }
        }
        //   LtLog.e("list="+list.toString());
        Collections.sort(list, new Comparator<FindResult>() {
            @Override
            public int compare(FindResult o1, FindResult o2) {
                //??????
                if (o1.x < o2.x) {
                    return -1;
                }
                if (o1.x == o2.x) {
                    return 0;
                }
                return 1;
            }
        });
        //     LtLog.e("list="+list.toString());
        Collections.sort(list, new Comparator<FindResult>() {
            @Override
            public int compare(FindResult o1, FindResult o2) {
                //??????
                if (o1.y < o2.y) {
                    return -1;
                }
                if (o1.y == o2.y) {
                    return 0;
                }
                return 1;
            }
        });
        //  LtLog.e("list="+list.toString());
        return list;
    }

    public static int getAppCpuUsedPercent() {
        String[] cpuInfos = null;
        int AppCpuUsedPercent = -1;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int uid = android.os.Process.myUid();
                int pid = android.os.Process.myPid();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        Runtime.getRuntime().exec("top -b -u " + uid + " -n 1 ").getInputStream()), 500);
                String load = reader.readLine();
                while (load != null) {
                    if (load.contains(String.valueOf(pid))) {
                        break;
                    }
                    load = reader.readLine();
                    //   LtLog.e("load111====="+load);
                }
                //    LtLog.e("load2222====="+load);
                reader.close();
                cpuInfos = load.split("\\s+");
                AppCpuUsedPercent = Double.valueOf(cpuInfos[9]).intValue();
                AppCpuUsedPercent = (AppCpuUsedPercent * 100) / 600;
            } else {
                int pid = android.os.Process.myPid();
                int uid = android.os.Process.myUid();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        Runtime.getRuntime().exec("top -n 1").getInputStream()), 500);
                String load = reader.readLine();
                while (load != null) {
                    if (load.contains(String.valueOf(pid))) {
                        break;
                    }
                    load = reader.readLine();
                }
                reader.close();
                cpuInfos = load.split("%");
                AppCpuUsedPercent = Integer.parseInt(cpuInfos[0].substring(cpuInfos[0].length() - 3).trim());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return AppCpuUsedPercent;
    }

    /* ADDED BY      on 2021-03-23. */
    private Set<OnCompatFiaryEvent> mOnFiaryEvents = new HashSet<>();

    private void dispatchCompatEvent(String event) {
        dispatchCompatEvent(event, null);
    }

    private void dispatchCompatEvent(String event, Bundle bundle) {
        synchronized (mOnFiaryEvents) {

            Iterator<OnCompatFiaryEvent> iterator = mOnFiaryEvents.iterator();

            LtLog.d("auto","dispatchCompatEvent : "+mOnFiaryEvents.size());

            while (iterator.hasNext()) {
                final OnCompatFiaryEvent onCompatFiaryEvent = iterator.next();
                if (onCompatFiaryEvent != null) {
                    Bundle b = new Bundle();
                    if (bundle != null) {
                        b.putAll(bundle);
                    }
                    b.putString(OnCompatFiaryEvent.KEY_TYPE, event);
                    onCompatFiaryEvent.onEvent(b);
                }

                LtLog.d("auto","onCompatFiaryEvent : "+onCompatFiaryEvent);

            }
        }
    }

    public void addOnFiaryEvent(OnCompatFiaryEvent onFiaryEvent) {
        synchronized (this.mOnFiaryEvents) {
            this.mOnFiaryEvents.add(onFiaryEvent);
        }
    }

    public void removeOnFiaryEvent(OnCompatFiaryEvent onFiaryEvent) {
        synchronized (this.mOnFiaryEvents) {
            Iterator<OnCompatFiaryEvent> iterator = mOnFiaryEvents.iterator();
            while (iterator.hasNext()) {
                final OnCompatFiaryEvent onCompatFiaryEvent = iterator.next();
                if (onCompatFiaryEvent == onFiaryEvent) {
                    iterator.remove();
                }
            }
            // this.mOnFiaryEvents.remove(onFiaryEvent);
        }
    }

    public interface OnCompatFiaryEvent {
        String KEY_TYPE = "TYPE";
        String TYPE_NEW_TASK_START0 = "TYPE_NEW_TASK_START0";
        String TYPE_NEW_TASK_START1 = "TYPE_NEW_TASK_START1";

        void onEvent(Bundle bundle);
    }

    private ICompatFairyProxy mFairyProxy;

    public interface ICompatFairyProxy {
        void finish(String taskId, int state);
    }

    public void setCompatFairyProxy(ICompatFairyProxy fairyProxy) {
        this.mFairyProxy = fairyProxy;
    }

    public static class SimpleFairyProxyImpl implements ICompatFairyProxy {

        @Override
        public void finish(String taskId, int state) {

        }
    }
    /* ADDED END. */
}
