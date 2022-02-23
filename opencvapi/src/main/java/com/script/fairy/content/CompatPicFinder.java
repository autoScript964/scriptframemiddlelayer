package com.script.fairy.content;

import android.util.Log;

import com.script.content.DTimer;
import com.script.content.LocalInterruptThread;
import com.script.content.MatOpt;
import com.script.content.MatchEngine;
import com.script.content.ScProxy;
import com.script.content.utils.UCompat;
import com.script.content.utils.UStr;
import com.script.opencvapi.FindResult;
import com.script.opencvapi.utils.TemplateInfo;

import org.opencv.core.Mat;


public class CompatPicFinder {

    public static FindResult findPicWithTempalteInfo(String... assets) throws InterruptedException {
        return findPicWithTempalteInfo(DTimer.ThreadLocalManager.currentTimer(), assets);
    }

    public static FindResult findPicWithTempalteInfo(DTimer dTimer, String... assets) throws InterruptedException {
        final FindResult findResult = new FindResult();
        if (assets != null) {
            for (String asset : assets) {
                TemplateInfo info = ScProxy.assets().info(asset);
                MatchEngine.MatchResult result = doFindingPicWithTemplateInfo(dTimer,
                        MatchEngine.Region.create(info.x, info.y, info.width, info.height), 0, asset);
                if (findResult.sim < result.getSim()) {
                    findResult.sim = result.getSim();
                    findResult.x = result.getX();
                    findResult.y = result.getY();
                    findResult.width = result.getW();
                    findResult.height = result.getH();
                }
            }
        }
        return findResult;
    }


    public static FindResult findPicWithTempalteInfoExpanded(DTimer dTimer, int expansion, String... assets) throws InterruptedException {
        return findPicWithTempalteInfoInRegionExpanded(dTimer, null, expansion, assets);
    }

    public static FindResult findPicWithTempalteInfoInRegion(DTimer dTimer, MatchEngine.Region region, String... assets) throws InterruptedException {
        return findPicWithTempalteInfoInRegionExpanded(dTimer, region, 0, assets);
    }

    public static FindResult findPicWithTempalteInfoInRegionExpanded(DTimer dTimer, MatchEngine.Region region, int expansion, String... assets) throws InterruptedException {
        final FindResult findResult = new FindResult();
        if (assets != null) {
            for (String asset : assets) {
                MatchEngine.MatchResult result = doFindingPicWithTemplateInfo(dTimer, region, expansion, asset);
                if (findResult.sim < result.getSim()) {
                    findResult.sim = result.getSim();
                    findResult.x = result.getX();
                    findResult.y = result.getY();
                    findResult.width = result.getW();
                    findResult.height = result.getH();
                }
            }
        }

        return findResult;
    }

    private static MatchEngine.MatchResult doFindingPicWithTemplateInfo(DTimer dTimer, MatchEngine.Region customRegion, int expansion, String asset) throws InterruptedException {
        final TemplateInfo info = ScProxy.assets().info(asset);
        final MatOpt.Method method = UCompat.getMatOptMethodWithTempFlag(info.flag);
        if(customRegion == null){
            customRegion = MatchEngine.Region.create(info.x, info.y, info.width, info.height);
        }
        MatchEngine.MatchOpt matchOpt = MatchEngine.Opt()
                .region(customRegion)
                .expand(expansion)
                .method(method)
                .binArgs(info.thresh, info.type, info.maxval);
        return doMatching(dTimer, asset, ScProxy.assets().mat(asset), matchOpt);
    }

    @SuppressWarnings("WeakerAccess")
    public static MatchEngine.MatchResult doMatching(DTimer dTimer, String dTimerMark, Mat tarMat, MatchEngine.MatchOpt matchOpt) throws InterruptedException {
        if(LocalInterruptThread.currentLocalInterrupted()){
            throw new InterruptedException();
        }
        dTimerMark = dTimerMark == null ? "" : dTimerMark;
        if (dTimer != null) {
            dTimer.markBegin(dTimerMark);
        }
        MatchEngine.MatchResult result = ScProxy.matchEngine().doMatching(
                ScProxy.captureEngine().getCurrentScreenMat(),
                tarMat,
                matchOpt,
                dTimerMark);
        if (dTimer != null) {
            dTimer.markEnd();
        }

        {  // todo: 实验代码
            final int LIM = ScProxy.config().Level().curMatching();

            final String tag = "MR[LIM:" + LIM + "]";
            final ScProxy.ConfigManager.Printer printer = ScProxy.config().Printer();
            final String msg = UStr.cfls(printer.getAssetPat(), dTimerMark, printer.getMaxAssetSpace())
                    + " >> " + result.toString() + (dTimer == null ? "" : "; time: " + dTimer.tailComsumed());
            if (result.ok()) {
                Log.e(tag, msg);
            } else {
                Log.d(tag, msg);
            }

            if (LIM > 0 && !result.ok()) {
                try {
                    Thread.sleep(LIM);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}
