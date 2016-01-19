package com.kvajpoj.service;

/**
 * Created by Andrej on 29.10.2015.
 */
public class ServiceConstants {

    public interface ACTION {
        public static String START_FOREGROUND_ACTION = "com.kvajpoj.service.action.startforeground";
        public static String STOP_FOREGROUND_ACTION = "com.kvajpoj.service.action.stopforeground";
        public static String SILENT_ACTION = "com.kvajpoj.service.action.silent";
        public static String LOUD_ACTION = "com.kvajpoj.service.action.loud";
        public static String HIDE_ACTION = "com.kvajpoj.service.action.hide";
        public static String SHOW_ACTION = "com.kvajpoj.service.action.show";


    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}