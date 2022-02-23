package com.script.network;


import com.script.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by     on 2016/8/10.
 */
public class GeneralNetRead {
    public static int readBuf(InputStream in, byte[] buf){
                int recvlen = 0 ;
                while(recvlen != buf.length){
                    try {
                        recvlen += in.read(buf,recvlen,buf.length-recvlen) ;
                    } catch (IOException e) {
                        e.printStackTrace();
                        break ;
                    }
                }
        return  recvlen ;
    }
    public static int NetRead(Socket socket, byte[] buf) {
        int dataLen = 0 ;
        InputStream in = null;
        try {
            in = socket.getInputStream();
            byte[] sizeBuf = new byte[4] ;
            int readSize = readBuf(in, sizeBuf) ;
            if(readSize != 4){
                return 0 ;
            }
            dataLen = Utils.bytesToInt(sizeBuf, 0) ;
            if(dataLen > 0 && dataLen <= buf.length){
                int recvlen = 0 ;
                while(recvlen != dataLen){
                    recvlen += in.read(buf,recvlen,dataLen-recvlen) ;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataLen ;
    }
}
