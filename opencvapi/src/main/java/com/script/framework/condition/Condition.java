package com.script.framework.condition;

import com.script.framework.FairyRect;
import com.script.framework.action.ClickableImage;
import com.script.framework.action.IAction;
import com.script.framework.action.TapAction;

import org.opencv.core.Mat;

import java.util.*;

public class Condition {


    public static final int FLAG_AND = 0x01;
    public static final int FLAG_OR = 0x02;
    public static final int FLAG_NOT = 0x10 ;

    private long mLastConfirmedTime;
    private int mId;
//    private Map<IConditionItem, Integer> mItemList;
    private ItemGroup mItemGroup ;
    private List<IAction> mActionList;
    private List<IAction> mFalseActionList ;
    private boolean mBreak = true;


    public Condition() {
        mActionList = new ArrayList<>();
        mFalseActionList = new ArrayList<>();
        mItemGroup = new ItemGroup() ;

    }

    public Condition(int id) {
        this() ;
        mId = id;
    }

    public Condition(String image) {
        this() ;
        andItem(new ImageCondition(image));
    }

    public Condition(ClickableImage clickableImage) {
        this() ;
        andItem(clickableImage);
        addAction(clickableImage);
    }


    /**
     * 创建一个可以点击的图片条件
     * */
    public static Condition createClickableCondition(String name) {
        ClickableImage clickableImage = new ClickableImage(name);
        return new Condition(clickableImage);
    }

    /**
     * 创建一个可以点击的图片条件
     * */
    public static Condition createClickableCondition(String name, FairyRect rect) {
        ClickableImage clickableImage = new ClickableImage(name, rect);
        return new Condition(clickableImage);
    }


    /**
     * 设置条件id
     * */
    public Condition setId(int id) {
        mId = id;
        return this;
    }

    public int getId() {
        return mId;
    }


    /**
     * 设置此添加是否打断场景循环
     *
     * @param breakNext 打断循环，如果Condition为true则不再继续往下判断
     */
    private Condition setBreak(boolean breakNext) {
        mBreak = breakNext;
        return this;
    }

    public ItemGroup getItemGroup(){
        return mItemGroup ;
    }

    /**
     * 此条件是否打断循环
     * */
    public boolean isBreak(){
        return mBreak ;
    }

    public Condition addItem(IConditionItem item , int relation){
        mItemGroup.addItem(item, relation) ;
        return this ;
    }


    /**
     *  添加一项条件，此条件与之前条件为并且关系
     * */
    public Condition andItem(IConditionItem item) {
        mItemGroup.andItem(item) ;
        return this;
    }
    /**
     *  添加一项条件，此条件取反后与之前条件为并且关系
     * */
    public Condition andNotItem(IConditionItem item){
        mItemGroup.andNotItem(item) ;
        return this ;
    }

    /**
     *  添加一项可点击的图片条件，此条件与之前条件为并且关系
     * */
    public Condition andClickableItem(ClickableImage clickableImage) {
        andItem(clickableImage);
        addAction(clickableImage);
        return this;
    }

    /**
     *  添加一项条件，此条件与之前条件为或者关系
     */
    public Condition orItem(IConditionItem item) {
        mItemGroup.orItem(item) ;
        return this;
    }
    /**
     *  添加一项条件，此条件取反后与之前条件为或者关系
     */
    public Condition orNotItem(IConditionItem item){
        mItemGroup.orNotItem(item) ;
        return  this ;
    }


    /**
     * 添加一个action
     * */
    public Condition addAction(IAction action) {
        mActionList.add(action);
        return this;
    }
    /**
     * 添加一个可点击的图片
     * */
    public Condition addAction(String img){
        mActionList.add(new ClickableImage(img));
        return this ;
    }
    public Condition addAction(String img, FairyRect rect){
        mActionList.add(new ClickableImage(img,rect));
        return this ;
    }
    public Condition addAction(FairyRect rect){
        mActionList.add(new TapAction(rect)) ;
        return this ;
    }
    /**
     * 添加一个action，在条件不成立时执行
     * */
    public Condition addFlaseAction(IAction action){
        mFalseActionList.add(action) ;
        return this ;
    }
    public void runAction(Mat screenMat)throws Exception{
        runActions(screenMat, mActionList);
    }
    public void runFalseAction(Mat screenMat)throws Exception{
        runActions(screenMat, mFalseActionList);
    }
    private void runActions(Mat screenMat, List<IAction> actions)throws Exception{
        for (IAction action : actions) {
            if (action instanceof ScreenItem) {
                ScreenItem item = (ScreenItem) action;
                item.looping(screenMat);
            }
            int delay = action.onAction();
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public long getConfirmedTime(){
        return mLastConfirmedTime ;
    }

    public void reset(){
        mItemGroup.reset() ;
    }

    public boolean looping(Mat screenMat) {
        boolean ret = mItemGroup.result(screenMat) ;
        if (ret) {
            if (mLastConfirmedTime == 0) {
                mLastConfirmedTime = System.currentTimeMillis();
            }
        } else {
            mLastConfirmedTime = 0;
        }
        return ret;
    }


}
