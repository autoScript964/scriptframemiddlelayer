package com.script.utils;

import android.text.TextUtils;
import com.script.Protocol;
import com.script.AtModule;
import com.script.AtModule2;
import com.script.module.MsgResponse;
import com.script.network.ClientNio;
import com.script.opencvapi.LtLog;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;

/**
 * Created by     on 2016/8/16.
 */
public class AsCmdRunner {

    public static final int DEFAULT_PORT    =12600 ;
    /*
    public static void runCmd(String ip, int port, final String cmd, final RunnerCallback callback){
        final StrNetClient netClient = new StrNetClient() ;
        netClient.setServer(ip, port);
        final StringBuffer result = new StringBuffer() ;
        netClient.setEventCallback(new NetWorkEvent() {
            @Override
            public void onConnect(Socket socket) {
                CmdModule cmdModule = new CmdModule(Protocol.TYPE_ADMIN_SHELL) ;
                cmdModule.cmd = cmd ;
                netClient.sendMessage(cmdModule.toData().array());
                netClient.stop();
            }

            @Override
            public void onDisconnect(Socket socket) {
                callback.onResult(result.toString());
            }

            @Override
            public void onTimeout() {
                callback.onResult(null);
            }

            @Override
            public void onRecv(Socket socket, byte[] data, int size) {

            }

            @Override
            public void onRecvStr(Socket socket, String line) {
                callback.onResult(line);
            }
        });
        netClient.start(1000) ;
    }*/

    /*
    public static void runCmdAsync(String ip, int port, String cmd){
        LtLog.d("run cmd async:"+cmd) ;
        runCmd(ip, port, cmd, new RunnerCallback() {
            @Override
            public void onResult(String result) {
               LtLog.d("runcmd result:"+result) ;
            }
        });
    }*/
    public static String runCmdSyncRetry(String ip, int port, String cmd, int timeout, int retryTimes) throws TimeoutException {
        cmd = "echo \"success\";" + cmd ;
        String result = null ;
        for(int i = 0 ;i < retryTimes ; ++i){
            try {
                result = runCmdSync2(ip, port, cmd, timeout) ;
            } catch (TimeoutException e) {
                throw  e ;
            }
            if(!TextUtils.isEmpty(result)){
               result = result.replaceFirst("success","").trim() ;
               break ;
            }
            LtLog.w("runCmdSyncRetry count:"+i) ;
            System.out.println("runCmdSyncRetry count:"+i) ;
        }
        return  result ;
    }

    public static String runCmdSyncRetry(String ip, int port, String cmd, int timeout) throws TimeoutException {
        return  runCmdSyncRetry(ip, port, cmd, timeout, 3) ;
    }
    public static String runCmdSync2(String ip , int port, String cmd, int timeout) throws TimeoutException {
        ClientNio clientNio = new ClientNio(timeout) ;
        String result ;
        try {
            if (!clientNio.connect(ip, port)) {
                throw new TimeoutException("连接As超时");
            }
            CmdModule cmdModule = new CmdModule(Protocol.TYPE_ADMIN_SHELL);
            cmdModule.cmd = cmd;
            clientNio.send(cmdModule.toDataWithLength());
            ByteBuffer byteBuffer = clientNio.read() ;
            result = Utils.bytesToString(byteBuffer.array(), 0, byteBuffer.position()) ;
        }finally {
            clientNio.disconnect();
        }
        return  result ;
    }
    public static void runCmdAsync2(String ip,int port, String cmd) throws TimeoutException {
        ClientNio clientNio = new ClientNio(1000) ;
        try{
            if (!clientNio.connect(ip, port)) {
                throw new TimeoutException("连接As超时");
            }
            CmdModule cmdModule = new CmdModule(Protocol.TYPE_ADMIN_SHELL);
            cmdModule.cmd = cmd;
            clientNio.send(cmdModule.toDataWithLength());
        }finally {
            clientNio.disconnect();
        }
    }
    public static int sendModule(String ip , int port, AtModule module) throws TimeoutException {
        ClientNio clientNio = new ClientNio(3000) ;
        try{
            if (!clientNio.connect(ip, port)) {
                throw new TimeoutException("连接As超时");
            }
            clientNio.send(module.toDataWithLength());
            ByteBuffer byteBuffer = clientNio.readPackage() ;
            MsgResponse response = new MsgResponse(byteBuffer.array(), Protocol.SIZE_OF_TYPE, byteBuffer.array().length - Protocol.SIZE_OF_TYPE) ;
            return response.state ;
        }finally {
            clientNio.disconnect();
        }
    }
    public static int sendModule(String ip , int port, AtModule2 module) throws TimeoutException {
        ClientNio clientNio = new ClientNio(3000) ;
        try{
            if (!clientNio.connect(ip, port)) {
                throw new TimeoutException("连接As超时");
            }
            clientNio.send(module.toDataWithLen());
            ByteBuffer byteBuffer = clientNio.readPackage() ;
            MsgResponse  response = new MsgResponse(byteBuffer.array(), Protocol.SIZE_OF_TYPE, byteBuffer.array().length - Protocol.SIZE_OF_TYPE) ;
            return response.state ;
        }finally {
            clientNio.disconnect();
        }
    }
    public static int sendType(String ip, int port, short type) throws TimeoutException {
        ClientNio clientNio = new ClientNio(3000) ;
        try{
            if (!clientNio.connect(ip, port)) {
                throw new TimeoutException("连接As超时");
            }
            ByteBuffer byteBuffer = ByteBuffer.allocate(6) ;
            byteBuffer.putInt(2) ;
            byteBuffer.putShort(type) ;
            clientNio.send(byteBuffer);
            ByteBuffer recvBuffer = clientNio.readPackage() ;
            MsgResponse  response = new MsgResponse(recvBuffer.array(), Protocol.SIZE_OF_TYPE, recvBuffer.array().length- Protocol.SIZE_OF_TYPE) ;
            return response.state ;
        }finally {
            clientNio.disconnect();
        }
    }
    /*
    public static String runCmdSync(String ip , int port, String cmd, int timeout){

        final StringBuffer sb = new StringBuffer() ;
        Socket socket = null;
        SocketAddress address = new InetSocketAddress(ip, port) ;
        for(int i =0 ;i < 10; ++i) {
                try {
                    socket = new Socket();
                    socket.connect(address, 500);
                    break ;
                }catch (Exception e){
                    LtLog.w("connect error "+i) ;
                   //e.printStackTrace();
                }
        }
        if(!socket.isConnected()){
                return null ;
        }
        try {
            final InputStream in = socket.getInputStream();
            final OutputStream out = socket.getOutputStream();
            CmdModule cmdModule = new CmdModule(Protocol.TYPE_ADMIN_SHELL);
            cmdModule.cmd = cmd;
            final byte[] b = cmdModule.toData().array();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(b.length + 4);
                    byteBuffer.putInt(b.length);
                    byteBuffer.put(b);
                    try {
                        out.write(byteBuffer.array());
                         BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        String line  ;
                        while ( (line = reader.readLine()) != null){
                            if(sb.length() != 0){
                                sb.append("\n") ;
                            }
                            sb.append(line) ;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
            thread.start();

            while(thread.isAlive() && timeout> 0){
                Thread.sleep( 200);
                timeout -=200 ;
            }
            if(socket != null){
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            if (socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        LtLog.d( ip+": rmd result:"+sb.toString()) ;
        return sb.toString() ;
    }*/

    public static class CmdModule extends AtModule {
        public String cmd ;
        public CmdModule(short type) {
            super(type);
        }

        @Override
        public void initField() {
            try {
                fields.put( Protocol.ATTR_CMD,CmdModule.class.getField("cmd")) ;
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    public static String modifyCmd(String info, boolean macModify){
       StringBuffer cmd = new StringBuffer("ypdevicemodify") ;
        cmd.append(" -i \"").append(info).append("\"") ;
        if(macModify){
            cmd.append(" -m") ;
        }
        return cmd.toString() ;
    }
    public static String writeDeviceInfo(String info){
        return  "echo \"" + info +"\" > /data/system/system.ini"  ;
    }

    public static int runCmdForPort(String server, String cmd){
        int cmdPort = 0 ;
        if(server != null && server.contains(":")){
            try {
                String ip = server.substring(0, server.indexOf(":")) ;
                int port = Integer.parseInt(server.substring(server.indexOf(":")+1)) ;
                cmdPort = runCmdForPort(ip, port,cmd) ;
            }catch (Exception e){

            }
        }
        return  cmdPort ;
    }
    public static int runCmdForPort(String ip, int port, String cmd){
        int resultPort = 0 ;
        try {
            Socket socket = null;
            SocketAddress address = new InetSocketAddress(ip, port) ;
            for(int i =0 ;i < 10; ++i) {
                try {
                    socket = new Socket();
                    socket.connect(address, 500);
                    break ;
                }catch (Exception e){
                    LtLog.w("connect error "+i) ;
                    //e.printStackTrace();
                }
            }
            if(!socket.isConnected()){
                return -1 ;
            }
            InputStream in = socket.getInputStream() ;
            OutputStream out = socket.getOutputStream() ;
            CmdModule cmdModule = new CmdModule(Protocol.TYPE_ADMIN_SHELL) ;
            cmdModule.cmd = cmd ;
            byte[] b = cmdModule.toData().array() ;
            ByteBuffer byteBuffer = ByteBuffer.allocate(b.length + 4) ;
            byteBuffer.putInt(b.length) ;
            byteBuffer.put(b) ;
            out.write(byteBuffer.array());
            BufferedReader reader = new BufferedReader( new InputStreamReader(in)) ;
            while(resultPort == 0){
                String line = reader.readLine() ;
                if(line == null){
                    break ;
                }
                try {
                    resultPort = Integer.parseInt(line.trim()) ;
                    break ;
                }catch (Exception e){
//                    e.printStackTrace();
                }
            }
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultPort ;
    }
    public interface RunnerCallback{
        public void onResult(String result) ;
    }
}
