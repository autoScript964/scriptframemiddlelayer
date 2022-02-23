package com.script.opencvapi.module;


import com.script.AtModule2;
import com.script.opencvapi.OpencvProtocol;

/**
 * Created by     on 2018/2/27.
 */
public class StateModule extends AtModule2 {
    public int state ;
    public StateModule(){
        super(OpencvProtocol.TYPE_STATE);
    }
    public StateModule(byte[] data, int offset, int len) throws YpModuleException {
        super(data, offset, len);
    }

    @Override
    public void initField() {
        try {
            fields.put(OpencvProtocol.ATTR_STATE, getClass().getField("state"));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
