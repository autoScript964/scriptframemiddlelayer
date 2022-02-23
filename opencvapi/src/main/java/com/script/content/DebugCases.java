package com.script.content;

import org.opencv.core.Mat;

/**
 * Created by daiepngfei on 2020-09-22
 */
public class DebugCases {

    /**
     *
     * @param name
     * @param mat
     */
    public static void case_imwrite_assets(String name, Mat mat){
        imwriteMat("assets_" + name, mat);
    }

    /**
     *
     * @param name
     * @param mat
     */
    public static void case_imwrite_screenCap(String name, Mat mat){
        imwriteMat("screencap_" + name, mat);
    }

    private static void imwriteMat(String name, Mat mat){
        ScProxy.config().Debugger().imwriteAssetsMat(name, mat);
    }

    public static void imwriteMatAdd(String... name){
        for(String n : name) {
            ScProxy.config().Debugger().addImwriteAssetsMat("screencap_" + n);
            ScProxy.config().Debugger().addImwriteAssetsMat("assets_" + n);
        }
    }

}
