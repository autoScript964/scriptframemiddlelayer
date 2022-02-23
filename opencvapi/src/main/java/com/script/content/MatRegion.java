package com.script.content;

import org.opencv.core.Rect;

/**
 * Created by daiepngfei on 2020-09-21
 */
public class MatRegion extends AbsBounderyRect {

    private Rect bounderyRect = null;

    public void setBounderyRect(Rect bounderyRect) {
        this.bounderyRect = bounderyRect;
    }
}

abstract class AbsBounderyRect {
    private Rect boundingRect = null;

}