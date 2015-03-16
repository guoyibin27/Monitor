package com.zte.monitor.app.handler;

import android.content.Context;
import android.content.Intent;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.model.response.UdpResponse;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 14-9-14.
 * 添加用户响应处理类
 */
public class AddUserResponseHandler extends ResponseHandler {

    public static final String ADD_USER_SUCCESS = "com.zte.monitor.app.udp.ADD_USER_SUCCESS";
    public static final String ADD_USER_FAILED = "com.zte.monitor.app.udp.ADD_USER_FAILED";
    public static final String ADD_USER_DATA = "com.zte.monitor.app.udp.ADD_USER_DATA";

    public AddUserResponseHandler(Context context) {
        super(context);
    }

    @Override
    public void handleMessage(IoBuffer message) {
        if (message != null) {
            UdpResponse response = CodecManager.getManager().spcUserRspDecode(message);
            if (response.state == (byte) 0) {
                Intent intent = new Intent(ADD_USER_SUCCESS);
                intent.putExtra(ADD_USER_DATA, response);
                mContext.sendBroadcast(intent);
            } else {
                if (response.cause == (byte) 2) {
                    Intent intent = new Intent(ADD_USER_SUCCESS);
                    intent.putExtra(ADD_USER_DATA, response);
                    mContext.sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(ADD_USER_FAILED);
                    intent.putExtra(ADD_USER_DATA, response);
                    mContext.sendBroadcast(intent);
                }
            }
        } else {
            Intent intent = new Intent(ADD_USER_FAILED);
            mContext.sendBroadcast(intent);
        }
    }
}
