package com.zte.monitor.app.handler;

import android.content.Context;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 14-9-11.
 * 响应处理父类。 所有响应处理类必须继承此类，并重写handleMessage方法。再将处理类添加进ResponseHandlerChain
 */
public abstract class ResponseHandler {
    public static final String CAUSE = "CAUSE";
    protected Context mContext;

    public ResponseHandler(Context context) {
        mContext = context;
    }

    public abstract void handleMessage(IoBuffer message);
}
