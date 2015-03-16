package com.zte.monitor.app.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.widget.RemoteViews;
import com.zte.monitor.app.R;

/**
 * Created by Sylar on 14/10/29.
 */
public class NotificationUtils {
    private Context mContext;
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private final static int NOTIFICATION_ID = 1000;

    private static NotificationUtils instance = null;

    private NotificationUtils(Context context) {
        this.mContext = context;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static NotificationUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (NotificationUtils.class) {
                if (instance == null)
                    instance = new NotificationUtils(context);
            }
        }
        return instance;
    }

    public void createCustomNotification() {
        if (mNotification != null) return;
        mNotification = new Notification();
        mNotification.contentView = new RemoteViews(mContext.getPackageName(), R.layout.custom_notification_layout);
        mNotification.icon = R.drawable.logo;
        mNotification.flags = Notification.FLAG_NO_CLEAR;
        mNotification.contentView.setTextViewText(R.id.gsm_count, "0");
        mNotification.contentView.setTextViewText(R.id.cdma_count, "0");
        mNotification.contentView.setTextViewText(R.id.wcdma_count, "0");
        mNotification.contentView.setTextViewText(R.id.td_count, "0");
        mNotification.contentView.setTextViewText(R.id.lte_count, "0");
        mNotification.contentView.setTextViewText(R.id.wifi_count, "0");
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    public void updateCustomNotification(long gsmCount, long cdmaCount, long wcdmaCount, long tdCount, long lteCount, long wifiCount) {
        mNotification.contentView.setTextViewText(R.id.gsm_count, gsmCount + "");
        mNotification.contentView.setTextViewText(R.id.cdma_count, cdmaCount + "");
        mNotification.contentView.setTextViewText(R.id.wcdma_count, wcdmaCount + "");
        mNotification.contentView.setTextViewText(R.id.td_count, tdCount + "");
        mNotification.contentView.setTextViewText(R.id.lte_count, lteCount + "");
        mNotification.contentView.setTextViewText(R.id.wifi_count, wifiCount + "");
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    public void cancelCustomNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }
}
