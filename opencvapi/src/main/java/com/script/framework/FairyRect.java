package com.script.framework;

/**
 * Created by     on 2018-12-18.
 */
public class FairyRect {
    private ICoord mCoord ;
    private IRange mRange ;

    /**
     * 使用固定位置的坐标和大小创建一个Rect
     * */
    public static FairyRect createRect(int x, int y, int w, int h){
        return  new FairyRect(new FixedCoord(x,y), new SizeRange(w,h)) ;
    }

    public FairyRect(ICoord coord, IRange range){
        mCoord = coord ;
        mRange = range ;
    }
    public int x(){
        return mCoord.x() ;
    }
    public int y(){
        return mCoord.y() ;
    }
    public int width(){
        return mRange.width() ;
    }
    public int height(){
        return mRange.height() ;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj != null && obj instanceof FairyRect){
            FairyRect rect = (FairyRect) obj;
            return rect.mCoord.equals(mCoord) && rect.mRange.equals(mRange) ;
        }else{
            return false ;
        }
    }
}
