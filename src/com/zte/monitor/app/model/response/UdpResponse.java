package com.zte.monitor.app.model.response;

import java.io.Serializable;

/**
 * Created by Sylar on 14-9-4.
 */
public class UdpResponse implements Serializable {

    public byte msgId;
    public byte action;
    public byte state;
    public byte cause;
    public byte state1;
    public byte cause1;
    public byte state2;
    public byte cause2;
}
