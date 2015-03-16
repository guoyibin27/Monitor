package com.zte.monitor.app.handler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.database.dao.*;
import com.zte.monitor.app.model.SettingModel;
import com.zte.monitor.app.model.StateModel;
import com.zte.monitor.app.model.UserModel;
import com.zte.monitor.app.model.response.MsgResponse;
import com.zte.monitor.app.util.NotificationUtils;
import com.zte.monitor.app.util.StringUtils;
import com.zte.monitor.app.util.TextToSpeechManager;
import com.zte.monitor.app.util.VibratorUtil;
import org.apache.mina.core.buffer.IoBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylar on 14-9-16.
 */
public class MessageResponseHandler extends ResponseHandler {
    public static final String USER_INFO = "com.zte.monitor.app.udp.USER_INFO";
    public static final String SMS_INFO = "com.zte.monitor.app.udp.SMS_INFO";
    public static final String BTS_STATE = "com.zte.monitor.app.udp.BTS_STATE";
    public static final String BTS_DATA = "com.zte.monitor.app.udp.BTS_DATA";
    public static final String ACTION_STOP_SPEAKING = "com.zte.monitor.app.ACTION_STOP_SPEAKING";
    public static final String ACTION_START_SPEAKING = "com.zte.monitor.app.ACTION_START_SPEAKING";


    private boolean isSpeakingThePower = true;
    private UserDao userDao;
    private SmsDao smsDao;
    private SettingDao settingDao;
    private SensitivePhoneNumberDao sensitivePhoneNumberDao;
    private SensitiveWordDao sensitiveWordDao;
    private BtsStateDao btsStateDao;
    private static long lastUpdateNotificationTime;//上次更新时间

    private List<String> cachedImsiList = new ArrayList<String>();

    public MessageResponseHandler(Context context) {
        super(context);
        userDao = new UserDao(context);
        smsDao = new SmsDao(context);
        settingDao = new SettingDao(context);
        sensitivePhoneNumberDao = new SensitivePhoneNumberDao(context);
        sensitiveWordDao = new SensitiveWordDao(context);
        btsStateDao = new BtsStateDao(context);
    }

    @Override
    public void handleMessage(IoBuffer message) {
        final MsgResponse response = CodecManager.getManager().msgRspDecode(message);
        Intent intent = new Intent();
        if ((byte) 1 == response.msgType) {//用户
            intent.setAction(USER_INFO);
            //将上报的号码给call_number，在界面面上根据状态判断是否更新号码信息
            if (response.userModel.status == 1 || response.userModel.status == 2 || response.userModel.status == 3) {
                response.userModel.callNumber = response.userModel.phoneNumber;
                response.userModel.phoneNumber = "";
                userDao.updateCallNumber(response.userModel);
            }
            if (response.userModel.property.equals(SystemConstants.USER_PROPERTY.UNKNOWN_LIST)) {
                response.userModel.username = "未知";
            }

            StateModel stateModel = btsStateDao.get();
            if (stateModel != null && stateModel.sysMode == SystemConstants.SYSTEM_MODE.MODE_COLLECT) {
                userDao.saveOrUpdate(response.userModel);
            } else {
                userDao.updateUserInfoAtFunctionStation(response.userModel);
            }

            //用户状态改变
            if (response.userModel.status == 1 || response.userModel.status == 3 || response.userModel.status == 4 || response.userModel.status == 5) {
                response.userModel.isStatusChanged = 1;
                userDao.updateUserStatusChanged(response.userModel);
            }
//            response.userModel.isStatusChanged = 1;
//            userDao.updateUserStatusChangedIfNeed(response.userModel);

//            if (userDao.isUserStatusChange(response.userModel)) {
//                VibratorUtil.vibrate(mContext, 2000);
//            }


            if (!StringUtils.isBlank(response.userModel.callNumber) && sensitivePhoneNumberDao.isExists(response.userModel.callNumber)) {
                VibratorUtil.vibrate(mContext, 2000);
                response.userModel.hasSensitiveNumber = 1;
                userDao.updateHasSensitiveNumber(response.userModel);
            }
            //空闲状态和被拒绝
            if (response.userModel.status == 0 || response.userModel.status == 8) {
                long currentTime = System.currentTimeMillis();//当前时间
                //距离到当前更新的时间间隔，单位秒
                long diff = (currentTime - lastUpdateNotificationTime) / 1000;
                //为了防止更新数据过快，造成手机卡死现象，如果间隔小于2秒，暂时不更新通知，但是这样的弊端就是人数不是实时的，会有误差。
                if (diff >= 2) {
                    //获取两个状态下的用户人数
                    long totalUser = userDao.getUserCount(0, 8);
                    NotificationUtils.getInstance(mContext.getApplicationContext()).updateCustomNotification(totalUser, 0, 0, 0, 0, 0);
                    lastUpdateNotificationTime = System.currentTimeMillis();
                }
            }

            //黑名单用户上线，1秒震动
            if (response.userModel.property.equals(SystemConstants.USER_PROPERTY.BLACK_LIST)) {
                if (response.userModel.status == 0) {
                    if (!cachedImsiList.contains(response.userModel.imsi)) {
                        VibratorUtil.vibrate(mContext, 1000);
                        cachedImsiList.add(response.userModel.imsi);
                    }
                }
            }
        } else if ((byte) 2 == response.msgType) {//功率上报
            intent.setAction(USER_INFO);
            if (!StringUtils.isBlank(response.powerImsi) && response.power != -1) {
                if (isSpeakingThePower) {
                    if (userDao.getCurrentLocateUser() != null && userDao.getCurrentLocateUser().imsi.equals(response.powerImsi)) {
                        TextToSpeechManager.getManager(mContext.getApplicationContext()).setTextToSpeechCallback(new TextToSpeechManager.TextToSpeechCallback() {

                            @Override
                            public void onBegin() {
                                userDao.updatePower(response.powerImsi, response.power);
                            }
                        });
                        TextToSpeechManager.getManager(mContext.getApplicationContext()).play(String.valueOf(response.power));
                    } else {
                        userDao.updatePower(response.powerImsi, response.power);
                    }

                    if (userDao.getCurrentCheckPowerUser() != null && userDao.getCurrentCheckPowerUser().imsi.equals(response.powerImsi)) {
                        TextToSpeechManager.getManager(mContext.getApplicationContext()).setTextToSpeechCallback(new TextToSpeechManager.TextToSpeechCallback() {

                            @Override
                            public void onBegin() {
                                userDao.updatePower(response.powerImsi, response.power);
                            }
                        });
                        TextToSpeechManager.getManager(mContext.getApplicationContext()).play(String.valueOf(response.power));
                    } else {
                        userDao.updatePower(response.powerImsi, response.power);
                    }
                } else {
                    userDao.updatePower(response.powerImsi, response.power);
                }
            }
        } else if ((byte) 4 == response.msgType) {//短信
            intent.setAction(SMS_INFO);
            SettingModel setting = settingDao.getSetting();
            if (setting.autoSendSms == SystemConstants.AUTO_SEND_SMS.AUTO_SEND) {
                response.smsModel.countdownNum = setting.autoSendSmsInterval;
            } else {
                response.smsModel.countdownNum = 0;
            }
            response.smsModel.status = SystemConstants.SMS_STATUS.NEW_ARRIVE;
            UserModel userModel = userDao.getByImsi(response.smsModel.imsi);
            if (userModel.isSmsAudit == 1) {
                response.smsModel.onSmsAudit = 1;
            }
            smsDao.save(response.smsModel);
            List<String> wordList = sensitiveWordDao.getList();
            if (wordList != null && wordList.size() > 0) {
                for (String word : wordList) {
                    if (response.smsModel.content.contains(word)) {
                        VibratorUtil.vibrate(mContext, 2000);
                        userModel.hasSensitiveWord = 1;
                        userDao.updateHasSensitiveWord(userModel);
                        return;
                    }
                }
            }
//            mContext.startService(new Intent(mContext, UdpDataSendService.class));
        } else if ((byte) 8 == response.msgType) {//基站状态
            intent.setAction(BTS_STATE);
            intent.putExtra(BTS_DATA, response);
            StateModel stateModel = btsStateDao.get();
            if (stateModel == null)
                stateModel = new StateModel();
            if (response.btsState != 0) {
                stateModel.btsState = response.btsState;
                btsStateDao.saveOrUpdate(stateModel);
            }
        }
        mContext.sendBroadcast(intent);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ACTION_STOP_SPEAKING.equals(action)) {
                isSpeakingThePower = false;
                TextToSpeechManager.getManager(mContext.getApplicationContext()).stop();
            } else if (ACTION_START_SPEAKING.equals(action)) {
                isSpeakingThePower = true;
            }
        }
    };

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STOP_SPEAKING);
        filter.addAction(ACTION_START_SPEAKING);
        mContext.registerReceiver(receiver, filter);
    }

    public void unRegisterReceiver() {
        mContext.unregisterReceiver(receiver);
    }

    public void clearCachedList() {
        cachedImsiList.clear();
    }

}
