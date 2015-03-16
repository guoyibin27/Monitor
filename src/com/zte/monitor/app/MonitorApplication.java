package com.zte.monitor.app;

import android.app.Application;

import java.util.Random;

/**
 * Created by Sylar on 8/25/14.
 */
public class MonitorApplication extends Application {

    public static String currentPlayMonitorPcmImsi;
    public static byte clientId;
    public static final byte heartBeatAction = (byte) new Random().nextInt(100);

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
