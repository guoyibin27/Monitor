package com.zte.monitor.app.model.response;

import android.content.Context;
import com.zte.monitor.app.handler.ResponseHandler;
import com.zte.monitor.app.model.UserModel;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 14-9-9.
 */
public class SpcInfoResponse extends ResponseHandler {

    public UserModel userModel;

    public SpcInfoResponse(Context context) {
        super(context);
    }

    @Override
    public void handleMessage(IoBuffer message) {

    }
}
