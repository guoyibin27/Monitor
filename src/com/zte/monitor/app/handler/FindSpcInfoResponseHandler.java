package com.zte.monitor.app.handler;

import android.content.Context;
import android.content.Intent;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.database.dao.UserDao;
import com.zte.monitor.app.model.response.UserResponse;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 14-9-16.
 */
public class FindSpcInfoResponseHandler extends ResponseHandler {
    public static final String FIND_SPC_INFO_SUCCESS = "com.zte.monitor.app.udp.FIND_SPC_INFO_SUCCESS";
    public static final String FIND_SPC_INFO_FAILED = "com.zte.monitor.app.udp.FIND_SPC_INFO_FAILED";
    public static final String FIND_SPC_INFO_DATA = "com.zte.monitor.app.udp.FIND_SPC_INFO_DATA";

    private UserDao userDao;

    public FindSpcInfoResponseHandler(Context context) {
        super(context);
        userDao = new UserDao(context);
    }

    @Override
    public void handleMessage(IoBuffer message) {
        UserResponse response = CodecManager.getManager().findSpcInfoRspDecode(message);
        if (response.state == (byte) 0) {
            Intent intent = new Intent(FIND_SPC_INFO_SUCCESS);
            intent.putExtra(FIND_SPC_INFO_DATA, response);
            mContext.sendBroadcast(intent);
        } else {
            Intent intent = new Intent(FIND_SPC_INFO_FAILED);
            intent.putExtra(FIND_SPC_INFO_DATA, response);
            mContext.sendBroadcast(intent);
        }
    }
}
