package com.script.framework;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.script.network.ServerNio2;
import com.script.opencvapi.LtLog;
import com.script.opencvapi.ScreenInfo;
import com.script.opencvapi.AtFairy2;
import com.script.opencvapi.AtFairyConfig;
import com.script.opencvapi.AtFairyService;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by     on 2018/8/22.
 */
public class TaskFairyImpl extends AtFairy2 {

    private static final String GAME_TASK_MAINCLASS = "com.script.fairy.TaskMain";
    private static final String GAME_TASK_GETSERVICEPORTFUNC = "getServicePort" ;
    private static final String GAME_TASK_TESTFUNC = "test" ;
    private static final String GAME_TASK_MAINFUNC = "main";

    private static final int TASK_LOOP_TIMESTAMP = 200 ;

    public static AtFairy2 getFairy(){
        return sInstance ;
    }

    private boolean mTaskRunning = false ;
    private Object mMainObj ;
    private Thread mTaskThread ;
    private Method mGetServicePortMethod ;
    private Method mTestMethod ;
    private static AtFairy2 sInstance ;


    private SceneEvent mEvent ;
    //任务列表
    private Map<String, Class<? extends  Task>> mTaskList ;
    //子任务列表
    private Map<String, Class<? extends  Task>> mChildTaskList ;
    private Task mCurrentTask ;

    //存放已经完成的子任务
    private List<String> mFinishedChildTasks ;
    //存放已经完成的任务
    private List<String> mFinishedTasks ;

    public TaskFairyImpl(Context context, Intent intentResult) {
        super(context, intentResult);

        sInstance = this ;
        mTaskList = new LinkedHashMap<>() ;
        mChildTaskList = new LinkedHashMap<>() ;
        mFinishedChildTasks = new ArrayList<>() ;
        mFinishedTasks = new ArrayList<>() ;

        initTask();
        if (AtFairyService.isApkInDebug(getContext())){
            keepalive(false);
        }
    }



    public void addTask(String taskid, Class<? extends Task> cls){
        mTaskList.put(taskid, cls);
    }
    public void addChildTask( String flag, Class<? extends  Task> cls){
        mChildTaskList.put(flag, cls) ;
    }

    /**
     * 设置场景回调事件
     * @param event 场景回调对象
     * */
    public void setEvent(SceneEvent event){
        mEvent = event ;
    }


    private void initTask(){
        LtLog.i("YPTaskFairyImpl initTask") ;
        try {
            Class cls = Class.forName(GAME_TASK_MAINCLASS);
            mMainObj = cls.newInstance();
            Method method = cls.getMethod(GAME_TASK_MAINFUNC, TaskFairyImpl.class);
            method.invoke(mMainObj, this);
            mGetServicePortMethod = initMethod(cls, GAME_TASK_GETSERVICEPORTFUNC) ;
            mTestMethod = initMethod(cls,GAME_TASK_TESTFUNC) ;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    private Method initMethod(Class cls, String name){
        try {
            return cls.getMethod(name) ;
        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
        }
        return  null ;
    }

    @Override
    public void onRestart() {
        super.onRestart();
        if(!TextUtils.isEmpty(AtFairyConfig.getTaskID())){
            if(mCurrentTask != null){
                mCurrentTask.onRestart() ;
            }
        }
    }

    private Task getTestTask(){
        if(mTestMethod == null){
            return  null ;
        }
        try {
            Class<? extends Task> cls = (Class<? extends Task>) mTestMethod.invoke(mMainObj);
            return  cls.newInstance() ;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null ;
    }

    private Task getChildTask(){
        Iterator<Map.Entry<String, Class<? extends Task>>> iter = mChildTaskList.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, Class<? extends Task>> entry = iter.next();
                    if(mFinishedChildTasks.contains(entry.getKey())){
                        continue;
                    }
                    String option = AtFairyConfig.getOption(entry.getKey()) ;
                    LtLog.i("getChildTask:"+entry.getKey()+" option:"+option) ;
                    if (AtFairyConfig.getOption(entry.getKey()).equals("1")) {
                        try {
                            return entry.getValue().newInstance();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                return  null ;
    }
    private Task getTask(){
        LtLog.i("get taskid:"+ AtFairyConfig.getTaskID()) ;
        int taskId = Integer.parseInt(AtFairyConfig.getTaskID()) ;
        Iterator<Map.Entry<String, Class<? extends Task>>> iter = mTaskList.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, Class<? extends Task>> entry = iter.next();
                    LtLog.i("get task entry key:"+entry.getKey()) ;
                    if(mFinishedTasks.contains(entry.getKey())){
                        continue;
                    }

                    int key = Integer.parseInt(entry.getKey()) ;
                    if (entry.getKey().equals(AtFairyConfig.getTaskID())) {
                        if(entry.getValue() != null) {
                            try {
                                LtLog.i("get task entry value:" + entry.getValue());
                                return entry.getValue().newInstance();
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    }else{
                        LtLog.i("get task not equal entry key:("+entry.getKey()+ ")task id:("+ AtFairyConfig.getTaskID()+")") ;
                    }
                }
                return  null ;
    }
    private boolean startNext(String current){
        LtLog.i("start next...") ;
        if(mTaskList.containsKey(current)){
            mFinishedTasks.add(current) ;
        }else {
            mFinishedChildTasks.add(current);
        }
        return  start() ;
    }
    private boolean start(){
        synchronized (this) {
            if (!TextUtils.isEmpty(AtFairyConfig.getTaskID())) {
                do{
                    mCurrentTask = getChildTask() ;
                    if(mCurrentTask != null){
                        break;
                    }
                    mCurrentTask = getTask() ;
                    if(mCurrentTask != null){
                        break;
                    }
                    LtLog.e("not define task ....") ;
                }while(false) ;
            }else{
                mCurrentTask = getTestTask() ;
                if(mCurrentTask != null){
                    LtLog.e("not define task  use test task....") ;
                }
            }
            if(mTaskThread == null || !mTaskThread.isAlive()){
                mTaskThread = new Thread(new TaskLoop());
                mTaskThread.start();
            }
            if(mCurrentTask != null) {
                mCurrentTask.init(AtFairyConfig.getUserTaskConfig());
                this.notify();
                return true;
            }else{
                LtLog.i("start task null........") ;
            }
        }
        return  false ;
    }
    private void stop(){
        synchronized (this) {
            mTaskRunning = false ;
            if (mTaskThread != null) {
                mTaskThread.interrupt();
            }
        }
    }


    public void startTest(){
        if(mTestMethod != null){
            try {
                mTestMethod.invoke(mMainObj) ;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStart() {
        LtLog.i("YpTaskFairyImpl on start................");
        mFinishedChildTasks.clear();
        mFinishedTasks.clear();
        synchronized (this) {
            mTaskRunning  = start() ;
        }
    }


    @Override
    public void onStop() {
        LtLog.i("YpTaskFairyImpl on stop................");
        synchronized (this) {
            mFinishedChildTasks.clear();
            mFinishedTasks.clear();
            stop();
            mTaskRunning = false;
        }
    }

    @Override
    public void onResume() {
        mFinishedChildTasks.clear();
        mFinishedTasks.clear();
        synchronized (this) {
            LtLog.i("YpTaskFairyImpl on resume................");
            mTaskRunning = start();
        }
    }

    @Override
    public void onPause() {
        synchronized (this) {
            LtLog.i("YpTaskFairyImpl on pause................");
            stop();
            mTaskRunning = false;
        }
    }

    @Override
    public void onCheckStart() {
        LtLog.i("YpTaskFairyImpl on check start................");
    }

    @Override
    public void onCheckStop() {

    }

    @Override
    public void onChangeConfig() {
    }

    @Override
    public void onData(ServerNio2.ServerNioObject object, byte[] data, int offset, int count) {
        if(mCurrentTask != null){
            mCurrentTask.onData(object, data, offset, count);
        }
    }
    @Override
    public void onDisconnect(ServerNio2.ServerNioObject object) {
        if(mCurrentTask != null){
            mCurrentTask.onDisconnect(object);
        }
    }


    public int getServicePort(){
        int port = 0 ;
        try {
            if(mGetServicePortMethod != null) {
                port = (int) mGetServicePortMethod.invoke(mMainObj);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return  port ;
    }


    public void taskFinish(String flag,int state){
        synchronized (this){
            mCurrentTask = null ;
            mTaskThread.interrupt();
        }
        if(!startNext(flag)){
            finish(AtFairyConfig.getTaskID(),state);
        }
    }
    public void taskFinish(Class<? extends Task> cls,int state){

        LtLog.i("task finish:"+cls+"state:"+state) ;
        Iterator<Map.Entry<String, Class<? extends Task>>> iter = mTaskList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Class<? extends Task>> entry = iter.next();
            if(entry.getValue().equals(cls)){
                taskFinish(entry.getKey(), state);
                return;
            }
        }
        iter = mChildTaskList.entrySet().iterator() ;
        while (iter.hasNext()) {
            Map.Entry<String, Class<? extends Task>> entry = iter.next();
            if(entry.getValue().equals(cls)){
                taskFinish(entry.getKey(),state);
                return;
            }
        }



    }

    public void finish(String taskId,int state) {
        super.finish(FAIRY_TYPE_TASK, taskId,state);
        stop();
        mTaskRunning = false ;
    }

    @Override
    public void finish(int type, String taskId, int state) {
        super.finish(type, taskId, state);
        stop();
        mTaskRunning = false ;
    }

    @Override
    public boolean onMonitorState(int state) {
        if(!TextUtils.isEmpty(AtFairyConfig.getTaskID()) && mCurrentTask != null){
            return mCurrentTask.onMonitorState(state) ;
        }
        return false;
    }

    class TaskLoop implements Runnable{

        @Override
        public void run() {
            while(true) {
                synchronized (TaskFairyImpl.this) {
                    while (mCurrentTask == null || !mTaskRunning) {
                        try {
                            TaskFairyImpl.this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    Thread.sleep(TASK_LOOP_TIMESTAMP);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
                ScreenInfo screenInfo = capture();
                if (screenInfo.raw == null) {
                    LtLog.i("game monitor error capture screen info null....");
                    continue;
                }
                Mat screenMat = new Mat(screenInfo.height, screenInfo.width, CvType.CV_8UC4);
                screenMat.put(0, 0, screenInfo.raw);
                mCurrentTask.taskLoop(screenMat);
                screenMat.release();
            }
        }
    }
}
