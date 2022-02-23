package com.script.framework;

/**
 * 固定位置的坐标
 * Created by     on 2018-12-18.
 */
public class FixedCoord implements ICoord {
    public final int x,y ;
    public FixedCoord(int x, int y){
        this.x = x ;
        this.y = y ;
    }

    @Override
    public int x() {
        return x ;
    }

    @Override
    public int y() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj != null && obj instanceof FixedCoord){
            FixedCoord coord = (FixedCoord) obj;
            return coord.x == x && coord.y == y ;
        }else{
            return  false ;
        }
    }
}
