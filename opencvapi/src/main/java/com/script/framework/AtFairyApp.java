package com.script.framework;

import android.app.Application;

import com.script.opencvapi.LtLog;
import com.script.opencvapi.AtFairyService;

/**
 * Created by Administrator on 2019/1/23 0023.
 */

public class AtFairyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LtLog.i("YPTaskFairyApp onCrate") ;
        AtFairyService.setStarterClass(AtFairyImpl.class);
    }
}