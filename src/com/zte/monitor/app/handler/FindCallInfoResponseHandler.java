package com.zte.monitor.app.handler;

import android.content.Context;
import android.content.Intent;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.model.response.CallInfoResponse;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 15/4/9.
 */
public class FindCallInfoResponseHandler extends ResponseHandler {
    public static final String FIND_CALL_INFO_SUCCESS = "com.zte.monitor.app.udp.FIND_CALL_INFO_SUCCESS";
    public static final String FIND_CALL_INFO_FAILED = "com.zte.monitor.app.udp.FIND_CALL_INFO_FAILED";
    public static final String FIND_CALL_INFO_DATA = "com.zte.monitor.app.udp.FIND_CALL_INFO_DATA";

    public FindCallInfoResponseHandler(Context context) {
        super(context);
    }


    @Override
    public void handleMessage(IoBuffer message) {
        CallInfoResponse response = CodecManager.getManager().findCallInfoRspDecode(message);
        if (response.state == (byte) 0) {
            Intent intent = new Intent(FIND_CALL_INFO_SUCCESS);
            intent.putExtra(FIND_CALL_INFO_DATA, response);
            mContext.sendBroadcast(intent);
        } else {
            Intent intent = new Intent(FIND_CALL_INFO_FAILED);
            intent.putExtra(FIND_CALL_INFO_DATA, response);
            mContext.sendBroadcast(intent);
        }
    }
}
