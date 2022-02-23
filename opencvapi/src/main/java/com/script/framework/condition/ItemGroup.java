package com.script.framework.condition;

import org.opencv.core.Mat;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;



public class ItemGroup implements IConditionItem {

    private Map<IConditionItem, Integer> mItemList;

    public ItemGroup(){
        mItemList = new LinkedHashMap<>() ;
    }

    public ItemGroup addItem(IConditionItem item, int relation){
        mItemList.put(item, relation);
        return this ;
    }

    public ItemGroup andItem(IConditionItem item){
        mItemList.put(item, Condition.FLAG_AND);

        return  this;
    }
    public ItemGroup andNotItem(IConditionItem item){
        mItemList.put(item, Condition.FLAG_AND | Condition.FLAG_NOT);

        return  this;
    }
    public ItemGroup orItem(IConditionItem item){
        mItemList.put(item, Condition.FLAG_OR);

        return this ;
    }
    public ItemGroup orNotItem(IConditionItem item){
        mItemList.put(item, Condition.FLAG_OR | Condition.FLAG_NOT);

        return  this ;
    }

    public void reset(){
        Iterator<Map.Entry<IConditionItem, Integer>> iter = mItemList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<IConditionItem, Integer> entry = iter.next();
            if(entry.getKey() instanceof ScreenItem){
                ((ScreenItem) entry.getKey()).reset();
            }
        }
    }

    private  boolean getItemResult(Mat screenMat, IConditionItem item , int flag){

        boolean result = item.result(screenMat) ;
        if ((flag & Condition.FLAG_NOT) == Condition.FLAG_NOT){
            result = !result ;
        }
        return  result ;
    }

    @Override
    public boolean result(Mat screenMat) {
        boolean ret = true;

        Iterator<Map.Entry<IConditionItem, Integer>> iter = mItemList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<IConditionItem, Integer> entry = iter.next();
            int flag = entry.getValue() & 0x0f ;
            if(ret && flag == Condition.FLAG_OR){
                continue;
            }
            if(!ret && flag == Condition.FLAG_AND){
                continue;
            }
            IConditionItem item = entry.getKey();
            boolean result = getItemResult(screenMat, item, entry.getValue());
            switch (flag) {
                case Condition.FLAG_AND:
                    ret &= result;
                    break;
                case Condition.FLAG_OR:
                    ret |= result;
                    break;
                default:
                    ret &= result;
                    break;
            }
        }
        return  ret ;
    }
}
