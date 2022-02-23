package com.script.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.script.compat.CompatibilityOnLocal;
import com.script.opencvapi.LtLog;
import com.script.opencvapi.AtFairyService;

/**
 * Created by daiepngfei on 7/29/21
 */
@CompatibilityOnLocal
public class RecordRespawnService extends Service {
    private static final String SP_NAME = "CompatibleSPOnLocalMode";
    private static final String SP_KEY_ISCUSTOMLOCAL = "isCustomLocalService";
    private static final String SP_KEY_ISCUSTOMLOCAL_NAME = "isCustomLocalService_NAME";
    private static final String SP_KEY_ISCUSTOMLOCAL_STARTER_NAME = "isCustomLocalService_STARTER_NAME";
    private static final String TAG = "RecordRespawnService#";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sp = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        final String starterName = sp.getString(SP_KEY_ISCUSTOMLOCAL_STARTER_NAME, "");
        LtLog.d(TAG + "starter: " + starterName);
        if (starterName.length() > 0) {
            final boolean isCustomService = sp.getBoolean(SP_KEY_ISCUSTOMLOCAL, false);
            final String customServiceName = sp.getString(SP_KEY_ISCUSTOMLOCAL_NAME, "");
            LtLog.d(TAG + "isCustom: " + isCustomService + "; name: " + customServiceName);
            if (isCustomService && customServiceName.length() > 0) {
                try {
                    //noinspection rawtypes
                    Class customServiceClass = Class.forName(customServiceName);
                    //noinspection unchecked
                    if (customServiceClass != null && AtFairyService.class.isAssignableFrom(customServiceClass)) {
                        //noinspection unchecked
                        AtFairyService.startService(getApplicationContext(), customServiceClass);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                AtFairyService.startService(getApplicationContext());
            }
        }
        stopSelf();
        return START_NOT_STICKY;
    }

    public static void cacheLocalServiceStatus(Context context, boolean isCustomLocalService, String customServiceName, String starterName) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(SP_KEY_ISCUSTOMLOCAL, isCustomLocalService)
                .putString(SP_KEY_ISCUSTOMLOCAL_NAME, customServiceName)
                .putString(SP_KEY_ISCUSTOMLOCAL_STARTER_NAME, starterName).apply();
    }

}
