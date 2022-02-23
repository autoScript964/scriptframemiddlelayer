package com.script.framework;

import com.script.framework.action.IAction;
import com.script.framework.condition.Condition;
import com.script.framework.condition.ImageCondition;
import com.script.opencvapi.LtLog;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class Scene {
    private Condition mCondition ;
    private int mFlag ;
    private String mName ;
    private boolean mInScene ;
    private  int mTimeout = -1;
    private  int mNoCondtionTimeout = -1 ;
    private SceneEvent mEvent ;
    private long mSceneInTimestamp ;
    private long mCondtionMatchedTimestamp ;
    private List<Condition> mConditionList ;
    private List<IAction> mSceneInActionList ;
    private List<IAction> mSceneOutActionList ;
    private IAction mTimeoutAction ;
    private IAction mNoConditionTimeoutAction ;

    /**
     * 构建一个场景
     * @param flag 场景唯一标示
     * @param name 场景命名
     * @param condition 场景条件
     * */
    public Scene(int flag, String name, Condition condition){
        init(flag, name, condition);
    }
    /**
     * 构建一个场景,此场景没有条件触发，每次loop，在此之前的场景如果没有进入则遍历此场景所有条件
     * @param flag 场景唯一标示
     * @param name 场景命名
     * */
    public Scene(int flag, String name){
        init(flag, name, null) ;
    }
    /**
     * 设置此场景超时时间
     * */
    public void setTimeout(int timeout){
        mTimeout = timeout ;
    }
    /**
     * 设置此场景超时时间
     * @param timeout  超时时间
     * @param action 超时触发操作
     * */
    public void setTimeout(int timeout, IAction action){
        mTimeout = timeout ;
        mTimeoutAction = action ;
    }
    /**
     * 设置此场景超时时间
     * @param timeout  超时时间
     * @param action 超时触发操作
     * */
    public void setTimeOut(int timeout, IAction action){
        mTimeout = timeout ;
        mTimeoutAction = action ;
    }
    /**
     * 设置没有任何条件触发的超时时间
     * */
    public void setNoCondtionTimeout(int timeout){
        mNoCondtionTimeout = timeout  ;
    }
    /**
     * 设置没有任何条件触发的超时时间
     * @param timeout  超时时间
     * @param action 超时触发操作
     * */
    public void setNoCondtionTimeout(int timeout, IAction action){
        mNoCondtionTimeout = timeout  ;
        mNoConditionTimeoutAction = action ;
    }
    /**
     * 一张图片构建一个场景
     * @param flag 场景唯一标示
     * @param name 场景命名
     * @param image 场景相关截图
     * */
    public Scene(int flag, String name, String image){
        Condition condition = new Condition()  ;
        condition.andItem(new ImageCondition(image));
        init(flag, name, condition);
    }
    private void init(int flag, String name, Condition condition){
        mFlag = flag ;
        mName = name ;
        mCondition = condition ;
        mConditionList = new ArrayList<>() ;
        mSceneOutActionList = new ArrayList<>() ;
        mSceneInActionList = new ArrayList<>() ;

    }
    /**
     * 添加一个action，在场景进入时执行
     * */
    @Deprecated
    public void setSceneInAction(IAction action){
        addSceneInAction(action) ;
    }

    /**
     * 添加一个action，在场景进入时执行
     * */
    public void addSceneInAction(IAction action){
        mSceneInActionList.add(action) ;
    }
    public void clearSceneInAction(){
        mSceneInActionList.clear();
    }
    /**
     * 添加一个action，在离开场景后执行
     * */
    @Deprecated
    public void setSceneOutAction(IAction action){
        addSceneOutAction(action);
    }

    /**
     * 添加一个action，在离开场景后执行
     * */
    public void addSceneOutAction(IAction action){
        mSceneOutActionList.add(action) ;
    }

    public void clearSceneOutAction(){
        mSceneOutActionList.clear();
    }
    /**
     * 设置场景回调事件
     * */
    public void setEvent(SceneEvent event){
        mEvent = event ;
    }
    public SceneEvent getEvent(){
        return mEvent ;
    }

    /**
     * 添加场景中触发的条件
     * @param condition  场景中触发的条件
     * */
    public void addSceneCondition(Condition condition){
        mConditionList.add(condition) ;
    }
    /**
     * 将一组条件加入到场景，优先判断
     * */
    public void addToHead(List<Condition> conditions){
        mConditionList.addAll(0, conditions) ;
    }
    /**
     * 将一组条件加入到场景，最后判断
     * */
    public void pushback(List<Condition> conditions){
        mConditionList.addAll(conditions) ;
    }
    /**
     * 移除条件
     * @param id 条件id
     * */
    public void removeSceneCondition(int id){
            for(Condition condition:mConditionList){
                if(condition.getId() == id){
                    mConditionList.remove(condition) ;
                    break;
                }
            }
    }

    /**
     * 获取场景标识
     * */
    public int getFlag(){
        return  mFlag ;
    }

    /**
     * 获取场景名称
     * */
    public String getName(){
        return  mName ;
    }
    public void sceneLoop(Mat screenMat){
        if(mCondition != null) {
            mInScene = mCondition.looping(screenMat);
        }else{
            mInScene =  false;
        }

        if(mInScene && mSceneInTimestamp == 0) {//进入场景
            mSceneInTimestamp = System.currentTimeMillis();
            mCondtionMatchedTimestamp = System.currentTimeMillis() ;
            for(IAction action: mSceneInActionList){
                action.onAction() ;
            }
            if (mEvent != null) {
                mEvent.onSceneIn(mFlag, mName);
            }
        }else if(mInScene && mSceneInTimestamp != 0){//已经在场景中
            if(mTimeout > 0 && System.currentTimeMillis() - mSceneInTimestamp > mTimeout){
                if(mTimeoutAction != null){
                    mTimeoutAction.onAction() ;
                }
                if(mEvent != null){
                    mEvent.onSceneTimeout(mFlag,mName) ;
                }
            }
        }else if(!mInScene && mSceneInTimestamp != 0){//离开场景
            mSceneInTimestamp = 0 ;
            mCondtionMatchedTimestamp = 0 ;
            for(Condition condition: mConditionList) {
                condition.reset();
            }

            for(IAction action: mSceneOutActionList){
                action.onAction() ;
            }
            if(mEvent != null){
                mEvent.onSceneOut(mFlag,mName) ;
            }
        }else if(!mInScene && mSceneInTimestamp == 0){//不在场景中
            //do nothing ;
        }

        //如果condition是空则每次遍历所有条件
        boolean conditionMatched = false  ;
            if(mInScene || mCondition == null){
                long start = System.currentTimeMillis() ;
                try{
                    for(Condition condition: mConditionList) {
                        if(condition.looping(screenMat)){
                            conditionMatched = true ;
                            condition.runAction(screenMat);
                            if(mEvent != null){
                                //根据事件返回值确认是否继续
                                if(mEvent.onSceneCondtion(getFlag(),getName(), condition.getId(),condition.getConfirmedTime())){
                                    break;
                                }
                            }else {//没有实现此事件将直接跳出循环
                                break;
                            }
                        }else{
                            condition.runFalseAction(screenMat);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                long end = System.currentTimeMillis() ;
                LtLog.d("condittion loop use time:"+(end - start)) ;
            }
            if(conditionMatched && mInScene){
                mCondtionMatchedTimestamp = System.currentTimeMillis() ;
            }else if(!conditionMatched && mInScene && mNoCondtionTimeout > 0){
                if(System.currentTimeMillis() - mCondtionMatchedTimestamp > mNoCondtionTimeout){
                    if(mNoConditionTimeoutAction != null){
                        mNoConditionTimeoutAction.onAction() ;
                    }
                    if(mEvent != null){
                        mEvent.onSceneNoConditionTimeout(mFlag, mName) ;
                    }
                }
            }
    }
    /**
     * 判断是否在场景中
     * */
    public boolean inScene(){
        return  mInScene ;
    }


}
