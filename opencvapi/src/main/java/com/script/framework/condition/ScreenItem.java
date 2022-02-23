package com.script.framework.condition;

import com.script.opencvapi.FindResult;
import com.script.opencvapi.LtLog;

import junit.framework.Assert;

import org.opencv.core.Mat;

public abstract class ScreenItem implements IConditionItem {


    /**
     * 图片存在
     */
    public static final int STATE_EXIST = 0x01;
    /**
     * 图片不存在
     */
    public static final int STATE_WITHOUT = 0x02;
    /**
     * 区域变化
     */
    public static final int STATE_CHANGE = 0x04;
    /**
     * 区域未变化
     */
    public static final int STATE_UNCHANGE = 0x08;
    /**
     * 区域颜色数量多于
     */
    public static final int STATE_MORE = 0x0100;
    /**
     * 区域颜色数量少于
     */
    public static final int STATE_LESS = 0x0200;

    /**
     * 状态
     */
    int state;

    long mLastConfirmedTime;
    int timeout;

    public ScreenItem setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public ScreenItem setState(int state) {
        switch (state) {
            case STATE_CHANGE:
            case STATE_UNCHANGE:
                Assert.assertTrue(this instanceof ColorCondition || this instanceof AreaCondition);
                break;
            case STATE_LESS:
            case STATE_MORE:
                Assert.assertTrue(this instanceof ColorCondition);
                break;
            case STATE_EXIST:
            case STATE_WITHOUT:
                Assert.assertTrue(this instanceof ImageCondition);
                break;
        }
        this.state = state;
        return this;
    }

    public boolean result(Mat screenMat) {
        looping(screenMat);
        int resultState = getState();
        boolean ret = (resultState & state) != 0;
        if (ret) {
            if (mLastConfirmedTime == 0) {
                mLastConfirmedTime = System.currentTimeMillis();
            }
            if (timeout > 0) {
                LtLog.i("condition confirmed time:" + (System.currentTimeMillis() - mLastConfirmedTime));
            }
        } else {
            mLastConfirmedTime = 0;
        }

        return ret && (System.currentTimeMillis() - mLastConfirmedTime) >= timeout;
    }

    public void reset() {
        mLastConfirmedTime = 0;
    }

    public abstract void looping(Mat screenMat);

    public abstract int getState();

    abstract FindResult getFindResult();
}
