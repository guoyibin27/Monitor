package com.zte.monitor.app.model;

import java.io.Serializable;

/**
 * Created by Sylar on 14-9-3.
 * <Direction:><Imsi:><PhNum:><SC:><UpTime:><SmsContent:
 */
public class SmsModel implements Serializable {
    public String upTime;
    public byte direction;
    public String phNum;
    public String sc;
    public String content;
    public String newContent;
    public String imsi;
    public int status;
    public int countdownNum;
    public int onSmsAudit;
}
