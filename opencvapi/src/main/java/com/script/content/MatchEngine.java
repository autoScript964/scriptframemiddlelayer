package com.script.content;

import android.util.Log;

import com.script.content.utils.UStr;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.HashMap;

/**
 * Created by daiepngfei on 2020-08-11
 */
public class MatchEngine {

    private static final String TAG = ScProxy.TAG + "#" + "MatchEngine";
    private static final ThreadLocal<HashMap<String, MatchResult>> sfCachedResults = new ThreadLocal<>();
    private static final ThreadLocal<Long> sfCachedResultsKey = new ThreadLocal<>();

    MatchEngine(ScProxy scProxy) {

    }

    public MatchResult doMatching(Mat bgr8UC3Src, Mat tar, MatchOpt opt, String tag) {
        return MatchCore.doMatching(bgr8UC3Src, tar, opt, tag);
    }

    static final class MatchCore {

        static MatchResult doMatching(Mat bgr8UC3Src, Mat tarMat, MatchOpt opt, String extraInfo) {
            if (bgr8UC3Src == null || bgr8UC3Src.empty() || tarMat == null || tarMat.empty()) {
                return new MatchResult(0.8F);
            }
            if (opt == null) {
                opt = Opt();
            }
            Long bgrSrcKey = sfCachedResultsKey.get();
            if (bgrSrcKey == null || bgrSrcKey != bgr8UC3Src.nativeObj) {
                bgrSrcKey = bgr8UC3Src.nativeObj;
                sfCachedResultsKey.set(bgrSrcKey);
                sfCachedResults.remove();
            }
            HashMap<String, MatchResult> cachedMap = sfCachedResults.get();
            if (cachedMap == null) {
                cachedMap = new HashMap<>();
                sfCachedResults.set(cachedMap);
            }
            final String key = bgrSrcKey + "_" + tarMat.nativeObj + "_" + opt.asKey();
            if (cachedMap.get(key) != null) {
                final MatchResult r = cachedMap.get(key);
                r.isCachedResult = true;
                return r;
            }

            final MatchResult r = doGetMatchResult(bgr8UC3Src, tarMat, opt, extraInfo);
            cachedMap.put(key, r);
            return r;
        }

        private static MatchResult doGetMatchResult(Mat bgr8UC3Src, Mat tarMat, MatchOpt opt, String extraInfo) {
            final MatchResult resultToBeSet = new MatchResult(opt.sim);
            try {
                // create regioned-src-mat
                Mat regionSrc;
                if (opt.region != null && !opt.region.empty()) {
                    final Rect rect = new Rect(0, 0, bgr8UC3Src.width(), bgr8UC3Src.height());
                    if (!rect.contains(new Point(opt.region.x, opt.region.y))
                            || opt.region.width > bgr8UC3Src.width() || opt.region.height > bgr8UC3Src.height()) {
                        return resultToBeSet;
                    }
                    final int x = Math.max(0, opt.region.x - opt.regionExpansion);
                    final int y = Math.max(0, opt.region.y - opt.regionExpansion);
                    final int w = Math.min(opt.region.x + opt.regionExpansion + opt.region.width, bgr8UC3Src.width()) - x;
                    final int h = Math.min(opt.region.y + opt.regionExpansion + opt.region.height, bgr8UC3Src.height()) - y;
                    final Rect expandedRegion = new Rect(x, y, w, h);
                    opt.setExpandedRegion(expandedRegion);
                    try {
                        regionSrc = new Mat(bgr8UC3Src, expandedRegion);
                    } catch (Exception e1) {
                        Log.e(TAG + "=> " + extraInfo,
                                "regionRect: "
                                        + expandedRegion.toString()
                                        + "/srcMat: "
                                        + bgr8UC3Src.width()
                                        + "," + bgr8UC3Src.height()
                                        + " # " + opt.region.toString(), e1);
                        throw e1;
                    }
                } else {
                    regionSrc = new Mat();
                    bgr8UC3Src.copyTo(regionSrc);
                }

                // cvt-color regioned-src-mat with method
                Mat processedMat = MatFactory.createCvtMat(regionSrc, opt.matOpt);

                // DEBUGING CASES
                {
                    DebugCases.case_imwrite_screenCap(extraInfo, processedMat);
                }

                // match template with cvt-region-src-mat
                Mat resultMat = new Mat();
                Imgproc.matchTemplate(processedMat, tarMat, resultMat, opt.matchMethod);

                // find result
                Core.MinMaxLocResult mmr;
                mmr = Core.minMaxLoc(resultMat);
                resultToBeSet.sim = (float) mmr.maxVal;
                resultToBeSet.shortSim = (int)(resultToBeSet.sim  * 100) / 100F + "";
                int offsetX = 0, offsetY = 0;
                if(opt.expandedRegion != null){
                    offsetX = opt.expandedRegion.x;
                    offsetY = opt.expandedRegion.y;
                }
                resultToBeSet.x = (int) mmr.maxLoc.x + offsetX;
                resultToBeSet.y = (int) mmr.maxLoc.y + offsetY;
                resultToBeSet.w = tarMat.width();
                resultToBeSet.h = tarMat.height();

                regionSrc.release();
                resultMat.release();
                processedMat.release();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultToBeSet;
        }

    }

    public static final class MatchResult {
        private final float defaultSim;
        private float sim = 0;
        private String shortSim = "0.00";
        private int x;
        private int y;
        private int w;
        private int h;
        private boolean isCachedResult;

        MatchResult(float targetSim) {
            this.defaultSim = targetSim;
        }

        public boolean ok() {
            return sim >= defaultSim;
        }

        public float getSim() {
            return sim;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getW() {
            return w;
        }

        public int getH() {
            return h;
        }

        public boolean isCachedResult() {
            return isCachedResult;
        }

        @Override
        public String toString() {
            return "{"
                    + "Hit: " + (ok() ? "Yes" : "No ") + "; "
                    + "Cache: " + (isCachedResult() ? "Yes" : "No ") + "; "
                    + "Sim/Def: " + UStr.sfls(UStr.Pat.SPACES_30, shortSim, 4) + "/"
                                  + UStr.sfls(UStr.Pat.SPACES_30, defaultSim + "", 4) + "; "
                    + "x/y/w/h: [" + UStr.pfls(UStr.Pat.SPACES_30, x + "", 4)
                            + ", " + UStr.pfls(UStr.Pat.SPACES_30, y + "", 4)
                            + ", " + UStr.pfls(UStr.Pat.SPACES_30, w + "", 4)
                            + ", " + UStr.pfls(UStr.Pat.SPACES_30, h + "", 4)
                            + "] "
                    + "}";
        }
    }


    public static MatchOpt Opt() {
        return Opt(null);
    }


    public static MatchOpt Opt(MatOpt opt){
        MatchOpt optR = new MatchOpt();
        if(opt != null) {
            optR.matOpt(opt);
        }
        return optR;
    }

    public static class Region extends Rect {

        private Region(int x, int y, int w, int h) {
            super(x, y, w, h);
        }

        private Region() {

        }

        public static Region create(int x, int y, int w, int h) {
            return new Region(x, y, w, h);
        }

        public static Region create() {
            return new Region();
        }

        public Region startAt(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Region expandWith(int w, int h) {
            this.width = w;
            this.height = h;
            return this;
        }

    }

    public static class MatchOpt {

        private float sim = 0.8f;
        private MatOpt matOpt;
        private int matchMethod = Imgproc.TM_CCOEFF_NORMED;
        private Region region;
        private Rect expandedRegion;
        private int regionExpansion = MIN_EXPANSION;
        private static final int MIN_EXPANSION = 5;

        private MatchOpt() {
            matOpt = new MatOpt();
        }


        public Rect getExpandedRegion() {
            return expandedRegion;
        }

        private void setExpandedRegion(Rect expandedRegion) {
            this.expandedRegion = expandedRegion;
        }

        public MatchOpt region(int x, int y, int w, int h) {
            this.region = new Region(x, y, w, h);
            return this;
        }

        public MatchOpt expand(int regionExpansion){
            this.regionExpansion = Math.max(MIN_EXPANSION, regionExpansion);
            return this;
        }

        public MatchOpt matOpt(MatOpt opt){
            if(opt != null){
                this.matOpt = opt;
            }
            return this;
        }

        public MatchOpt region(Region region) {
            this.region = region;
            return this;
        }


        public MatchOpt method(MatOpt.Method m) {
            this.matOpt.method = m;
            return this;
        }

        public MatchOpt matchMethod(int matchMethod) {
            this.matchMethod = matchMethod;
            return this;
        }

        public MatchOpt sim(float sim) {
            this.sim = sim;
            return this;
        }

        public MatchOpt binArgsThreshold(int threshold) {
            this.matOpt.threshold = threshold;
            return this;
        }

        public MatchOpt binArgsType(int type) {
            this.matOpt.binType = type;
            return this;
        }

        public boolean isLandscapeRegion() {
            return region != null && region.width > region.height;
        }

        public MatchOpt binArgs(int threshold, int type, int max) {
            this.matOpt.threshold = threshold;
            this.matOpt.binType = type;
            this.matOpt.binMax = max;
            return this;
        }

        private String asKey() {
            return matOpt.asKey() + "_" + (region == null ? "" : region.toString());
        }
    }

}
