package com.script.framework.action;

import com.script.framework.FairyRect;

/**
 * 连续点击事件
 * */
public class SeriesClickAction implements IAction {
    private TapAction mTapAction ;
    private static final int DEFAULT_PERIOD = 100 ;
    private int mTapDelay = DEFAULT_DELAY;
    private int mCount ;
    private int mPeriod = DEFAULT_PERIOD ;

    /**
     * 构造方法
     * @param rect 点击区域
     * @param count  点击次数
     * @param period 每次点击间隔
     * */
    public SeriesClickAction(FairyRect rect, int count, int period){
        mCount = count ;
        mPeriod = period ;
        mTapAction = new TapAction(rect) ;
    }


    /**
     * 构造方法,默认点击间隔为100ms
     * @param rect 点击区域
     * @param count  点击次数
     * */
    public SeriesClickAction(FairyRect rect,int count){
        this(rect,count, DEFAULT_PERIOD) ;
    }
    @Override
    public int onAction(){
        for(int i = 0 ;i < mCount ; ++i){
            mTapAction.onAction() ;
            try {
                Thread.sleep(mPeriod);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        return mTapDelay;
    }
    public SeriesClickAction setTapDelay(int delay){
        mTapDelay = delay ;
        return  this ;
    }
}
