package com.script.opencvapi.module;

import com.script.AtModule;

import static com.script.Protocol.ATTR_THE3RD_PACK_NAME;
import static com.script.Protocol.ATTR_THE3RD_SERV_NAME;
import static com.script.Protocol.TYPE_THE3RD_HEARTBEAT_OVER;

/**
 * Created by     on 2018/9/30.
 */
public class HeartBeatOverModule extends AtModule {
    public String packageName ;
    public String serviceName ;
    public HeartBeatOverModule() {
        super(TYPE_THE3RD_HEARTBEAT_OVER);
    }

    @Override
    public void initField() {
        try {
            fields.put(ATTR_THE3RD_PACK_NAME, getClass().getField("packageName")) ;
            fields.put(ATTR_THE3RD_SERV_NAME, getClass().getField("serviceName")) ;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
