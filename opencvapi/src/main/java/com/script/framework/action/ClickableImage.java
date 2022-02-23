package com.script.framework.action;

import com.script.framework.FairyRect;
import com.script.framework.Task;
import com.script.framework.condition.ImageCondition;
import com.script.framework.TaskFairyImpl;
import com.script.framework.condition.ImageInfo;
import com.script.opencvapi.FindResult;
import com.script.opencvapi.LtLog;

import java.util.Random;

public class ClickableImage extends ImageCondition implements IAction {

    private int mShiftX = 0 ;
    private int mShiftY = 0 ;
    private int mTapDelay = DEFAULT_DELAY;

    public ClickableImage(ImageInfo imageInfo){
        super(imageInfo);
    }
    public ClickableImage(String name){
        super(name);
    }
    public ClickableImage(String name, FairyRect rect){
        super(name,rect);
    }
    public ClickableImage(ImageInfo info, FairyRect rect){
        super(info, rect);
    }
    public ClickableImage setShift(int x,int y){
        mShiftX = x ;
        mShiftY = y ;
        return  this ;
    }
    @Override
    public int onAction() {
        //防止此Item未执行loop
        looping(Task.getCurrentTask().getScreenMat());
        if(mImageInfo.imageExist()){
            FindResult result = mImageInfo.getFindResult() ;
            if(result != null){
                int x = new Random().nextInt(result.width) + result.x ;
                int y = new Random().nextInt(result.height) + result.y ;
                x += mShiftX ;
                y += mShiftY ;
                LtLog.i("ClickAble onAction:"+x+":"+y) ;

                try {
                    TaskFairyImpl.getFairy().tap(x,y);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return  mTapDelay ;
            }
        }
        return  0 ;
    }
    public ClickableImage setTapDelay(int delay){
        mTapDelay = delay ;
        return  this ;
    }

    @Override
    public ClickableImage setTimeout(int timeout) {
        super.setTimeout(timeout);
        return this ;
    }
}
