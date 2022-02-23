package com.script.opencvapi.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.script.opencvapi.FindResult;
import com.script.opencvapi.LtLog;
import com.script.opencvapi.ScreenInfo;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by     on 2018/3/29.
 */
public class AtOpencvUtils {

    public static final int DEFAULT_METHOD = Imgproc.TM_CCOEFF_NORMED;
    public static final int THRESH_FLAG = -1;
    public static final int GRAY_FLAG = Imgcodecs.IMREAD_GRAYSCALE;
    public static final int COLOR_FLAG = Imgcodecs.IMREAD_COLOR;

    public static final String KEY_VERSION = "version";
    public static final String KEY_FLAG = "flag";
    public static final String KEY_THRESH = "thresh";
    public static final String KEY_MAXVAL = "maxval";
    public static final String KEY_TYPE = "type";
    public static final String KEY_X = "x";
    public static final String KEY_Y = "y";
    public static final String KEY_WIDTH = "width";
    public static final String KEY_HEIGHT = "height";
    public static final String KEY_SIM = "sim";
    private Map<String, Mat> mTemplateMap;
    private Map<String, TemplateInfo> mTemplateInfoMap;
    private TemplateInfo mDefaultInfo;


    static {
        System.loadLibrary("opencv_java3");
    }

    public AtOpencvUtils() {
        mTemplateMap = new HashMap<>();
        mTemplateInfoMap = new HashMap<>();
        mDefaultInfo = new TemplateInfo();
        mDefaultInfo.x = 0;
        mDefaultInfo.y = 0;
        mDefaultInfo.width = 0;
        mDefaultInfo.height = 0;
        mDefaultInfo.flag = Imgcodecs.IMREAD_COLOR;
        mDefaultInfo.maxval = 0;
        mDefaultInfo.thresh = 0;
        mDefaultInfo.type = 0;
    }

    private Mat getColorMat(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(byteBuffer);
        Mat colorMat = new Mat();
        switch (bitmap.getConfig()) {
            case RGB_565: {
                Mat mat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC3);
                mat.put(0, 0, byteBuffer.array());
                colorMat = mat;
            }
            break;
            case ARGB_8888: {
                Mat mat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
                mat.put(0, 0, byteBuffer.array());
                Imgproc.cvtColor(mat, colorMat, Imgproc.COLOR_RGBA2RGB);
                mat.release();
            }
            break;
        }
        return colorMat;
    }

    private Mat getGrayMat(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(byteBuffer);
        Mat grayMat = new Mat();
        switch (bitmap.getConfig()) {
            case RGB_565: {
                Mat mat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC3);
                mat.put(0, 0, byteBuffer.array());
                Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_RGB2GRAY);
                mat.release();
            }
            break;
            case ARGB_8888: {
                Mat mat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
                mat.put(0, 0, byteBuffer.array());
                Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_RGB2GRAY);
                mat.release();
            }
            break;
        }
        return grayMat;
    }

    private Mat threshMat(Mat mat, int thresh, int maxval, int type) {
        Mat thrashMat = new Mat();
        Imgproc.threshold(mat, thrashMat, thresh, maxval, type);
        return thrashMat;
    }

    /*
    public Mat getTemplateMat(String name, int flag, int thresh, int maxval, int type){
        TemplateMatInfo templateMatInfo = mTemplateMatMap.get(name) ;
        Mat requiredMat = null;
        if(templateMatInfo == null){
            templateMatInfo = new TemplateMatInfo() ;
            mTemplateMatMap.put(name, templateMatInfo) ;
        }
        switch (flag){
            case  Imgcodecs.IMREAD_COLOR :
                requiredMat = templateMatInfo.colorMat ;
                break;
            case Imgcodecs.IMREAD_GRAYSCALE :
                requiredMat = templateMatInfo.grayMat ;
                break ;
        }
        if(requiredMat == null){
            byte[] data = YpFairyUtils.getInstance().getTemplateData(name) ;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length) ;
            switch (flag){
                case Imgcodecs.IMREAD_COLOR :
                    requiredMat = getColorMat(bitmap) ;
                    templateMatInfo.colorMat = requiredMat ;
                    break;
                case Imgcodecs.IMREAD_GRAYSCALE :
                    requiredMat = getGrayMat(bitmap) ;
                    templateMatInfo.grayMat = requiredMat ;
                    break;
                default:
                    Mat grayMat = templateMatInfo.grayMat ;
                    if(grayMat == null){
                        grayMat = getGrayMat(bitmap) ;
                        templateMatInfo.grayMat = grayMat ;
                    }
                    requiredMat = threshMat(grayMat, thresh, maxval, type) ;
                    if(templateMatInfo.threshMat != null){
                        templateMatInfo.threshMat.release();
                    }
                    templateMatInfo.threshMat = requiredMat ;
                    break;
            }
            bitmap.recycle();
        }
        return  requiredMat ;
    }*/
    public static void writeTemplateInfo(TemplateInfo info, OutputStream out) {
        Properties properties = new Properties();
        properties.setProperty(KEY_FLAG, String.valueOf(info.flag));
        properties.setProperty(KEY_HEIGHT, String.valueOf(info.height));
        properties.setProperty(KEY_WIDTH, String.valueOf(info.width));
        properties.setProperty(KEY_MAXVAL, String.valueOf(info.maxval));
        properties.setProperty(KEY_THRESH, String.valueOf(info.thresh));
        properties.setProperty(KEY_X, String.valueOf(info.x));
        properties.setProperty(KEY_Y, String.valueOf(info.y));
        properties.setProperty(KEY_TYPE, String.valueOf(info.type));
        properties.setProperty(KEY_SIM, String.valueOf(info.sim));
        try {
            properties.store(out, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TemplateInfo getTemplateInfo(InputStream in) {
        TemplateInfo info = new TemplateInfo();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        Properties properties = new Properties();
        try {
            properties.load(reader);
            try {
                info.version = Integer.parseInt(properties.getProperty(KEY_VERSION));
            } catch (Exception e) {
            }
            try {
                info.flag = Integer.parseInt(properties.getProperty(KEY_FLAG));
            } catch (Exception e) {
            }
            ;
            try {
                info.height = Integer.parseInt(properties.getProperty(KEY_HEIGHT));
            } catch (Exception e) {
            }
            ;
            try {
                info.width = Integer.parseInt(properties.getProperty(KEY_WIDTH));
            } catch (Exception e) {
            }
            ;
            try {
                info.maxval = Integer.parseInt(properties.getProperty(KEY_MAXVAL));
            } catch (Exception e) {
            }
            ;

            try {
                info.thresh = Integer.parseInt(properties.getProperty(KEY_THRESH));
            } catch (Exception e) {
            }
            ;
            try {
                info.x = Integer.parseInt(properties.getProperty(KEY_X));
            } catch (Exception e) {
            }
            ;
            try {
                info.y = Integer.parseInt(properties.getProperty(KEY_Y));
            } catch (Exception e) {
            }
            ;
            try {
                info.type = Integer.parseInt(properties.getProperty(KEY_TYPE));
            } catch (Exception e) {
            }
            ;
            try {
                info.sim = Integer.parseInt(properties.getProperty(KEY_SIM));
            } catch (Exception e) {
            }
            ;


        } catch (IOException e) {
            e.printStackTrace();
        }
        return info;
    }

    public TemplateInfo getTemplateInfo(String picName) {
        TemplateInfo info = mTemplateInfoMap.get(picName);
        if (info == null) {
            String templateInfoFile = picName + ".info";
            byte data[] = AtFairyUtils.getInstance().getTemplateData(templateInfoFile);
            if (data == null) {
                return mDefaultInfo;
            }
            info = getTemplateInfo(new ByteArrayInputStream(data));
            /*
            info = new TemplateInfo() ;
            BufferedReader reader = new BufferedReader(new StringReader(new String(data))) ;

            Properties properties = new Properties() ;
            try {
                properties.load(reader);
                try {
                    info.flag = Integer.parseInt(properties.getProperty(KEY_FLAG));
                }catch (Exception e){} ;
                try {
                    info.height = Integer.parseInt(properties.getProperty(KEY_HEIGHT));
                }catch (Exception e){} ;
                try {
                    info.width = Integer.parseInt(properties.getProperty(KEY_WIDTH));
                }catch (Exception e){} ;
                try {
                    info.maxval = Integer.parseInt(properties.getProperty(KEY_MAXVAL));
                }catch (Exception e){} ;

                try {
                    info.thresh = Integer.parseInt(properties.getProperty(KEY_THRESH));
                }catch (Exception e){} ;
                try {
                    info.x = Integer.parseInt(properties.getProperty(KEY_X));
                }catch (Exception e){} ;
                try {
                    info.y = Integer.parseInt(properties.getProperty(KEY_Y));
                }catch (Exception e){} ;
                try {
                    info.type = Integer.parseInt(properties.getProperty(KEY_TYPE));
                }catch (Exception e){} ;


            } catch (IOException e) {
                e.printStackTrace();
            }*/
            //mTemplateInfoMap.put(picName, info);
        }
        return info;
    }

    public Mat getTemplateMat(String picName) {
        Mat mat = mTemplateMap.get(picName);
        if (mat == null) {
            Mat buf = new MatOfByte(AtFairyUtils.getInstance().getTemplateData(picName));
            mat = Imgcodecs.imdecode(buf, Imgcodecs.IMREAD_UNCHANGED);
            buf.release();
//            mTemplateMap.put(picName, mat);
        }
        return mat;
    }

    public Mat getScreenMat(ScreenInfo screenInfo, TemplateInfo info) {
        return getScreenMat(info.x, info.y, info.width, info.height, info.flag, screenInfo, info.thresh, info.maxval, info.type);
    }

    public Mat getScreenMat(int leftX, int leftY, int width, int height, int flag, Mat screenMat, int thresh, int maxval, int type) {
        Rect roi = new Rect(leftX, leftY, width, height);
        Mat srcMat = new Mat(screenMat, roi);
        Mat cvtMat = new Mat();
        switch (flag) {
            case Imgcodecs.IMREAD_COLOR:
                Imgproc.cvtColor(srcMat, cvtMat, Imgproc.COLOR_BGR2RGB);
                break;
            case Imgcodecs.IMREAD_GRAYSCALE:
                Imgproc.cvtColor(srcMat, cvtMat, Imgproc.COLOR_BGR2GRAY);
                break;
            case THRESH_FLAG:
                Mat tmpMat = new Mat();
                Imgproc.cvtColor(srcMat, cvtMat, Imgproc.COLOR_BGR2GRAY);
                Imgproc.threshold(cvtMat, tmpMat, thresh, maxval, type);
                cvtMat.release();
                cvtMat = tmpMat;
                break;
            default:
                Imgproc.cvtColor(srcMat, cvtMat, flag);
                break;
        }
        srcMat.release();
        return cvtMat;
    }

    public Mat getScreenMat(int leftX, int leftY, int width, int height,
                            int flag, ScreenInfo rawScreenInfo,
                            int thresh, int maxval, int type) {
        if (rawScreenInfo.raw == null) {
            return null;
        }
        if (leftX + width > rawScreenInfo.width || leftY + height > rawScreenInfo.height) {
          /*  LtLog.i("getScreenMat error " +
                     " screen width:" +rawScreenInfo.width +"rightX:"+(leftX+width)  +
                     " screen height:"+rawScreenInfo.height+" rightY:"+(leftY+height)) ;*/
            return null;
        }
        Mat screenMat = new Mat(rawScreenInfo.height, rawScreenInfo.width, CvType.CV_8UC4);
        screenMat.put(0, 0, rawScreenInfo.raw);
        Mat resultMat = getScreenMat(leftX, leftY, width, height, flag, screenMat, thresh, maxval, type);
        screenMat.release();
        return resultMat;
    }

    public Mat getTemplateMatFromAssert(String picName) {
        Mat mat = mTemplateMap.get(picName);
        if (mat == null) {
            Mat buf = new MatOfByte(AtFairyUtils.getInstance().getTemplateDataFromAssert(picName));
            mat = Imgcodecs.imdecode(buf, Imgcodecs.IMREAD_UNCHANGED);
            buf.release();
            mTemplateMap.put(picName, mat);
        }
        return mat;
    }

    public void reset() {
        if (mTemplateMap != null) {
            Collection<Mat> mats = mTemplateMap.values();
            for (Mat mat : mats) {
                mat.release();
            }
            mTemplateMap.clear();
        }
        if (mTemplateInfoMap != null) {
            mTemplateInfoMap.clear();
        }
    }

    public TemplateInfo getTemplateInfoFromAssrt(String picName) {
        TemplateInfo info = mTemplateInfoMap.get(picName);
        if (info == null) {
            String templateInfoFile = picName + ".info";
            byte data[] = AtFairyUtils.getInstance().getTemplateDataFromAssert(templateInfoFile);
            if (data == null) {
                return mDefaultInfo;
            }
            info = getTemplateInfo(new ByteArrayInputStream(data));
            mTemplateInfoMap.put(picName, info);
        }
        return info;
    }


    public FindResult findTemplate(int leftX, int leftY,
                                   Mat templateMat, Mat screenMat, int method) {
        FindResult findResult = new FindResult();
        long start = System.currentTimeMillis();
        if (templateMat != null && screenMat != null) {
            Mat resultMat = new Mat();
            if (screenMat.width() >= templateMat.width() && screenMat.height() >= templateMat.height()) {
                Imgproc.matchTemplate(screenMat, templateMat, resultMat, method);
                Core.MinMaxLocResult mmr;
                mmr = Core.minMaxLoc(resultMat);
                findResult.sim = (float) mmr.maxVal;
                findResult.x = (int) mmr.maxLoc.x + leftX;
                findResult.y = (int) mmr.maxLoc.y + leftY;
                resultMat.release();
            } else {
                findResult.sim = 0;
                findResult.x = 0;
                findResult.y = 0;
                LtLog.e("findTemplate error screen mat size less then template mat");
//                Imgproc.matchTemplate(screenMat, templateMat, resultMat, method);
            }
        }
        long end = System.currentTimeMillis();

        findResult.width = templateMat.width();
        findResult.height = templateMat.height();
        findResult.timestamp = (int) (end - start);
        return findResult;
    }

    public FindResult findPic(int leftX, int leftY, int rightX, int rightY, String picName, ScreenInfo screenInfo) {
        TemplateInfo templateInfo = getTemplateInfo(picName);
        if (templateInfo == null) {
            templateInfo = mDefaultInfo;
        }
        Mat templateMat = getTemplateMat(picName);
        if (leftX < 0) {
            leftX = 0;
        }
        if (leftY < 0) {
            leftY = 0;
        }
        int width = rightX - leftX;
        if (width > screenInfo.width) {
            width = screenInfo.width;
        }
        int height = rightY - leftY;
        if (height > screenInfo.height) {
            height = screenInfo.height;
        }
        Mat screenMat = getScreenMat(leftX, leftY, width, height,
                templateInfo.flag, screenInfo, templateInfo.thresh, templateInfo.maxval, templateInfo.type);
        FindResult result = findTemplate(leftX, leftY, templateMat, screenMat, DEFAULT_METHOD);
        if (screenMat != null) {
            screenMat.release();
        }
        return result;
    }

    public FindResult findPic(String picName, ScreenInfo screenInfo) {
        return findPicRange(picName, screenInfo, 0);
    }

    public FindResult findPicRange(String picName, ScreenInfo screenInfo, int wide) {
        TemplateInfo templateInfo = getTemplateInfo(picName);
        if (templateInfo == null) {
            return null;
        }
        Mat templateMat = getTemplateMat(picName);
        int leftX = templateInfo.x - wide;
        if (leftX < 0) {
            leftX = 0;
        }
        int leftY = templateInfo.y - wide;
        if (leftY < 0) {
            leftY = 0;
        }
        int width = templateInfo.width + wide;
        if (width > screenInfo.width) {
            width = screenInfo.width;
        }
        int height = templateInfo.height + wide;
        if (height > screenInfo.height) {
            height = screenInfo.height;
        }

        Mat screenMat = getScreenMat(leftX, leftY, width, height,
                templateInfo.flag, screenInfo, templateInfo.thresh, templateInfo.maxval, templateInfo.type);

        FindResult result = findTemplate(leftX, leftY, templateMat, screenMat, DEFAULT_METHOD);
        if (screenMat != null) {
            screenMat.release();
        }
        return result;
    }

    public int getColorCount(int color, float sim, Mat mat) {
        return getColorCount(Color.red(color), Color.green(color), Color.blue(color), sim, mat);
    }

    public int getColorCount(int r, int g, int b, float sim, Mat mat) {
        double simValue = 255 * (1 - sim);
        double t;
        double min_r = (t = r - simValue) < 0 ? 0 : t;
        double min_g = (t = g - simValue) < 0 ? 0 : t;
        double min_b = (t = b - simValue) < 0 ? 0 : t;
        double max_r = (t = r + simValue) > 255 ? 255 : t;
        double max_g = (t = g + simValue) > 255 ? 255 : t;
        double max_b = (t = b + simValue) > 255 ? 255 : t;

        Scalar minValues = new Scalar(min_r, min_g, min_b);
        Scalar maxValues = new Scalar(max_r, max_g, max_b);
        Core.inRange(mat, minValues, maxValues, mat);
        return Core.countNonZero(mat);
    }


    public FindResult findTemplate(int leftX, int leftY, int rightX, int rightY,
                                   String picName, ScreenInfo rawScreenInfo,
                                   int thresh, int maxval, int type, int method) {
        /*
        long start = System.currentTimeMillis() ;
        Mat templateMat = getTemplateMat(picName) ;
        Mat screenMat = getScreenMat(leftX, leftY, rightX, rightY,THRESH_FLAG,
                                        rawScreenInfo,thresh, maxval, type ) ;
        Mat resultMat = new Mat() ;
        Imgproc.matchTemplate(screenMat, templateMat,resultMat, method);
        Core.MinMaxLocResult mmr;
        mmr = Core.minMaxLoc(resultMat);
        long end = System.currentTimeMillis() ;
        FindResult findResult = new FindResult() ;
        findResult.sim = (float) mmr.maxVal;
        findResult.x = (int) mmr.maxLoc.x + leftX;
        findResult.y = (int) mmr.maxLoc.y + leftY;
        findResult.width = templateMat.width() ;
        findResult.height = templateMat.height() ;
        findResult.timestamp = (int) (end -start);
        */

        Mat templateMat = getTemplateMat(picName);
        Mat screenMat = getScreenMat(leftX, leftY, rightX, rightY, THRESH_FLAG,
                rawScreenInfo, thresh, maxval, type);
        FindResult result = findTemplate(leftX, leftY, templateMat, screenMat, method);
        if (screenMat != null) {
            screenMat.release();
        }
        return result;
    }

}

