package com.zte.monitor.app.handler;

import android.content.Context;
import android.content.Intent;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.model.response.UdpResponse;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 14-9-13.
 */
public class ConfigParamResponseHandler extends ResponseHandler {
    public static final String CONF_PARAM_SUCCESS = "com.zte.monitor.app.udp.CONF_PARAM_SUCCESS";
    public static final String CONF_PARAM_FAILED = "com.zte.monitor.app.udp.CONF_PARAM_FAILED";
    public static final String DATA = "com.zte.monitor.zpp.udp.DATA";

    public ConfigParamResponseHandler(Context context) {
        super(context);
    }


    @Override
    public void handleMessage(IoBuffer message) {
        UdpResponse response = CodecManager.getManager().confParamRspDecode(message);
        if (response.state == (byte) 0 && response.state1 == (byte) 0) {
            Intent intent = new Intent(CONF_PARAM_SUCCESS);
            mContext.sendBroadcast(intent);
        } else {
            Intent intent = new Intent(CONF_PARAM_FAILED);
            intent.putExtra(CAUSE, "参数配置失败");
            mContext.sendBroadcast(intent);
        }
    }
}
