package com.zte.monitor.app.model;

import java.io.Serializable;

/**
 * Created by Sylar on 8/25/14.
 */
public class UserModel implements Serializable {

    public String username;
    public String imsi;
    public String imei;
    public String phoneNumber;
    public String area;
    public String lastUpdated;
    public byte status = -2;
    public String property;
    public String tmsi;
    public String callNumber;
    public int power = -1;
    public int isSmsAudit;
    public int isLocate;
    public int isMonitor;
    public int isCheckPower;
    public int isStatusChanged;//状态发生变化后，是否查看过详情
    public int hasSensitiveNumber;//敏感号码
    public int hasSensitiveWord;//敏感词汇

    public boolean isChecked = false;
}
