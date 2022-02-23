package com.script.content;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daiepngfei on 2020-08-10
 */
public class DTimer {

    private DTMark mHead;
    private DTMark mTail;
    private DTMark min;
    private DTMark max;
    private long total;
    private int size;
    private String fTag;
    private int maxLabelLen = 0;
    private int layer = 0;
    private boolean isEnable;
    private IMarkPrinter mIMarkPrinter = new IMarkPrinter() {
        @Override
        public void print(DTMark marked, String defaultLog) {
            System.out.println(defaultLog);
        }
    };

    public DTimer(String tag) {
        this(tag, null);
    }

    public DTimer() {
        isEnable = true;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public void setTag(String fTag) {
        this.fTag = fTag;
    }

    public DTimer(String tag, IMarkPrinter markPrinter) {
        fTag = tag;
        if (markPrinter != null) {
            this.mIMarkPrinter = markPrinter;
        }
    }

    public void stepIn() {
        synchronized (this) {
            layer++;
        }
    }

    public void stepOut() {
        synchronized (this) {
            layer--;
            layer = Math.max(0, layer);
        }
    }

    public void markBegin(String label) {
        markBegin(label, null);
    }

    public long tailComsumed() {
        return mTail == null ? -1 : mTail.consumed;
    }

    public void markEnd() {
        if (mTail != null && mTail.consumed == 0) {
            mTail.consumed = System.currentTimeMillis() - mTail.mark;
        }
    }

    public void markBegin(String label, String lastTip) {
        synchronized (this) {
            if (size >= 150) {
                reset();
            }
            DTMark mark = new DTMark(label);
            mark.layer = layer;
            if (mHead == null) {
                mHead = mark;
            }
            if (mTail == null) {
                mTail = mHead;
            } else {
                mTail.next = mark;
                final long consumed = mTail.consumed == 0 ? mark.mark - mTail.mark : mTail.consumed;
                mTail.consumed = consumed;


                if (min == null || min.consumed > consumed) {
                    min = mTail;
                }
                if (max == null || max.consumed < consumed) {
                    max = mTail;
                }
                mTail.tip = lastTip;
                mTail = mTail.next;
                total += consumed;
                size++;
            }
            if (label != null && maxLabelLen < label.length()) {
                maxLabelLen = label.length();
            }
        }
    }

    public void reset() {
        size = 0;
        total = 0;
        mHead = mTail = min = max = null;
    }

    private static class DTMark {
        String tip;
        DTMark next;
        private long mark;
        private long consumed;
        private String label;
        private int layer = 0;
        private List<DTMark> children;


        DTMark(String label) {
            this.label = label;
            this.mark = System.currentTimeMillis();
        }

        void addChild(DTMark mark) {
            if (mark != null) {
                if (children == null) {
                    children = new ArrayList<>();
                }
                mark.layer = layer + 1;
                children.add(mark);
            }
        }

    }

    @Override
    public String toString() {
        return "DTimer=> {total:" + total + "; max/min:" + max + "/" + min +
                "} =#= {size:" + size + "; average: " + (total * 1.0D / size) + "}";
    }

    public void printAllMarks() {
        if(!isEnable){
            return;
        }
        final String sfTag = fTag == null ? "未命名timer" : fTag;
        final String printTag = "printAllMarks[" + sfTag + "]";
        System.out.println(printTag + "::");
        System.out.println(printTag + ">>>>>>>>>>>>>>>>::" + (fTag == null ? "未命名timer" : fTag) + "::>>>>>>>>>>>>>>>>");
        System.out.println(printTag + ":: thread#" + Thread.currentThread().getName() + "| this#" + this.hashCode());
        DTMark cursor = mHead;
        if (cursor == null) {
            System.err.println(printTag + "::" + (fTag == null ? "null异常cusor是空timer" : fTag));
            return;
        }
        while (true) {
            DTMark next = cursor.next;
            if (next == null) {
                if (mIMarkPrinter != null) {
                    mIMarkPrinter.print(cursor, toString());
                }
                break;
            }
            if (mIMarkPrinter != null) {
                StringBuilder sb = new StringBuilder(cursor.label == null ? "" : cursor.label);
                final int max = maxLabelLen - sb.length();
                sb.append("_");
                for (int i = 0; i < max; i++) {
                    sb.append("$");
                }

                StringBuilder sb1 = new StringBuilder("");
                if (cursor.layer > 0) {
                    for (int i = 0; i < cursor.layer; i++) {
                        sb1.append("____");
                    }
                }
                mIMarkPrinter.print(cursor, printTag + "(label:" + (sb.toString()) + ")>>"
                        + sb1.toString()
                        + "markBegin-at: " + cursor.mark
                        + "/"
                        + "consumed: " + cursor.consumed
                        + "; "
                        + (cursor.tip == null ? "" : "tip - : " + cursor.tip));

            }
            cursor = next;
        }
        if (mIMarkPrinter != null) {
            System.out.println(printTag + "|------------------------------------------------------------------------------|");
            //System.out.println(printTag + "|                                                                              |");
            mIMarkPrinter.print(cursor, printTag + "| count=" + size + "; fun-total=" + total
                    + ((mTail != null && mHead != null) ? "; summon= " + (mTail.mark - mHead.mark) : ""));
            if (min != null) {
                //System.out.println(printTag + "|                                                                              |");
                mIMarkPrinter.print(cursor, printTag + "| fastest[" + getMarkLabel(min) + "]: " + min.consumed);
            }
            if (max != null) {
                //System.out.println(printTag + "|                                                                              |");
                mIMarkPrinter.print(cursor, printTag + "| slowest[" + getMarkLabel(max) + "]: " + max.consumed);
            }
            //System.out.println(printTag + "|                                                                              |");
            System.out.println(printTag + "|------------------------------------------------------------------------------|");
        }

        System.out.println(printTag + "<<<<<<<<<<<<<<<<::" + fTag + "::<<<<<<<<<<<<<<<<\n");
        System.out.println(printTag + "");
    }

    private String getMarkLabel(DTMark max) {
        return max == null || max.label == null ? "?" : max.label;
    }

    public interface IMarkPrinter {
        void print(DTMark marked, String defaultLog);
    }

    public static class ThreadLocalManager {
        private static ThreadLocal<DTimer> timerThreadLocal = new ThreadLocal<>();
        private static ThreadLocal<Boolean> timerThreadLocalEnable = new ThreadLocal<>();
        public static void newTimer(String tag) {
            currentTimer(true).setTag(tag);
        }

        public static DTimer currentTimer() {
            return currentTimer(false);
        }

        private static DTimer currentTimer(boolean clear) {
            if (clear) {
                timerThreadLocal.remove();
            }
            DTimer dTimer = timerThreadLocal.get();
            if (dTimer == null) {
                dTimer = new DTimer();
                Boolean enable = timerThreadLocalEnable.get();
                dTimer.setEnable(enable != null && enable);
                timerThreadLocal.set(dTimer);
            }
            return dTimer;
        }

        public static void dumpCurrent() {
            currentTimer(false).printAllMarks();
        }

        public static void setTimerThreadLocalEnable(boolean enable){
            timerThreadLocalEnable.set(enable);
            DTimer dTimer = timerThreadLocal.get();
            if(dTimer != null){
                dTimer.setEnable(enable);
            }
        }
    }


}

