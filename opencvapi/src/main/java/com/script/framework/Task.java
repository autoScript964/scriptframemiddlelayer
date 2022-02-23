package com.script.framework;

import com.script.framework.action.IAction;
import com.script.network.ServerNio2;
import com.script.opencvapi.AtFairy2;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public abstract class Task {
//    private static final int INVAILD_FLAG = -1 ;
    protected List<Scene> mSceneList ;
    private static Task sCurrentTask ;
//    private SceneEvent mEvent ;

    private Scene mCurrentScene ;
    private Mat mScreenMat ;

    public static Task getCurrentTask(){
        return sCurrentTask ;
    }

    public Task(){
        sCurrentTask = this ;
        mSceneList = new ArrayList<>() ;

    }

    public Mat getScreenMat(){
        return mScreenMat ;
    }
    public void addScene(Scene scene) {
        mSceneList.add(scene) ;
    }

    public void removeAllScene(){
        mSceneList.clear();
    }

    public Scene getScene(int id){
        for(Scene scene:mSceneList){
            if(scene.getFlag() == id){
                return scene ;
            }
        }
        return  null ;
    }


    public void taskLoop(Mat screenMat){
        mScreenMat = screenMat ;
        do{
            if(mCurrentScene != null) {
                mCurrentScene.sceneLoop(screenMat);
                if (mCurrentScene.inScene()) {
                    break;
                }
                mCurrentScene = null;
            }

            for(Scene scene:mSceneList) {
                scene.sceneLoop(screenMat);
                if (scene.inScene()) {
                    mCurrentScene = scene ;
                    break;
                }
            }
        }while(false) ;
    }
    public IAction getFinishAction(){
        return  new IAction() {
            @Override
            public int onAction() {
                finish();
                return 0;
            }
        } ;
    }
    public IAction getFinishAction(final int state){
        return  new IAction() {
            @Override
            public int onAction() {
                finish(state);
                return 0;
            }
        } ;
    }


    public void finish(){
        TaskFairyImpl impl = (TaskFairyImpl) TaskFairyImpl.getFairy();
        impl.taskFinish(this.getClass(), AtFairy2.TASK_STATE_FINISH);

    }
    /**
     * 任务结束
     * @param state 结束状态,如果结束的是子任务而此任务不是最后一个，结束状态将被忽略
     * */
    public void finish(int state){
        TaskFairyImpl impl = (TaskFairyImpl) TaskFairyImpl.getFairy();
        impl.taskFinish(this.getClass(),state);
    }

    public abstract void init(String config) ;
    public abstract boolean onMonitorState(int state) ;
    public void onRestart(){}
    public void onData(ServerNio2.ServerNioObject object, byte[] data, int offset, int count){}
    public void onDisconnect(ServerNio2.ServerNioObject object){}

}
