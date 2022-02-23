package com.script.opencvapi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;


/**
 * Created by     on 2018/3/16.
 */
public class RequestPermissionActivity extends Activity {

    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private static final int REQUEST_EXTERNAL_STORAGE = 2;

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    private Intent mIntentResult = null;

    public static void startActivity(Context from) {
        Intent intent = new Intent(from, Build.VERSION.SDK_INT > 21 ? RequestPermissionActivity.class : CompatRequestPermissionActivity.class);
        if (!(from instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PackageInfo info = null;
        ;
        int serviceVersion = 0;
        try {
            info = getPackageManager().getPackageInfo(AtFairy2.YPSERVICE_PKG_NAME, 0);
            serviceVersion = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {

            e.printStackTrace();
        }

        if (mIntentResult == null && serviceVersion < AtFairyService.YPSERVICE_CAPTURE_VERSION) {
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
        } else if (!checkPermissions(this)) {
            verifyStoragePermissions(this);
        } else {
            finish();
            LtLog.i("on create start service........");
            startService();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LtLog.i("on activity result requestCode:" + requestCode);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            mIntentResult = data;
        }
        if (!checkPermissions(this)) {
            verifyStoragePermissions(this);
        } else {
            startService();
            finish();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        LtLog.i("onRequestPermissionsResult....:" + requestCode);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            startService();
            finish();
        }
    }

    private void startService() {
        AtFairyService.startService(this, mIntentResult);
    }


    public static void verifyStoragePermissions(Activity activity) {
        try {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean checkPermissions(Context context) {
        for (String s : PERMISSIONS_STORAGE) {
            int permission = ActivityCompat.checkSelfPermission(context, s);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


}
