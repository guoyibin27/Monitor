package com.zte.monitor.app.handler;

import android.content.Context;
import android.content.Intent;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.model.response.TaskGetPhNumResponse;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 14-9-16.
 */
public class TaskGetPhNumResponseHandler extends ResponseHandler {
    public static final String TASK_GET_PH_NUM_SUCCESS = "com.zte.monitor.app.udp.TASK_GET_PH_NUM_SUCCESS";
    public static final String TASK_GET_PH_NUM_FAILED = "com.zte.monitor.app.udp.TASK_GET_PH_NUM_FAILED";
    public static final String DATA = "com.zte.monitor.app.udp.TASK_GET_PH_NUM_DATA";

    public TaskGetPhNumResponseHandler(Context context) {
        super(context);
    }

    @Override
    public void handleMessage(IoBuffer message) {
        TaskGetPhNumResponse response = CodecManager.getManager().taskGetPhNumRspDecode(message);
        if (response.state == (byte) 0) {
            Intent intent = new Intent(TASK_GET_PH_NUM_SUCCESS);
            intent.putExtra(DATA, response);
            mContext.sendBroadcast(intent);
        } else {
            Intent intent = new Intent(TASK_GET_PH_NUM_FAILED);
            intent.putExtra(DATA, response);
            mContext.sendBroadcast(intent);
        }
    }
}
