package com.zte.monitor.app.handler;

import android.content.Context;
import android.content.Intent;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.model.response.UdpResponse;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 14-9-16.
 */
public class PcmBroadcastResponseHandler extends ResponseHandler {

    public static final String PCM_BROADCAST_SUCCESS = "com.zte.monitor.app.udp.PCM_BROADCAST_SUCCESS";
    public static final String PCM_BROADCAST_FAILED = "com.zte.monitor.app.udp.PCM_BROADCAST_FAILED";
    public static final String PCM_BROADCAST_DATA = "com.zte.monitor.app.udp.PCM_BROADCAST_DATA";

    public PcmBroadcastResponseHandler(Context context) {
        super(context);
    }

    @Override
    public void handleMessage(IoBuffer message) {
        UdpResponse response = CodecManager.getManager().pcmBroadcastRspDecode(message);
        if (response.state == (byte) 0) {
            Intent intent = new Intent(PCM_BROADCAST_SUCCESS);
            intent.putExtra(PCM_BROADCAST_DATA, response);
            mContext.sendBroadcast(intent);
        } else {
            Intent intent = new Intent(PCM_BROADCAST_FAILED);
            intent.putExtra(PCM_BROADCAST_DATA, response);
            mContext.sendBroadcast(intent);
        }
    }
}
