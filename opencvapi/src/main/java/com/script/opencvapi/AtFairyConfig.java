package com.script.opencvapi;

import android.annotation.SuppressLint;
import android.os.Build;

import com.script.opencvapi.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by     on 2018/3/9.
 */
@SuppressWarnings("ALL")
public class AtFairyConfig {



    /* MODIFIED BY      on 2021-03-23. */
    public static void initConfig() {
        CompatImpl.getInstance().initConfig();
    }

    public static String getOption(String key) {
        return CompatImpl.getInstance().getOption(key);
    }

    public static void clearConfig() {
        CompatImpl.getInstance().clearConfig();
    }

    public static String getUserTaskConfig() {
        return CompatImpl.getInstance().getUserTaskConfig();
    }

    public static String getServerUrl() {
        return CompatImpl.getInstance().getServerUrl();
    }

    public static String getUID() {
        return CompatImpl.getInstance().getUID();
    }

    public static String getDID() {
        return CompatImpl.getInstance().getDID();
    }

    public static String getASID() {
        return CompatImpl.getInstance().getASID();
    }

    public static String getCNID() {
        return CompatImpl.getInstance().getCNID();
    }

    public static String getASPort() {
        return CompatImpl.getInstance().getASPort();
    }

    public static String getBaseDownload() {
        return CompatImpl.getInstance().getBaseDownload();
    }

    public static String getGameID() {
        return CompatImpl.getInstance().getGameID();
    }

    public static String getGamePackage() {
        return CompatImpl.getInstance().getGamePackage();
    }

    public static String getTaskPackage() {
        return CompatImpl.getInstance().getTaskPackage();
    }

    public static String getChannelID() {
        return CompatImpl.getInstance().getChannelID();
    }

    public static String getServerInfo() {
        return CompatImpl.getInstance().getServerInfo();
    }

    public static String getTaskID() {
        return CompatImpl.getInstance().getTaskID();
    }

    private static String getConfig(String config, String key) {
        String val = "";
        Properties properties = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(config);
            properties.load(in);
            val = properties.getProperty(key);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return val;
    }


    public static void setOptionProxy(OptionProxy sOptionProxy) {
        CompatImpl.getInstance().setOptionProxy(sOptionProxy);
    }

    public static void setUseCustomOptionProxy(boolean useCustomOptionProxy){
        CompatImpl.getInstance().setUseCustomOptionProxy(useCustomOptionProxy);
    }

    public interface OptionProxy {
        String onGetOption(String key);
    }

    private static class CompatImpl {

        private boolean isUsingCustomOptionProxy = false;

        private static class F {
            @SuppressLint("StaticFieldLeak")
            private static final CompatImpl sInstance = new CompatImpl();
        }

        public static CompatImpl getInstance() {
            return F.sInstance;
        }

        private CompatImpl() {
            // do nothing
        }


        private OptionProxy mOptionProxy;

        public void setOptionProxy(OptionProxy sOptionProxy) {
            this.mOptionProxy = sOptionProxy;
        }


        private static final String OPTION_FILE_PATH1 = "/sdcard/fksd_files/uicache/task.uicfg";
        private static final String OPTION_FILE_PATH2 = "/data/as/task_config/task.uicfg";
        private static final String CONFIG_FILE = "/etc/ypconfig.ini";
        private static final String USER_CONFIG_PATH = "/etc/user_config.ini";
        private static final String KEY_URL = "api_base";
        private static final String KEY_ASID = "as_id";
        private static final String KEY_CNID = "cn_id";
        private static final String KEY_ASPORT = "as_port";
        private static final String KEY_BASE_DOWNLOAD = "base_download";
        private static final String KEY_GAMEID = "game_id";
        private static final String KEY_CHANNELID = "channel_id";
        private static final String KEY_UID = "uid";
        private static final String KEY_DID = "did";
        private static final String KEY_NAME = "name";
        private static final String KEY_TASK_NAME = "task_name";
        private static final String KEY_TASKID = "task_id";
        private static final String KEY_OPENCV_SERVICE = "opencv_service";
        private static String sCfg;
        private static String sCfgFile;

        static {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sCfgFile = OPTION_FILE_PATH2;
            } else {
                sCfgFile = OPTION_FILE_PATH1;
            }
        }


        public void initConfig() {
            sCfg = Utils.stringFile(sCfgFile);
        }

        public void setUseCustomOptionProxy(boolean useCustomOptionProxy){
            this.isUsingCustomOptionProxy = useCustomOptionProxy;
        }

        public String getOption(String key) {
            /* ADDED BY      on 2021-03-23. */
            if (isUsingCustomOptionProxy && mOptionProxy != null) {
                LtLog.d("auto","getOption 1:"+key);

                return mOptionProxy.onGetOption(key);
            }

            /* ADDED END. */
            String option = "";

            LtLog.d("auto","getOption 2:"+key);

            try {
                if (sCfg != null && sCfg.length() > 0) {
                    JSONObject jsonObject = new JSONObject(sCfg);
                    option = jsonObject.optString(key);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return option;
        }

        public void clearConfig() {
            new File(sCfgFile).delete();
            sCfg = "";
        }

        public String getUserTaskConfig() {
            return sCfg;
        }

        public String getServerUrl() {
            return getConfig(CONFIG_FILE, KEY_URL);
        }

        public String getUID() {
            return getConfig(USER_CONFIG_PATH, KEY_UID);
        }

        public String getDID() {
            return getConfig(USER_CONFIG_PATH, KEY_DID);
        }

        public String getASID() {
            return getConfig(CONFIG_FILE, KEY_ASID);
        }

        public String getCNID() {
            return getConfig(CONFIG_FILE, KEY_CNID);
        }

        public String getASPort() {
            return getConfig(CONFIG_FILE, KEY_ASPORT);
        }

        public String getBaseDownload() {
            return getConfig(CONFIG_FILE, KEY_BASE_DOWNLOAD);
        }

        public String getGameID() {
            return getConfig(USER_CONFIG_PATH, KEY_GAMEID);
        }

        public String getGamePackage() {
            return getConfig(USER_CONFIG_PATH, KEY_NAME);
        }

        public String getTaskPackage() {
            return getConfig(USER_CONFIG_PATH, KEY_TASK_NAME);
        }

        public String getChannelID() {
            return getConfig(USER_CONFIG_PATH, KEY_CHANNELID);
        }

        public String getServerInfo() {
            return getConfig(CONFIG_FILE, KEY_OPENCV_SERVICE);
        }

        public String getTaskID() {
            return getOption(KEY_TASKID);
        }

        private String getConfig(String config, String key) {
            String val = "";
            Properties properties = new Properties();
            FileInputStream in = null;
            try {
                in = new FileInputStream(config);
                properties.load(in);
                val = properties.getProperty(key);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return val;
        }

    }
    /* MODIFIED END. */
}
