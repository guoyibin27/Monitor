package com.zte.monitor.app.handler;

import android.content.Context;
import android.content.Intent;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.model.response.LocationResponse;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 14-9-17.
 */
public class LocationResponseHandler extends ResponseHandler {
    public static final String LOCATION_SUCCESS = "com.zte.monitor.app.udp.LOCATION_SUCCESS";
    public static final String LOCATION_FAILED = "com.zte.monitor.app.udp.LOCATION_FAILED";

    public LocationResponseHandler(Context context) {
        super(context);
    }

    @Override
    public void handleMessage(IoBuffer message) {
        LocationResponse response = CodecManager.getManager().locationRspDecode(message);
        if ((byte) 0 == response.state) {
            mContext.sendBroadcast(new Intent(LOCATION_SUCCESS));
        } else {
            mContext.sendBroadcast(new Intent(LOCATION_FAILED));
        }
    }
}
