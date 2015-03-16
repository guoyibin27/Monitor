package com.zte.monitor.app.model.response;

/**
 * Created by Sylar on 14-9-9.
 */
public class CallInfoResponse extends UdpResponse {
    public String imsi;
    public String callTime;
    public String phNum;
    public String callType;
    public byte taskId;
    public byte readFlag;
}
