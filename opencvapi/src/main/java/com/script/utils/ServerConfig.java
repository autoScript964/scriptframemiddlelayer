package com.script.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by     on 2016/8/22.
 */
public class ServerConfig {
    private static final String CONFIG_NAME     = "/etc/jcn/cn_config.ini" ;
    private static  String DEVICELIST_PATH = "/etc/jcn/" ;
    //private static final String DEVICELIST_PATH = "E:\\work\\设备列表\\" ;
    private static final String BLACK_LIST      = "black_list" ;
    private static final String KEY_BASE_PATH   = "base_path" ;//apk路径
    private static final String KEY_ASMPORT     = "asm_port" ;
    private static final String KEY_USMPORT     = "usm_port" ;
    private static final String KEY_RPCPORT     = "rpc_port" ;
    private static final String KEY_LOGSERVER   = "log_server" ;
    private static final String KEY_LOGPORT      = "log_port" ;
    private static final String KEY_FILEPORT    = "file_port" ;
    private static final String KEY_API_BASE    = "api_base" ;
    private static final String KEY_ID          = "id" ;
    private static final String KEY_IP          = "ip" ;
    private static final String KEY_LOCAL_IP    = "local_ip" ;
    private static final String KEY_RPC_SERVER  = "rpc_server" ;
    private static final String KEY_UPLOAD_URL  = "upload_url" ;
    private static final String KEY_WEBSOCKETPORT= "websocket_port" ;
    private static final String KEY_PER_CONNECTION_SIZE = "per_connection_size" ;
    private static final String KEY_MFS_BASE     = "mfs_base" ;
    private static final String KEY_NFS_BASE     = "nfs_base" ;
    private static final String KEY_DB_SERVER    = "db_server" ;
    private static final String KEY_DNPORT       = "dn_port" ;
    private static final String KEY_MGR_PORT     = "mgr_port" ;
    private static final String KEY_SOCATD_PORT     = "socatd_port" ;
    private static final String KEY_IME_ID          = "ime" ;
    private static final String KEY_SCRIPT_USR_PORT = "script_usr_port" ;
    private static final String KEY_SCRIPT_AS_PORT = "script_as_port" ;
    private static final String KEY_SCRIPT_BASE_PATH = "script_base_path" ;
    private static final String KEY_CM_BASEPATH = "cm_base_path" ;
    private static final String KEY_DEVICE_SERVICE_PORT = "device_service_port" ;

    private static String base_path  = " " ;
    private static String cm_base_path  = " " ;
    private static int asm_port ;
    private static int usm_port ;
    private static String api_base ;
    private static int mgr_port = 0  ;
    private static int socatd_port ;
    private static int file_port ;
    private static int websocket_port ;
    private static int rpc_port;
    private static String log_server = "127.0.0.1";
    private static int log_port = 514;
    private static int dn_port = 9090 ;
    private static String id ;
    private static String ip ;
    private static String ime_id ;
    private static int script_usr_port ;
    private static int script_as_port ;
    private static String script_base_path ;
    private static int device_service_port ;

    public static String getLocal_ip() {
        return local_ip;
    }

    private static String local_ip ;
    private static String rpc_server ;

    public static String getDb_server() {
        return db_server;
    }

    private static String db_server  = "127.0.0.1";

    private static String upload_url ;
    private static int per_connection_size ;
    private static String mfs_base = "/home/mfs/mnt/" ;
    private static String nfs_base = "/home/nfs/mnt/" ;
    private static List<String> black_list ;
    public static int getAsm_port(){
        return asm_port ;
    }
    public static int getMgr_port(){
        return mgr_port ;
    }
    public static int getUsm_port(){
        return usm_port ;
    }
    public static int getFile_port(){
        return file_port ;
    }
    public static int getWebsocket_port(){
        return websocket_port ;
    }
    public static int getRpc_port(){
        return rpc_port ;
    }
    public static String getId(){
        return id ;
    }
    public static String getIp(){
        return ip ;
    }
    public static String getRpc_server(){
        return rpc_server ;
    }

    public static int getDn_port() {
        return dn_port;
    }

    public static String getApi_base(){
        return  api_base ;
    }

    static{
        Properties properties = new Properties();
        try {
            if(System.getProperty("os.name").toLowerCase().startsWith("win")){
                DEVICELIST_PATH = "E:\\work\\设备列表\\" ;
                properties.load(new FileInputStream("cn_config.ini"));
            }else{
                String configFile = System.getProperty("config_name",CONFIG_NAME) ;
                properties.load(new FileInputStream(configFile));
            }

            try {
                websocket_port = Integer.parseInt(properties.getProperty(KEY_WEBSOCKETPORT));
            }catch (NumberFormatException e){
            }
            rpc_server = properties.getProperty(KEY_RPC_SERVER) ;
            base_path = properties.getProperty(KEY_BASE_PATH) ;
            cm_base_path = properties.getProperty(KEY_CM_BASEPATH, cm_base_path) ;
            try {
                asm_port = Integer.parseInt(properties.getProperty(KEY_ASMPORT));
            }catch (Exception e){}
            try {
                usm_port = Integer.parseInt(properties.getProperty(KEY_USMPORT)) ;
            }catch (Exception e){}
            try {
                rpc_port = Integer.parseInt(properties.getProperty(KEY_RPCPORT));
            }catch (Exception e){}
            try {
                file_port = Integer.parseInt(properties.getProperty(KEY_FILEPORT));
            }catch (Exception e){}
            id = properties.getProperty(KEY_ID) ;
            ip = properties.getProperty(KEY_IP) ;
            api_base = properties.getProperty(KEY_API_BASE) ;
            local_ip = properties.getProperty(KEY_LOCAL_IP) ;
            db_server = properties.getProperty(KEY_DB_SERVER) ;
            log_server = properties.getProperty(KEY_LOGSERVER, log_server) ;
            ime_id = properties.getProperty(KEY_IME_ID, null) ;
            try {
                log_port = Integer.parseInt(properties.getProperty(KEY_LOGPORT,log_port+"")) ;
            }catch (NumberFormatException e){
            }

            try{
                mgr_port = Integer.parseInt(properties.getProperty(KEY_MGR_PORT,mgr_port+"")) ;
            }catch (Exception e){}

            try{
                socatd_port = Integer.parseInt(properties.getProperty(KEY_SOCATD_PORT,-1+"")) ;
            }catch (Exception e){}

            try {
                dn_port = Integer.parseInt(properties.getProperty(KEY_DNPORT,dn_port+"")) ;
            }catch (NumberFormatException e){
            }
            try {
                per_connection_size = Integer.parseInt(properties.getProperty(KEY_PER_CONNECTION_SIZE));
            }catch (NumberFormatException e){
            }
            try {
                device_service_port = Integer.parseInt(properties.getProperty(KEY_DEVICE_SERVICE_PORT));
            }catch (NumberFormatException e){
            }
            upload_url = properties.getProperty(KEY_UPLOAD_URL) ;
            mfs_base = properties.getProperty(KEY_MFS_BASE) ;
            nfs_base = properties.getProperty(KEY_NFS_BASE) ;
            try{
                script_usr_port = Integer.parseInt(properties.getProperty(KEY_SCRIPT_USR_PORT)) ;
            }catch (Exception e){}
            try{
                script_as_port = Integer.parseInt(properties.getProperty(KEY_SCRIPT_AS_PORT)) ;
            }catch (Exception e){}
            script_base_path = properties.getProperty(KEY_SCRIPT_BASE_PATH) ;


            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(BLACK_LIST)));
                String line;
                black_list = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    black_list.add(line);
                }
            }catch (Exception e){}finally {
                if(reader != null) {
                    reader.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static String getLog_server(){
        return log_server ;
    }
    public static int getLog_port(){
        return log_port ;
    }
    public static String getUpload_url(){
        return upload_url ;
    }
    public static int getPer_connection_size(int defaultSize){
        if(per_connection_size != 0){
            return per_connection_size ;
        }
        return defaultSize ;
    }
    public static String getBasePath(){
        return base_path ;
    }


    public static String getTmpFile(String fileName){
       return System.getProperty("user.dir") + File.separator + fileName ;
    }
    public static String getImeId(){
        return ime_id ;
    }

    public static String getMfs_base() {
        return mfs_base;
    }
    public static String getNfs_base() {
        return nfs_base;
    }

    public static int getScript_usr_port(int default_val){
        return script_usr_port >0?script_usr_port:default_val;
    }
    public static int getScript_as_port(int default_val){
        return  script_as_port>0?script_as_port:default_val ;
    }
    public static String getScript_base_path(){
        return script_base_path ;
    }

    public static String getCm_base_path(){
        return cm_base_path ;
    }
    public static int getSocatd_port(){return  socatd_port ;} ;

    public static String getdevicelistPath(){
        return DEVICELIST_PATH ;
    }
    public static int getDevice_service_port(){
        return device_service_port ;
    }
}
