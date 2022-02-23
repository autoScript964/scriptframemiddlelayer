package com.script.utils;

import com.script.opencvapi.LtLog;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by     on 15-3-4.
 */
public class HttpDownLoad {
    public static final String TAG = HttpDownLoad.class.getSimpleName() ;
    private String mUrl ;
    private DownLoadEventCallback mCallback ;
    private String mPath ;
    private String mName ;

    private void mkdir(File f){
        LtLog.i(TAG,"mkdir "+f.getAbsolutePath()) ;
        if(!f.exists()){
            while(!f.getParentFile().exists()){
                mkdir(f.getParentFile());
            }
            f.mkdir() ;
        }
    }
    public HttpDownLoad(String url, String pathFile, String name){
        LtLog.i(TAG,"download url:"+url);
        mUrl = url ;
        mPath = pathFile ;
        mName = name ;

        File file = new File(mPath) ;
        if(!file.exists()){
            mkdir(file);
        }
    }
    private void _start(){
        URL url = null;
        OutputStream out = null;
        long lastSpeed ;
        try {

            url = new URL(mUrl) ;
            String fileName ;
            if(mName != null){
                fileName = mName ;
            }else{
                fileName = mUrl.substring(mUrl.lastIndexOf("/")+1);
            }


            String downloadName = fileName+".download";
            File downloadingFile = new File(mPath+"/"+downloadName) ;
            File downloadFile = new File(mPath+"/"+fileName) ;

            //存在的文件不重复下载
            if(downloadFile.exists()){
                if(mCallback != null){
                    mCallback.finished(true, downloadFile.getAbsolutePath());
                }
                return ;
            }

            URLConnection conn = url.openConnection() ;
            if(downloadingFile.exists()){
                long downloadedLength = downloadingFile.length();
                String start = "bytes="+downloadedLength + "-";
                conn.addRequestProperty("Range",start);
            }
            conn.setReadTimeout(20*1000);
            InputStream in = conn.getInputStream() ;
            int contentLen = conn.getContentLength() ;
            out = new FileOutputStream(downloadingFile,true) ;
            byte b[] = new byte[1024*100] ;
            int readLen = 0 ;
            int recvedLen = 0 ;
            int speed = 0 ;
            long readEnd ;
            lastSpeed = System.currentTimeMillis() ;
            int speedSize = 0 ;
            while((readLen = in.read(b)) > 0){
                readEnd = System.currentTimeMillis() ;
                recvedLen +=readLen ;
                out.write(b,0,readLen);
                speedSize += readLen ;
                if(readEnd - lastSpeed >= 1000) {
                    long useTime = readEnd - lastSpeed;
                    if (useTime != 0) {
                        speed = (int) (speedSize * 1.0 / useTime);
                        speed = (int) (speed * 1.0 / 1000 * 1024);
                        mCallback.progress(contentLen, recvedLen, speed, downloadFile.getAbsolutePath());
                    }
                    lastSpeed = readEnd ;
                    speedSize = 0 ;
                }
            }
            out.flush();
            in.close();
            out.close();
            boolean downloadOver = false ;
            if(contentLen == recvedLen){
                downloadOver = true ;
                downloadingFile.renameTo(downloadFile) ;
            }

            LtLog.i(TAG,"download success"+mUrl);
            if(mCallback != null) {
                mCallback.finished(downloadOver, downloadFile.getAbsolutePath());
            }

        } catch (MalformedURLException e) {
            LtLog.w(TAG,"download MalformedURLException"+e.toString());
            if(mCallback != null){
                mCallback.finished(false,null);
            }
            e.printStackTrace();
        } catch (IOException e) {
            LtLog.w(TAG,"download ioexception"+e.toString()+" file:"+mUrl);
            if(out != null){
                try {
                    out.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if(mCallback != null){
                mCallback.finished(false,null);
            }
            e.printStackTrace();
        }
    }
    public void start(){
        _start();
    }

    public void start(final DownLoadEventCallback callback){
        mCallback = callback ;
        new Thread(new Runnable() {
            @Override
            public void run() {
                _start();
            }
        }).start();

    }

    public void pause(){

    }
    public void resume(){

    }
    public void stop(){

    }

    public interface DownLoadEventCallback{
        public void finished(boolean over, String filePath) ;
        public void progress(int size, int progress, int speed, String filePath) ;
    }
}
