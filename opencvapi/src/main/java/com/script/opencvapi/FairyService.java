package com.script.opencvapi;


import com.script.AtModule2;
import com.script.network.ServerNio2;
import com.script.network.StickPackageForNio2;

public class FairyService {

    private int mPort ;
    private StickPackageForNio2 mServer ;
    private StickPackageForNio2.StickPackageCallback mCallback ;
    public FairyService( StickPackageForNio2.StickPackageCallback callback){
        mCallback = callback ;
    }

    public void start(int port){
        mPort = port ;
        if(mPort > 0 ) {
            mServer = new StickPackageForNio2();
            mServer.setDataCallback(mCallback);
            mServer.start(mPort,false);
        }

    }
    public void stop(){
        if(mServer != null){
            mServer.stop();
        }
    }
    public void sendPackage(ServerNio2.ServerNioObject client, AtModule2 atModule2){
        client.sendBuffer = atModule2.toDataWithLen() ;
        mServer.sendMessage(client);
    }



}
