package com.zte.monitor.app.udp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.zte.monitor.app.MonitorApplication;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.database.dao.BtsStateDao;
import com.zte.monitor.app.model.StateModel;
import com.zte.monitor.app.model.response.HeartBeatResponse;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;

/**
 * Created by Sylar on 14-9-11.
 */
public class HeartBeatFilter extends KeepAliveFilter {

    private static final int INTERVAL = 30; //in seconds
    private static final int TIMEOUT = 10;  //in seconds


    public HeartBeatFilter(Context context) {
        super(new KeepAliveMessageFactoryImpl(context), IdleStatus.BOTH_IDLE, new HeartBeatExceptionHandler(context), INTERVAL, TIMEOUT);
        this.setForwardEvent(false);  //此消息不会继续传递，不会被业务层看见
    }

}


class HeartBeatExceptionHandler implements KeepAliveRequestTimeoutHandler {
    private Context mContext;
    private BtsStateDao btsStateDao;

    HeartBeatExceptionHandler(Context context) {
        mContext = context;
        btsStateDao = new BtsStateDao(context);
    }

    public void keepAliveRequestTimedOut(KeepAliveFilter filter, IoSession session) throws Exception {
        Log.e("TAG", "Connection lost, session will be closed");
        session.close(true);
        StateModel stateModel = btsStateDao.get();
        if (stateModel == null) {
            stateModel = new StateModel();
        }
        stateModel.connState = SystemConstants.CONN_STATUS.DISCONNECT;
        btsStateDao.saveOrUpdate(stateModel);
        mContext.sendBroadcast(new Intent("com.zte.monitor.action.ACTION_HEART_BEAT_FAILED"));
    }
}


class KeepAliveMessageFactoryImpl implements KeepAliveMessageFactory {
    private Context mContext;
    private BtsStateDao btsStateDao;

    KeepAliveMessageFactoryImpl(Context context) {
        mContext = context;
        btsStateDao = new BtsStateDao(context);
    }

    public Object getRequest(IoSession session) {
        return CodecManager.getManager().heartBeatReqEncode((byte) 1, (byte) 1, (byte) 1).duplicate();
    }

    public Object getResponse(IoSession session, Object request) {
        return null;
    }

    public boolean isRequest(IoSession session, Object message) {
        return false;
    }

    public boolean isResponse(IoSession session, Object message) {
        if (!(message instanceof IoBuffer))
            return false;
        IoBuffer realMessage = (IoBuffer) message;
        realMessage.rewind();
        Log.e("TAG", "HeartBeat " + realMessage.toString());
        boolean result = false;
        if (realMessage.limit() > 2) {
            if (realMessage.get(0) == MonitorApplication.clientId && realMessage.get(2) == 0x04) {
                HeartBeatResponse response = CodecManager.getManager().heartBeatRspDecode((IoBuffer) message);
                StateModel stateModel = btsStateDao.get();
                if (stateModel == null) {
                    stateModel = new StateModel();
                }
                if (response.clientId == MonitorApplication.clientId) {
                    stateModel.connState = SystemConstants.CONN_STATUS.CONNECTED;
                    result = true;
//                    mContext.sendBroadcast(new Intent("com.zte.monitor.action.ACTION_HEART_BEAT_SUCCESS"));
                } else {
                    result = false;
                    stateModel.connState = SystemConstants.CONN_STATUS.DISCONNECT;
//                    mContext.sendBroadcast(new Intent("com.zte.monitor.action.ACTION_HEART_BEAT_FAILED"));
                }
                btsStateDao.saveOrUpdate(stateModel);
            }
        }
        return result;
    }
}
