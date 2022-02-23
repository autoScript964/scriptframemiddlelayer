package com.script.content.utils;

import com.script.content.MatOpt;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Created by daiepngfei on 2020-09-22
 */
public class UCompat {

    /**
     *
     * @param flag
     * @return
     */
    public static MatOpt.Method getMatOptMethodWithTempFlag(int flag) {
        switch (flag) {
            case Imgcodecs.IMREAD_GRAYSCALE:
                return MatOpt.Method.GRAYS;
            case -1 /*THRESH_FLAG*/:
                return MatOpt.Method.BIN;
            case Imgproc.COLOR_BGR2HLS:
                return MatOpt.Method.BGR2HLS;
            case Imgproc.COLOR_BGR2HSV:
                return MatOpt.Method.BGR2HSV;
            case Imgproc.COLOR_RGB2HLS:
                return MatOpt.Method.RGB2HLS;
            case Imgproc.COLOR_RGB2HSV:
                return MatOpt.Method.RGB2HSV;
            default:
                return MatOpt.Method.DEFAULT;
        }
    }
}
