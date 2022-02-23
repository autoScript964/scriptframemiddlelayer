package com.script.content;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Created by daiepngfei on 2020-08-12
 */
public class MatFactory {

    /**
     * 
     * @param bgr8UC3Src
     * @param opt
     * @return
     * @throws Exception
     */
    public static Mat createCvtMat(Mat bgr8UC3Src, MatOpt opt) throws Exception {

        if(opt == null){
            return bgr8UC3Src;
        }

        Mat mat = new Mat();
        switch (opt.method) {
            case BIN:
                Imgproc.cvtColor(bgr8UC3Src, mat, Imgproc.COLOR_BGR2GRAY);
                Imgproc.threshold(mat, mat, opt.threshold, opt.binMax, opt.binType);
                break;
            case GRAYS:
                Imgproc.cvtColor(bgr8UC3Src, mat, Imgproc.COLOR_BGR2GRAY);
                break;
            case BGR2HLS:
                Imgproc.cvtColor(bgr8UC3Src, mat, Imgproc.COLOR_BGR2HLS);
                break;
            case RGB2HLS:
                Imgproc.cvtColor(bgr8UC3Src, mat, Imgproc.COLOR_BGR2RGB);
                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2HLS);
                break;
            case BGR2HSV:
                Imgproc.cvtColor(bgr8UC3Src, mat, Imgproc.COLOR_BGR2HSV);
                break;
            case RGB2HSV:
                Imgproc.cvtColor(bgr8UC3Src, mat, Imgproc.COLOR_BGR2RGB);
                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2HSV);
                break;
            default:
                mat  = bgr8UC3Src;
                break;
        }

        return mat;
    }

    public static Mat createBGRMatWithArray(byte[] data, int flag) {
        Mat mat = null;
        if(data != null) {
            final Mat buf = new MatOfByte(data);
            mat = Imgcodecs.imdecode(buf, flag);
            buf.release();
        }
        return mat;
    }
}
