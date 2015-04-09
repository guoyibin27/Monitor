package com.zte.monitor.app.model.response;

/**
 * Created by Sylar on 14-9-9.
 */
public class SmsInfoResponse extends UdpResponse {

    public String imsi;
    public String datetime;
    public String smsNum;
    public String modSmsNum;
    public String message;
    public String modMessage;
    public String scenter;
    public byte smsType;
    public byte disCardFlag;
    public byte taskId;
}
