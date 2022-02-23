package com.script.opencvapi.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.script.opencvapi.LtLog;
import com.script.opencvapi.ScreenInfo;
import com.script.opencvapi.AtFairy2;

import org.opencv.core.Mat;

import java.nio.ByteBuffer;

import static org.opencv.core.CvType.CV_8U;

/**
 * Created by     on 2018/3/29.
 */
public class AtScreencap {

    private Context mContext ;
    private Intent mIntentResult ;

    private int mWindowWidth = 0;
    private int mWindowHeight = 0;
    private int mScreenWidth  = 0 ;
    private int mScreenHeight = 0 ;
    private int mScreenDensity = 0;
    private ImageReader mImageReader = null;
    private MediaProjectionManager mMediaProjectionManager ;
    private MediaProjection mMediaProjection = null;
    private VirtualDisplay mVirtualDisplay = null;
    private boolean mEnableCapture = true ;

    public AtScreencap(Context context, Intent resultIntent){
        mContext = context ;
        mIntentResult = resultIntent ;
        if(mIntentResult != null) {
            createVirtual();
        }
    }

    private void createVirtual(){
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        mWindowWidth = metrics.widthPixels;
        mWindowHeight = metrics.heightPixels;
        mScreenWidth = mWindowWidth ;
        mScreenHeight = mWindowHeight ;
        mImageReader = ImageReader.newInstance(mWindowWidth, mWindowHeight, 0x01, 3); //ImageFormat.RGB_565
        setUpMediaProjection();
        virtualDisplay();
    }

    private void setUpMediaProjection(){
        mMediaProjectionManager = (MediaProjectionManager)mContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if(mMediaProjection == null) {
            mMediaProjection = mMediaProjectionManager.getMediaProjection(-1, mIntentResult);
        }
    }
    private void virtualDisplay(){
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                mWindowWidth, mWindowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }
    private void recreate(){
        mVirtualDisplay.release();
        mImageReader.close();
        createVirtual();
    }
    public int  getWidth(){
        return   mWindowWidth;
    }
    public int  getHeight(){
        return  mWindowHeight;
    }
    public ScreenInfo captureRawScreen(ScreenInfo screenInfo){
        Image image = mImageReader.acquireLatestImage();
        if(image == null){
            return  null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        if(width != screenInfo.width || height != screenInfo.height){
            LtLog.i("width:"+width+" screeninfo width:"+screenInfo.width) ;
            recreate();
            captureRawScreen(screenInfo) ;
        }

        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        width = rowStride/pixelStride ;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);

        int bytecount = bitmap.getByteCount() ;
        ByteBuffer rawBuffer = ByteBuffer.allocate(bytecount) ;
        bitmap.copyPixelsToBuffer(rawBuffer);
        bitmap.recycle();
        screenInfo.raw = rawBuffer.array() ;
        screenInfo.width = width ;
        screenInfo.height = height ;
        image.close();
        return  screenInfo ;

    }

    public byte[] captureBitmap(){
        Image image = mImageReader.acquireLatestImage();
        if(image == null){
            return  new byte[1] ;
        }
       /* int width = image.getWidth();
        int height = image.getHeight();*/
       /* if(width != screenInfo.width || height != screenInfo.height){
            LtLog.i("width:"+width+" screeninfo width:"+screenInfo.width) ;
            recreate();
            captureRawScreen(screenInfo) ;
        }*/

      //aaa
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int len =buffer.limit() - buffer.position();
        byte[] bytes1 = new byte[len];
        buffer.get(bytes1);
        image.close();
        return bytes1;
    }
    public Mat captureMat(){
        Image image = mImageReader.acquireLatestImage();
        if(image == null){
            return null;
        }
       /* int width = image.getWidth();
        int height = image.getHeight();*/
       /* if(width != screenInfo.width || height != screenInfo.height){
            LtLog.i("width:"+width+" screeninfo width:"+screenInfo.width) ;
            recreate();
            captureRawScreen(screenInfo) ;
        }*/
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int len =buffer.limit() - buffer.position();
        byte[] bytes1 = new byte[len];
        buffer.get(bytes1);
        Mat mat2 = new Mat(width, height, CV_8U);
        mat2.put(0, 0,bytes1);
        image.close();
        return mat2;
    }

    public AtFairy2.RawScreenInfo captureRaw(AtFairy2.RawScreenInfo screenInfo){
        Image image = mImageReader.acquireLatestImage();
        if(image == null){
            return  null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        if(width != screenInfo.width || height != screenInfo.height){
            LtLog.i("width:"+width+" screeninfo width:"+screenInfo.width) ;
            recreate();
            captureRaw(screenInfo) ;
        }

        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        width = rowStride/pixelStride ;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);

        int bytecount = bitmap.getByteCount() ;
        ByteBuffer rawBuffer = ByteBuffer.allocate(bytecount) ;
        bitmap.copyPixelsToBuffer(rawBuffer);
        bitmap.recycle();
        screenInfo.raw = rawBuffer.array() ;
        screenInfo.width = width ;
        screenInfo.height = height ;
        return  screenInfo ;
    }

}
