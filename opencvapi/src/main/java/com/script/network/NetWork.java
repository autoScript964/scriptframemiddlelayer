package com.script.network;

import com.script.opencvapi.LtLog;
import com.script.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by     on 2015/6/16.
 */
public class NetWork {

    private static final int MAX_BUF_SIZE       = 1024*1024*2 ;
    private static final int DEFAULT_BUFFER_SIZE= 1024*10 ;
    public static final int EVENT_CONNECTED     = 1 ;
    public static final int EVENT_DISCONNECTED  = 2 ;
    public static final int EVENT_TIMEOUT       = 3 ;

    private Socket mSocket ;
    private String mIp ;
    private int mPort ;
    private NetWorkCallback mCallback ;
    private boolean mStarted = false ;
    private InputStream mIn ;
    private OutputStream mOut ;
    private boolean mExit =false;

    private ByteBuffer mSendBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE) ;
    public NetWork(String ip, int port){
        LtLog.i("net work construct :"+ip+":"+port);
        mIp = ip ;
        mPort = port ;
    }
    public void stop(){
        LtLog.i("NetWork stop") ;
        if(mSocket != null && mSocket.isConnected()){
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mExit = true ;
        mStarted = false ;
    }
    public boolean isConnected(){
        if(mSocket != null){
            return mSocket.isConnected();
        }
        return false ;
    }

    private void _start(int timeout){
        LtLog.i("net work start: exit:"+ mExit) ;
        int useTime = 0 ;
        while(!mExit ){
            if(timeout > 0){
                if(useTime > timeout){
                    break ;
                }
            }
            try {

                LtLog.i("new Socket ing") ;
                mSocket = new Socket();
                SocketAddress address = new InetSocketAddress(mIp, mPort) ;
                mSocket.connect(address, 500);
                mIn = mSocket.getInputStream();
                mOut = mSocket.getOutputStream();
                if (mCallback != null) {
                    mCallback.netEventCallback(EVENT_CONNECTED);
                }
                break;
            }catch (IOException e) {
                e.printStackTrace();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                useTime += 1000 ;
            }
        }
        if(mSocket == null || mIn == null || mOut == null){
            if(mCallback != null) {
                mCallback.netEventCallback(EVENT_TIMEOUT);
            }
            return ;
        }
        try{

            byte datalenBytes[] = new byte[4];
            byte dataBuf[] = new byte[MAX_BUF_SIZE] ;
            while(!mExit) {
                LtLog.i("net work read waiting....................") ;
                int len = mIn.read(datalenBytes, 0, 4);
                if (len < 0) {
                    LtLog.i("net work read int break:"+len) ;
                    break;
                }
                int dataLen = Utils.bytesToInt(datalenBytes, 0) ;
                LtLog.i("net work read int :"+dataLen) ;
                if(dataLen > 0 && dataLen <= dataBuf.length){
                    int recvlen = 0 ;
                    while(recvlen != dataLen){
                        LtLog.i("net work read buf waiting....................recvlen:"+recvlen) ;
                        recvlen += mIn.read(dataBuf,recvlen,dataLen-recvlen) ;
                    }
                    if(mCallback != null){
                        mCallback.onData(dataBuf,dataLen);
                    }
                }else{
                    LtLog.i("net work read data break:"+dataLen) ;
                    break ;
                }
            } // end while
            if(mSocket != null && mSocket.isConnected()){
                mSocket.close();
            }
            LtLog.i("netEventCallback mexit:"+mExit) ;
            //主动调用stop不发回调
            if(mCallback != null && !mExit){
                mCallback.netEventCallback(EVENT_DISCONNECTED);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(NetWorkCallback callback, final int timeout, boolean block){
        LtLog.i("NetWork start") ;
        mCallback = callback ;
        mExit = false ;
        if(!mStarted){
            mStarted = true ;
            if(block){
                _start(timeout);
            }else{
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        _start(timeout);
                    }
                }).start();
            }
        }
        LtLog.i("start finished") ;

    }

    public void send(ByteBuffer buffer){
        byte[] buf = buffer.array() ;
        int len = buffer.position() ;

        try {
            if(mOut != null){
                mOut.write(buf,0,len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public interface NetWorkCallback{
        public void netEventCallback(int event);
        public void onData(byte[] data, int len) ;
    }

}
