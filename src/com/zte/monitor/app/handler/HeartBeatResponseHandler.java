package com.zte.monitor.app.handler;

import android.content.Context;
import com.zte.monitor.app.MonitorApplication;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.database.dao.BtsStateDao;
import com.zte.monitor.app.model.StateModel;
import com.zte.monitor.app.model.response.UdpResponse;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 15/2/2.
 */
public class HeartBeatResponseHandler extends ResponseHandler {
    private BtsStateDao btsStateDao;
    private boolean isConnected = false;

    public HeartBeatResponseHandler(Context context) {
        super(context);
        btsStateDao = new BtsStateDao(context);
    }

    @Override
    public void handleMessage(IoBuffer message) {
        UdpResponse response = CodecManager.getManager().newHeartBeatRspDecode(message);
        //判断是否能收到响应
        if (response != null) {
            isConnected = true;
        } else {
            isConnected = false;
        }

        StateModel stateModel = btsStateDao.get();
        if (stateModel == null) {
            stateModel = new StateModel();
        }
        if (response.action == MonitorApplication.heartBeatAction) {
            stateModel.connState = SystemConstants.CONN_STATUS.CONNECTED;
        } else {
            stateModel.connState = SystemConstants.CONN_STATUS.DISCONNECT;
        }
        btsStateDao.saveOrUpdate(stateModel);
    }

    public boolean isReceiveHeartBeatRsp() {
        boolean temp = isConnected;
        isConnected = false;
        return temp;
    }
}
