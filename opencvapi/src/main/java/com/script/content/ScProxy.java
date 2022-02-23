package com.script.content;

import android.content.Context;

import com.script.content.utils.UStr;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.HashSet;

/**
 * Created by daiepngfei on 2020-08-11
 */
@SuppressWarnings("ALL")
public class ScProxy {

    public static final String TAG = "ScProxy";
    private CaptureEngine mCaptureEngine = new CaptureEngine(this);
    private MatchEngine mMatchEngine = new MatchEngine(this);
    private Assets mAssets = new Assets();
    private ConfigManager config = new ConfigManager(this);
    private DProfiler profiler = new DProfiler(this);
    private Context context;

    private ScProxy(){
    }

    private static final class F {
        private static final ScProxy sIns = new ScProxy();
    }

    public static void init(Context context){
        if(context != null) {
            F.sIns.context = context;
            profiler().setPackageName(context.getPackageName());
        }
        WatchDog.ins().start();
        captureEngine().start();
    }

    public static void destroy(){
        captureEngine().destroy();
        WatchDog.ins().stop();
    }

    public Context getContext() {
        return context;
    }

    public static Assets assets() {
        return F.sIns.mAssets;
    }

    public static CaptureEngine captureEngine(){
        return F.sIns.mCaptureEngine;
    }

    public static MatchEngine matchEngine() {
        return F.sIns.mMatchEngine;
    }

    public static ConfigManager config() {
        return F.sIns.config;
    }

    public static DProfiler profiler() {
        return F.sIns.profiler;
    }

    public static class ConfigManager {

        private final ScProxy proxy;
        private final Printer printer = new Printer();
        private Level level = new Level();
        private Debugger debugger = new Debugger();

        ConfigManager(ScProxy scProxy) {
            this.proxy = scProxy;
        }

        private static final ThreadLocal<Integer> MATCHING_TIME_INTERVAL_LIMIT = new ThreadLocal<>();

        public Printer Printer(){
            return printer;
        }
        public Level Level(){
            return level;
        }
        public Debugger Debugger(){
            return debugger;
        }

        public class Printer {
            private int maxAssetSpace = 25;
            private UStr.Pat assetPat = UStr.Pat.LH_30;

            public void setMaxAssetSpace(int maxAssetSpace) {
                this.maxAssetSpace = maxAssetSpace + 2;
            }

            public int getMaxAssetSpace() {
                return maxAssetSpace;
            }

            public UStr.Pat getAssetPat() {
                return assetPat;
            }

            public void setAssetPat(UStr.Pat assetPat) {
                this.assetPat = assetPat == null ? this.assetPat : assetPat;
            }

            public void setDTimerEnable(boolean enable){
                DTimer.ThreadLocalManager.setTimerThreadLocalEnable(enable);
            }
        }


        public class Level {
            /**
             * 1 - 10
             * 默认等级为2
             * 1-5级为实用等级
             * todo：因为设备性能缘故，目前6-10 有边际效应现象存在收益递减。后续优化
             * @param level
             */
            public void capturing(int level) {
                proxy.mCaptureEngine.setEngineMaxSpeedLevel(level);
            }

            /**
             * 0-10
             * @param limit
             */
            public void matching(int limit){
                limit = Math.max(0, Math.min(limit, 10));
                MATCHING_TIME_INTERVAL_LIMIT.set(limit * 10);
            }

            public int curMatching() {
                Integer level = MATCHING_TIME_INTERVAL_LIMIT.get();
                if(level == null){
                    matching(5);
                }
                return MATCHING_TIME_INTERVAL_LIMIT.get();
            }
        }

        /**
         * 1 - 10
         * 默认等级为2
         * 1-5级为实用等级
         * todo：因为设备性能缘故，目前6-10 有边际效应现象存在收益递减。后续优化
         * @see {@link Level#capturing(int)}
         * @param level
         */
        @Deprecated
        public void setEngineMaxSpeedLevel(int level) {
            Level().capturing(level);
        }

        /**
         * @see #Level#
         * 0-10
         * @see {@link Level#matching(int)}
         * @param limit
         */
        @Deprecated
        public void setMatchingTimeIntervalLimitLevel(int limit){
            Level().matching(limit);
        }

        /**
         *
         * @see {@link Level#curMatching()}
         * @return
         */
        @Deprecated
        public int getMatchingTimeIntervalLimitLevelValue() {
            return Level().curMatching();
        }


        /**
         * 设置加载asset的时候是否同时读取templateinfo文件
         * @param enableTemplateInfo
         */
        public void setAssetsEnableTemplateInfo(boolean enableTemplateInfo){
            assets().setAssetsEnableTemplateInfo(enableTemplateInfo);
        }

        public static class Debugger {
            private HashSet imwriteAssetsMat = new HashSet();

            public void addImwriteAssetsMat(String... key) {
                for(String k : key) {
                    this.imwriteAssetsMat.add(key);
                }
            }

            private boolean isImwriteAssetsMat(String key) {
                return this.imwriteAssetsMat.contains(key);
            }

            public void removeImwriteAssetsMat(String key){
                this.imwriteAssetsMat.remove(key);
            }

            public void imwriteAssetsMat(String name, Mat mat){
                if(isImwriteAssetsMat(name)){
                    Imgcodecs.imwrite("/sdcard/createCvtMat_" + name, mat);
                }
            }
        }

    }

}
