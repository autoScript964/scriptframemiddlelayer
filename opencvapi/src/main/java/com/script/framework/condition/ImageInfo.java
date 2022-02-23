package com.script.framework.condition;

import com.script.framework.FairyRect;
import com.script.framework.TaskFairyImpl;
import com.script.opencvapi.FindResult;
import com.script.opencvapi.LtLog;
import com.script.opencvapi.utils.TemplateInfo;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ImageInfo {
    public static final float DEFUALT_SIM = 0.8f;
    private static Map<String, ImageInfo> infos;
    private static float sDefaultSim = DEFUALT_SIM;

    public static ImageInfo newInstance(String name) {
        if (infos == null) {
            infos = new HashMap<>();
        }
        ImageInfo imageInfo;
        if (infos.containsKey(name)) {
            imageInfo = infos.get(name);
        } else {
            imageInfo = new ImageInfo(name);
            infos.put(name, imageInfo);
        }
        return imageInfo;

    }

    public static void updateImageInfo(String name){
        if(infos  != null && infos.containsKey(name)){
            infos.remove(name) ;
        }
        newInstance(name) ;
    }

    /**
     * 设置默认图片相似度
     */
    public static final void setDefualtSim(float sim) {
        sDefaultSim = sim;
    }


    private String mName;
    private long mLastScreenMat;
    //    private TemplateInfo mInfo;
//    private Mat mMat;
    private List<TemplateInfo> mInfoList;
    private float mSim = sDefaultSim;
    private FindResult mFindResult;
    private boolean mImageExist;

    private List<Mat> mMatList;
    private boolean mDebugInfo = false;
    private FairyRect mLastRect;

    private ImageInfo(String name) {
        mName = name;
        init();
    }

    /**
     * 设置图片相似度
     */
    public ImageInfo setSim(float sim) {
        mSim = sim;
        return this;
    }

    public static void setDebugInfo(String name) {

        setDebugInfo(name, true);

    }

    public static void setDebugInfo(String name, boolean debug) {
        if (infos == null) {
            return;
        }
        ImageInfo imageInfo = infos.get(name);
        if (imageInfo != null) {
            imageInfo.mDebugInfo = debug;
        }


    }

    public static void clearDebug() {
        Iterator<Map.Entry<String, ImageInfo>> iter = infos.entrySet().iterator();
        while (iter.hasNext()) {
            iter.next().getValue().mDebugInfo = false;
        }
    }

    private void init() {
        if (mInfoList == null) {
            mInfoList = new ArrayList<>();
        }
        if (mMatList == null) {
            mMatList = new ArrayList<>();
        }

        String[] names = mName.split("\\|");
        for (String ss : names) {
            mInfoList.add(TaskFairyImpl.getFairy().getTemplateInfo(ss));
            mMatList.add(TaskFairyImpl.getFairy().getTemplateMat(ss));
        }
    }

    public void matchResult(Mat screenMat, FairyRect rect) {
        TemplateInfo mInfo;
        Mat mMat;

        if (screenMat == null) {
            mImageExist = false;
            mFindResult = null ;
        } else if (screenMat.nativeObj != mLastScreenMat || mLastRect !=rect || ( mLastRect != null && !mLastRect.equals(rect) )  ) {
            mLastRect = rect ;
            mLastScreenMat = screenMat.nativeObj;
            int leftX = 0, leftY = 0, width = 0, height = 0;
            if (rect != null) {
                leftX = rect.x();
                leftY = rect.y();
                width = rect.width();
                height = rect.height();
            }
            for (int i = 0; i < mInfoList.size(); i++) {
                if (rect == null) {
                    leftX = mInfoList.get(i).x;
                    leftY = mInfoList.get(i).y;
                    width = mInfoList.get(i).width;
                    height = mInfoList.get(i).height;
                }
                if (width > screenMat.width()) {
                    width = screenMat.width();
                }
                if (height > screenMat.height()) {
                    height = screenMat.height();
                }
                if (leftX <0 || leftX > screenMat.width() || leftY < 0 || leftY > screenMat.height() ) {
                    LtLog.i("error rect coord x:" + leftX + " y:" + leftY);
                } else {
                    mInfo = mInfoList.get(i);
                    mMat = mMatList.get(i);
                    Mat rectMat = TaskFairyImpl.getFairy().getScreenMat(leftX, leftY, width, height, mInfo.flag, mInfo.thresh, mInfo.maxval, mInfo.type, screenMat);
                    if (rectMat.width() >= mMat.width() && rectMat.height() >= mMat.height()) {
                        try {
                            mFindResult = TaskFairyImpl.getFairy().matchMat(leftX, leftY, mMat, rectMat);
                            if (mFindResult.sim >= mSim) {
                                break;
                            }
                        } catch (Exception e) {
                            LtLog.i(e.getMessage());
                            LtLog.i("match exception:image info:" + mMat.width() + ":" + mMat.height() +
                                    "screen info: " + rectMat.width() + ":" + rectMat.height() + " image:" + mName);
                        }
                    } else {
                        LtLog.i("match err:image info:" + mMat.width() + ":" + mMat.height() +
                                "screen info: " + rectMat.width() + ":" + rectMat.height() + " image:" + mName);
                    }
                    rectMat.release();
                }
            }
        }
        if (mDebugInfo) {
            LtLog.i("imageInfo:" + mName + ":" + mFindResult + ", request sim:" + mSim);
        }
        if (mFindResult != null && mFindResult.sim >= mSim) {
            mImageExist = true;
        } else {
            mImageExist = false;
        }

    }


    public void matchResult(Mat screenMat) {
        matchResult(screenMat, null);
    }


    public boolean imageExist() {
        return mImageExist;
    }

    public FindResult getFindResult() {
        return mFindResult;
    }


}
