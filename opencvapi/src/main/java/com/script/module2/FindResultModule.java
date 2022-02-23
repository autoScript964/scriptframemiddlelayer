package com.script.module2;

import com.script.Protocol;
import com.script.AtModule2;

/**
 * Created by     on 2018/3/29.
 */
public class FindResultModule extends AtModule2 {
    public int x ;
    public int y ;
    public int width ;
    public int height ;
    public int sim ;
    public int timestamp ;

    public FindResultModule() {
        super(Protocol.TYPE_FIND_RESULT);
    }

    public FindResultModule(byte []data, int offset, int count) throws YpModuleException {
        super(data, offset, count);
    }
    @Override
    public void initField() {
        try {
            fields.put(Protocol.ATTR_X, getClass().getField("x")) ;
            fields.put(Protocol.ATTR_Y, getClass().getField("y")) ;
            fields.put(Protocol.ATTR_SIM, getClass().getField("sim")) ;
            fields.put(Protocol.ATTR_TIMESTAMP, getClass().getField("timestamp")) ;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
