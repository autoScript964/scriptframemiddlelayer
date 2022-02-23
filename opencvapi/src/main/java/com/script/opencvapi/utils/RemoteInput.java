package com.script.opencvapi.utils;

import android.view.inputmethod.EditorInfo;
import com.script.network.NetWork;
import com.script.opencvapi.LtLog;
import com.script.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by     on 15-9-8.
 */
public class RemoteInput implements NetWork.NetWorkCallback {


    public static final short TYPE_START_INPUT_VIEW   = 0x0601 ;
    public static final short TYPE_FINISH_INPUT       = 0x0602 ;
    public static final short TYPE_TEXT               = 0x0610 ;
    public static final short TYPE_SEND               = 0x0620 ;
    public static final short TYPE_HID_KEYBOARD       = 0x0630 ;
    public static final short TYPE_KEYBOARD_HEIGHT    = 0x0640 ;
    private NetWork mNetWork ;
    private String mIp ;
    private int mPort  ;
    private RemoteCallback mCallback ;
    private ByteBuffer mSendBuffer ;
    public RemoteInput(String ip, int port){
        mIp = ip ;
        mPort = port ;
        mSendBuffer = ByteBuffer.allocate(1024*5) ;
    }
    public void start(RemoteCallback callback){
        mCallback = callback ;
        mNetWork = new NetWork(mIp, mPort) ;
        mNetWork.start(this, -1, true);
    }
    public void onSend(){
        mSendBuffer.clear() ;
        mSendBuffer.putInt(0) ;
        mSendBuffer.putShort(TYPE_SEND) ;
        int len = mSendBuffer.position();
        mSendBuffer.putInt(0, len - 4);
        mNetWork.send(mSendBuffer);

    }
    public void onText(String text){
        mSendBuffer.clear() ;
        mSendBuffer.putInt(0) ;
        mSendBuffer.putShort(TYPE_TEXT) ;
        putString(mSendBuffer, text);

        int len = mSendBuffer.position();
        mSendBuffer.putInt(0, len - 4);
        mNetWork.send(mSendBuffer);
    }
    public void hidKeyBoard(){
        mSendBuffer.clear() ;
        mSendBuffer.putInt(0) ;
        mSendBuffer.putShort(TYPE_HID_KEYBOARD) ;
        int len = mSendBuffer.position();
        mSendBuffer.putInt(0, len - 4);
        mNetWork.send(mSendBuffer);

    }

    public void keyboardHeight(int screenHeight, int keyboardHeight){
        mSendBuffer.clear() ;
        mSendBuffer.putInt(0) ;
        mSendBuffer.putShort(TYPE_KEYBOARD_HEIGHT) ;
        mSendBuffer.putInt(screenHeight) ;
        mSendBuffer.putInt(keyboardHeight);
        int len = mSendBuffer.position();
        mSendBuffer.putInt(0, len - 4);
        mNetWork.send(mSendBuffer);
    }

    public void sendText(String text){
        mSendBuffer.clear() ;
        mSendBuffer.putInt(0) ;
        mSendBuffer.putShort(TYPE_TEXT) ;
        putString(mSendBuffer, text);

        int len = mSendBuffer.position();
        mSendBuffer.putInt(0, len - 4);
        mNetWork.send(mSendBuffer);

    }
    public void sendType(short type){
        mSendBuffer.clear() ;
        mSendBuffer.putInt(0) ;
        mSendBuffer.putShort(type) ;
        int len = mSendBuffer.position();
        mSendBuffer.putInt(0, len - 4);
        mNetWork.send(mSendBuffer);
    }
    private void putString(ByteBuffer buf, String str) {
        if (str == null) {
//            buf.putInt(0);
            return;
        }
        try {
            byte strBytes[] = str.getBytes("UTF-8");
//            buf.putInt(strBytes.length);
            buf.put(strBytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    public void stop(){
        mNetWork.stop();
    }
    @Override
    public void netEventCallback(int event) {
        if(event == NetWork.EVENT_DISCONNECTED){
            mNetWork = new NetWork(mIp, mPort) ;
            mNetWork.start(this, -1, true);
        }
        if(event == NetWork.EVENT_CONNECTED){
            sendType((short)0x0113);
	    mCallback.onConnected() ;
        }
    }

    @Override
    public void onData(byte[] data, int len) {

        short type = Utils.bytesToShort(data,0);

        LtLog.i("remote input onData type:"+type) ;
        switch (type){
            case TYPE_START_INPUT_VIEW :
                mCallback.onStartInputView(null, true);
                break ;
            case TYPE_FINISH_INPUT :
                mCallback.onFinishedInput();
                break ;
        }
    }

    public interface RemoteCallback{
	public void onConnected() ;
        public void onStartInputView(EditorInfo info, boolean restarting) ;
        public void onFinishedInput() ;
    }
}
