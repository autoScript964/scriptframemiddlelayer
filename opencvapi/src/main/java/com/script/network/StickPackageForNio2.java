package com.script.network;


/**
 * Created by     on 2016/11/11.
 */
public class StickPackageForNio2 extends ServerNio2 implements ServerNio2.DataCallback {

    private  StickPackageCallback mCallback;

    public StickPackageForNio2(){
        super.setDataCallback(this);
    }

    public void setDataCallback(StickPackageCallback callback){
       mCallback = callback ;
    }

    private void handleEvent(ServerNioObject serverNioObject){
        int offset = 0 ;
        int packageSize = serverNioObject.recvBuffer.getInt(offset) ;
        offset += 4 ;
        int dataSize = serverNioObject.recvBuffer.position() ;
        if(dataSize >= (packageSize + 4) ){
            byte[] data = serverNioObject.recvBuffer.array() ;

            mCallback.onData(serverNioObject, data, offset, packageSize);

            serverNioObject.recvBuffer.clear() ;

            int remainSize = dataSize - (packageSize +4) ;

            if(remainSize > 0 ) {
                serverNioObject.recvBuffer.put(data, packageSize + 4, remainSize);
                handleEvent(serverNioObject);
            }else if(remainSize < 0){

            }
        }else if(dataSize > serverNioObject.recvBuffer.capacity()){
            serverNioObject.recvBuffer.clear() ;
        }
    }

    @Override
    public void onClientConnected(ServerNioObject serverNioObject) {
        mCallback.onConnect(serverNioObject);
    }

    @Override
    public void onDataEvent(ServerNioObject serverNioObject) {
        handleEvent(serverNioObject);
    }

    @Override
    public void onClientDisconnect(ServerNioObject serverNioObject) {
        mCallback.onDisconnect(serverNioObject);

    }

    public interface StickPackageCallback{
        public void onConnect(ServerNioObject object) ;
        public void onData(ServerNioObject object, byte data[], int offist, int count) ;
        public void onDisconnect(ServerNioObject object) ;
    }
}
