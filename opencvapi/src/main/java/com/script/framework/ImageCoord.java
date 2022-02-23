package com.script.framework;

import com.script.framework.condition.ImageInfo;
import com.script.opencvapi.FindResult;

/**
 * 根据图片位置返回相应的坐标
 * Created by     on 2018-12-18.
 */
public class ImageCoord implements ICoord {
    private int mShiftX,mShiftY ;
    private ImageInfo mImageInfo ;
    private FairyRect mRect ;
    private Task mTask ;

    /**
     * 构造ImageCoord
     * @param task Task对象
     * @param imageInfo 图片信息
     * @param rect 需要查找的图片的区域
     * */
    public ImageCoord(Task task, ImageInfo imageInfo, FairyRect rect){
        mTask = task ;
        mImageInfo = imageInfo ;
        mRect = rect ;
    }
    /**
     * 构造ImageCoord
     * @param task Task对象
     * @param name 图片名
     * @param rect 需要查找的图片的区域
     * */
    public ImageCoord(Task task, String name, FairyRect rect){
        this(task, ImageInfo.newInstance(name), rect) ;
    }
    /**
     * 构造ImageCoord
     * @param task Task对象
     * @param imageInfo 图片信息
     * */
    public ImageCoord(Task task,ImageInfo imageInfo){
        this(task,imageInfo, null) ;
    }
    /**
     * 构造ImageCoord
     * @param task Task对象
     * @param name 图片名
     * */
    public ImageCoord(Task task,String name){
        this(task, ImageInfo.newInstance(name)) ;
    }

    /**
     * 设置坐标相对于图片的偏移
     * */
    public ImageCoord setShift(int x, int y){
        mShiftX = x;
        mShiftY = y ;
        return this ;
    }
    @Override
    public int x() {
        mImageInfo.matchResult(mTask.getScreenMat(), mRect);
        FindResult result = mImageInfo.getFindResult() ;
        if(result != null){
            return result.x + mShiftX ;
        }else{
            return -1 ;
        }
    }

    @Override
    public int y() {
        FindResult result = mImageInfo.getFindResult() ;
        if(result != null){
            return result.y + mShiftY ;
        }else{
            return -1 ;
        }
    }
}
