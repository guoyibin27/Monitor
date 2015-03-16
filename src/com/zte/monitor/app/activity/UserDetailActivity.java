package com.zte.monitor.app.activity;

import android.app.AlertDialog;
import android.content.*;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.zte.monitor.app.MonitorApplication;
import com.zte.monitor.app.R;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.database.dao.*;
import com.zte.monitor.app.handler.*;
import com.zte.monitor.app.model.*;
import com.zte.monitor.app.model.response.*;
import com.zte.monitor.app.pcm.PcmAudioParam;
import com.zte.monitor.app.pcm.PcmPlayer;
import com.zte.monitor.app.udp.UdpDataSendService;
import com.zte.monitor.app.udp.aidl.IUdpConnectionInterface;
import com.zte.monitor.app.util.*;
import org.apache.mina.core.buffer.IoBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class UserDetailActivity extends BaseActivity {
    public static final String DATA = "UserDetailActivity.DATA";
    private static final int STATUS_PLAY = 0;
    private static final int STATUS_STOP = 1;
    private static final int STATUS_PAUSE = 3;

    private static final int SMS_CONTENT_MAX = 256;

    private ArrayAdapter<KeyValuePair> smsCenterAdapter;
    private TextView mUserName;
    private TextView mImsi;
    private TextView mImei;
    private TextView mPhoneNumber;
    private TextView mArea;
    private TextView mLastUpdated;
    private TextView mStatus;
    private TextView mPower;
    private LinearLayout mSmsLayout;
    private TextView mSmsContent;
    private TextView mSmsDirection;
    private TextView callNumber;
    private TextView mSmsPhoneNum;
    private Button deleteButton;
    private Button copyImsiButton;
    private Button addToBlackButton;
    private IUdpConnectionInterface anInterface;
    private int status = STATUS_PLAY;
    private PcmAudioParam audioParam;
    private PcmPlayer pcmPlayer;
    private UserModel mUserModel;
    private UserDao userDao;
    private SettingDao settingDao;
    private MonitorLineDao monitorLineDao;
    private SmsDao smsDao;
    private boolean isRecord = false;
    private AudioRecordThread audioRecordThread;
    private BtsStateDao btsStateDao;
    private AlertDialog audioRecordDialog;
    private AlertDialog fixedPcmPlayDialog;
    private AlertDialog fixedPcmListDialog;
    private static final Object lockObj = new Object();
    private boolean isCall = false;
    private AudioRecord audioRecord;
    private int locateMessageId = 10001;
    private int checkPowerMessageId = 10002;
    private int monitorMessageId = 10003;
    private int voiceCallMessageId = 10004;
    private int fixedVoiceMessageId = 10005;
    private int recordMessageId = 10006;
    private int getPhNumMessageId = 10007;
    private int smsAuditMessageId = 10008;
    private int addToBlackMessageId = 1009;
    private int deleteUserMessageId = (int) System.currentTimeMillis();

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            anInterface = IUdpConnectionInterface.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            anInterface = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        userDao = new UserDao(this);
        settingDao = new SettingDao(this);
        monitorLineDao = new MonitorLineDao(this);
        smsDao = new SmsDao(this);
        btsStateDao = new BtsStateDao(this);

        audioParam = new PcmAudioParam();
        audioParam.mFrequency = 8000;
        audioParam.mChannel = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        audioParam.mSampBit = AudioFormat.ENCODING_PCM_16BIT;
        pcmPlayer = new PcmPlayer(audioParam);
        pcmPlayer.prepare();
        audioRecordThread = new AudioRecordThread();
        audioRecordThread.start();

        initViews();
        initData();
        registerReceiver();
        initSmsCenter();

        Intent intent = new Intent(this, UdpDataSendService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private void initSmsCenter() {
        try {
            InputStream inputStream = getAssets().open("sms_center.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder json = new StringBuilder();
            while (reader.ready()) {
                json.append(reader.readLine());
            }
            JSONArray arr = new JSONArray(json.toString());
            List<KeyValuePair> smsCenterList = new ArrayList<KeyValuePair>(arr.length());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject object = arr.getJSONObject(i);
                KeyValuePair pair = new KeyValuePair();
                pair.key = object.getString("center_no");
                pair.value = object.getString("center_name");
                smsCenterList.add(pair);
            }
            smsCenterAdapter = new ArrayAdapter<KeyValuePair>(this, android.R.layout.simple_spinner_dropdown_item, smsCenterList);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听器注册
     */
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AddUserResponseHandler.ADD_USER_SUCCESS);
        filter.addAction(AddUserResponseHandler.ADD_USER_FAILED);
        filter.addAction(TaskRcdListResponseHandler.TASK_RCD_LIST_SUCCESS);
        filter.addAction(PlayRecordResponseHandler.PLAY_RECORD_SUCCESS);
        filter.addAction(ShortMsgSwithResponseHandler.SHORT_MSG_SWITH_SUCCESS);
        filter.addAction(ShortMsgSwithResponseHandler.SHORT_MSG_SWITH_FAILED);
        filter.addAction(LocationResponseHandler.LOCATION_FAILED);
        filter.addAction(LocationResponseHandler.LOCATION_SUCCESS);
        filter.addAction(TaskGetPhNumResponseHandler.TASK_GET_PH_NUM_SUCCESS);
        filter.addAction(TaskGetPhNumResponseHandler.TASK_GET_PH_NUM_FAILED);
        filter.addAction(SmsBroadcastResponseHandler.SMS_BROADCAST_SUCCESS);
        filter.addAction(SmsBroadcastResponseHandler.SMS_BROADCAST_FAILED);
        filter.addAction(MonitorResponseHandler.MONITOR_SUCCESS);
        filter.addAction(MonitorResponseHandler.MONITOR_FAILED);
        filter.addAction(CheckPowerResponseHandler.CHECK_POWER_SUCCESS);
        filter.addAction(CheckPowerResponseHandler.CHECK_POWER_FAILED);
        filter.addAction(MessageResponseHandler.USER_INFO);
        filter.addAction(MessageResponseHandler.SMS_INFO);
        filter.addAction(PcmBroadcastResponseHandler.PCM_BROADCAST_SUCCESS);
        filter.addAction(PcmBroadcastResponseHandler.PCM_BROADCAST_FAILED);
        filter.addAction(VoiceCallFeedbackResponseHandler.VOICE_CALL_SUCCESS);
        filter.addAction(AddUserResponseHandler.ADD_USER_FAILED);
        filter.addAction(AddUserResponseHandler.ADD_USER_SUCCESS);
        registerReceiver(receiver, filter);
    }

    private void initData() {
        mUserModel = (UserModel) getIntent().getSerializableExtra(DATA);
        refreshData();
        loadSmsData();
    }

    /**
     * 加载短信信息
     */
    private void loadSmsData() {
        if (mUserModel != null) {
            SmsModel smsModel = smsDao.getSmsModelByImsi(mUserModel.imsi);
            if (smsModel != null) {
                setSmsLayoutVisibility(View.VISIBLE);
                fillSmsData(smsModel);
            } else {
                setSmsLayoutVisibility(View.GONE);
                fillSmsData(null);
            }
        }
    }

    /**
     * 当拦截到短信时，获取短信数据，填充界面控件
     *
     * @param o
     */
    private void fillSmsData(SmsModel o) {
        if (o == null) {
            mSmsDirection.setText("");
            mSmsContent.setText("");
            mSmsPhoneNum.setText("");
        } else {
            if (o.direction == 1) {
                mSmsDirection.setText("发送短信");
            } else if (o.direction == 2) {
                mSmsDirection.setText("接收短信");
            }
            mSmsContent.setText(o.content);
            mSmsPhoneNum.setText(o.phNum);

        }
    }

    private void setSmsLayoutVisibility(int visible) {
        mSmsLayout.setVisibility(visible);
    }

    /**
     * 刷新数据
     */
    private void refreshData() {
        if (mUserModel != null) {
            mUserName.setText(mUserModel.username);
            mImsi.setText(mUserModel.imsi);
            mImei.setText(mUserModel.imei);
            mPhoneNumber.setText(mUserModel.phoneNumber);
            //用户状态为 主叫，被叫，通话时，显示call_number
            if (mUserModel.status == 1 || mUserModel.status == 2 || mUserModel.status == 3) {
                callNumber.setText(mUserModel.callNumber);
            }
            mArea.setText(mUserModel.area);
            mLastUpdated.setText(mUserModel.lastUpdated);
            mPower.setText(mUserModel.power + "");
            if (mUserModel.isCheckPower == 1) {
                mStatus.setText("功率检测");
            } else if (mUserModel.isLocate == 1) {
                mStatus.setText("定位中");
            } else {
                mStatus.setText(SystemConstants.USER_STATUS.get(mUserModel.status));
            }

            if (mUserModel.property.equals(SystemConstants.USER_PROPERTY.UNKNOWN_LIST)) {
                deleteButton.setVisibility(View.GONE);
                copyImsiButton.setVisibility(View.VISIBLE);
                addToBlackButton.setVisibility(View.VISIBLE);
            } else {
                deleteButton.setVisibility(View.VISIBLE);
                copyImsiButton.setVisibility(View.GONE);
                addToBlackButton.setVisibility(View.GONE);
            }
        }
    }

    private void initViews() {
        mUserName = (TextView) findViewById(R.id.tv_username);
        mImsi = (TextView) findViewById(R.id.tv_imsi);
        mImei = (TextView) findViewById(R.id.tv_imei);
        mPhoneNumber = (TextView) findViewById(R.id.tv_phone_number);
        mArea = (TextView) findViewById(R.id.tv_area);
        mLastUpdated = (TextView) findViewById(R.id.tv_last_updated);
        mStatus = (TextView) findViewById(R.id.tv_status);
        mPower = (TextView) findViewById(R.id.tv_power);
        mSmsLayout = (LinearLayout) findViewById(R.id.sms_layout);
        mSmsContent = (TextView) findViewById(R.id.tv_sms_content);
        mSmsDirection = (TextView) findViewById(R.id.tv_sms_direction);
        mSmsPhoneNum = (TextView) findViewById(R.id.tv_sms_phone_num);
        callNumber = (TextView) findViewById(R.id.call_phone_number);
        deleteButton = (Button) findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String message = "确认删除用户" + (StringUtils.isBlank(mUserModel.username) ? "" : mUserModel.username + " ") + "?";
                new AlertDialog.Builder(UserDetailActivity.this).setTitle("删除用户")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                DialogUtils.showProgressDialog(UserDetailActivity.this, "删除用户");
                                IoBuffer bu = CodecManager.getManager().spcUserReqEncode((byte) 1, mUserModel);
                                byte[] out = new byte[bu.limit()];
                                bu.flip();
                                bu.get(out);
                                try {
                                    anInterface.sendRequest(out);
                                    userDao.delete(mUserModel.imsi, mUserModel.imei);
                                    startLoadingTimer(deleteUserMessageId);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                    removeTimer(deleteUserMessageId);
                                    DialogUtils.dismissProgressDialog();
                                }
                            }
                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
            }
        });

        copyImsiButton = (Button) findViewById(R.id.copy_imsi_button);
        copyImsiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager copy = (ClipboardManager) UserDetailActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("IMSI", mImsi.getText().toString());
                copy.setPrimaryClip(clipData);
                ToastUtils.show(UserDetailActivity.this, "已复制到剪贴板");
            }
        });

        addToBlackButton = (Button) findViewById(R.id.add_to_black_button);
        addToBlackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.showProgressDialog(UserDetailActivity.this, "正在保存用户，请稍等...");
                UserModel tempUserModel = mUserModel;
                tempUserModel.property = SystemConstants.USER_PROPERTY.BLACK_LIST;
                IoBuffer bu = CodecManager.getManager().spcUserReqEncode((byte) 0, tempUserModel);
                byte[] out = new byte[bu.limit()];
                bu.flip();
                bu.get(out);
                try {
                    anInterface.sendRequest(out);
                    startLoadingTimer(addToBlackMessageId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    removeTimer(addToBlackMessageId);
                    DialogUtils.dismissProgressDialog();
                }
            }
        });
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (NoSuchMethodException e) {
                    Log.e("TAG", "onMenuOpened", e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        //如果用户是白名单，无操作菜单
        if (mUserModel.property.equals(SystemConstants.USER_PROPERTY.WHITE_LIST)) {
            return super.onPrepareOptionsMenu(menu);
        }

        //如果用户状态未被拒绝/脱网/关机，无操作菜单
        if (mUserModel.status == 8 || mUserModel.status == 6 || mUserModel.status == -1) {
            return super.onPrepareOptionsMenu(menu);
        }

        if (settingDao.getSetting().takeOverMode == SystemConstants.TAKE_OVER_MODE.LOCATE
                && mUserModel.property.equals(SystemConstants.USER_PROPERTY.UNKNOWN_LIST)) {
            return super.onPrepareOptionsMenu(menu);
        }

        MenuItem checkPowerItem;
        if (mUserModel.isCheckPower == 0) {
            checkPowerItem = menu.add(1, 1, 1, R.string.action_power_detection);
        } else {
            checkPowerItem = menu.add(1, 1, 1, R.string.action_cancel_power_detection);
        }
        checkPowerItem.setIcon(R.drawable.check_power);
        checkPowerItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        if (btsStateDao.get().sysMode == SystemConstants.SYSTEM_MODE.MODE_COLLECT) {
            if (mUserModel.status == 0) {//状态为空闲，可用
                checkPowerItem.setEnabled(true);
            } else {
                checkPowerItem.setEnabled(false);
            }
        } else {
            checkPowerItem.setEnabled(false);
        }

        MenuItem monitorItem;
        if (mUserModel.isMonitor == 0) {
            monitorItem = menu.add(1, 2, 2, R.string.action_monitor);
        } else {
            monitorItem = menu.add(1, 2, 2, R.string.action_cancel_monitor);
        }
        monitorItem.setIcon(R.drawable.ic_monitor);
        monitorItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        if (mUserModel.isMonitor == 0) {
            if (mUserModel.status == 0 || mUserModel.status == 7) {//状态为空闲或定位，可用
                monitorItem.setEnabled(true);
            } else {
                monitorItem.setEnabled(false);
            }
        }

        MenuItem locateItem;
        if (mUserModel.isLocate == 0) {
            locateItem = menu.add(1, 3, 3, R.string.action_locate);
        } else {
            locateItem = menu.add(1, 3, 3, R.string.action_cancel_locate);
        }
        locateItem.setIcon(R.drawable.dingwei);
        locateItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        //状态为空闲，或定位，或功率检测，可用
        if (mUserModel.isLocate == 0) {
            if (mUserModel.status == 0 || mUserModel.isLocate == 1 || mUserModel.isCheckPower == 1) {
                locateItem.setEnabled(true);
            } else {
                locateItem.setEnabled(false);
            }
        }


        MenuItem voiceCallItem = menu.add(1, 4, 4, R.string.action_real_time_voice_call);
        voiceCallItem.setIcon(R.drawable.shishiyuyin);
        voiceCallItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        MenuItem fixedCallItem = menu.add(1, 5, 5, R.string.action_fixed_voice_call);
        fixedCallItem.setIcon(R.drawable.gudingyuyin);
        fixedCallItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        //状态脱网，关机，不可用
        if (mUserModel.status == 6 || mUserModel.status == 255) {
            voiceCallItem.setEnabled(false);
            fixedCallItem.setEnabled(false);
        } else {
            //状态为空闲，或功率检测，或定位时，语音可用
            if (mUserModel.status == 0 || mUserModel.isCheckPower == 1 || mUserModel.isLocate == 1) {
                if (mUserModel.isMonitor == 1) {
                    voiceCallItem.setEnabled(false);
                } else {
                    voiceCallItem.setEnabled(true);
                }
            } else {
                voiceCallItem.setEnabled(false);
            }

            //状态为空闲，或监听时，固定语音可用
            if (mUserModel.status == 0 || mUserModel.isMonitor == 1) {
                fixedCallItem.setEnabled(true);
            } else {
                fixedCallItem.setEnabled(false);
            }
        }

        menu.add(1, 6, 6, R.string.action_play_audio).setIcon(R.drawable.play).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        MenuItem obtainPnNum = menu.add(1, 7, 7, R.string.action_obtain_phone_number);
        obtainPnNum.setIcon(R.drawable.get_phone_num);
        obtainPnNum.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        //状态为监听，或定位时，可用
        if (mUserModel.isMonitor == 1 || mUserModel.isLocate == 1) {
            obtainPnNum.setEnabled(true);
        } else {
            obtainPnNum.setEnabled(false);
        }

        MenuItem sendSmsItem = menu.add(1, 8, 8, R.string.action_send_sms);
        sendSmsItem.setIcon(R.drawable.send_message);
        sendSmsItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        //状态空闲，或监听，或定位，或功率检测时，可用
        if (mUserModel.status == 0 || mUserModel.isMonitor == 1 || mUserModel.isLocate == 1 || mUserModel.isCheckPower == 1) {
            sendSmsItem.setEnabled(true);
        } else {
            sendSmsItem.setEnabled(false);
        }

        MenuItem smsAudit;
        if (mUserModel.isSmsAudit == 0) {
            smsAudit = menu.add(1, 9, 9, R.string.action_sms_audit);
        } else {
            smsAudit = menu.add(1, 9, 9, R.string.action_cancel_sms_audit);
        }
        smsAudit.setIcon(R.drawable.message);
        smsAudit.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        //用户在监听，或定位，功能可用
        if (mUserModel.isMonitor == 1 || mUserModel.isLocate == 1) {
            smsAudit.setEnabled(true);
        } else {
            smsAudit.setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case 1://功率检测
            {
                if (userDao.getCurrentLocateUser() != null) {
                    ToastUtils.show(UserDetailActivity.this, "请先关闭定位后在进行功率检测");
                    return super.onOptionsItemSelected(item);
                }
                IoBuffer buffer = null;
                if (mUserModel.isCheckPower == 0) {
                    DialogUtils.showProgressDialog(UserDetailActivity.this, "正在发送功率检测请求，请稍等...");
                    buffer = CodecManager.getManager().checkPowerReqEncode((byte) 0, mImsi.getText().toString());
                } else {
                    DialogUtils.showProgressDialog(UserDetailActivity.this, "正在发送取消功率检测请求，请稍等...");
                    buffer = CodecManager.getManager().checkPowerReqEncode((byte) 1, mImsi.getText().toString());
                }

                buffer.flip();
                byte[] powerReq = new byte[buffer.limit()];
                buffer.get(powerReq);
                try {
                    anInterface.sendRequest(powerReq);
                    startLoadingTimer(checkPowerMessageId, 200);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    removeTimer(checkPowerMessageId);
                    DialogUtils.dismissProgressDialog();
                    ToastUtils.show(UserDetailActivity.this, "请求发送失败");
                }
                break;
            }
            case 2://监听
            {
                //根据当前配置的监听路数，判断是否超过总的监听路数.
                SettingModel settingModel = settingDao.getSetting();
                if (StringUtils.isBlank(mUserModel.imsi)) {
                    ToastUtils.show(this, "IMSI数据错误");
                    break;
                }
                String carrier = mUserModel.imsi.substring(2, 4);
                String currentCarrier = carrier.equals(SystemConstants.CARRIER.CMCC) ? SystemConstants.CARRIER.CMCC : SystemConstants.CARRIER.CUCC;
                //当前只判断GSM
                long monitorLineCount = monitorLineDao.getCountByNetworkSystemAndCarrier(SystemConstants.NETWORK_SYSTEM.GSM, carrier);
                if (settingModel != null) {
                    if (currentCarrier.equals(SystemConstants.CARRIER.CMCC)) {
                        if (settingModel.cmccMonitorCount < monitorLineCount) {
                            ToastUtils.show(this, "中国移动当前监听路数已满");
                            break;
                        }
                    } else {
                        if (settingModel.cuccMonitorCount < monitorLineCount) {
                            ToastUtils.show(this, "中国联通当前监听路数已满");
                            break;
                        }
                    }
                }
                List<UserModel> userModelList = new ArrayList<UserModel>();
                IoBuffer buffer;
                userModelList.add(mUserModel);
                if (mUserModel.isMonitor == 0) {
                    DialogUtils.showProgressDialog(UserDetailActivity.this, "发送监听请求，请稍等...");
                    buffer = CodecManager.getManager().monitorReqEncode((byte) 0, userModelList);
                } else {
                    DialogUtils.showProgressDialog(UserDetailActivity.this, "发送取消监听请求，请稍等...");
                    buffer = CodecManager.getManager().monitorReqEncode((byte) 1, userModelList);
                }
                buffer.flip();
                byte[] out = new byte[buffer.limit()];
                buffer.get(out);
                try {
                    anInterface.sendRequest(out);
                    startLoadingTimer(monitorMessageId, 200);
                    changeSysModeState();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    removeTimer(monitorMessageId);
                    DialogUtils.dismissProgressDialog();
                    if (mUserModel.isMonitor == 0) {//开启监听
                        ToastUtils.show(UserDetailActivity.this, "监听请求发送失败");
                    } else {
                        ToastUtils.show(UserDetailActivity.this, "取消监听请求发送失败");
                    }
                }
                break;
            }
            case 3://定位
            {
                if (userDao.getCurrentCheckPowerUser() != null) {
                    ToastUtils.show(UserDetailActivity.this, "请先关闭功率检测后在进行定位");
                    return super.onOptionsItemSelected(item);
                }
                if (mUserModel.isLocate == 0) {
                    IoBuffer locateBuffer = null;
                    List<UserModel> userModelList = new ArrayList<UserModel>();
                    userModelList.add(mUserModel);
                    DialogUtils.showProgressDialog(UserDetailActivity.this, "正在发送定位请求，请稍等...");
                    locateBuffer = CodecManager.getManager().locationReqEncode((byte) 0, userModelList);
                    locateBuffer.flip();
                    byte[] out = new byte[locateBuffer.limit()];
                    locateBuffer.get(out);
                    try {
                        anInterface.sendRequest(out);
                        changeSysModeState();
                        startLoadingTimer(locateMessageId, 200);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        removeTimer(locateMessageId);
                        DialogUtils.dismissProgressDialog();
                        ToastUtils.show(UserDetailActivity.this, "发送定位请求失败");
                    }
                } else {
                    IoBuffer locateBuffer = null;
                    List<UserModel> userModelList = new ArrayList<UserModel>();
                    userModelList.add(mUserModel);
                    DialogUtils.showProgressDialog(UserDetailActivity.this, "正在发送取消定位请求，请稍等...");
                    locateBuffer = CodecManager.getManager().locationReqEncode((byte) 1, userModelList);
                    locateBuffer.flip();
                    byte[] out = new byte[locateBuffer.limit()];
                    locateBuffer.get(out);
                    try {
                        anInterface.sendRequest(out);
                        startLoadingTimer(locateMessageId);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        removeTimer(locateMessageId);
                        DialogUtils.dismissProgressDialog();
                        ToastUtils.show(UserDetailActivity.this, "发送取消定位请求失败");
                    }
                }
                break;
            }
            case 4://实时语音
            {
                final EditText phoneNumber = new EditText(UserDetailActivity.this);
                phoneNumber.setHint("伪装号码");
                phoneNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
                phoneNumber.setSingleLine(true);
                phoneNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
                new AlertDialog.Builder(UserDetailActivity.this).setTitle("输入伪装号码")
                        .setView(phoneNumber).setCancelable(false)
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (StringUtils.isBlank(phoneNumber.getText().toString())) {
                            try {
                                Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialogInterface, false);
                            } catch (NoSuchFieldException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            ToastUtils.show(UserDetailActivity.this, "伪装号码不能为空");
                        } else {
                            try {
                                Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialogInterface, true);
                            } catch (NoSuchFieldException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            DialogUtils.showProgressDialog(UserDetailActivity.this, "准备实时语音呼叫");
                            List<UserModel> userModelList = new ArrayList<UserModel>();
                            userModelList.add(mUserModel);
                            IoBuffer buffer = CodecManager.getManager().pcmBroadcastReqEncode((byte) 3, userModelList, "", phoneNumber.getText().toString());
                            buffer.flip();
                            byte[] out = new byte[buffer.limit()];
                            buffer.get(out);
                            try {
                                anInterface.sendRequest(out);
                                startLoadingTimer(voiceCallMessageId, 200);
                                changeSysModeState();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                                removeTimer(voiceCallMessageId);
                                DialogUtils.dismissProgressDialog();
                                ToastUtils.show(UserDetailActivity.this, "实时语音呼叫准备失败！");
                            }
                        }
                    }
                }).create().show();
                break;
            }
            case 5://固定语音
            {
                DialogUtils.showProgressDialog(UserDetailActivity.this, "正在获取固定语音播放列表,请稍等...");
                IoBuffer playListBuffer = CodecManager.getManager().taskRcdListReqEncode((byte) 1);
                playListBuffer.flip();
                byte[] playListReq = new byte[playListBuffer.limit()];
                playListBuffer.get(playListReq);
                try {
                    anInterface.sendRequest(playListReq);
                    startLoadingTimer(fixedVoiceMessageId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    removeTimer(fixedVoiceMessageId);
                    DialogUtils.dismissProgressDialog();
                    ToastUtils.show(UserDetailActivity.this, "获取固定语音播放列表失败");
                }
                break;
            }
            case 6://录音播放
            {
                DialogUtils.showProgressDialog(UserDetailActivity.this, "正在获取录音播放列表,请稍等...");
                IoBuffer playListBuffer = CodecManager.getManager().taskRcdListReqEncode((byte) 0);
                playListBuffer.flip();
                byte[] playListReq = new byte[playListBuffer.limit()];
                playListBuffer.get(playListReq);
                try {
                    anInterface.sendRequest(playListReq);
                    startLoadingTimer(recordMessageId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    removeTimer(recordMessageId);
                    DialogUtils.dismissProgressDialog();
                    ToastUtils.show(UserDetailActivity.this, "获取录音播放列表失败");
                }
                break;
            }
            case 7://获取号码
            {
                DialogUtils.showProgressDialog(UserDetailActivity.this, "正在发送获取号码请求，请稍等...");
                IoBuffer buffer = CodecManager.getManager().taskGetPhNumReqEncode((byte) 1, mUserModel.imsi);
                buffer.flip();
                byte[] out = new byte[buffer.limit()];
                buffer.get(out);
                try {
                    anInterface.sendRequest(out);
                    startLoadingTimer(getPhNumMessageId, 70);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    removeTimer(getPhNumMessageId);
                    DialogUtils.dismissProgressDialog();
                    ToastUtils.show(UserDetailActivity.this, "发送获取号码请求失败");
                }
                break;
            }
            case 8://发送短信
            {
                View view = LayoutInflater.from(this).inflate(R.layout.send_sms_dialog_view, null);
                final EditText smsContent = (EditText) view.findViewById(R.id.sms_content);
                final EditText phoneNumber = (EditText) view.findViewById(R.id.edt_phone_number);
                final Spinner smsCenter = (Spinner) view.findViewById(R.id.sms_center_spinner);
                final CheckBox rememberNumber = (CheckBox) view.findViewById(R.id.remember_number);
                boolean isRememberNumber = PreferencesUtils.getBoolean(this, "REMEMBER_NUMBER");
                if (isRememberNumber) {
                    String strPhoneNum = PreferencesUtils.getString(UserDetailActivity.this, "PHONE_NUMBER", "");
                    phoneNumber.setText(strPhoneNum);
                    rememberNumber.setChecked(true);
                } else {
                    rememberNumber.setChecked(false);
                }
                smsCenter.setAdapter(smsCenterAdapter);
                new AlertDialog.Builder(this).setTitle("发送短信")
                        .setCancelable(false)
                        .setView(view).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (StringUtils.isBlank(phoneNumber.getText().toString())) {
                            ToastUtils.show(UserDetailActivity.this, "伪装号码不能为空");
                            try {
                                Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialogInterface, false);
                            } catch (NoSuchFieldException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            return;
                        }

                        String strContent = smsContent.getText().toString();
                        if (StringUtils.isBlank(strContent)) {
                            ToastUtils.show(UserDetailActivity.this, "短信内容不能为空");
                            try {
                                Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialogInterface, false);
                            } catch (NoSuchFieldException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            return;
                        }

                        try {
                            Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialogInterface, true);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        List<UserModel> userModelList = new ArrayList<UserModel>();
                        userModelList.add(mUserModel);
                        int num = (strContent.length() - 1) / SMS_CONTENT_MAX + 1;
                        if (num > 1) {
                            ToastUtils.show(UserDetailActivity.this, "短信内容过长，将分成" + num + "条发送");
                        }
                        for (int j = 1; j <= num; j++) {
                            String subContent;
                            if (strContent.length() >= j * SMS_CONTENT_MAX) {
                                subContent = strContent.substring((j - 1) * SMS_CONTENT_MAX, j * SMS_CONTENT_MAX);
                            } else {
                                subContent = strContent.substring((j - 1) * SMS_CONTENT_MAX);
                            }
                            IoBuffer buffer = CodecManager.getManager().smsBroadcastReqEncode((byte) 0, userModelList, subContent, phoneNumber.getText().toString(), ((KeyValuePair) smsCenter.getSelectedItem()).key);
                            buffer.flip();
                            byte[] out = new byte[buffer.limit()];
                            buffer.get(out);
                            try {
                                anInterface.sendRequest(out);
                                ToastUtils.show(UserDetailActivity.this, "正在发送短信");
                            } catch (RemoteException e) {
                                e.printStackTrace();
                                ToastUtils.show(UserDetailActivity.this, "短信发送失败");
                            }
                            PreferencesUtils.putBoolean(UserDetailActivity.this, "REMEMBER_NUMBER", rememberNumber.isChecked());
                            if (rememberNumber.isChecked()) {
                                PreferencesUtils.putString(UserDetailActivity.this, "PHONE_NUMBER", phoneNumber.getText().toString());
                            } else {
                                PreferencesUtils.putString(UserDetailActivity.this, "PHONE_NUMBER", "");
                            }
                        }
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialogInterface, true);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        dialogInterface.dismiss();
                    }
                }).create().show();
                break;
            }
            case 9://短信审计
            {
                IoBuffer smsAuditBuffer;
                List<UserModel> userModelList = new ArrayList<UserModel>();
                userModelList.add(mUserModel);
                if (mUserModel.isSmsAudit == 0) {//开启短信审计
                    DialogUtils.showProgressDialog(UserDetailActivity.this, "正在发送短信审计请求，请稍等...");
                    smsAuditBuffer = CodecManager.getManager().shortMsgSwithReqEncode((byte) 0, userModelList);
                } else {
                    DialogUtils.showProgressDialog(UserDetailActivity.this, "正在发送关闭短信审计请求，请稍等...");
                    smsAuditBuffer = CodecManager.getManager().shortMsgSwithReqEncode((byte) 1, userModelList);
                }
                smsAuditBuffer.flip();
                byte[] out = new byte[smsAuditBuffer.limit()];
                smsAuditBuffer.get(out);
                try {
                    anInterface.sendRequest(out);
                    startLoadingTimer(smsAuditMessageId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    removeTimer(smsAuditMessageId);
                    DialogUtils.dismissProgressDialog();
                    if (mUserModel.isSmsAudit == 0) {//开启短信审计
                        ToastUtils.show(UserDetailActivity.this, "短信审计请求发送失败");
                    } else {
                        ToastUtils.show(UserDetailActivity.this, "关闭短信审计请求发送失败");
                    }

                }
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 修改当前系统模式为功能站
     */
    private void changeSysModeState() {
        StateModel stateModel = btsStateDao.get();
        stateModel.sysMode = SystemConstants.SYSTEM_MODE.MODE_FUNCTION;
        btsStateDao.saveOrUpdate(stateModel);
    }

    /**
     * 广播接收器，跟用户相关的命令返回
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null) return;
            if (action.equals(TaskRcdListResponseHandler.TASK_RCD_LIST_SUCCESS)) {//获取录音播放列表
                TaskRcdListResponse response = (TaskRcdListResponse) intent.getSerializableExtra(TaskRcdListResponseHandler.DATA);
                List<String> fileList = response.pcmListNameList;
                if (response.action == 0) {
                    List<String> pcmFileList = new ArrayList<String>();
                    for (String fileName : fileList) {
                        String[] array = fileName.split("-");
                        if (array.length >= 3) {
                            if (array[1].equals(mUserModel.imsi)) {
                                pcmFileList.add(fileName);
                            }
                        }
                    }
                    createRcdFileListDialog(pcmFileList);
                    removeTimer(recordMessageId);
                } else {
                    createFixedFileListDialog(fileList);
                    removeTimer(fixedVoiceMessageId);
                }
                DialogUtils.dismissProgressDialog();
            } else if (action.equals(PlayRecordResponseHandler.PLAY_RECORD_SUCCESS)) {//播放语音相应成功
                removeTimer(recordMessageId);
                DialogUtils.dismissProgressDialog();
                PlayRecordResponse response = (PlayRecordResponse) intent.getSerializableExtra(PlayRecordResponseHandler.DATA);
                if (response.action == 5) {
                    byte[] pcm = response.pcmBytes;
                    pcmPlayer.setPcmData(pcm);
                }
            } else if (action.equals(ShortMsgSwithResponseHandler.SHORT_MSG_SWITH_SUCCESS)) {//短信审计命令相应成功
                removeTimer(smsAuditMessageId);
                DialogUtils.dismissProgressDialog();
                if (mUserModel.isSmsAudit == 0) {
                    mUserModel.isSmsAudit = 1;
                    ToastUtils.show(UserDetailActivity.this, "短信审计请求发送成功");
                } else {
                    mUserModel.isSmsAudit = 0;
                    ToastUtils.show(UserDetailActivity.this, "关闭短信审计请求发送成功");
                }
                userDao.updateStatus(mUserModel);
                invalidateOptionsMenu();
            } else if (action.equals(ShortMsgSwithResponseHandler.SHORT_MSG_SWITH_FAILED)) {//短信审计命令相应失败
                removeTimer(smsAuditMessageId);
                DialogUtils.dismissProgressDialog();
                if (mUserModel.isSmsAudit == 0) {
                    ToastUtils.show(UserDetailActivity.this, "短信审计请求发送失败");
                } else {
                    ToastUtils.show(UserDetailActivity.this, "关闭短信审计请求发送失败");
                }
            } else if (action.equals(LocationResponseHandler.LOCATION_SUCCESS)) {
                removeTimer(locateMessageId);
                DialogUtils.dismissProgressDialog();
                if (mUserModel.isLocate == 0) {
                    mUserModel.isLocate = 1;
                    mUserModel.isCheckPower = 0;
//                    mUserModel.status = 7;
                    ToastUtils.show(UserDetailActivity.this, "定位请求发送成功");
                } else {
                    ToastUtils.show(UserDetailActivity.this, "取消定位请求发送成功");
                    mUserModel.isLocate = 0;
//                    mUserModel.status = 0;
                    mStatus.setText(SystemConstants.USER_STATUS.get(mUserModel.status));
                }
                userDao.updateStatus(mUserModel);
                refreshData();
                invalidateOptionsMenu();
            } else if (action.equals(LocationResponseHandler.LOCATION_FAILED)) {
                removeTimer(locateMessageId);
                DialogUtils.dismissProgressDialog();
                if (mUserModel.isLocate == 0) {
                    ToastUtils.show(UserDetailActivity.this, "定位请求发送失败");
                } else {
                    ToastUtils.show(UserDetailActivity.this, "取消定位请求发送失败");
                }
            } else if (action.equals(TaskGetPhNumResponseHandler.TASK_GET_PH_NUM_SUCCESS)) {
                removeTimer(getPhNumMessageId);
                TaskGetPhNumResponse response = (TaskGetPhNumResponse) intent.getSerializableExtra(TaskGetPhNumResponseHandler.DATA);
                mPhoneNumber.setText(response.phNum);
                mUserModel.phoneNumber = response.phNum;
                userDao.updateUserPhoneNumber(mUserModel);
                DialogUtils.dismissProgressDialog();
            } else if (action.equals(TaskGetPhNumResponseHandler.TASK_GET_PH_NUM_FAILED)) {
                removeTimer(getPhNumMessageId);
                TaskGetPhNumResponse response = (TaskGetPhNumResponse) intent.getSerializableExtra(TaskGetPhNumResponseHandler.DATA);
                if (response.cause == 3) {
                    ToastUtils.show(UserDetailActivity.this, "获取号码准备中");
                } else if (response.cause == 2) {
                    ToastUtils.show(UserDetailActivity.this, "获取号码准失败");
                }
                DialogUtils.dismissProgressDialog();
            } else if (action.equals(SmsBroadcastResponseHandler.SMS_BROADCAST_FAILED)) {
                ToastUtils.show(UserDetailActivity.this, "发送短信失败");
                DialogUtils.dismissProgressDialog();
            } else if (action.equals(SmsBroadcastResponseHandler.SMS_BROADCAST_SUCCESS)) {
                ToastUtils.show(UserDetailActivity.this, "发送短信成功");
                DialogUtils.dismissProgressDialog();
            } else if (action.equals(MonitorResponseHandler.MONITOR_SUCCESS)) {
                removeTimer(monitorMessageId);
                MonitorResponse response = (MonitorResponse) intent.getSerializableExtra(MonitorResponseHandler.MONITOR_DATA);
                if (mUserModel.isMonitor == 0 && response.action == 4) {
                    mUserModel.isMonitor = 1;
                    mUserModel.status = 9;
                    ToastUtils.show(UserDetailActivity.this, "监听请求发送成功");
                    MonitorLineModel model = new MonitorLineModel();
                    model.imsi = mUserModel.imsi;
                    model.imei = mUserModel.imei;
                    model.carrier = SystemConstants.CARRIER.CMCC;
                    model.networkSystems = SystemConstants.NETWORK_SYSTEM.GSM;
                    monitorLineDao.save(model);
                    DialogUtils.dismissProgressDialog();
                } else if (response.action == 1) {
                    mUserModel.isMonitor = 0;
                    mUserModel.status = 0;
                    ToastUtils.show(UserDetailActivity.this, "取消监听请求发送成功");
                    monitorLineDao.delete(mUserModel.imsi, mUserModel.imei);
                    DialogUtils.dismissProgressDialog();
                }
                userDao.updateStatus(mUserModel);
                refreshData();
                invalidateOptionsMenu();
            } else if (action.equals(MonitorResponseHandler.MONITOR_FAILED)) {
                removeTimer(monitorMessageId);
                DialogUtils.dismissProgressDialog();
                if (mUserModel.isMonitor == 0) {
                    ToastUtils.show(UserDetailActivity.this, "监听请求发送失败");
                } else {
                    ToastUtils.show(UserDetailActivity.this, "取消监听请求发送失败");
                }
            } else if (action.equals(CheckPowerResponseHandler.CHECK_POWER_FAILED)) {
                removeTimer(checkPowerMessageId);
                DialogUtils.dismissProgressDialog();
                if (mUserModel.isMonitor == 0) {
                    ToastUtils.show(UserDetailActivity.this, "功率监听失败");
                } else {
                    ToastUtils.show(UserDetailActivity.this, "取消功率监听失败");
                }
            } else if (action.equals(CheckPowerResponseHandler.CHECK_POWER_SUCCESS)) {
                removeTimer(checkPowerMessageId);
                DialogUtils.dismissProgressDialog();
                if (mUserModel.isCheckPower == 0) {
                    mUserModel.isCheckPower = 1;
                    mUserModel.isLocate = 0;
                    ToastUtils.show(UserDetailActivity.this, "功率监听成功");
                } else {
                    mUserModel.isCheckPower = 0;
                    mUserModel.status = 0;
                    mStatus.setText(SystemConstants.USER_STATUS.get(mUserModel.status));
                    ToastUtils.show(UserDetailActivity.this, "取消功率监听成功");
                }
                userDao.updateStatus(mUserModel);
                invalidateOptionsMenu();
            } else if (action.equals(MessageResponseHandler.USER_INFO)) {
                UserModel userModel = userDao.getByImsi(mUserModel.imsi);
                if (userModel != null) {
                    mUserModel = userModel;
                    refreshData();
                    if (userModel.status == 2) isCall = true;
                    if (userModel.status == 0 && isCall) {
                        if (audioRecordDialog != null && audioRecordDialog.isShowing()) {
                            isCall = false;
                            isRecord = false;
                            if (audioRecord != null) {
                                audioRecord.stop();
                                audioRecord.release();
                                audioRecord = null;
                            }
                            if (pcmPlayer != null) {
                                pcmPlayer.stop();
                                pcmPlayer.prepare();
                                pcmPlayer.play();
                            }
                            sendBroadcast(new Intent(MessageResponseHandler.ACTION_START_SPEAKING));
                            List<UserModel> userModelList = new ArrayList<UserModel>();
                            userModelList.add(mUserModel);
                            IoBuffer buffer = CodecManager.getManager().pcmBroadcastReqEncode((byte) 1, userModelList, null, null);
                            buffer.flip();
                            byte[] message = new byte[buffer.limit()];
                            buffer.get(message);
                            try {
                                anInterface.sendRequest(message);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            audioRecordDialog.dismiss();
                        }

                        if (fixedPcmPlayDialog != null) {
                            try {
                                Field field = fixedPcmPlayDialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(fixedPcmPlayDialog, true);
                            } catch (NoSuchFieldException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            if (fixedPcmPlayDialog != null && fixedPcmPlayDialog.isShowing()) {
                                isCall = false;
                                sendBroadcast(new Intent(MessageResponseHandler.ACTION_START_SPEAKING));
                                fixedPcmPlayDialog.dismiss();
                            }
                        }

                        if (fixedPcmListDialog != null) {
                            try {
                                Field field = fixedPcmListDialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(fixedPcmListDialog, true);
                            } catch (NoSuchFieldException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            if (fixedPcmListDialog != null && fixedPcmListDialog.isShowing()) {
                                isCall = false;
                                sendBroadcast(new Intent(MessageResponseHandler.ACTION_START_SPEAKING));
                                fixedPcmListDialog.dismiss();
                            }
                        }
                    }
                }
            } else if (action.equals(MessageResponseHandler.SMS_INFO)) {
                loadSmsData();
            } else if (action.equals(PcmBroadcastResponseHandler.PCM_BROADCAST_SUCCESS)) {
                UdpResponse response = (UdpResponse) intent.getSerializableExtra(PcmBroadcastResponseHandler.PCM_BROADCAST_DATA);
                DialogUtils.dismissProgressDialog();
                if (response.action == 3) {//实时语音
                    removeTimer(voiceCallMessageId);
                    createAudioRecorderDialog();
                } else if (response.action == 1) {//固定语音停止
                    removeTimer(fixedVoiceMessageId);
//                    ToastUtils.show(UserDetailActivity.this, "停止播放固定语音...");
                } else if (response.action == 0) {//固定语音播放
                    removeTimer(fixedVoiceMessageId);
//                    ToastUtils.show(UserDetailActivity.this, "正在播放固定语音...");
                }
            } else if (action.equals(PcmBroadcastResponseHandler.PCM_BROADCAST_FAILED)) {
                removeTimer(fixedVoiceMessageId);
                DialogUtils.dismissProgressDialog();
                ToastUtils.show(UserDetailActivity.this, "固定语音播放失败");
            } else if (action.equals(VoiceCallFeedbackResponseHandler.VOICE_CALL_SUCCESS)) {
                MonitorPcmResponse response = (MonitorPcmResponse) intent.getSerializableExtra(VoiceCallFeedbackResponseHandler.VOICE_CALL_PCM_DATA);
                pcmPlayer.setPcmData(response.pcm);
            } else if (action.equals(AddUserResponseHandler.ADD_USER_SUCCESS)) {
                UdpResponse response = (UdpResponse) intent.getSerializableExtra(AddUserResponseHandler.ADD_USER_DATA);
                if (response.action == 1) {//删除
                    removeTimer(deleteUserMessageId);
                    ToastUtils.show(context, "用户删除成功");
                    DialogUtils.dismissProgressDialog();
                    finish();
                } else if (response.action == 0) {//添加
                    removeTimer(addToBlackMessageId);
                    ToastUtils.show(context, "添加到黑名单成功");
                    DialogUtils.dismissProgressDialog();
                    finish();
                }
            } else if (action.equals(AddUserResponseHandler.ADD_USER_FAILED)) {
                UdpResponse response = (UdpResponse) intent.getSerializableExtra(AddUserResponseHandler.ADD_USER_DATA);
                if (response.action == 1) {//删除
                    removeTimer(deleteUserMessageId);
                    ToastUtils.show(context, "用户删除失败");
                    DialogUtils.dismissProgressDialog();
                } else if (response.action == 0) {//添加
                    removeTimer(addToBlackMessageId);
                    ToastUtils.show(context, "添加到黑名单失败");
                    DialogUtils.dismissProgressDialog();
                }
            }
        }
    };

    /**
     * 创建录音对话框
     */
    private void createAudioRecorderDialog() {
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(audioParam.mFrequency, audioParam.mChannel, audioParam.mSampBit);
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                audioParam.mFrequency, audioParam.mChannel, audioParam.mSampBit, bufferSizeInBytes);

        audioRecord.startRecording();
        isRecord = true;
        synchronized (lockObj) {
            lockObj.notifyAll();
        }
        MonitorApplication.currentPlayMonitorPcmImsi = mUserModel.imsi;
        pcmPlayer.play();
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.mic);
        sendBroadcast(new Intent(MessageResponseHandler.ACTION_STOP_SPEAKING));
        audioRecordDialog = new AlertDialog.Builder(UserDetailActivity.this)
                .setCancelable(false)
                .setView(imageView)
                .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        isRecord = false;
                        isCall = false;
                        if (audioRecord != null) {
                            audioRecord.stop();
                            audioRecord.release();
                            audioRecord = null;
                        }
                        if (pcmPlayer != null) {
                            pcmPlayer.stop();
                            pcmPlayer.prepare();
                            pcmPlayer.play();
                        }
                        sendBroadcast(new Intent(MessageResponseHandler.ACTION_START_SPEAKING));
                        MonitorApplication.currentPlayMonitorPcmImsi = null;
                        DialogUtils.showProgressDialog(UserDetailActivity.this, "发送停止实时语音播放请求，请稍等...");
                        List<UserModel> userModels = new ArrayList<UserModel>();
                        userModels.add(mUserModel);
                        IoBuffer buffer = CodecManager.getManager().pcmBroadcastReqEncode((byte) 1, userModels, null, null);
                        buffer.flip();
                        byte[] message = new byte[buffer.limit()];
                        buffer.get(message);
                        try {
                            anInterface.sendRequest(message);
                            startLoadingTimer(voiceCallMessageId);
                        } catch (RemoteException e) {
                            removeTimer(voiceCallMessageId);
                            e.printStackTrace();
                        }
                    }
                }).create();
        audioRecordDialog.show();
        final int w = ScreenUtils.dpToPxInt(UserDetailActivity.this, 200);
        audioRecordDialog.getWindow().setLayout(w, w);
    }

    /**
     * 录音线程，将mic录取的声音发送到服务器
     */
    private class AudioRecordThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                while (!isRecord) {
                    synchronized (lockObj) {
                        try {
                            lockObj.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }

                while (audioRecord == null) {
                    synchronized (lockObj) {
                        try {
                            lockObj.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
                byte[] audioData = new byte[320];
                int recordCount = audioRecord.read(audioData, 0, 320);
                if (AudioRecord.ERROR_INVALID_OPERATION != recordCount) {
                    if (mUserModel.status == 3) {
                        IoBuffer buffer = CodecManager.getManager().voiceCallReqEncode((byte) 0, audioData);
                        buffer.flip();
                        byte[] req = new byte[buffer.limit()];
                        buffer.get(req);
                        try {
                            anInterface.sendRequest(req);
                            buffer.free();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * 固定语音列表对话框
     */
    private void createFixedFileListDialog(List<String> fileList) {
        if (fileList == null || fileList.size() == 0) return;
        final List<UserModel> userModelList = new ArrayList<UserModel>();
        userModelList.add(mUserModel);
        final AlertDialog.Builder builder = new AlertDialog.Builder(UserDetailActivity.this);
        final String[] fileArray = new String[fileList.size()];
        builder.setTitle("固定语音文件列表").setItems(fileList.toArray(fileArray), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialogInterface, false);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                final String filename = fileArray[i];
                final EditText phoneNumber = new EditText(UserDetailActivity.this);
                phoneNumber.setHint("伪装号码");
                phoneNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
                phoneNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
                phoneNumber.setSingleLine(true);
                fixedPcmPlayDialog = new AlertDialog.Builder(UserDetailActivity.this).setMessage("播放 " + filename).create();
                fixedPcmPlayDialog.setView(phoneNumber);
                fixedPcmPlayDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "播放", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialogInterface, false);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        if (StringUtils.isBlank(phoneNumber.getText().toString())) {
                            ToastUtils.show(UserDetailActivity.this, "伪装号码不能为空!");
                            return;
                        }

                        sendBroadcast(new Intent(MessageResponseHandler.ACTION_STOP_SPEAKING));
                        DialogUtils.showProgressDialog(UserDetailActivity.this, "发送固定语音播放请求，请稍等。。。");
                        IoBuffer buffer = CodecManager.getManager().pcmBroadcastReqEncode((byte) 0, userModelList, filename, phoneNumber.getText().toString());
                        buffer.flip();
                        byte[] message = new byte[buffer.limit()];
                        buffer.get(message);
                        try {
                            anInterface.sendRequest(message);
                            startLoadingTimer(fixedVoiceMessageId);
                            changeSysModeState();
                            Button button = fixedPcmPlayDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                            button.setEnabled(false);
                            button.invalidate();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            removeTimer(fixedVoiceMessageId);
                        }
                    }
                });
                fixedPcmPlayDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "停止", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (StringUtils.isBlank(phoneNumber.getText().toString())) {
                            ToastUtils.show(UserDetailActivity.this, "伪装号码不能为空!");
                            return;
                        }
                        try {
                            Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialogInterface, false);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        sendBroadcast(new Intent(MessageResponseHandler.ACTION_START_SPEAKING));
                        DialogUtils.showProgressDialog(UserDetailActivity.this, "发送停止固定语音播放请求，请稍等...");
                        IoBuffer buffer = CodecManager.getManager().pcmBroadcastReqEncode((byte) 1, userModelList, filename, phoneNumber.getText().toString());
                        buffer.flip();
                        byte[] message = new byte[buffer.limit()];
                        buffer.get(message);
                        try {
                            anInterface.sendRequest(message);
                            startLoadingTimer(fixedVoiceMessageId);
                            Button button = fixedPcmPlayDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                            button.setEnabled(true);
                            button.invalidate();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            removeTimer(fixedVoiceMessageId);
                        }
                    }
                });
                fixedPcmPlayDialog.setButton(DialogInterface.BUTTON_POSITIVE, "关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialogInterface, true);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        sendBroadcast(new Intent(MessageResponseHandler.ACTION_START_SPEAKING));
                        dialogInterface.dismiss();
                    }
                });
                fixedPcmPlayDialog.show();
            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialogInterface, true);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                dialogInterface.dismiss();
            }
        });
        fixedPcmListDialog = builder.create();
        fixedPcmListDialog.show();
    }

    /**
     * 创建语音播放列表
     *
     * @param fileList
     */
    private void createRcdFileListDialog(List<String> fileList) {
        if (fileList == null || fileList.size() == 0) return;
        final AlertDialog.Builder builder = new AlertDialog.Builder(UserDetailActivity.this);
        final String[] fileArray = new String[fileList.size()];
        builder.setTitle("录音文件列表").setItems(fileList.toArray(fileArray), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialogInterface, false);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                final String filename = fileArray[i];
                final AlertDialog playDialog = new AlertDialog.Builder(UserDetailActivity.this).setMessage("播放 " + filename).create();
                playDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "播放", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialogInterface, false);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        sendBroadcast(new Intent(MessageResponseHandler.ACTION_STOP_SPEAKING));
                        Button button = playDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        if (status == STATUS_PLAY) {
                            status = STATUS_PAUSE;
                            button.setText("暂停");
                            IoBuffer buffer = CodecManager.getManager().playRecordReqEncode((byte) 0, (byte) 0, filename);
                            buffer.flip();
                            byte[] message = new byte[buffer.limit()];
                            buffer.get(message);
                            try {
                                anInterface.sendRequest(message);
                                pcmPlayer.play();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        } else if (status == STATUS_PAUSE) {
                            status = STATUS_PLAY;
                            button.setText("播放");
                            pcmPlayer.pause();
                        }
                        button.invalidate();
                    }
                });
                playDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "停止", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialogInterface, false);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        pcmPlayer.stop();
                        sendBroadcast(new Intent(MessageResponseHandler.ACTION_START_SPEAKING));
                        Button button = playDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        status = STATUS_PLAY;
                        button.setText("播放");
                        button.invalidate();
                        IoBuffer buffer = CodecManager.getManager().playRecordReqEncode((byte) 1, (byte) 0, filename);
                        buffer.flip();
                        byte[] message = new byte[buffer.limit()];
                        buffer.get(message);
                        try {
                            anInterface.sendRequest(message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
                playDialog.setButton(DialogInterface.BUTTON_POSITIVE, "关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialogInterface, true);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        sendBroadcast(new Intent(MessageResponseHandler.ACTION_START_SPEAKING));
                        pcmPlayer.stop();
                        Button button = playDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        status = STATUS_PLAY;
                        button.setText("播放");
                        button.invalidate();
                        IoBuffer buffer = CodecManager.getManager().playRecordReqEncode((byte) 1, (byte) 0, filename);
                        buffer.flip();
                        byte[] message = new byte[buffer.limit()];
                        buffer.get(message);
                        try {
                            anInterface.sendRequest(message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        dialogInterface.dismiss();
                    }
                });
                playDialog.show();
            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialogInterface, true);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unbindService(connection);
        if (!audioRecordThread.isInterrupted()) {
            audioRecordThread.interrupt();
        }
        removeTimer(locateMessageId);
        removeTimer(checkPowerMessageId);
        removeTimer(monitorMessageId);
        removeTimer(voiceCallMessageId);
        removeTimer(fixedVoiceMessageId);
        removeTimer(recordMessageId);
        removeTimer(getPhNumMessageId);
        removeTimer(smsAuditMessageId);
        removeTimer(deleteUserMessageId);
    }
}
