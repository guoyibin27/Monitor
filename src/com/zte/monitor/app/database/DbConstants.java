package com.zte.monitor.app.database;

/**
 * Created by sylar on 14-3-12.
 */
public class DbConstants {

    public static final String DB_NAME = "monitor.db";

    public static final int DB_VERSION = 1;

    public static interface TABLE {
        String server_info = "server_info";
        String user_info = "user_info";
        String sms_info = "sms_info";
        String bts_state = "bts_state";
        String monitor_line = "monitor_line";
        String setting = "setting";
        String sensitive_word = "sensitive_word";
        String sensitive_phone_number = "sensitive_phone_number";
    }

    public static interface COLUMN_SERVER {
        String IP = "ip";
        String PORT = "port";
    }

    public static interface COLUMN_USER {
        String NAME = "name";
        String IMSI = "imsi";
        String IMEI = "imei";
        String PHONE_NUMBER = "phone_number";
        String TMSI = "tmsi";
        String UP_TIME = "up_time";
        String STATUS = "status";
        String PROPERTY = "property";
        String AREA = "area";//归属地
        String POWER = "power";//用户功率
        String IS_SMS_AUDIT = "is_sms_audit";//是否开启了短信审计
        String IS_LOCATE = "is_locate";
        String IS_MONITOR = "is_monitor";//是否处于监听状态
        String IS_CHECK_POWER = "is_check_power";
        String IS_READ = "is_read";//状态发生变化后，是否查看过详情
        String IS_STATUS_CHANGED = "is_status_changed";
        String HAS_SENSITIVE_NUMBER = "has_sensitive_number";
        String HAS_SENSITIVE_WORD = "has_sensitive_word";
        String CALL_NUMBER = "call_number";//呼叫/被叫/通话时的电话号码
    }

    public static interface COLUMN_SMS {
        String DIRECTION = "direction";
        String IMSI = "imsi";
        String PH_NUM = "ph_num";
        String SC = "sc";
        String UP_TIME = "up_time";
        String SMS_CONTENT = "sms_content";
        String SMS_NEW_CONTENT = "sms_new_content";
        String SMS_STATUS = "sms_status";
        String COUNT_DOWN_NUM = "count_down_num";
        String SMS_IS_ON_AUDIT = "on_audit";
    }

    /**
     * 基站状态表
     */
    public static interface COLUMN_BTS_STATE {
        String BTS_STATE = "bts_state"; //基站状态
        String CONN_STATE = "conn_state";//链接状态
        String PROXY_STATE = "proxy_state";//监听模块状态
        String GET_PH_NUM_STATE = "get_ph_num_state";//获取号码监听状态
        String SYS_MODE = "sys_mode";//系统模式:采集站、功能功能
    }

    public static interface COLUMN_MONITOR_LINE {
        String IMSI = "imsi";
        String IMEI = "imei";
        String LINE_NO = "line_no";//监听序列号
        String CARRIER = "mcc";//运营商,CMCC,China Unicom
        String NETWORK_SYSTEMS = "network_systems";//网络制式，GSM,CDMA,WCDMA,TD,LTE,WIFI
    }

    public static interface COLUMN_SETTING {
        String TAKE_OVER_MODE = "take_over_mode";
        String AUTO_SEND_SMS_INTERVAL = "auto_send_sms_interval";
        String CMCC_MONITOR_COUNT = "cmcc_monitor_count";
        String CUCC_MONITOR_COUNT = "cucc_monitor_count";
        String AUTO_SEND_SMS = "auto_send_sms";
        String STATION_FREQUENCY = "station_frequency";
        String REGION_FREQUENCY = "region_frequency";
        String OPERATOR = "operator";
        String POWER = "power";
        String INTENSITY = "intensity";
    }

    public static interface COLUMN_SENSITIVE_PHONE_NUMBER {
        String SENSITIVE_PHONE_NUMBER = "sensitive_phone_number";
    }

    public static interface COLUMN_SENSITIVE_WORD {
        String SENSITIVE_WORD = "sensitive_word";
    }

    //创建服务器信息表
    public static StringBuffer create_server_info_table = new StringBuffer();
    //插入默认数据
    public static StringBuffer insert_default_server_info = new StringBuffer();
    //创建用户信息表
    public static StringBuffer create_user_info_table = new StringBuffer();
    //创建短信表
    public static StringBuffer create_sms_table = new StringBuffer();
    //创建基站状态数据库
    public static StringBuffer create_bts_state_table = new StringBuffer();
    //创建监听路数数据库表
    public static StringBuffer create_monitor_line_table = new StringBuffer();
    //创建设置数据库
    public static StringBuffer create_setting_table = new StringBuffer();
    //敏感词汇表
    public static StringBuffer create_sensitive_word_table = new StringBuffer();
    //敏感电话表
    public static StringBuffer create_sensitive_phone_number_table = new StringBuffer();

    static {

        create_setting_table.append("create table ").append(TABLE.setting);
        create_setting_table.append("(");
        create_setting_table.append(COLUMN_SETTING.TAKE_OVER_MODE).append(" INTEGER,");
        create_setting_table.append(COLUMN_SETTING.AUTO_SEND_SMS_INTERVAL).append(" INTEGER,");
        create_setting_table.append(COLUMN_SETTING.CMCC_MONITOR_COUNT).append(" INTEGER,");
        create_setting_table.append(COLUMN_SETTING.CUCC_MONITOR_COUNT).append(" INTEGER,");
        create_setting_table.append(COLUMN_SETTING.STATION_FREQUENCY).append(" INTEGER,");
        create_setting_table.append(COLUMN_SETTING.REGION_FREQUENCY).append(" INTEGER,");
        create_setting_table.append(COLUMN_SETTING.OPERATOR).append(" TEXT,");
        create_setting_table.append(COLUMN_SETTING.AUTO_SEND_SMS).append(" INTEGER,");
        create_setting_table.append(COLUMN_SETTING.INTENSITY).append(" INTEGER,");
        create_setting_table.append(COLUMN_SETTING.POWER).append(" INTEGER");
        create_setting_table.append(")");

        create_monitor_line_table.append("create table ").append(TABLE.monitor_line);
        create_monitor_line_table.append("(");
        create_monitor_line_table.append(COLUMN_MONITOR_LINE.LINE_NO).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
        create_monitor_line_table.append(COLUMN_MONITOR_LINE.IMSI).append(" TEXT,");
        create_monitor_line_table.append(COLUMN_MONITOR_LINE.IMEI).append(" TEXT,");
        create_monitor_line_table.append(COLUMN_MONITOR_LINE.CARRIER).append(" TEXT,");
        create_monitor_line_table.append(COLUMN_MONITOR_LINE.NETWORK_SYSTEMS).append(" TEXT");
        create_monitor_line_table.append(")");

        create_bts_state_table.append("create table ").append(TABLE.bts_state);
        create_bts_state_table.append("(");
        create_bts_state_table.append(COLUMN_BTS_STATE.BTS_STATE).append(" int,");
        create_bts_state_table.append(COLUMN_BTS_STATE.CONN_STATE).append(" int,");
        create_bts_state_table.append(COLUMN_BTS_STATE.PROXY_STATE).append(" int,");
        create_bts_state_table.append(COLUMN_BTS_STATE.GET_PH_NUM_STATE).append(" int,");
        create_bts_state_table.append(COLUMN_BTS_STATE.SYS_MODE).append(" int");
        create_bts_state_table.append(")");

        create_server_info_table.append("create table ").append(TABLE.server_info);
        create_server_info_table.append("(");
        create_server_info_table.append(COLUMN_SERVER.IP).append(" text,");
        create_server_info_table.append(COLUMN_SERVER.PORT).append(" int");
        create_server_info_table.append(")");

        insert_default_server_info.append("insert into ").append(TABLE.server_info).append(" values(").append("'192.168.43.233'")
                .append(", 20001)");

        create_user_info_table.append("create table ").append(TABLE.user_info);
        create_user_info_table.append("(");
        create_user_info_table.append(COLUMN_USER.NAME).append(" TEXT,");
        create_user_info_table.append(COLUMN_USER.IMSI).append(" TEXT,");
        create_user_info_table.append(COLUMN_USER.IMEI).append(" TEXT,");
        create_user_info_table.append(COLUMN_USER.PHONE_NUMBER).append(" TEXT,");
        create_user_info_table.append(COLUMN_USER.TMSI).append(" TEXT,");
        create_user_info_table.append(COLUMN_USER.UP_TIME).append(" DATETIME,");
        create_user_info_table.append(COLUMN_USER.PROPERTY).append(" TEXT,");
        create_user_info_table.append(COLUMN_USER.AREA).append(" TEXT,");
        create_user_info_table.append(COLUMN_USER.POWER).append(" INTEGER,");
        create_user_info_table.append(COLUMN_USER.CALL_NUMBER).append(" TEXT,");
        create_user_info_table.append(COLUMN_USER.STATUS).append(" INTEGER,");
        create_user_info_table.append(COLUMN_USER.IS_SMS_AUDIT).append(" INTEGER,");
        create_user_info_table.append(COLUMN_USER.IS_LOCATE).append(" INTEGER,");
        create_user_info_table.append(COLUMN_USER.IS_CHECK_POWER).append(" INTEGER,");
        create_user_info_table.append(COLUMN_USER.IS_MONITOR).append(" INTEGER,");
        create_user_info_table.append(COLUMN_USER.IS_READ).append(" INTEGER default 0,");
        create_user_info_table.append(COLUMN_USER.IS_STATUS_CHANGED).append(" INTEGER  default 0,");
        create_user_info_table.append(COLUMN_USER.HAS_SENSITIVE_NUMBER).append(" INTEGER default 0,");
        create_user_info_table.append(COLUMN_USER.HAS_SENSITIVE_WORD).append(" INTEGER default 0");
        create_user_info_table.append(")");


        create_sms_table.append("create table ").append(TABLE.sms_info);
        create_sms_table.append("(");
        create_sms_table.append(COLUMN_SMS.IMSI).append(" TEXT,");
        create_sms_table.append(COLUMN_SMS.DIRECTION).append(" TEXT,");
        create_sms_table.append(COLUMN_SMS.PH_NUM).append(" TEXT,");
        create_sms_table.append(COLUMN_SMS.SC).append(" TEXT,");
        create_sms_table.append(COLUMN_SMS.SMS_CONTENT).append(" TEXT,");
        create_sms_table.append(COLUMN_SMS.SMS_NEW_CONTENT).append(" TEXT,");
        create_sms_table.append(COLUMN_SMS.UP_TIME).append(" TEXT,");
        create_sms_table.append(COLUMN_SMS.SMS_STATUS).append(" INTEGER,");
        create_sms_table.append(COLUMN_SMS.COUNT_DOWN_NUM).append(" INTEGER,");
        create_sms_table.append(COLUMN_SMS.SMS_IS_ON_AUDIT).append(" INTEGER default 0");
        create_sms_table.append(")");

        create_sensitive_phone_number_table.append("create table ").append(TABLE.sensitive_phone_number);
        create_sensitive_phone_number_table.append("(");
        create_sensitive_phone_number_table.append(COLUMN_SENSITIVE_PHONE_NUMBER.SENSITIVE_PHONE_NUMBER).append(" TEXT PRIMARY KEY");
        create_sensitive_phone_number_table.append(")");

        create_sensitive_word_table.append("create table ").append(TABLE.sensitive_word);
        create_sensitive_word_table.append("(");
        create_sensitive_word_table.append(COLUMN_SENSITIVE_WORD.SENSITIVE_WORD).append(" TEXT PRIMARY KEY");
        create_sensitive_word_table.append(")");
    }

}
