package com.script.framework;

/**
 * 固定大小的范围
 * Created by     on 2018-12-18.
 */
public class SizeRange  implements IRange{
    public final int w,h ;
    public SizeRange(int w, int h){
        this.w = w ;
        this.h = h ;
    }
    @Override
    public int width() {
        return w;
    }

    @Override
    public int height() {
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj != null && obj instanceof FixedCoord){
            SizeRange range = (SizeRange) obj;
            return range.w == w && range.h == h ;
        }else{
            return  false ;
        }
    }
}
