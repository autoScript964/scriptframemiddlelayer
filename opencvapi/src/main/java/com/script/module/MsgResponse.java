package com.script.module;


import com.script.Protocol;
import com.script.AtModule;

/**
 * Created by     on 2018/1/18.
 */
public class MsgResponse extends AtModule {
    public int state ;
    public MsgResponse() {
        super(Protocol.TYPE_RESPONSE);
    }
    public MsgResponse(byte[] data, int offset,int count){
        super(data, offset, count);
    }

    @Override
    public void initField() {
        try {
            fields.put(Protocol.ATTR_STATE, getClass().getField("state")) ;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
