package com.zte.monitor.app.handler;

import android.content.Context;
import android.content.Intent;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.model.response.UdpResponse;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 14-9-17.
 */
public class ShortMsgSwithResponseHandler extends ResponseHandler {

    public static final String SHORT_MSG_SWITH_SUCCESS = "com.zte.monitor.app.udp.SHORT_MSG_SWITH_SUCCESS";
    public static final String SHORT_MSG_SWITH_FAILED = "com.zte.monitor.app.udp.SHORT_MSG_SWITH_FAILED";

    public ShortMsgSwithResponseHandler(Context context) {
        super(context);
    }

    @Override
    public void handleMessage(IoBuffer message) {
        UdpResponse response = CodecManager.getManager().shortMsgSwithRspDecode(message);
        if (response.state1 == (byte) 0) {
            mContext.sendBroadcast(new Intent(SHORT_MSG_SWITH_SUCCESS));
        } else {
            mContext.sendBroadcast(new Intent(SHORT_MSG_SWITH_FAILED));
        }
    }
}
