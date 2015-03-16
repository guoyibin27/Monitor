package com.zte.monitor.app.handler;

import android.content.Context;
import android.content.Intent;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.model.response.PlayRecordResponse;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 14-9-17.
 */
public class PlayRecordResponseHandler extends ResponseHandler {

    public static final String PLAY_RECORD_SUCCESS = "com.zte.monitor.app.udp.PLAY_RECORD_SUCCESS";
    public static final String PLAY_RECORD_FAILED = "com.zte.monitor.app.udp.PLAY_RECORD_FAILED";
    public static final String DATA = "com.zte.monitor.app.udp.PLAY_RECORD_DATA";

    public PlayRecordResponseHandler(Context context) {
        super(context);
    }

    @Override
    public void handleMessage(IoBuffer message) {
        PlayRecordResponse response = CodecManager.getManager().playRecordRspDecode(message);
        if (response.state == (byte) 0) {
            Intent intent = new Intent(PLAY_RECORD_SUCCESS);
            intent.putExtra(DATA, response);
            mContext.sendBroadcast(intent);
        } else {
            mContext.sendBroadcast(new Intent(PLAY_RECORD_FAILED));
        }
    }
}
