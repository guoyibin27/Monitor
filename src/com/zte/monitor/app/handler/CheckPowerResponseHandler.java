package com.zte.monitor.app.handler;

import android.content.Context;
import android.content.Intent;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.model.response.UdpResponse;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 14-9-17.
 */
public class CheckPowerResponseHandler extends ResponseHandler {
    public static final String CHECK_POWER_SUCCESS = "com.zte.monitor.app.udp.CHECK_POWER_SUCCESS";
    public static final String CHECK_POWER_FAILED = "com.zte.monitor.app.udp.CHECK_POWER_FAILED";

    public CheckPowerResponseHandler(Context context) {
        super(context);
    }

    @Override
    public void handleMessage(IoBuffer message) {
        UdpResponse response = CodecManager.getManager().checkPowerRspDecode(message);
        if (response.state == (byte) 0) {
            mContext.sendBroadcast(new Intent(CHECK_POWER_SUCCESS));
        } else {
            mContext.sendBroadcast(new Intent(CHECK_POWER_FAILED));
        }
    }
}
