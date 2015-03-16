package com.zte.monitor.app.handler;

import android.content.Context;
import android.content.Intent;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.model.response.TaskRcdListResponse;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 14-9-17.
 */
public class TaskRcdListResponseHandler extends ResponseHandler {
    public static final String TASK_RCD_LIST_SUCCESS = "com.zte.monitor.app.udp.TASK_RCD_LIST_SUCCESS";
    public static final String TASK_RCD_LIST_FAILED = "com.zte.monitor.app.udp.TASK_RCD_LIST_FAILED";
    public static final String DATA = "com.zte.monitor.app.udp.RCD_LIST_DATA";

    public TaskRcdListResponseHandler(Context context) {
        super(context);
    }

    @Override
    public void handleMessage(IoBuffer message) {
        TaskRcdListResponse response = CodecManager.getManager().taskRcdListRspDecode(message);
        Intent intent = new Intent(TASK_RCD_LIST_SUCCESS);
        intent.putExtra(DATA, response);
        mContext.sendBroadcast(intent);
    }
}
