package com.zte.monitor.app.util;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;

/**
 * Created by Sylar on 14-10-8.
 * 手机震动工具类
 */
public class VibratorUtil {

    public static void vibrate(final Context context, long milliseconds) {
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }

    public static void vibrate(final Context context, long[] pattern, boolean isRepeat) {
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(pattern, isRepeat ? 1 : -1);
    }
}
