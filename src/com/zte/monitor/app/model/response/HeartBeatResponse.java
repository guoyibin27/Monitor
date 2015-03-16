package com.zte.monitor.app.model.response;

/**
 * Created by Sylar on 14-9-4.
 */
public class HeartBeatResponse extends UdpResponse {
    public byte clientId;
    public byte serverId;
    public byte reserveSlot;
    public byte msgLength;
    public byte msgBody;

    @Override
    public String toString() {
        return clientId + "" + serverId + "" + msgId + "" + reserveSlot + "" + msgLength + "" + msgBody;
    }
}
