package com.zte.monitor.app.handler;

import android.content.Context;
import android.content.Intent;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.model.response.UdpResponse;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 14-9-11.
 */
public class TaskConfigResponseHandler extends ResponseHandler {

    public static final String TASK_CFG_SUCCESS = "com.zte.monitor.app.udp.TASK_CFG_SUCCESS";
    public static final String TASK_CFG_FAILED = "com.zte.monitor.app.udp.TASK_CFG_FAILED";

    public TaskConfigResponseHandler(Context context) {
        super(context);
    }

    @Override
    public void handleMessage(IoBuffer message) {
        UdpResponse taskCfgRsp = CodecManager.getManager().taskCfgRspDecode(message);
        if (taskCfgRsp.state == (byte) 0) {
            Intent intent = new Intent(TASK_CFG_SUCCESS);
            mContext.sendBroadcast(intent);
        } else {
            Intent intent = new Intent(TASK_CFG_FAILED);
            if (taskCfgRsp.cause == (byte) 2) {
                intent.putExtra(CAUSE, "初始化成功，但获取号码功能不可用");

            } else {
                intent.putExtra(CAUSE, "基站配置失败");
            }
            mContext.sendBroadcast(intent);
        }
    }
}
