package com.script.content;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LruCache;

import com.script.content.utils.UCompat;
import com.script.opencvapi.utils.TemplateInfo;
import com.script.opencvapi.utils.AtFairyUtils;
import com.script.opencvapi.utils.AtOpencvUtils;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by daiepngfei on 2020-07-22
 */
public class Assets {

    private AssetsCacher mAssetsCacher = new AssetsCacher(1);
    private AtOpencvUtils mOpencvUtils = new AtOpencvUtils();
    private ConcurrentHashMap<String, Entity> mAssetBeans = new ConcurrentHashMap<>();
    private boolean isEnableTemplateInfo = true;
    private IMatPreProcesser mMatPreProcesser = null;

    private final IMatPreProcesser fDefaultMatPreProcesser = new IMatPreProcesser() {
        @Override
        public Mat onPreProcessing(@NonNull byte[] assetsFileData, @Nullable MatOpt preset) {
            // load to type: BGR-U8C3
            final Mat buf = new MatOfByte(assetsFileData);
            Mat mat = Imgcodecs.imdecode(buf, Imgcodecs.IMREAD_UNCHANGED);
            try {
                return MatFactory.createCvtMat(mat, preset);
            } catch (Exception e) {
                // do nothing
                e.printStackTrace();
                return null;
            }
        }
    };


    Assets() {
    }

    /*private Bitmap getImageFromAssetsFile(String fileName) {
        Bitmap image = null;
        AssetManager am = mFairy.mContext.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }*/


    public void setMatPreProcesser(IMatPreProcesser mMatPreProcesser) {
        this.mMatPreProcesser = mMatPreProcesser;
    }


    /**
     * @param assetsName
     *
     * @return
     */
    public Entity entity(String assetsName) {
        return entity(assetsName, null);
    }

    /**
     * @param assetsName
     * @param preset
     *
     * @return
     */
    public Entity entity(String assetsName, MatOpt preset) {
        if (!mAssetBeans.containsKey(assetsName)) {

            // the new entity
            final Entity bean = new Entity();

            // get template info
            TemplateInfo info = null;
            if (isEnableTemplateInfo) {
                try {
                    info = mOpencvUtils.getTemplateInfo(assetsName);
                    bean.setInfo(info);
                } catch (Exception ignored) {
                    // do nothing
                    ignored.printStackTrace();
                }
            }

            /*
             * If the 'preset' is not null, use it as preset, otherwise we create
             * a new MatOpt with a non-null template-info if it is possible
             *
             */
            if (preset == null) {
                if (info != null) {
                    preset = MatOpt.newIns();
                    preset.method(UCompat.getMatOptMethodWithTempFlag(info.flag));
                    preset.binArgs(info.thresh, info.type, info.maxval);
                }
            }

            bean.setMat(getLoadedMat(assetsName, preset));

            // caching
            mAssetBeans.put(assetsName, bean);

        }
        return mAssetBeans.get(assetsName);
    }

    /**
     * @param assetsName
     *
     * @return
     */
    public Mat mat(String assetsName) {
        return mat(assetsName, null);
    }

    /**
     * @param assetsName
     * @param opt
     *
     * @return
     */
    public Mat mat(String assetsName, MatOpt opt) {
        final Entity entity = entity(assetsName, opt);
        return entity == null ? null : entity.mat;
    }

    /**
     * @param assetsName
     *
     * @return
     */
    public TemplateInfo info(String assetsName) {
        Entity entity = entity(assetsName);
        if (entity != null) {
            return entity.info;
        }
        return null;
    }

    /**
     * @param assetsName
     * @param preset
     *
     * @return
     */
    private Mat getLoadedMat(String assetsName, MatOpt preset) {
        final String optKey = assetsName + (preset == null ? "" : preset.asKey());
        Mat mat = mAssetsCacher.get(optKey);
        if (mat == null || mat.empty()) {
            mAssetsCacher.remove(optKey);
            try {
                mat = null;
                // get data from asset file
                final byte[] data = AtFairyUtils.getInstance().getTemplateData(assetsName);
                if (data != null) {
                    // create mat from data
                    mat = getCurrentPrePorcesser().onPreProcessing(data, preset);
                    // mat
                    if (mat != null && !mat.empty()) {

                        // DEBUGING CASES
                        {
                            DebugCases.case_imwrite_assets(assetsName, mat);
                        }

                        mAssetsCacher.put(optKey, mat);
                    }
                }
            } catch (Exception ignore) {
                // do nothing
            }
        }
        return mat;
    }

    @NonNull
    private IMatPreProcesser getCurrentPrePorcesser() {
        return mMatPreProcesser == null ? fDefaultMatPreProcesser : mMatPreProcesser;
    }


    void setAssetsEnableTemplateInfo(boolean enableTemplateInfo) {
        this.isEnableTemplateInfo = enableTemplateInfo;
    }

    public interface IMatPreProcesser {
        Mat onPreProcessing(@NonNull byte[] assetsFileData, @Nullable MatOpt preset);
    }


    /**
     * Created by daiepngfei on 2020-07-22
     */
    public static class Entity {
        private TemplateInfo info;
        private Mat mat;

        public boolean isValid() {
            return mat != null && !mat.empty() && info != null;
        }

        public TemplateInfo getInfo() {
            return info;
        }

        public void setInfo(TemplateInfo info) {
            this.info = info;
        }

        public Mat getMat() {
            return mat;
        }

        public void setMat(Mat mat) {
            this.mat = mat;
        }
    }

    /**
     * Created by daiepngfei on 2020-07-22
     */
    private static class AssetsCacher extends LruCache<String, Mat> {

        private int mMaxMemory;

        /**
         * @param maxSize
         *         for caches that do not override {@link #sizeOf}, this is the maximum number of
         *         entries in the cache. For all other caches, this is the maximum sum of the sizes
         *         of the entries in this cache.
         */
        private AssetsCacher(int maxSize) {
            super(maxSize);
            resizeWithCache();
        }

        @Override
        protected int sizeOf(String key, Mat value) {
            return value.rows() * value.cols() / 1024;
        }

        private void resizeWithCache() {
            final int maxMemory = (int) (Runtime.getRuntime().totalMemory() / 1024);
            if (Math.abs(maxMemory - mMaxMemory) > 10240) {
                mMaxMemory = maxMemory;
                final int cacheSize = maxMemory / 8;
                resize(cacheSize);
                System.out.println("AssetsCacher#: maxMemory -> " + maxMemory + ", cacheSize -> " + cacheSize);
            }
        }
    }
}
