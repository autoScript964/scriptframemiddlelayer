package com.script.opencvapi.module;

import com.script.AtModule2;
import com.script.opencvapi.utils.AtScreencap2;

import java.nio.ByteBuffer;

/**
 * Created by     on 2018/7/19.
 */
public class ScreenInfoModule extends AtModule2 {
    public int width ;
    public int height ;
    public int timestamp ;
    public ByteBuffer img ;
    public ScreenInfoModule(byte[] data, int offset, int len) throws YpModuleException {
        super(data, offset, len);
    }
    public ScreenInfoModule(){
        super(AtScreencap2.TYPE_IAMGE);
    }

    @Override
    public void initField() {
        try {
            fields.put(AtScreencap2.ATTR_WIDTH, getClass().getField("width")) ;
            fields.put(AtScreencap2.ATTR_HEIGHT, getClass().getField("height")) ;
            fields.put(AtScreencap2.ATTR_IMG, getClass().getField("img")) ;
            fields.put(AtScreencap2.ATTR_TIMESTAMP, getClass().getField("timestamp")) ;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

    }
}
