package com.script.opencvapi.module;


import com.script.AtModule2;
import com.script.opencvapi.OpencvProtocol;

import java.util.List;

/**
 * Created by     on 2018/2/27.
 */
public class TemplateListModule extends AtModule2 {
    public String packageName ;
    public int packageVersion ;
    public List<String> templateList ;
    public TemplateListModule(){
        super(OpencvProtocol.TYPE_TEMPLATE_LIST);
    }
    public TemplateListModule(byte[] data, int offset, int len) throws YpModuleException {
        super(data, offset, len);
    }

    @Override
    public void initField() {
        try {
            fields.put(OpencvProtocol.ATTR_PACKAGE,getClass().getField("packageName")) ;
            fields.put(OpencvProtocol.ATTR_VERSION,getClass().getField("packageVersion")) ;
            fields.put(OpencvProtocol.ATTR_LIST,getClass().getField("templateList")) ;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
