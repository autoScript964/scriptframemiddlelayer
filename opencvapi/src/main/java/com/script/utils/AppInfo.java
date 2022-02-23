package com.script.utils;

public class  AppInfo {
        public String gameId ;
        public String channelId ;
        public String packagePath ;
        public String packageName ;
        public String mainActivity ;
        public int version ;

        public AppInfo(String packageName, String activityName) {
                this.packageName = packageName ;
                this.mainActivity = activityName ;
        }
        public AppInfo(){} ;
}