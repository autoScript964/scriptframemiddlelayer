package com.script.network;


import com.script.opencvapi.LtLog;
import com.script.utils.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by     on 2016/10/12.
 */
public class ServerNio2 {

    public static final int DEFAULT_BUFFER_SIZE = 1024 * 200;
    public static final int LOCK_TIMEOUT        = 1000 ;
    private static int sCurrentId = 0;
    private DataCallback mCallback;
    private ServerSocketChannel mServerChannel;
    private Selector mSelector;
//    private ExecutorService mThreadPool ;
    private int mThreadCount = 20 ;
    private Thread mSelectorThread;
    private boolean mExit;

    public ServerNio2(){
//        mThreadPool = Executors.newFixedThreadPool(mThreadCount) ;
    }
    public void setThreadCount(int count){
        mThreadCount = count ;
//        mThreadPool = Executors.newFixedThreadPool(mThreadCount) ;
    }

    public void start(int port) {
        start(port, true);
    }

    public void start(final int port, boolean block) {
        if (block) {
            _start(port);
        } else {
            mSelectorThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    _start(port);
                }
            });
            mSelectorThread.start();
        }
    }

    private void _start(int port) {
        mExit = false;
        try {
            mServerChannel = ServerSocketChannel.open();
            mSelector = Selector.open();
            mServerChannel.socket().bind(new InetSocketAddress(port));
            mServerChannel.configureBlocking(false);
            mServerChannel.register(mSelector, SelectionKey.OP_ACCEPT);
            while (!mExit) {
                int ret = mSelector.select();
                if (ret <= 0) {
                    LtLog.e("Servernio select ret:" + ret);
                } else {
                    Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        handleKey(iterator.next()) ;
                        iterator.remove();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleNewClient(SelectionKey key) {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        try {
            SocketChannel socketChannel = serverChannel.accept();
            socketChannel.configureBlocking(false) ;
            SelectionKey newClientKey = socketChannel.register(mSelector, SelectionKey.OP_READ);
            ServerNioObject object = new ServerNioObject(newClientKey) ;
            newClientKey.attach(object);
            LtLog.d("connect client count:"+ mSelector.keys().size()) ;
            if(mCallback != null){
                mCallback.onClientConnected(object);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRead(SelectionKey key){
       SocketChannel channel = (SocketChannel) key.channel();
        ServerNioObject attachment = (ServerNioObject) key.attachment();
        if (attachment.recvBuffer == null){
            attachment.recvBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE) ;
        }
        try {
            if(attachment.recvBuffer.position() == attachment.recvBuffer.capacity()){
                attachment.recvBuffer = Utils.expend(attachment.recvBuffer);
            }
            int ret = channel.read(attachment.recvBuffer) ;
            if(ret > 0){
                if(mCallback != null){
                    mCallback.onDataEvent(attachment);
                }
            }else{
                channel.close();
                key.cancel();
                LtLog.d("disconnect client count:"+ mSelector.keys().size()) ;
                if(mCallback != null){
                    mCallback.onClientDisconnect(attachment);
                }
            }
        } catch (IOException e) {
            key.cancel();
            if(mCallback != null){
                    mCallback.onClientDisconnect(attachment);
                }
            //e.printStackTrace();
        }
    }

    private void handleKey(SelectionKey key) {
        try {
            if (key.isAcceptable()) {
                handleNewClient(key);
            } else if (key.isReadable()) {
                handleRead(key);
            } else {
                System.out.println("error handleRead lock timeout........");
            }
        }catch (Exception e){
            e.printStackTrace();
            key.cancel();
        }
        //mThreadPool.execute(new DisposeThread(key));

    }

    public void sendMessage(ServerNioObject serverNioObject){
       SelectionKey key = serverNioObject.selectionKey ;
        SocketChannel channel = (SocketChannel) key.channel();
        serverNioObject.sendBuffer.flip() ;
        if(!channel.isConnected() || !channel.isOpen() ){
            //LtLog.e("send message error channel no connected"+ channel) ;
           return ;
        }
        try {
            while(serverNioObject.sendBuffer.hasRemaining()) {
                channel.write(serverNioObject.sendBuffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void disconnect(ServerNioObject object){
        SelectionKey key = object.selectionKey ;
        try {
            key.channel().close();
            key.cancel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void closeKey(SelectionKey key) {
        try {
            key.channel().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        mExit = true;
        mSelector.wakeup();
        try {
            Iterator<SelectionKey> iterator = mSelector.keys().iterator();
            while (iterator.hasNext()) {
                closeKey(iterator.next());
            }
            mSelector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDataCallback(DataCallback callback) {
        mCallback = callback;
    }

    public class ServerNioObject {
        public SelectionKey selectionKey ;
        public int id;
        public ByteBuffer recvBuffer;
        public ByteBuffer sendBuffer ;
        public Object obj ;
        public Lock lock ;

        public ServerNioObject(SelectionKey key) {
            this.selectionKey = key ;
            id = nextId();
            lock = new ReentrantLock() ;
        }
        public String getClientIp(){
            String ip = "" ;
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            if(channel.isConnected()){
                try {
                    String addressInfo = channel.getRemoteAddress().toString() ;
                    ip = addressInfo.substring(addressInfo.indexOf("/") + 1,addressInfo.indexOf(":")) ;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return  ip ;
        }
        private int nextId() {
            return ++sCurrentId;
        }
    }


    class DisposeThread implements Runnable{
        private  SelectionKey key ;
        public DisposeThread(SelectionKey key){
            this.key = key ;

        }
        @Override
        public void run() {
            /*if (key.isAcceptable()) {
                handleNewClient(key);
            } else*/ if (key.isReadable()) {
                ServerNioObject object = (ServerNioObject) key.attachment();
                try {
                    if (object.lock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                        handleRead(key);
                        object.lock.unlock();
                    } else {
                        System.out.println("error handleRead lock timeout........");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public interface DataCallback {
        public void onClientConnected(ServerNioObject serverNioObject) ;
        public void onDataEvent(ServerNioObject serverNioObject);
        public void onClientDisconnect(ServerNioObject serverNioObject) ;
    }
    public static void main(String args[]){
        final ServerNio2 serverNio = new ServerNio2() ;
        serverNio.setDataCallback(new DataCallback() {
            @Override
            public void onClientConnected(ServerNioObject serverNioObject) {
               LtLog.i("client connected:"+ serverNioObject.id ) ;
            }

            @Override
            public void onDataEvent(ServerNioObject serverNioObject) {
                LtLog.i("data event:"+ serverNioObject.id) ;
                serverNioObject.sendBuffer = serverNioObject.recvBuffer ;
                //serverNio.sendMessage(serverNioObject);
            }

            @Override
            public void onClientDisconnect(ServerNioObject serverNioObject) {
                LtLog.i("client disconnect: "+ serverNioObject) ;
            }
        });
    }
}


