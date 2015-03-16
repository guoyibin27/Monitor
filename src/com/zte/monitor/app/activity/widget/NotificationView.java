package com.zte.monitor.app.activity.widget;

import android.widget.RemoteViews;

import java.lang.annotation.Annotation;

/**
 * Created by Sylar on 14-9-9.
 */
public class NotificationView implements RemoteViews.RemoteView {
    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}
