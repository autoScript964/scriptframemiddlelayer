package com.script.network;

import com.script.opencvapi.LtLog;
import com.script.utils.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeoutException;

/**
 * Created by     on 2017/2/24.
 */
public class ClientNio {
    private static final int MAX_CACHE = 1024 * 1024 * 4;
    private SocketChannel mChannel;
    private int mMaxCache = MAX_CACHE;
    private int mTimeout;

    public ClientNio(int timeout) {
        mTimeout = timeout;
    }

    public void setMaxCache(int cache) {
        mMaxCache = cache;
    }

    public boolean connect(String ip, int port) {
        try {
            LtLog.i("connect:" + ip + " port:" + port);
            mChannel = SocketChannel.open();
            synchronized (mChannel) {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(ip, port);
                mChannel.configureBlocking(false);
                mChannel.connect(inetSocketAddress);
                int timeout = mTimeout;
                while (timeout > 0) {
                    if (mChannel.finishConnect()) {
                        break;
                    }
                    timeout -= 10;
                    Thread.sleep(10);
                }
                return mChannel.isConnected();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void disconnect() {
        try {
            if (mChannel != null) {
                synchronized (mChannel) {
                    mChannel.close();
                    mChannel = null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int send(ByteBuffer byteBuffer) throws TimeoutException {
        if (mChannel == null) {
            return -1;
        }
        int writeSize = 0;
        synchronized (mChannel) {
            if(!mChannel.isConnected()){
                return  -1 ;
            }
            byteBuffer.flip();
            long writeStart = System.currentTimeMillis();
            int timeout = mTimeout;
            while (byteBuffer.hasRemaining()) {
                if (System.currentTimeMillis() - writeStart >= timeout) {
                    throw new TimeoutException("发送数据超时");
                }
                try {
                    writeSize += mChannel.write(byteBuffer);
                } catch (SocketException e) {
                    try {
                        mChannel.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    return -1;
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

            }
        }
        return writeSize;
    }

    public ByteBuffer readPackage() throws TimeoutException {
        ByteBuffer sizeBuffer = read(4);
        int size = 0;
        if (sizeBuffer != null && sizeBuffer.position() == 4) {
            sizeBuffer.flip();
            size = sizeBuffer.getInt();
        }
        if (size < 0 || size > mMaxCache) {
            LtLog.i("read package error:" + size);
            return null;
        }

        return read(size);
    }

    public ByteBuffer read() throws TimeoutException {
        if (mChannel == null ) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(mMaxCache);
        synchronized (mChannel) {
            if(!mChannel.isConnected()){
                return  null ;
            }
            long readStart = System.currentTimeMillis();
            while (true) {
                try {
                    if (byteBuffer.position() == byteBuffer.capacity()) {
                        if (byteBuffer.position() > mMaxCache) {
                            System.out.println("clientnio buffer error:" + byteBuffer.position());
                            break;
                        }
                        byteBuffer = Utils.expend(byteBuffer);
                        System.out.println("clientnio expend buffer");
                    }
                    int readSize = mChannel.read(byteBuffer);
                    if (readSize < 0) {
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (System.currentTimeMillis() - readStart >= mTimeout) {
                    throw new TimeoutException("读取数据超时");
                }
            }
        }
        return byteBuffer;
    }

    public ByteBuffer read(int size) throws TimeoutException {
        if (mChannel == null){
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        synchronized (mChannel) {
            if(!mChannel.isConnected()){
                return  null ;
            }
            int readSize = 0;
            long readStart = System.currentTimeMillis();
            try {
                while (readSize < size) {
                    if (System.currentTimeMillis() - readStart >= mTimeout) {
                        throw new TimeoutException("读取数据超时");
                    }
                    readSize += mChannel.read(byteBuffer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return byteBuffer;
    }
}
