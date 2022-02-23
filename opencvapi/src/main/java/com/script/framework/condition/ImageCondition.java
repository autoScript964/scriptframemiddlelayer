package com.script.framework.condition;

import com.script.framework.FairyRect;
import com.script.opencvapi.FindResult;

import org.opencv.core.Mat;


public class ImageCondition extends ScreenItem {
    protected ImageInfo mImageInfo;
    protected FairyRect mRect;

    public ImageCondition(ImageInfo imageInfo, FairyRect rect) {
        mRect = rect;
        mImageInfo = imageInfo;
        setState(STATE_EXIST);
    }

    public ImageCondition(ImageInfo imageInfo) {
        this(imageInfo, null) ;
    }

    public ImageCondition(String name) {
        this(ImageInfo.newInstance(name)) ;
    }

    public ImageCondition(String name, FairyRect rect) {
        this(ImageInfo.newInstance(name), rect) ;
    }



    @Override
    public void looping(Mat screenMat) {
        mImageInfo.matchResult(screenMat, mRect);
    }

    @Override
    public int getState() {
        return mImageInfo.imageExist() ? STATE_EXIST : STATE_WITHOUT;
    }

    @Override
    FindResult getFindResult() {
        return mImageInfo.getFindResult();
    }

}