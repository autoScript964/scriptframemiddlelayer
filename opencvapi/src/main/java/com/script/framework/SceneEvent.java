package com.script.framework;

public interface SceneEvent{
        /**
         * 进入到场景
         * @param flag  场景标示
         * */
        void onSceneIn(int flag,String name) ;

        /**
         * 触发场景条件
         * @param sceneFlag 场景标识
         * @param name 场景名称
         * @param conditionid 条件id
         * @param time 条件持续时间
         * @return  返回此条件是否打断当前循环
         * */
        boolean onSceneCondtion(int sceneFlag, String name, int conditionid, long time) ;
        /**
         * 离开场景
         * @param flag  场景标示
         */
        void onSceneOut(int flag, String name) ;

        void onSceneTimeout(int flag, String name) ;

        void onSceneNoConditionTimeout(int flag, String name) ;
    }