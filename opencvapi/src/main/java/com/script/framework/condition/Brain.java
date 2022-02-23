package com.script.framework.condition;

import com.script.framework.action.IAction;
import com.script.opencvapi.LtLog;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Brain {

    private static final String DEFAULT_CONFIG_VALUE = "1" ;
    private Map<Integer, IConditionItem> mConditionMap  ;
    private String mConfig  ;
    private static final List<Integer> debugList = new ArrayList<>() ;
    /***
     * @param config 用户配置文件
     */
    public Brain(String config){
        mConfig = config ;
        mConditionMap = new HashMap<>() ;
    }
    public Brain(){
        mConditionMap = new HashMap<>() ;
    }

    public static void setDebug(int id, boolean debug){
        if(debug){
            if(!debugList.contains(id)) {
                debugList.add(id);
            }
        }else{
            debugList.remove(new Integer(id)) ;
        }
    }
    public static void clearDebug(){
        debugList.clear();
    }
    /**
     * 添加一个判断条件
     * */
    @Deprecated
    public void addJudge(int id, boolean defaultResult){
        mConditionMap.put(id, new JudgeCondition(id,defaultResult)) ;
    }

    /**
     * 添加一个计数器
     * */
    @Deprecated
    public void addCountJudge(int id, int threshhold){
        mConditionMap.put(id, new CounterCondition(id,threshhold)) ;
    }

    /**
     * 添加一个判断条件
     * */
    public IConditionItem addItem(int id, boolean defaultValue){
        JudgeCondition condition = (JudgeCondition) mConditionMap.get(id);
        Assert.assertNull(condition);
        if(condition == null){
            condition = new JudgeCondition(id, defaultValue) ;
            mConditionMap.put(id, condition) ;
        }
        return  condition ;
    }
    /**
     * 添加一个计数器
     * */
    public IConditionItem addItem(int id, int threshold){
        CounterCondition condition = (CounterCondition) mConditionMap.get(id);
        Assert.assertNull(condition);
        if(condition == null) {
                condition = new CounterCondition(id,threshold);
                mConditionMap.put(id, condition);
        }
        return condition ;
    }
    /**
     * 添加一个计时器
     * @param id 计时器id
     * @param delay 计时器第一次延迟返回true的时间
     * @param period 计时器每次返回true的时间间隔
     * */
    public IConditionItem addItem(int id,long delay, long period){
        TimerCondition condition = (TimerCondition) mConditionMap.get(id);
        Assert.assertNull(condition);
        condition = new TimerCondition(id,delay,period) ;
        mConditionMap.put(id, condition) ;
        return condition ;
    }

    /**
     * 添加一个计时器,在延迟指定时间后返回true
     * @param id 计时器id
     * @param delay 计时器延迟返回true的时间
     * */
    public IConditionItem addItem(int id,long delay){
        TimerCondition condition = (TimerCondition) mConditionMap.get(id);
        Assert.assertNull(condition);
        condition = new TimerCondition(id,delay) ;
        mConditionMap.put(id, condition) ;
        return condition ;
    }

    /**
     * 添加一个计时器
     * @param id 计时器id
     * @param date 返回true的时间
     * @param maxTimeout 最大超时时间
     * */
    public IConditionItem addItem(int id, Date date, long maxTimeout){
        DateTimer condition = (DateTimer) mConditionMap.get(id);
        Assert.assertNull(condition);
        condition = new DateTimer(id,date,maxTimeout) ;
        mConditionMap.put(id, condition) ;
        return condition ;
    }
    public void removeAll(){
        mConditionMap.clear();
    }
    /**
     * 获取一个条件
     * @param id 条件id
     * */
    public IConditionItem getItem(int id) {return  mConditionMap.get(id) ;}

    /**
     * 获取一个判断条件
     * @param id 条件id
     * @param defaultValue 条件默认值
     * */
    public IConditionItem getItem(int id, boolean defaultValue){
        IConditionItem item = getItem(id) ;
        if(item == null){
            item = addItem(id, defaultValue);
        }
        return  item ;
    }
    /**
     * 获取用户配置文件相关的Item,默认对应的value为1
     * @param key 相关选项的key
     * */
    public IConditionItem getItem(String key){
        return getItem(key, DEFAULT_CONFIG_VALUE) ;
    }
    /**
     * 获取用户配置文件相关的Item
     * @param key 相关选项的key
     * @param value 对应的value
     * */
    public IConditionItem getItem(String key ,String value){
        JudgeCondition judgeCondition = null ;
        try {
            boolean judgeResult = (new JSONObject(mConfig).optString(key).equals(value)) ;
            judgeCondition = new JudgeCondition(key,judgeResult) ;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(judgeCondition);
        return judgeCondition ;
    }


    /**
     * 获取一个计数器条件
     * @param id 条件id
     * @param threshold 计数器阀值
     * */
    public IConditionItem getItem(int id, int threshold){
        IConditionItem item = getItem(id) ;
        if(item == null){
            item = addItem(id, threshold);
        }
        return  item ;
    }
    /**
     * 获取一个计时器条件
     * @param id 条件id
     * @param delay 计时器第一次延迟返回true的时间
     * @param period 计时器每次返回true的时间间隔
     * */
    public IConditionItem getItem(int id, long delay, long period){
        IConditionItem item = getItem(id) ;
        if(item == null){
            item = addItem(id, delay, period);
        }
        return  item ;
    }

    /**
     * 获取一个计时器条件
     * @param id 条件id
     * @param delay 计时器第一次延迟返回true的时间
     * */
    public IConditionItem getItem(int id, long delay){
        IConditionItem item = getItem(id) ;
        if(item == null){
            item = addItem(id, delay);
        }
        return  item ;
    }


    /**
     * 获取一个action,此action会将条件设置为与原来相反到值
     * */
    public IAction getSwitchAction(int id){
        IConditionItem item = getItem(id) ;
        Assert.assertTrue (item != null && item instanceof JudgeCondition) ;

        return  new BrainSwitchAction((JudgeCondition) item) ;

    }
    /**
     * 获取一个action,此action会将条件设为true
     * */
    public IAction getTrueAction(int id){
        IConditionItem item = getItem(id) ;
        Assert.assertTrue (item != null && item instanceof JudgeCondition) ;

        return  new BrainTrueAction((JudgeCondition) item) ;

    }
    /**
     * 获取一个action,此action会将条件设置为false
     * */
    public IAction getFalseAction(int id){
        IConditionItem item = getItem(id) ;
        Assert.assertTrue(item != null && item instanceof JudgeCondition) ;
        return  new BrainFalseAction((JudgeCondition) item) ;
    }
    /**
     * 获取一个action,此action会将数量条件的数量+1
     * */
    public IAction getPlusAction(int id) {
        IConditionItem item = getItem(id);
        Assert.assertTrue (item != null && item instanceof CounterCondition) ;

        Assert.assertTrue(item != null && item instanceof CounterCondition);
        return new BrainPlusAction((CounterCondition) item);

    }
    /**
     * 获取一个action,此action会将数量条件的数量-1
     * */
    public IAction getSubAction(int id){
        IConditionItem item = getItem(id);
        Assert.assertTrue (item != null && item instanceof CounterCondition) ;

        return new BrainSubAction((CounterCondition) item);

    }
    /**
     * 获取一个action
     * 如果给定的条件是计数器，此action会将计数器条件数量重置为0
     * 如果给定的条件是定时器，此action会将计时器重置
     * */
    public IAction getResetAction(int id){
        IConditionItem item = getItem(id) ;
        Assert.assertTrue (item != null
                    && (item instanceof CounterCondition || item instanceof TimerCondition )
        ) ;
        if(item instanceof CounterCondition){
            return  new BrainResetAction((CounterCondition) item) ;
        }else if(item instanceof TimerCondition){
            return  new BrainResetAction((TimerCondition) item) ;
        }
        //never here maybe.........
        return null ;
    }
    /**
     * 获取一个action,此action会将数量条件的数量重置为指定数量
     * */
    public IAction getResetAction(int id,int count){
        IConditionItem item = getItem(id) ;
        Assert.assertTrue (item != null && item instanceof CounterCondition) ;
        return  new BrainResetAction((CounterCondition) item,count) ;

    }

    public class BrainSwitchAction implements IAction{

        private JudgeCondition mJudgeCondition ;

        public BrainSwitchAction(JudgeCondition judgeCondition){
            mJudgeCondition = judgeCondition ;
        }
        @Override
        public int onAction() {
            mJudgeCondition.setCondition(!mJudgeCondition.getCondition());
            return 0;
        }
    }
    public class BrainTrueAction implements IAction{
        private JudgeCondition mJudgeCondition ;
        public BrainTrueAction(JudgeCondition judgeCondition){
            mJudgeCondition = judgeCondition ;
        }

        @Override
        public int onAction() {
            mJudgeCondition.setCondition(true);
            return 0;
        }
    }

    public class BrainFalseAction implements IAction{
        private JudgeCondition mJudgeCondition ;
        public BrainFalseAction(JudgeCondition judgeCondition){
            mJudgeCondition = judgeCondition ;
        }

        @Override
        public int onAction() {
            mJudgeCondition.setCondition(false);
            return 0;
        }
    }
    public class BrainPlusAction implements IAction{
        private CounterCondition mCountCondtion;

        public BrainPlusAction(CounterCondition countCondtion){
            mCountCondtion = countCondtion ;
        }

        @Override
        public int onAction() {
            mCountCondtion.plus();
            return 0;
        }
    }
    public class BrainSubAction implements IAction{
        private CounterCondition mCountCondtion;

        public BrainSubAction(CounterCondition countCondtion){
            mCountCondtion = countCondtion ;
        }

        @Override
        public int onAction() {
            mCountCondtion.sub();
            return 0;
        }
    }
    public class BrainResetAction implements IAction{

        private CounterCondition mCountCondition;
        private TimerCondition mTimerCondition ;
        private int mCount = 0 ;

        public BrainResetAction(CounterCondition countCondtion, int count){
            mCountCondition = countCondtion ;
            mCount = count ;
        }
        public BrainResetAction(CounterCondition countCondtion){
            this(countCondtion, 0) ;
        }
        public BrainResetAction(TimerCondition timerCondition){
            mTimerCondition = timerCondition ;
        }

        @Override
        public int onAction() {
            if(mCountCondition != null) {
                mCountCondition.reset(mCount);
            }else if(mTimerCondition != null){
                mTimerCondition.reset();
            }
            return 0;
        }
    }
    public static class DateTimer implements IConditionItem{

        private long mScheduleTime ;
        private long  mMaxTimeout ;
        private boolean mScheduled = false ;
        private int mId ;

        public DateTimer(int id, Date date, long maxTimeout){
            mId = id ;
            mScheduleTime = date.getTime() ;
            mMaxTimeout = maxTimeout ;
        }
        public boolean result(Mat screenMat){
            boolean ret = false;
            if(mScheduled){
                ret = false ;
            }else {
                long current = System.currentTimeMillis();
                if (current >= mScheduleTime && (mScheduleTime + mMaxTimeout) > current) {
                    mScheduled = true;
                    ret = true ;
                }
            }
            if(debugList.contains(mId)){
                LtLog.i("condition:"+mId+" "+ret);
            }
            return  ret ;
        }
    }

    public static class TimerCondition implements IConditionItem{
        private long mDelay ;
        private long mPeriod ;
        private long mStartTimestamp ;
        private boolean mFirstScheduled ;
        private  boolean mLoop = false ;
        private int mId ;

        public TimerCondition(int id, long delay, long period){
            this(id,delay) ;
            mPeriod = period ;
            mLoop = true ;
        }
        public TimerCondition(int id,long delay){
            mId = id ;
            mFirstScheduled = false ;
            mDelay = delay ;
            mStartTimestamp = System.currentTimeMillis() ;
        }
        public void reset(){
            mStartTimestamp = System.currentTimeMillis() ;
        }

        @Override
        public boolean result(Mat screenMat) {
            boolean ret = false ;
            long cur = System.currentTimeMillis() ;
            if(!mFirstScheduled ){
                if(cur - mStartTimestamp >= mDelay){
                    ret = true ;
                    mFirstScheduled = true ;
                    mStartTimestamp = cur ;
                }
            }else if(mLoop){
                if (System.currentTimeMillis() - mStartTimestamp >= mPeriod) {
                    ret = true;
                    mStartTimestamp = System.currentTimeMillis();
                }
            }
            if(debugList.contains(mId)){
                LtLog.i("condition:"+mId+" "+ret);
            }
            return ret;
        }
    }

    public static class CounterCondition implements IConditionItem {
        private int mCount = 0 ;
        private int mThreshold ;
        private int mId ;

        public CounterCondition(int id, int threshold){
            mId = id ;
            mThreshold = threshold ;
        }
        public void plus(){
            mCount++ ;
        }
        public void reset(int count){
            mCount = count ;
        }
        public void sub(){
            mCount-- ;
        }
        public int getCount(){
            return mCount ;
        }
        @Override
        public boolean result(Mat screenMat) {
            if(debugList.contains(mId)){
                LtLog.i("condition:"+mId+" "+(mCount >= mThreshold));
            }
            return mCount >= mThreshold;
        }
    }

    public static class JudgeCondition implements IConditionItem {
        private  boolean mCondition ;
        private int mId ;
        private String mKey = null ;

        public JudgeCondition(int id, boolean defaultValue){
            mId = id ;
            mCondition = defaultValue ;
        }
        public JudgeCondition(String key, boolean defaultValue){
            mKey = key ;
            mCondition = defaultValue ;
        }

        public boolean getCondition(){
            return mCondition ;
        }

        @Override
        public boolean result(Mat screenMat) {
            if(mKey == null && debugList.contains(mId)){
                LtLog.i("condition:"+mId+" "+mCondition);
            }
            return mCondition;
        }


        public void setCondition(boolean condition) {
            mCondition = condition ;
        }
    }
}
