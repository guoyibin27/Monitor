package com.zte.monitor.app.handler;

import android.content.Context;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.database.dao.BtsStateDao;
import com.zte.monitor.app.model.StateModel;
import com.zte.monitor.app.model.response.CardStateResponse;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 14-10-15.
 */
public class CardStateResponseHandler extends ResponseHandler {

    private BtsStateDao btsStateDao;

    public CardStateResponseHandler(Context context) {
        super(context);
        btsStateDao = new BtsStateDao(context);
    }

    @Override
    public void handleMessage(IoBuffer message) {
        if (message != null) {
            CardStateResponse response = CodecManager.getManager().getCardStateRspDecode(message);
            StateModel stateModel = btsStateDao.get();
            if (stateModel == null)
                stateModel = new StateModel();
            stateModel.getPhNumCardState = response.getPhoneNumberCardState;
            stateModel.proxyCardState = response.proxyCardState;
            btsStateDao.saveOrUpdate(stateModel);
        }
    }
}
