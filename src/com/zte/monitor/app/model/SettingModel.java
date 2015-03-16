package com.zte.monitor.app.model;

import java.io.Serializable;

/**
 * Created by Sylar on 14-9-17.
 */
public class SettingModel implements Serializable {
    /**
     * 接管模式
     */
    public int takeOverMode;

    /**
     * 运营商
     */
    public String operator;

    /**
     * 自动发送短信间隔时间
     */
    public int autoSendSmsInterval;

    /**
     * 移动监听路数
     */
    public int cmccMonitorCount;

    /**
     * 联通监听路数
     */
    public int cuccMonitorCount;

    /**
     * 是否自动发送短信，0：否；1：是
     */
    public int autoSendSms;

    /**
     * 基站频点
     */
    public int stationFrequency;

    /**
     * 邻区频点
     */
    public int regionFrequency;

    /**
     * 强度
     */
    public int intensity;

    /**
     * 功率
     */
    public int power;
}
