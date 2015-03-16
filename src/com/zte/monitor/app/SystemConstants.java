package com.zte.monitor.app;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sylar on 14-9-16.
 */
public class SystemConstants {
    //用户属性
    public static interface USER_PROPERTY {
        //友好用户
        String WHITE_LIST = "1";
        //严控用户
        String BLACK_LIST = "2";
        //未知用户
        String UNKNOWN_LIST = "0";
    }

    //工作模式
    public static interface TAKE_OVER_MODE {
        int TAKE_OVER = 1;
        int LOCATE = 2;
    }

    //运营商
    public static interface CARRIER {
        String CMCC = "CMCC";
        String CUCC = "CUCC";
    }

    //网络制式
    public static interface NETWORK_SYSTEM {
        String GSM = "GSM";
        String CDMA = "CDMA";
        String WCDMA = "WCDMA";
        String TD = "TD";
        String LTE = "LTE";
        String WIFI = "Wi-Fi";
    }

    //短信处理状态
    public static interface SMS_STATUS {
        int NEW_ARRIVE = 0;//新拦截短信
        int IGNORE = 1;//丢弃
        int MODIFIED_AND_SENT = 2;//修改并发送
        int UNHANDLED = 3;//未处理
        int SENT = 4;//已发送
    }

    public static interface AUTO_SEND_SMS {
        int AUTO_SEND = 1;
        int DO_NOT_SEND = 0;
    }

    //基站状态
    public static interface BTS_STATUS {
        int STATION_NOT_START = -1;
        int STATION_POWER_ON = 1;
        int STATION_START_SUCCESS = 2;
        int STATION_DISCONNECTED = 4;
        int STATION_START_FAILED = 5;
        int STATION_PAUSE = 6;
    }

    //系统模式
    public static interface SYSTEM_MODE {
        int MODE_FUNCTION = 1;//功能站
        int MODE_COLLECT = 2;//采集站
    }

    //卡状态
    public static interface CARD_STATE {
        int STATE_GREEN = 1;
        int STATE_RED = 2;
        int STATE_ERROR = -1;
    }

    public static interface CONN_STATUS {
        int CONNECTED = 1;
        int DISCONNECT = 2;
    }

    public static interface MODE {
        int PREVIEW = 1;
        int EDIT = 2;
    }

    //用户状态
    public static Map<Byte, String> USER_STATUS;

    //设备状态
    public static Map<Integer, String> BTS_STATE;

    //客户端链接状态
    public static Map<Integer, String> CONN_STATE;

    //系统模式
    public static Map<Integer, String> SYSTEM_MODE_DATA;

    public static Map<Integer, String> SMS_STATUS_DATA;

    static {
        USER_STATUS = new HashMap<Byte, String>();
        USER_STATUS.put((byte) 0, "空闲");
        USER_STATUS.put((byte) 1, "呼叫");
        USER_STATUS.put((byte) 2, "振铃");
        USER_STATUS.put((byte) 3, "通话");
        USER_STATUS.put((byte) 4, "发送短信");
        USER_STATUS.put((byte) 5, "接收短信");
        USER_STATUS.put((byte) 6, "脱网");
        USER_STATUS.put((byte) 7, "定位中");
        USER_STATUS.put((byte) 8, "被拒绝");
        USER_STATUS.put((byte) 9, "监听");
        USER_STATUS.put((byte) -1, "关机");

        BTS_STATE = new HashMap<Integer, String>();
        BTS_STATE.put(BTS_STATUS.STATION_NOT_START, "未启动");
        BTS_STATE.put(BTS_STATUS.STATION_START_SUCCESS, "正常");
        BTS_STATE.put(BTS_STATUS.STATION_DISCONNECTED, "断连");
        BTS_STATE.put(BTS_STATUS.STATION_PAUSE, "暂停");

        CONN_STATE = new HashMap<Integer, String>(2);
        CONN_STATE.put(CONN_STATUS.CONNECTED, "正常");
        CONN_STATE.put(CONN_STATUS.DISCONNECT, "断开");

        SYSTEM_MODE_DATA = new HashMap<Integer, String>(2);
        SYSTEM_MODE_DATA.put(SYSTEM_MODE.MODE_COLLECT, "采集模式");
        SYSTEM_MODE_DATA.put(SYSTEM_MODE.MODE_FUNCTION, "功能模式");

        SMS_STATUS_DATA = new HashMap<Integer, String>();
        SMS_STATUS_DATA.put(SMS_STATUS.NEW_ARRIVE, "未处理");
        SMS_STATUS_DATA.put(SMS_STATUS.IGNORE, "丢弃");
        SMS_STATUS_DATA.put(SMS_STATUS.MODIFIED_AND_SENT, "修改并发送");
        SMS_STATUS_DATA.put(SMS_STATUS.UNHANDLED, "未处理");
        SMS_STATUS_DATA.put(SMS_STATUS.SENT, "已发送");
    }
}
