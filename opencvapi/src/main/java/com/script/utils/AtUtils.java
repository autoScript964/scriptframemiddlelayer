package com.script.utils;


import com.script.opencvapi.LtLog;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * Created by     on 2016/8/24.
 */
public class AtUtils {
    private static final int AS_FIRST_PORT = 12400 ;
    public static AppInfo getAppInfo(String apkFile){
        String aaptCmd = "aapt dump badging "+ apkFile ;
        String cmdResult = Utils.runCmd(aaptCmd) ;
        if(cmdResult == null || cmdResult.length() == 0){
            return null ;
        }
        AppInfo info = new AppInfo() ;
        info.packagePath = apkFile ;
        BufferedReader reader = new BufferedReader(new StringReader(cmdResult)) ;
        try {
            String line ;
            while( (line = reader.readLine()) != null){
                if(line.startsWith("package")){
                    String nameStartFlag= "name='" ;
                    String versionStartFlag = "versionCode='" ;
                    int nameStart = line.indexOf(nameStartFlag) + nameStartFlag.length() ;
                    int versionStart = line.indexOf(versionStartFlag) + versionStartFlag.length() ;
                    info.packageName = line.substring( nameStart, line.indexOf('\'',nameStart)   ) ;
                    try {
                        info.version = Integer.parseInt(line.substring(  versionStart, line.indexOf('\'', versionStart) )) ;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else if(line.startsWith("launchable-activity")){
                    String nameStartFlag= "name='" ;
                    int nameStart = line.indexOf(nameStartFlag) + nameStartFlag.length() ;
                    info.mainActivity = line.substring( nameStart, line.indexOf('\'',nameStart)   ) ;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return info ;
    }

    public static void asInit(String asIp, String asId){
        String cmd = "asinit "+ asId +" "+ asIp ;
        Utils.runCmd(cmd) ;
    }
    public static boolean userDeviceInit(String asIp, String userDir){
        StringBuffer cmd = new StringBuffer("userinit.py") ;
        cmd.append(" --user_dir=").append(userDir) ;
        cmd.append(" --as_ip=").append(asIp) ;
        cmd.append(" --user_cmd=mount") ;
        return Utils.runCmdWithResult(cmd.toString()) != 2 ;
    }
    public static boolean userDataInit(String userDir){
        StringBuffer cmd = new StringBuffer("userinit.py") ;
        cmd.append(" --user_dir=").append(userDir) ;
        cmd.append(" --user_cmd=init") ;
        return Utils.runCmdWithResult(cmd.toString()) != 2 ;
    }
    public static void userDeviceUninit(String asIp, String userDir){
        StringBuffer cmd = new StringBuffer("userinit.py") ;
        cmd.append(" --user_dir=").append(userDir) ;
        cmd.append(" --as_ip=").append(asIp) ;
        cmd.append(" --user_cmd=umount") ;
        Utils.runCmd(cmd.toString()) ;
    }
    public static boolean deviceCheck(String asIp){
        StringBuffer cmd = new StringBuffer("userinit.py") ;
        cmd.append(" --as_ip=").append(asIp) ;
        cmd.append(" --user_cmd=check") ;
        return Utils.runCmdWithResult(cmd.toString()) != 2;
    }
    public static String localFileMd5(String file){
        String cmd = "md5sum "+ file ;
        String md5result = LocalCmdRunner.runCmdSync(cmd) ;
        if(md5result != null && md5result.trim().length() > 0){
            return md5result.substring(0, md5result.indexOf(' ')) ;
        }
        return null ;
    }
    public static String SHA1(String decript) {
        if(decript == null){
            return "" ;
        }
        try {
            MessageDigest digest = MessageDigest
                    .getInstance("SHA-1");
            digest.update(decript.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void writeUserId(String userId, String deviceId, String verify){
        File idFile = new File(getUsrInfoPath(userId, deviceId)) ;

        LtLog.i("write userid:"+ idFile) ;
        Properties properties = new Properties() ;

        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(idFile) ;
            properties.setProperty("userId",userId) ;
            properties.setProperty("deviceId",deviceId) ;
            properties.setProperty("verify",verify) ;
            properties.store(outputStream,null);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(outputStream != null){
                try {
                    outputStream.close() ;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static String portToAsip(int port){
        int third = (port - AS_FIRST_PORT) / 255 ;
        int fourth = (port - AS_FIRST_PORT)%255 ;
        return "192.168." + third+"."+fourth ;
    }
    public static int ipToPort(String ip){
        String[] ipArray = ip.split("\\.") ;
        if(ipArray != null && ipArray.length == 4){
            int third = Integer.parseInt(ipArray[2]) ;
            int fouth = Integer.parseInt(ipArray[3]) ;
            return  AS_FIRST_PORT + third*255 + fouth ;
        }
        return 0;
    }
    public static String getGamePath(String gameId, String channelId){
        StringBuffer path = new StringBuffer(ServerConfig.getCm_base_path()) ;
        path.append("/").append(gameId) ;
        path.append("/").append(channelId) ;
        path.append(".apk") ;
        return  path.toString() ;


    }

    public static String getShaDir(String hashcode){
        if(hashcode == null || hashcode.length() < 5 ){
            return null ;
        }
        StringBuffer dir = new StringBuffer() ;
        for(int i = 0 ; i < 6 ;i+=2){
           dir.append(hashcode.substring(i,i+2)).append(File.separator) ;
        }
        return dir.toString() ;
    }
    public static String getUsrDeviceDir(String userId, String deviceId){
        String userIdSha1 = SHA1(userId) ;
        StringBuffer userdir  = new StringBuffer();
        for (int i = 0 ; i < 4 ; i+=2){
           userdir.append(userIdSha1.substring(i,i+2)).append(File.separator) ;
        }
        userdir.append(userId).append(File.separator) ;
        userdir.append(deviceId) ;
        return userdir.toString() ;
    }
    public static String getUsrPackagesPath(String userId, String deviceId){
        String usrmfsDir = getUsrMfsDir(userId, deviceId) ;
        return usrmfsDir + "/sdcard/.user/packages.xml" ;
    }
    public static String getUsrInfoPath(String userId, String deviceId){
        String usrmfsDir = getUsrMfsDir(userId, deviceId) ;
        return usrmfsDir + "/sdcard/.user/user_config.info" ;
    }
    public static String getUsrDataDir(String userId, String deviceId){
        String usrmfsDir = getUsrMfsDir(userId, deviceId) ;
        return usrmfsDir + "/sdcard/.user/data/" ;
    }
    public static String getUsrSdDir(String userId, String deviceId){
        String usrmfsDir = getUsrMfsDir(userId, deviceId) ;
        return usrmfsDir + "/sdcard/Android/data/" ;
    }

    public static String getUsrMfsDir(String userId, String deviceId){
        return ServerConfig.getMfs_base() + File.separator +getUsrDeviceDir(userId, deviceId);
    }
    public static boolean fileExist(String hashcode){
        String filePath = getUploadFilePath(hashcode) ;
        return new File(filePath).exists() ;
    }
    public static String getUploadFilePath(String hashCode){
        StringBuffer sb = new StringBuffer(ServerConfig.getBasePath()) ;
        sb.append(File.separator).append(getShaDir(hashCode)).append(hashCode).append(".apk");
        return sb.toString() ;
    }
    public static String getUploadTmpFile(String hashCode){
       return getUploadFilePath(hashCode) +".tmp" ;
    }
    public static String getUploadPath(String userId, String deviceId){
        StringBuffer sb = new StringBuffer(ServerConfig.getMfs_base()) ;
        sb.append(File.separator).append(AtUtils.getUsrDeviceDir(userId,deviceId)) ;
        sb.append("/sdcard/upload/") ;
        return sb.toString() ;
    }
    public static String getAndroidPath(String mfsPath, String userId, String deviceId){
        String mfsPrefix = ServerConfig.getMfs_base() + File.separator + AtUtils.getUsrDeviceDir(userId, deviceId);

        mfsPrefix = new File(mfsPrefix).getAbsolutePath() ;
        mfsPath = new File(mfsPath).getAbsolutePath() ;
        LtLog.i("getAndroidPath: mfsprefix:"+ mfsPrefix+" mfsPath:" + mfsPath) ;
        return mfsPath.substring(mfsPrefix.length()) ;
    }

    public static String recvFile(String ip, int port){
        StringBuffer sb = new StringBuffer() ;
        try {

            Socket socket = new Socket(ip, port) ;
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream())) ;
            String line ;
            while( (line = reader.readLine()) != null){
                if(sb.length() != 0){
                    sb.append("\n") ;
                }
                sb.append(line) ;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString() ;
    }
    public static void recvFile(String ip, int port, String file){
        try {
            Socket socket = new Socket(ip, port) ;

            OutputStream out = new FileOutputStream(file) ;
            InputStream in = socket.getInputStream() ;
            byte b[] = new byte[1024*1024] ;
            while(true){
                try {
                    int len = in.read(b) ;
                    if(len <=0)
                        break ;
                    out.write(b, 0, len);
                }catch (Exception e){
                   break;
                }
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static boolean sendFile(String file, String ip, int port){
        InputStream in = null;
        Socket socket  = null;
        int fileSize = 0 ;
        int sendSize = 0 ;
        try {
            File f = new File(file) ;
            fileSize = (int) f.length();
            if(fileSize == 0 ){
                return false ;
            }
            in = new FileInputStream(f) ;
            socket = new Socket(ip, port) ;
            OutputStream out = socket.getOutputStream() ;
            byte b[] = new byte[1024*1024] ;
            while(true){
                try {
                    int len = in.read(b) ;
                    if(len <=0)
                        break ;
                    sendSize += len ;
                    out.write(b, 0, len);
                }catch (Exception e){
                   break;
                }
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
           if(in != null){
               try {
                   in.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return  fileSize == sendSize ;
    }

    public static byte[] testSend(String ip, int port, byte[] msg, int timeout){
        Socket socket = new Socket() ;
        SocketAddress address = new InetSocketAddress(ip, port) ;
        try {
            socket.connect(address, timeout);
            socket.getOutputStream().write(msg);
            BufferedReader reader = new BufferedReader( new InputStreamReader(socket.getInputStream()));

            String s = reader.readLine() ;
            LtLog.i("result:"+ s) ;
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null ;
    }

    public static byte[] tcpSend(String ip, int port, byte[] msg, int timeout){
        SocketAddress address = new InetSocketAddress(ip, port) ;
        SocketChannel socketChannel = null;
        Selector selector = null;
        try {
            socketChannel = SocketChannel.open() ;
            socketChannel.configureBlocking(false) ;
            socketChannel.connect(address) ;

            long starttime = System.currentTimeMillis() ;
            while (!socketChannel.finishConnect()){
                if(System.currentTimeMillis() - starttime > timeout){
//                    LtLog.e("tcp connect timeout") ;
                   return null ;
                }
               Thread.sleep(100);
            }
            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_WRITE) ;
            int i = selector.select(timeout) ;
            if( i > 0){
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                while(keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if(key.isWritable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        channel.write(ByteBuffer.wrap(msg));
                    }else{
                        LtLog.i("select writeable error") ;
                        return null ;
                    }
                }
            }else{
                LtLog.i("select writeable timeout") ;
                return null ;
            }
            socketChannel.register(selector, SelectionKey.OP_READ) ;
            i = selector.select(timeout) ;
            if(i > 0){
                Set<SelectionKey> selectionKeys = selector.selectedKeys() ;
                Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                while(keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if(key.isReadable()){
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024) ;
                        int ret = channel.read(byteBuffer) ;
                        byteBuffer.flip() ;
                        if(ret > 0) {
                            byte b[] = new byte[ret];
                            byteBuffer.get(b, 0, ret);
                            return b ;
                        }else{
                            return null ;
                        }
                    }else{
                        LtLog.w("select readable error") ;
                        return null ;
                    }
                }
            }else{
                LtLog.w("select readable timeout") ;
                return null ;
            }
        } catch (IOException e) {
//            e.printStackTrace();
        } catch (InterruptedException e) {
//            e.printStackTrace();
        }finally {
            if (socketChannel != null){
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(selector != null){
                try {
                    selector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null ;
    }

}
