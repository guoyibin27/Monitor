package com.zte.monitor.app.model;

import com.zte.monitor.app.SystemConstants;

import java.io.Serializable;

/**
 * Created by Sylar on 14-10-15.
 */
public class StateModel implements Serializable {

    public int btsState = -1;
    public int proxyCardState = SystemConstants.CARD_STATE.STATE_ERROR;
    public int getPhNumCardState = SystemConstants.CARD_STATE.STATE_ERROR;
    public int connState = SystemConstants.CONN_STATUS.DISCONNECT;
    public int sysMode = SystemConstants.SYSTEM_MODE.MODE_COLLECT;

    @Override
    public String toString() {
        return "StateModel{" +
                "btsState=" + btsState +
                ", proxyCardState=" + proxyCardState +
                ", getPhNumCardState=" + getPhNumCardState +
                ", connState=" + connState +
                ", sysMode=" + sysMode +
                '}';
    }
}
