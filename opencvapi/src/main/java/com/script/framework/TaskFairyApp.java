package com.script.framework;

import android.app.Application;
import com.script.opencvapi.LtLog;
import com.script.opencvapi.AtFairyService;

/**
 * Created by     on 2018/8/22.
 */
public class TaskFairyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LtLog.i("YPTaskFairyApp onCrate") ;
        AtFairyService.setStarterClass(TaskFairyImpl.class);

    }
}
