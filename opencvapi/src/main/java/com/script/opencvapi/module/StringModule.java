package com.script.opencvapi.module;


import com.script.AtModule2;
import com.script.opencvapi.OpencvProtocol;

/**
 * Created by     on 2018/2/27.
 */
public class StringModule extends AtModule2 {
    public String str ;
    public StringModule(short type){
        super(type);
    }
    public StringModule(byte[] data, int offset, int len) throws YpModuleException {
        super(data, offset, len);
    }

    @Override
    public void initField() {
        try {
            fields.put(OpencvProtocol.ATTR_STR, getClass().getField("str")) ;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
