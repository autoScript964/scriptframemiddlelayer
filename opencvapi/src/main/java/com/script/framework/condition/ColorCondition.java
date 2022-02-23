package com.script.framework.condition;

import com.script.framework.FairyRect;
import com.script.opencvapi.FindResult;

import org.opencv.core.Mat;

/**
 * Created by     on 2018-12-06.
 */
public class ColorCondition extends ScreenItem {
    private  static final int DEFAULT_INTERVAL = 1000 ;
    private  static final float DEFAULT_SIM = 0.9F ;
    private ColorInfo mInfo ;
//    private int mRangeCount  = 0 ;
    private int mInterval = DEFAULT_INTERVAL ;
    private int mConditionCount ;
    private int mLastCount ;
    private boolean mChanged ;
    private long mLastChangeCaleTime ;

    public ColorCondition(ColorInfo colorInfo){
        this.setState(STATE_UNCHANGE);
        mInfo = colorInfo ;
    }
    public ColorCondition(int color, FairyRect rect){
        this.setState(STATE_UNCHANGE);
        mInfo = ColorInfo.newInstance(rect, color, DEFAULT_SIM) ;
    }

    @Override
    public void looping(Mat screenMat) {
        int count = mInfo.caleCount(screenMat) ;
        if(!mChanged || System.currentTimeMillis() - mLastChangeCaleTime > mInterval){
            if(Math.abs(mLastCount - count) > mConditionCount){
                mChanged = true ;
            }else{
                mChanged = false ;
            }
            mLastChangeCaleTime = System.currentTimeMillis() ;
        }else{
        }
        mLastCount = count ;

    }
    /**
     * 颜色数量小于指定count则result为true
     * */
    public ColorCondition lessCount(int count){
        mConditionCount = count ;
        setState(STATE_LESS) ;
        return  this ;
    }
    /**
     * 颜色数量多于指定count则result为true
     * */
    public ColorCondition moreCount(int count){
        mConditionCount = count ;
        setState(STATE_MORE) ;
        return this ;
    }
    /**
     * 设置条件数量(多于，少于，或者变化区间)
     * */
    public void setConditionCount(int count){
        mConditionCount = count ;
    }
    /**
     * 颜色数量在指定范围内变动不视为变化
     * */
//    public ColorCondition setRange(int rangeCount){
//        mRangeCount = rangeCount ;
//        return  this ;
//    }

    public ColorCondition setCaleInterval(int interval){
        mInterval = interval ;
        return  this ;
    }

    @Override
    public int getState() {
        int state = 0 ;
        state |= mLastCount > mConditionCount?STATE_MORE:STATE_LESS ;
        state |= mChanged?STATE_CHANGE:STATE_UNCHANGE ;
        return state;
    }

    @Override
    public FindResult getFindResult() {
        return null;
    }
}
