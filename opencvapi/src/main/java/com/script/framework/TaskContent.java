package com.script.framework;

import android.util.Log;

import com.script.opencvapi.FindResult;
import com.script.opencvapi.LtLog;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 2019/4/11.
 */

public abstract class TaskContent {
    private int taskContentNum = 0;
    private boolean taskContentEnd = false;
    public static int Sleep = 1000;
    public static int SleepWhile = 1;
    public int err = 0;
    public Map<String, Integer> picCountMap = new HashMap<>();
    public Map<String, Integer> picCountMapS = new HashMap<>();
    public Map<String, Long> timeKeepMap = new HashMap<>();
    public AtFairyImpl atFairy;
    private static final int MIN_TASK_LOOP_INTERVAL = 500;

    public void setTaskName(int taskContentNum) throws Exception {
        err = 0;
        this.taskContentNum = taskContentNum;
        LtLog.e(logname + ":【切换到--content__" + taskContentNum + "__err:" + err + "】");
    }

    public void setTaskEnd() throws Exception {
        err = 0;
        taskContentEnd = true;
        LtLog.e("40级" + ":【content__" + taskContentNum + "__err:" + err + "】8989 END2");
    }

    public String logname;

    public void taskContent(AtFairyImpl mFairy, String string) throws Exception {
        atFairy = mFairy;
        logname = string;
        LtLog.e(string + ":【content__" + taskContentNum + "__err:" + err + "】999");
        create();
        while (mFairy.condit()) {
            final Thread thread = Thread.currentThread();
            // Log.d("ProcessCenter#Out#", thread.getName() + ": " + thread.isInterrupted());
            if(thread.isInterrupted()){
                Log.d("auto", "CurrentThread is interrupted!");
                break;
            }
            Thread.sleep(MIN_TASK_LOOP_INTERVAL);
            LtLog.e(string + ":【content__" + taskContentNum + "__err:" + err + "】8989");
            if (taskContentEnd) {
                LtLog.e(string + ":【content__" + taskContentNum + "__err:" + err + "】8989 END");
                LtLog.e(string + ":【当前任务结束,End!】");
                break;
            }
            LtLog.e(string + ":【content__" + taskContentNum + "__err:" + err + "】8989 END ***");
            inOperation();
            LtLog.e(string + ":【content__" + taskContentNum + "__err:" + err + "】8989-");
            if (taskContentNum == 0) {
                LtLog.e(string + ":【content__" + taskContentNum + "__err:" + err + "】8989----");
                mFairy.newThreadTimer("new-content_0");
                content_0();
                mFairy.currenThreadTimerDump();
                continue;
            }
            if (taskContentNum == 1) {
                mFairy.newThreadTimer("new-content_1");
                content_1();
                mFairy.currenThreadTimerDump();
                continue;
            }
            if (taskContentNum == 2) {
                mFairy.newThreadTimer("new-content_2");
                content_2();
                mFairy.currenThreadTimerDump();
                continue;
            }
            if (taskContentNum == 3) {
                mFairy.newThreadTimer("new-content_3");
                content_3();
                mFairy.currenThreadTimerDump();
                continue;
            }
            if (taskContentNum == 4) {
                mFairy.newThreadTimer("new-content_4");
                content_4();
                mFairy.currenThreadTimerDump();
                continue;
            }
            if (taskContentNum == 5) {
                mFairy.newThreadTimer("new-content_5");
                content_5();
                mFairy.currenThreadTimerDump();
                continue;
            }
            if (taskContentNum == 6) {
                mFairy.newThreadTimer("new-content_6");
                content_6();
                mFairy.currenThreadTimerDump();
                continue;
            }
            if (taskContentNum == 7) {
                mFairy.newThreadTimer("new-content_7");
                content_7();
                mFairy.currenThreadTimerDump();
                continue;
            }
            if (taskContentNum == 8) {
                mFairy.newThreadTimer("new-content_8");
                content_8();
                mFairy.currenThreadTimerDump();
                continue;
            }
            if (taskContentNum == 9) {
                mFairy.newThreadTimer("new-content_9");
                content_9();
                mFairy.currenThreadTimerDump();
                continue;
            }
            if (taskContentNum == 10) {
                mFairy.newThreadTimer("new-content_10");
                content_10();
                mFairy.currenThreadTimerDump();
                continue;
            }
        }
    }

    /**
     * 只执行一次
     */
    public void create() throws Exception {
    }


    /**
     * 每次while都会执行
     */
    public void inOperation() throws Exception {

    }

    /**
     * 对图片就行计次
     *
     * @param sim
     * @param result
     * @param string
     *         给图片设定一个key
     *
     * @return
     *
     * @throws Exception
     */
    public int picCount(float sim, FindResult result, String string) throws Exception {
        if (picCountMap.containsKey(string)) {
            if (result.sim > sim) {
                int num = picCountMap.get(string);
                num++;
                picCountMap.put(string, num);
            } else {
                picCountMap.put(string, 0);
            }
        } else {
            picCountMap.put(string, 0);
        }
        //  LtLog.e("计次："+string+"="+picCountMap.get(string));
        return picCountMap.get(string);
    }

    public int picCountS(float sim, FindResult result, String string) throws Exception {
        if (picCountMapS.containsKey(string)) {
            if (result.sim < sim) {
                int num = picCountMapS.get(string);
                num++;
                picCountMapS.put(string, num);
            } else {
                picCountMapS.put(string, 0);
            }
        } else {
            picCountMapS.put(string, 0);
        }
        //  LtLog.e("计次："+string+"="+picCountMap.get(string));
        return picCountMapS.get(string);
    }

    /**
     * 计时
     *
     * @param order
     *         1代表第一次就执行
     * @param t
     * @param string
     *         设置一个key
     *
     * @return
     *
     * @throws Exception
     */
    public boolean timekeep(int order, long t, String string) throws Exception {
        if (timeKeepMap.containsKey(string)) {
            if (System.currentTimeMillis() - timeKeepMap.get(string) >= t) {
                timeKeepMap.put(string, System.currentTimeMillis());
                return true;
            }
            // LtLog.e("计时："+string+"="+(System.currentTimeMillis() - timeKeepMap.get(string))+",设置的时间="+t);
        } else {
            timeKeepMap.put(string, System.currentTimeMillis());
            if (order == 1) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * 初始化设定的时间
     *
     * @param string
     *
     * @throws Exception
     */
    public void timekeepInit(String string) throws Exception {
        timeKeepMap.put(string, System.currentTimeMillis());
    }

    /**
     * 超时处理
     */
    public boolean overtime(int num, int taskContentNum) {
        err++;
        if (err >= num) {
            if (taskContentNum == 99) {
                LtLog.e("【异常超时结束当前任务.....】");
                LtLog.e("40级" + ":【content__" + taskContentNum + "__err:" + err + "】8989 END3");
                taskContentEnd = true;
            } else {
                LtLog.e("【异常超时切换到" + taskContentNum + ".....】");
                this.taskContentNum = taskContentNum;
            }
            err = 0;
            return true;
        }
        return false;
    }

    /**
     * 处理时间和次数控件
     *
     * @param string
     *
     * @return
     *
     * @throws Exception
     */
    public ControlSplit strSplit(String string) throws Exception {
        ControlSplit controlSplit = new ControlSplit();
        String[] arrstr = string.split("\\|\\|");
        if (arrstr.length < 2) {
            String[] arrstr1 = arrstr[0].split(":");
            if (arrstr1.length < 2) {
                controlSplit.count = Integer.parseInt(arrstr1[0]);
                return controlSplit;
            }
            controlSplit.h = Integer.parseInt(arrstr1[0]);
            controlSplit.m = Integer.parseInt(arrstr1[1]);
            controlSplit.s = Integer.parseInt(arrstr1[2]);
            controlSplit.timeMillis = controlSplit.h * 3600000 + controlSplit.m * 60000 + controlSplit.s * 1000;
            return controlSplit;
        }
        controlSplit.choice = Integer.parseInt(arrstr[0]);
        String[] arrstr1 = arrstr[1].split(":");
        if (arrstr1.length < 2) {
            controlSplit.count = Integer.parseInt(arrstr1[0]);
        } else {
            controlSplit.h = Integer.parseInt(arrstr1[0]);
            controlSplit.m = Integer.parseInt(arrstr1[1]);
            controlSplit.s = Integer.parseInt(arrstr1[2]);
            controlSplit.timeMillis = controlSplit.h * 3600000 + controlSplit.m * 60000 + controlSplit.s * 1000;
        }
        return controlSplit;
    }

    public void content_0() throws Exception {
    }

    public void content_1() throws Exception {
    }

    public void content_2() throws Exception {
    }

    public void content_3() throws Exception {
    }

    public void content_4() throws Exception {
    }

    public void content_5() throws Exception {
    }

    public void content_6() throws Exception {
    }

    public void content_7() throws Exception {
    }

    public void content_8() throws Exception {
    }

    public void content_9() throws Exception {
    }

    public void content_10() throws Exception {
    }

    public class ControlSplit {
        public int choice;
        public int h;
        public int m;
        public int s;
        public long timeMillis;
        public int count;
    }

}
