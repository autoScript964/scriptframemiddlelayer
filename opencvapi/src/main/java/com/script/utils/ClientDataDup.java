package com.script.utils;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by     on 2016/8/23.
 */
public class ClientDataDup {
    private OutputStream out ;
    public ClientDataDup(OutputStream out){
        this.out = out ;
    }
    public void externalData(byte b[] , int offsit, int count) throws IOException {
        synchronized (out){
            out.write(b, offsit, count);
        }
    }
    public void addSrc(final InputStream in, final SrcDisconnectCallback callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte mBuf[] = new byte[1024*100] ;
               while(true){
                   int len ;
                   try {
                       len = in.read(mBuf) ;
                       if(len <=0){
                           callback.onDumpError();
                           break ;
                       }
                   } catch (IOException e) {
                       e.printStackTrace();
                       callback.onDumpError();
                       break;
                   }
                   synchronized (out) {
                       try {
                           out.write(mBuf, 0, len);
                        } catch (IOException e) {
                           callback.onDumpError();
                        }
                   }
               }
            }
        }).start();
    }
    public interface SrcDisconnectCallback{
        void onDumpError() ;
    }
}
