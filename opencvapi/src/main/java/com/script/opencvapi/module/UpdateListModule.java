package com.script.opencvapi.module;


import com.script.AtModule2;
import com.script.opencvapi.OpencvProtocol;

import java.util.List;

/**
 * Created by     on 2018/2/27.
 */
public class UpdateListModule extends AtModule2 {
    public List<String> updateList ;
    public UpdateListModule(){
        super(OpencvProtocol.TYPE_UPDATELIST);
    }
    public UpdateListModule(byte[] data, int offset, int len) throws YpModuleException {
        super(data, offset, len);
    }

    @Override
    public void initField() {
        try {
            fields.put(OpencvProtocol.ATTR_LIST, getClass().getField("updateList")) ;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
