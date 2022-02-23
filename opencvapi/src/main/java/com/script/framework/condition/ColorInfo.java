package com.script.framework.condition;

import com.script.framework.FairyRect;
import com.script.framework.TaskFairyImpl;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by     on 2018-12-06.
 */
public class ColorInfo {
    private static Map<FairyRect, ColorInfo> sInfoMap ;

    private FairyRect mRect ;
    int color ;
    float sim ;
    private long mLastMatId ;
    private int mCount ;

    public static ColorInfo newInstance(FairyRect rect, int color, float sim ){
        if(sInfoMap == null){
            sInfoMap = new HashMap<>() ;
        }

        ColorInfo areaInfo ;
        if(!sInfoMap.containsKey(rect)){
            areaInfo = new ColorInfo(rect,color,sim) ;
            sInfoMap.put(rect, areaInfo) ;
        }else{
            areaInfo = sInfoMap.get(rect) ;
        }
        return  areaInfo ;

    }

    public ColorInfo(FairyRect rect, int color, float sim){
        mRect = rect ;
        this.color = color ;
        this.sim = sim ;
    }
    public int caleCount(Mat screenMat){
        if(screenMat.getNativeObjAddr() != mLastMatId) {
            Mat rectMat = TaskFairyImpl.getFairy().getScreenMat(mRect.x(), mRect.y(), mRect.width(), mRect.height(), Imgcodecs.IMREAD_COLOR, 0, 0, 0, screenMat);
            mLastMatId = screenMat.getNativeObjAddr();
            long start = System.currentTimeMillis() ;
            mCount = TaskFairyImpl.getFairy().getColorCount(color, sim, rectMat);
            long end = System.currentTimeMillis() ;
            rectMat.release();
        }
        return mCount ;
    }
}
