package com.script.network;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by     on 2016/8/9.
 */
public class NetClient {
    private NetWorkEvent mEvent ;
    private String mServer ;
    private int mPort ;
    private Socket mSocket ;
    private boolean mExit ;
    private byte[] mBuf ;
    private int mBufSize = 1024*10 ;
    public NetClient(){
        mBuf = new byte[mBufSize] ;
    }
    public NetClient(int size){
        mBuf = new byte[size] ;
    }
    public void setServer(String server, int port)
    {
        mServer = server ;
        mPort = port ;
        mExit = false ;
    }
    public void setEventCallback(NetWorkEvent event){
        mEvent = event ;
    }
    public void sendMessage(ByteBuffer byteBuffer){
        if(mSocket!= null && mSocket.isConnected() && !mSocket.isClosed()){
            try {
                OutputStream out = mSocket.getOutputStream() ;
                out.write(byteBuffer.array());
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    mSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        }
    }
    /*
    public void sendMessage(byte b[]){
        if(mSocket!= null && mSocket.isConnected()){
            try {
                OutputStream out = mSocket.getOutputStream() ;
                ByteBuffer byteBuffer = ByteBuffer.allocate(b.length + 4) ;
                byteBuffer.putInt(b.length) ;
                byteBuffer.put(b) ;
                out.write(byteBuffer.array());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    */
    public boolean start(final int timeout){
        if(mSocket != null && mSocket.isConnected()){
            return false ;
        }
        mExit = false ;
        new Thread(new Runnable() {
            @Override
            public void run() {
                SocketAddress address = new InetSocketAddress(mServer, mPort) ;
                long connectStart = System.currentTimeMillis() ;
                while(!mExit){
                    if(timeout != -1 && System.currentTimeMillis() - connectStart > timeout){
                        break ;
                    }
                    mSocket = new Socket() ;
                    try {
                        if(timeout > 0) {
                            mSocket.connect(address, timeout);
                        }else{
                            mSocket.connect(address);
                        }
                        break ;
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }

                if(mSocket != null && mSocket.isConnected()){
                    mEvent.onConnect(mSocket);
                }else{
                    mEvent.onTimeout();
                    return ;
                }
                while(!mExit){
                    int size = GeneralNetRead.NetRead(mSocket,mBuf);
                    if(size > 0){
                        mEvent.onRecv(mSocket, mBuf, size);
                    }else{
                        mEvent.onDisconnect(mSocket);
                        break ;
                    }
                }
                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return true ;
    }
    public void stop()
    {
        mExit = true ;
        try {
            if(mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public interface NetClientCallback{
        public void onConnect() ;
        public void onDisconnect() ;
        public void onTimeout() ;
        public void onRecv(byte[] data, int size) ;
    }
}
