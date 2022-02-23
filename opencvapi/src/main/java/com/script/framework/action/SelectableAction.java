package com.script.framework.action;

import com.script.framework.condition.Brain;

import java.util.ArrayList;
import java.util.List;

/**
 * 多个选择的Action
 * */
public class SelectableAction implements IAction {

    private List<IAction> mActionList ;
    private Brain.CounterCondition mCounterContion ;

    /**
     * 构造一个选择Action
     * @param counterCondition  计数器
     * */
    public SelectableAction(Brain.CounterCondition counterCondition){
        mActionList = new ArrayList<>() ;
        mCounterContion = counterCondition ;
    }

    /**
     * 添加一个action
     * 此Action会根据计数器返回的数值执行指定的action
     * */
    public SelectableAction addAction(IAction action){
        mActionList.add(action) ;
        return  this ;
    }

    @Override
    public int onAction(){
        return mActionList.get(mCounterContion.getCount()).onAction();
    }
}
