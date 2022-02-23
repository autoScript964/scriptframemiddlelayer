package com.script.opencvapi.module;

import com.script.AtModule;

import static com.script.Protocol.ATTR_THE3RD_PACK_NAME;
import static com.script.Protocol.ATTR_THE3RD_SERV_NAME;
import static com.script.Protocol.TYPE_THE3RD_HEARTBEAT;

/**
 * Created by     on 2018/9/30.
 */
public class HeartBeatModule extends AtModule {
    public String packageName ;
    public String serviceName ;
    public HeartBeatModule() {
        super(TYPE_THE3RD_HEARTBEAT);
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
