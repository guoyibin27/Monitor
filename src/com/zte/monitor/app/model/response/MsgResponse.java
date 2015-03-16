package com.zte.monitor.app.model.response;

import com.zte.monitor.app.model.SmsModel;
import com.zte.monitor.app.model.UserModel;

/**
 * 基站上报用户信息，基站状态等
 * Created by Sylar on 14-9-4.
 */
public class MsgResponse extends UdpResponse {
    public byte msgType;
    //用户信息
    public UserModel userModel;
    //功率检测上报数据
    public String powerImsi;
    public byte power = -1;

    //短信内容
    public SmsModel smsModel;

    //基站状态
    public byte btsState;
}
