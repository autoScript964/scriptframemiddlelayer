package com.script.opencvapi.module;

import com.script.AtModule2;
import com.script.opencvapi.OpencvProtocol;

import java.nio.ByteBuffer;

/**
 * Created by     on 2018/2/24.
 */
public class PicModule extends AtModule2 {

    public ByteBuffer pic ;
    public String templateName ;
    public String templateMd5 ;
    public String packageName ;
    public int packageVersion ;
    public int flag ;

    public PicModule() {
        super(OpencvProtocol.TYPE_PIC);
    }

    @Override
    public void initField() {
        try {
            fields.put(OpencvProtocol.ATTR_PIC, getClass().getField("pic")) ;
            fields.put(OpencvProtocol.ATTR_NAME, getClass().getField("templateName")) ;
            fields.put(OpencvProtocol.ATTR_MD5, getClass().getField("templateMd5")) ;
            fields.put(OpencvProtocol.ATTR_FLAG, getClass().getField("flag")) ;
            fields.put(OpencvProtocol.ATTR_PACKAGE, getClass().getField("packageName")) ;
            fields.put(OpencvProtocol.ATTR_VERSION, getClass().getField("packageVersion")) ;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
