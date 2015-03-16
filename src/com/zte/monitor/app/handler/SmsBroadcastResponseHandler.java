package com.zte.monitor.app.handler;

import android.content.Context;
import android.content.Intent;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.model.response.UdpResponse;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 14-9-16.
 */
public class SmsBroadcastResponseHandler extends ResponseHandler {

    public static final String SMS_BROADCAST_SUCCESS = "com.zte.monitor.app.udp.SMS_BROADCAST_SUCCESS";
    public static final String SMS_BROADCAST_FAILED = "com.zte.monitor.app.udp.SMS_BROADCAST_FAILED";

    public SmsBroadcastResponseHandler(Context context) {
        super(context);
    }

    @Override
    public void handleMessage(IoBuffer message) {
        UdpResponse response = CodecManager.getManager().smsBroadcastRspDecode(message);
        if (response.state == (byte) 0) {
            mContext.sendBroadcast(new Intent(SMS_BROADCAST_SUCCESS));
        } else {
            Intent intent = new Intent(SMS_BROADCAST_FAILED);
            if (response.cause == (byte) -1) {
                intent.putExtra(CAUSE, "IMSI参数错误");
            } else if (response.cause == (byte) -2) {
                intent.putExtra(CAUSE, "非法电话号码");
            } else if (response.cause == (byte) -3) {
                intent.putExtra(CAUSE, "发送内容长度超过140字节");
            } else if (response.cause == (byte) 2) {
                intent.putExtra(CAUSE, "非代理状态，不允许使用数据卡发送短信");
            } else if (response.cause == (byte) 0) {
                intent.putExtra(CAUSE, "发送命令下发失败");
            }
            mContext.sendBroadcast(intent);
        }
    }
}
