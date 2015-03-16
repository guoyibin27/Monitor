package com.zte.monitor.app.handler;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.model.response.MonitorResponse;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 14-9-17.
 */
public class MonitorResponseHandler extends ResponseHandler {

    public static final String MONITOR_SUCCESS = "com.zte.monitor.app.udp.MONITOR_SUCCESS";
    public static final String MONITOR_FAILED = "com.zte.monitor.app.udp.MONITOR_FAILED";
    public static final String MONITOR_DATA = "com.zte.monitor.app.udp.MONITOR_DATA";

    public MonitorResponseHandler(Context context) {
        super(context);
    }

    @Override
    public void handleMessage(IoBuffer message) {
        MonitorResponse response = CodecManager.getManager().monitorRspDecode(message);
        if (response.state == (byte) 0) {
            Intent intent = new Intent(MONITOR_SUCCESS);
            intent.putExtra(MONITOR_DATA, response);
            mContext.sendBroadcast(intent);
        } else {
            Log.e("TAG", response.cause + "");
            if (response.cause == (byte) 2) {
                Intent intent = new Intent(MONITOR_SUCCESS);
                intent.putExtra(MONITOR_DATA, response);
                mContext.sendBroadcast(intent);
            } else {
                mContext.sendBroadcast(new Intent(MONITOR_FAILED));
            }
        }
    }
}
