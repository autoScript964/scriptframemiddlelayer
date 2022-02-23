package com.script.framework.condition;


import com.script.framework.FairyRect;
import com.script.framework.TaskFairyImpl;
import com.script.opencvapi.FindResult;
import com.script.opencvapi.LtLog;
import org.opencv.core.Mat;

import java.util.*;

public class AreaInfo  {

    //最低匹配间隔
    private static final int MIN_MATCH_TIME = 1000 ;

    private static Map<FairyRect, AreaInfo> sInfoMap ;
    private FairyRect mRect ;
    int flag ;
    int thresh ;
    int maxval ;
    int type ;
    float sim ;
    private Mat mLastMat ;
    private long mLastTime ;
    private long mLastMatId ;
    private boolean mChanged ;
    private boolean mDebugInfo = false ;



    public static AreaInfo newInstance(FairyRect rect, int flag, int thresh , int maxval, int type, float sim ){
        if(sInfoMap == null){
            sInfoMap = new HashMap<>() ;
        }

        AreaInfo areaInfo ;
        if(!sInfoMap.containsKey(rect)){
            areaInfo = new AreaInfo(rect,flag, thresh, maxval, type, sim) ;
            sInfoMap.put(rect, areaInfo) ;
        }else{
            areaInfo = sInfoMap.get(rect) ;
        }
        return  areaInfo ;
    }

    private AreaInfo(FairyRect rect, int flag, int thresh , int maxval, int type, float sim ){
        mRect = rect ;
        this.flag = flag ;
        this.thresh = thresh ;
        this.maxval = maxval ;
        this.type = type ;
        this.sim = sim ;
    }
    public void setDebugInfo(){
        mDebugInfo = true ;
    }
    public boolean changed(){
        if(mDebugInfo) {
            LtLog.i("areainfo changed:" + this + " changed:" + mChanged);
        }
        return  mChanged ;
    }
    public void matchResult(Mat screenMat){

        if(screenMat.getNativeObjAddr() == mLastMatId){
            return;
        }
        if(System.currentTimeMillis() - mLastTime < MIN_MATCH_TIME){
            //如果上一次已经变化，下次检测时间时隔1s防止发生频繁停止
            if(mChanged) {
                return;
            }
        }

        Mat rectMat = TaskFairyImpl.getFairy().getScreenMat(mRect.x(), mRect.y(), mRect.width(), mRect.height(), flag, thresh, maxval, type, screenMat) ;
        if(mLastMat == null) {
            mLastMat = rectMat;
            mChanged = true ;
            mLastMatId = screenMat.getNativeObjAddr() ;
            return;
        }

        FindResult result = TaskFairyImpl.getFairy().matchMat(mRect.x(), mRect.y(), mLastMat, rectMat);
        if(result != null && result.sim >= sim){
            mChanged = false ;
        }else{
            mChanged = true ;
        }
        mLastMatId = screenMat.getNativeObjAddr() ;
        mLastTime = System.currentTimeMillis() ;
        mLastMat.release();
        mLastMat = rectMat ;
    }
    public void reset(){
        mLastTime =0 ;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            return  false ;
        }
        if(obj instanceof AreaInfo){
            AreaInfo info = (AreaInfo) obj;
            return info.mRect.equals(mRect) ;
        }
        return false ;
    }

}
