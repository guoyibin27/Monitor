package com.zte.monitor.app.model;

import java.io.Serializable;

/**
 * Created by Sylar on 14-9-17.
 */
public class MonitorLineModel implements Serializable {

    public String imsi;
    public String imei;
    public int lineNo;
    public String carrier;
    public String networkSystems;
    public boolean isChecked;
}
