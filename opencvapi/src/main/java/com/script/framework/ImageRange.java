package com.script.framework;

import com.script.opencvapi.utils.TemplateInfo;

public class ImageRange implements IRange {

    private int w,h ;

    public ImageRange(String image){
        TemplateInfo info = TaskFairyImpl.getFairy().getTemplateInfo(image) ;
        w = info.width ;
        h = info.height ;
    }

    @Override
    public int width() {
        return w;
    }

    @Override
    public int height() {
        return h;
    }
}
