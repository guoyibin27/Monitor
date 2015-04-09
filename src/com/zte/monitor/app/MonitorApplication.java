package com.zte.monitor.app;

import android.app.Application;
import com.zte.monitor.app.pcm.PlayService;

import java.util.Random;

/**
 * Created by Sylar on 8/25/14.
 */
public class MonitorApplication extends Application {

    public static String currentPlayMonitorPcmImsi;//实时语音,录音
    public static String playImsi;//监听时播放用户的IMSI
    public static byte clientId;
    public static final byte heartBeatAction = (byte) new Random().nextInt(100);
    public static int playerStatus = PlayService.PlayStatus.stop;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
