package com.script.utils;


import com.script.AtModule2;
import com.script.network.ClientNio;
import com.script.opencvapi.LtLog;

import okhttp3.*;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.concurrent.TimeoutException;

/**
 * Created by     on 2015/6/16.
 */
public class Utils {
    public static int bytesToInt(byte[] b, int offset) {
        int value= 0;
        for (int i = 0; i < 4; i++) {
            int shift= (4 - 1 - i) * 8;
            value +=(b[i + offset] & 0x000000FF) << shift;//往高位游
        }
        return value;
    }
    public static long bytesToLong(byte[] b, int offset){
        ByteBuffer byteBuffer = ByteBuffer.allocate(8) ;
        byteBuffer.put(b, offset, 8) ;
        byteBuffer.flip() ;
        return byteBuffer.getLong() ;
    }
    public static float bytesToFloat(byte[]b, int offset){
        ByteBuffer byteBuffer = ByteBuffer.allocate(4) ;
        byteBuffer.put(b, offset, 4) ;
        byteBuffer.flip() ;
        return byteBuffer.getFloat() ;
    }
    public static double bytesToDouble(byte[]b , int offset){
        ByteBuffer byteBuffer = ByteBuffer.allocate(8) ;
        byteBuffer.put(b, offset, 8) ;
        byteBuffer.flip() ;
        return byteBuffer.getDouble() ;
    }
    public static short bytesToShort(byte[] bytes,int start){
        if( start+2 > bytes.length){
            return -1 ;
        }
        short s = 0 ;
        s |= bytes[start] << 8 ;
        s |= bytes[start+1] ;
        return s ;
    }
    public static String bytesToString(byte[] bytes,int start,int count){
        try {
            return new String(bytes,start,count,"UTF-8") ;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null ;
    }
    public static void mkdirs(String dir){
        File f = new File(dir) ;
        if(!f.exists()){
            mkdirs(f.getParent());
            f.mkdir() ;
        }
    }
    public static void deleteFile(File oldPath) {
        if (oldPath.isDirectory()) {
            File[] files = oldPath.listFiles();
            for (File file : files) {
                deleteFile(file);
            }

        }
        oldPath.delete();

    }
    public static void sendTo(String ip, int port, byte[] msg){
        try {
            Socket socket = new Socket(ip, port) ;
            OutputStream out = socket.getOutputStream() ;
            out.write(msg);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
    public static String fileMd5(String file){
        return fileMd5(file,0, (int) new File(file).length()) ;
    }
    public static String fileMd5(InputStream inputStream){
        try {
            MessageDigest MD5 = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024 * 8] ;
            //inputStream.skip(offset) ;
            int readLen  ;
            while( (readLen = inputStream.read(buffer)) > 0){
                MD5.update(buffer, 0 , readLen);
            }
            return bytesToHexString(MD5.digest());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
    public static String fileMd5(String filePath, int offset, int count) {
        File file = new File(filePath) ;
        if (!file.isFile()) {
            return null;
        }
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            MessageDigest MD5 = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024 * 8] ;
            inputStream.skip(offset) ;
            int readLen  ;
            int updatedSize = 0 ;
            while( (readLen = inputStream.read(buffer)) > 0){
                if(updatedSize + readLen >= count){
                    MD5.update(buffer, 0 , count -  updatedSize);
                    break;
                }else{
                    MD5.update(buffer, 0 , readLen);
                }
                updatedSize += readLen ;
            }
            return bytesToHexString(MD5.digest());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static String readFrom(String ip, int port){
        StringBuffer sb = new StringBuffer() ;
        try {
            Socket socket = new Socket(ip, port) ;
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream())) ;
            String line ;
            while( (line = reader.readLine()) != null){
                if(sb.length() > 0){
                    sb.append("\n") ;
                }
                sb.append(line) ;
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString() ;
    }
    public static boolean saveFile(String file, byte[] content){
        if(content == null || content.length == 0){
            return  false ;
        }
        File f = new File(file) ;
        if(!f.getParentFile().exists()){
            f.getParentFile().mkdirs() ;
        }
        try {
            FileOutputStream fos = new FileOutputStream(f) ;
            fos.write(content);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false ;
        } catch (IOException e) {
            e.printStackTrace();
            return false ;
        }
        return true ;
    }

    public static byte[] fileContent(String file){
        byte[] b = null ;
        FileInputStream fileInputStream = null ; 
        try {
            fileInputStream = new FileInputStream(file) ;
            long size = fileInputStream.available() ;
            b = new byte[(int) size] ;
            fileInputStream.read(b) ;
            
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
           if(fileInputStream != null){
               try {
                   fileInputStream.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           } 
        }
        return  b ;
    }
    public static String stringFile(String file){
        StringBuffer sb = new StringBuffer() ;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file))) ;
            String line ;
            while( (line = reader.readLine())!= null){
                if(sb.length() > 0){
                    sb.append("\n") ;
                }
                sb.append(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
           if(reader != null){
               try {
                   reader.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
        }
        return  sb.toString();
    }
    public static int runCmdWithResult(String cmd){
        int result = -1 ;
        try {
            Process process = Runtime.getRuntime().exec(cmd) ;
            String line ;
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while( (line = reader.readLine()) != null){
               System.out.println(line) ;
            }
            result = process.waitFor() ;
            process.destroy();
            System.out.println("run cmd wisth result cmd:"+cmd+" result:"+ result) ;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result ;
    }
   public static String runCmd(String cmd){
       LtLog.d("run cmd:"+ cmd) ;
       StringBuffer sb  = new StringBuffer( ) ;
       try {
           Process process = Runtime.getRuntime().exec(cmd) ;
           BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())) ;
           String line ;
           while( (line = reader.readLine()) != null){
               if(sb.length() > 0){
                   sb.append("\n") ;
               }
               sb.append(line) ;
           }
       } catch (IOException e) {
           e.printStackTrace();
       }
       LtLog.d("runCmd result:"+ sb.toString()) ;
       return sb.toString() ;

   }
    public static ByteBuffer expend(ByteBuffer byteBuffer){
        int currentSize = byteBuffer.capacity() ;
        int currentPosition = byteBuffer.position() ;
        ByteBuffer newBuffer = ByteBuffer.allocate(currentSize*2) ;
        byteBuffer.flip() ;
        newBuffer.put(byteBuffer) ;
        newBuffer.position(currentPosition) ;
        return  newBuffer ;
    }
    public static ByteBuffer sendModule(String ip , int port, int timeout, AtModule2 module) throws TimeoutException {
        ClientNio clientNio = new ClientNio(timeout) ;
        try{
            if (!clientNio.connect(ip, port)) {
                throw new TimeoutException("连接As超时");
            }
            clientNio.send(module.toDataWithLen());
            ByteBuffer byteBuffer = clientNio.readPackage() ;
            return byteBuffer ;
        }finally {
            clientNio.disconnect();
        }
    }

    public static void sleep(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static String httpGet(String url){
        Request request = new Request.Builder().url(url).build();
        OkHttpClient okHttpClient = new OkHttpClient() ;
        String result = null ;
        try {
            Response response = okHttpClient.newCall(request).execute() ;
            result = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  result ;
    }
//    public static void httpDownload(String url, final String downloadfile){
//        Request request = new Request.Builder().url(url).build();
//        OkHttpClient okHttpClient = new OkHttpClient() ;
//        okHttpClient.newCall(request).enqueue(new Callback()
//        {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                // 下载失败 listener.onDownloadFailed();
//            }
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                InputStream is = null;
//                byte[] buf = new byte[2048];
//                int len = 0;
//                FileOutputStream fos = null;
//                // 储存下载文件的目录
//                try {
//                    is = response.body().byteStream();
//                    long total = response.body().contentLength();
//                    File file = new File(downloadfile);
//                    fos = new FileOutputStream(file);
//                    long sum = 0;
//                    while ((len = is.read(buf)) != -1) {
//                        fos.write(buf, 0, len);
//                        sum += len;
//                        int progress = (int) (sum * 1.0f / total * 100);
//                        // 下载中
//                        listener.onDownloading(progress);
//                    } fos.flush();
//                    // 下载完成
//                    listener.onDownloadSuccess();
//                } catch (Exception e) {
//                    listener.onDownloadFailed();
//                } finally {
//                    try {
//                        if (is != null)
//                            is.close();
//                    } catch (IOException e) { }
//                    try {
//                        if (fos != null)
//                            fos.close();
//                    } catch (IOException e) { }
//                }
//            }
//        });
//
//    }
}
