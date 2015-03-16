package com.zte.monitor.app.handler;

import android.content.Context;
import android.content.Intent;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.model.response.ScanFreqResponse;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 14-9-13.
 */
public class ScanFreqResponseHandler extends ResponseHandler {

    public static final String SCAN_SUCCESS = "com.zte.monitor.app.udp.SCAN_FREQ_SUCCESS";
    public static final String SCAN_FAILED = "com.zte.monitor.app.udp.SCAN_FREQ_FAILED";
    public static final String DATA = "com.zte.monitor.zpp.udp.DATA";

    public ScanFreqResponseHandler(Context context) {
        super(context);
    }

    @Override
    public void handleMessage(IoBuffer message) {
        ScanFreqResponse response = CodecManager.getManager().scanFreqRspDecode(message);
        if (response.state == (byte) 0) {
            Intent intent = new Intent(SCAN_SUCCESS);
            intent.putExtra(DATA, response);
            mContext.sendBroadcast(intent);
        } else {
            Intent intent = new Intent(SCAN_FAILED);
            intent.putExtra(DATA, response);
            mContext.sendBroadcast(intent);
        }
    }
}
