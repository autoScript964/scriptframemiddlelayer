package com.script.opencvapi;

/**
 * Created by     on 2018/3/30.
 */
public class FindResult {
    public int x ;
    public int y ;
    public int width ;
    public int height ;
    public float sim ;
    public int timestamp ;

    @Override
    public String toString() {
        return "x:"+x+"y:"+y+"sim:"+sim;
    }
}
