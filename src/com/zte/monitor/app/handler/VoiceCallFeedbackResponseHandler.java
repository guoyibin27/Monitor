package com.zte.monitor.app.handler;

import android.content.Context;
import android.content.Intent;
import com.zte.monitor.app.MonitorApplication;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.model.response.MonitorPcmResponse;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 14/10/24.
 */
public class VoiceCallFeedbackResponseHandler extends ResponseHandler {

    public static final String VOICE_CALL_SUCCESS = "com.zte.monitor.app.udp.MONITOR_PCM_SUCCESS";
    public static final String VOICE_CALL_PCM_DATA = "com.zte.monitor.app.udp.MONITOR_PCM_DATA";

    public VoiceCallFeedbackResponseHandler(Context context) {
        super(context);
    }

    @Override
    public void handleMessage(IoBuffer message) {
        MonitorPcmResponse response = CodecManager.getManager().monitorPcmRspDecode(message);
        if (response.imsi.equals(MonitorApplication.currentPlayMonitorPcmImsi)) {
            Intent intent = new Intent(VOICE_CALL_SUCCESS);
            intent.putExtra(VOICE_CALL_PCM_DATA, response);
            mContext.sendBroadcast(intent);
        }
    }
}
