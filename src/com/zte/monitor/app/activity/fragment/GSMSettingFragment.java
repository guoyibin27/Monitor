package com.zte.monitor.app.activity.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.zte.monitor.app.R;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.activity.BaseActivity;
import com.zte.monitor.app.activity.ScanFrequencyResultActivity;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.database.dao.BtsStateDao;
import com.zte.monitor.app.database.dao.SettingDao;
import com.zte.monitor.app.database.dao.UserDao;
import com.zte.monitor.app.handler.ConfigParamResponseHandler;
import com.zte.monitor.app.handler.MessageResponseHandler;
import com.zte.monitor.app.handler.ScanFreqResponseHandler;
import com.zte.monitor.app.handler.TaskConfigResponseHandler;
import com.zte.monitor.app.model.KeyValuePair;
import com.zte.monitor.app.model.SettingModel;
import com.zte.monitor.app.model.StateModel;
import com.zte.monitor.app.model.response.MsgResponse;
import com.zte.monitor.app.model.response.ScanFreqResponse;
import com.zte.monitor.app.udp.UdpDataSendService;
import com.zte.monitor.app.udp.aidl.IUdpConnectionInterface;
import com.zte.monitor.app.util.DialogUtils;
import com.zte.monitor.app.util.StringUtils;
import com.zte.monitor.app.util.ToastUtils;
import org.apache.mina.core.buffer.IoBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Sylar on 14-9-13.
 */
public class GSMSettingFragment extends Fragment {

    private Spinner mOperator;
    private EditText mStationFrequency;
    //    private EditText mNFrequency;
    private Button mGetStationFrequency;
    private Spinner mUserDensity;
    private Spinner mPowerLevel;
    private RadioGroup mTakeOverGroup;
    private CheckBox mAutoSendSMS;
    private EditText mAutoSendSMSInterval;
    private RadioButton takeOverRadioButton;
    private RadioButton locateRadioButton;
    private Button saveButton;
    private IUdpConnectionInterface anInterface;
    private int takeOverMode = SystemConstants.TAKE_OVER_MODE.LOCATE;
    private EditText cmccMonitorLineCount;
    private EditText chinaUnicomMonitorCount;

    private BtsStateDao btsStateDao;
    private SettingDao settingDao;
    private UserDao userDao;
    //选择 小区还是自动扫频
    private int selectedItem;

    private int setConfMessageId = 20000;
    private int startServerMessageId = 20002;
    private int scanMessageId = 20001;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        btsStateDao = new BtsStateDao(getActivity());
        settingDao = new SettingDao(getActivity());
        userDao = new UserDao(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.gsm_setting_fragment, null);
        mOperator = (Spinner) view.findViewById(R.id.sp_operator);
        mStationFrequency = (EditText) view.findViewById(R.id.station_frequency);
        mGetStationFrequency = (Button) view.findViewById(R.id.button_get_station_frequency);
        mUserDensity = (Spinner) view.findViewById(R.id.sp_user_density);
        mPowerLevel = (Spinner) view.findViewById(R.id.sp_power);
        mTakeOverGroup = (RadioGroup) view.findViewById(R.id.radio_group);
        mAutoSendSMS = (CheckBox) view.findViewById(R.id.cbx_auto_send_sms);
        mAutoSendSMSInterval = (EditText) view.findViewById(R.id.auto_send_sms_interval);
//        mNFrequency = (EditText) view.findViewById(R.id.frequency_edt);
        cmccMonitorLineCount = (EditText) view.findViewById(R.id.monitor_line_count_cmcc);
        chinaUnicomMonitorCount = (EditText) view.findViewById(R.id.monitor_line_count_china_unicom);
        takeOverRadioButton = (RadioButton) view.findViewById(R.id.take_over_radio);
        locateRadioButton = (RadioButton) view.findViewById(R.id.locate_radio);

        initOperatorSpinner();
        initUserDensitySpinner();
        initPowerLevelSpinner();

        mTakeOverGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int id = radioGroup.getCheckedRadioButtonId();
                switch (id) {
                    case R.id.take_over_radio://反制,接管
                        takeOverMode = SystemConstants.TAKE_OVER_MODE.TAKE_OVER;
                        break;
                    case R.id.locate_radio://定位
                        takeOverMode = SystemConstants.TAKE_OVER_MODE.LOCATE;
                        break;
                }

            }
        });

        mAutoSendSMS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mAutoSendSMS.isChecked()) {
                    mAutoSendSMSInterval.setEnabled(true);
                } else {
                    mAutoSendSMSInterval.setText("");
                    mAutoSendSMSInterval.setEnabled(false);
                }
            }
        });

        mGetStationFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("扫频")
                        .setItems(R.array.frequency, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                selectedItem = i;
                                readyToScan();
                            }
                        }).create().show();
            }
        });

        saveButton = (Button) view.findViewById(R.id.save);

        StateModel stateModel = btsStateDao.get();
        if (stateModel != null && stateModel.btsState == SystemConstants.BTS_STATUS.STATION_START_SUCCESS) {
            mOperator.setEnabled(false);
            saveButton.setText("设置");
        } else {
            mOperator.setEnabled(true);
            saveButton.setText("启动");
        }

        SettingModel settingModel = settingDao.getSetting();
        if (settingModel != null) {
            if (!StringUtils.isBlank(settingModel.operator)) {
                if ("1".equals(settingModel.operator)) {
                    mOperator.setSelection(1);
                } else {
                    mOperator.setSelection(0);
                }
            }
            if (settingModel.stationFrequency != 0) {
                mStationFrequency.setText(settingModel.stationFrequency + "");
            }
//            if (settingModel.regionFrequency != 0) {
//                mNFrequency.setText(settingModel.regionFrequency + "");
//            }
            if (settingModel.cmccMonitorCount != 0) {
                cmccMonitorLineCount.setText(settingModel.cmccMonitorCount + "");
            }
            if (settingModel.cuccMonitorCount != 0) {
                chinaUnicomMonitorCount.setText(settingModel.cuccMonitorCount + "");
            }
            if (settingModel.takeOverMode == SystemConstants.TAKE_OVER_MODE.LOCATE || settingModel.takeOverMode == 0) {
                locateRadioButton.setChecked(true);
                takeOverRadioButton.setChecked(false);
            } else {
                locateRadioButton.setChecked(false);
                takeOverRadioButton.setChecked(true);
            }
            if (settingModel.intensity != 0) {
                mUserDensity.setSelection(settingModel.intensity);
            }
            if (settingModel.power != 0) {
                mPowerLevel.setSelection(settingModel.power);
            }
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyValuePair mnc = (KeyValuePair) mOperator.getSelectedItem();
                String freq = mStationFrequency.getText().toString();
                if (StringUtils.isBlank(freq.trim())) {
                    ToastUtils.show(getActivity(), "基站频点不能为空");
                    return;
                }
                int intFreq = Integer.parseInt(freq);
                if (mnc.key.equals("0") &&
                        !((intFreq >= 1 && intFreq <= 94) || (intFreq >= 512 && intFreq <= 561) && (intFreq >= 587 && intFreq <= 636))) {
                    //中国移动 1~94、512~561以及587~636
                    ToastUtils.show(getActivity(), "基站频点输入不合法，请重新输入");
                    return;
                } else if (mnc.key.equals("1") &&
                        !((intFreq >= 96 && intFreq <= 124) || (intFreq >= 661 && intFreq <= 735))) {
                    //中国联通 96~124、661~735
                    ToastUtils.show(getActivity(), "基站频点输入不合法，请重新输入");
                    return;
                }
//                String strFreq = mNFrequency.getText().toString();
//                if (StringUtils.isBlank(strFreq.trim())) {
//                    ToastUtils.show(getActivity(), "邻区频点不能为空");
//                    return;
//                }

//                int intNFreq = Integer.parseInt(strFreq);
//                if (mnc.key.equals("0")
//                        && !((intNFreq >= 1 && intNFreq <= 94) || (intNFreq >= 512 && intNFreq <= 561) && (intNFreq >= 587 && intNFreq <= 636))) {
//                    ToastUtils.show(getActivity(), "邻区频点输入不合法，请重新输入");
//                    return;
//                } else if (mnc.key.equals("1")
//                        && !((intFreq >= 96 && intFreq <= 124) || (intFreq >= 661 && intFreq <= 735))) {
//                    ToastUtils.show(getActivity(), "邻区频点输入不合法，请重新输入");
//                    return;
//                }

                if (takeOverMode == -1) {
                    ToastUtils.show(getActivity(), "请选择工作模式");
                    return;
                }

                if (StringUtils.isBlank(cmccMonitorLineCount.getText().toString())) {
                    ToastUtils.show(getActivity(), "中国移动监听路数不能为空");
                    return;
                }

                if (StringUtils.isBlank(chinaUnicomMonitorCount.getText().toString())) {
                    ToastUtils.show(getActivity(), "中国联通监听路数不能为空");
                    return;
                }

                if (Integer.valueOf(cmccMonitorLineCount.getText().toString()) + Integer.valueOf(chinaUnicomMonitorCount.getText().toString()) > 4) {
                    ToastUtils.show(getActivity(), "总监听路数不能大于4");
                    return;
                }

                if (mAutoSendSMS.isChecked()) {
                    if (StringUtils.isBlank(mAutoSendSMSInterval.getText().toString())) {
                        ToastUtils.show(getActivity(), "短信自动发送时间不能为空");
                        return;
                    }

                    if (Integer.valueOf(mAutoSendSMSInterval.getText().toString()) > 30) {
                        ToastUtils.show(getActivity(), "自动发送时间必须小于30秒");
                        return;
                    }
                }
                IoBuffer workMode = CodecManager.getManager().workModeReqEncode((byte) 0, (byte) takeOverMode);
                workMode.flip();
                byte[] workModeReq = new byte[workMode.limit()];
                workMode.get(workModeReq);
                try {
                    anInterface.sendRequest(workModeReq);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                KeyValuePair power = (KeyValuePair) mPowerLevel.getSelectedItem();
                KeyValuePair density = (KeyValuePair) mUserDensity.getSelectedItem();
                StateModel stateModel = btsStateDao.get();
                if (stateModel != null && stateModel.btsState == SystemConstants.BTS_STATUS.STATION_START_SUCCESS) {
//                    IoBuffer buffer = CodecManager.getManager().confParamReqEncode((byte) 0, (short) 460, Byte.valueOf(mnc.key), (short) intFreq, (short) intNFreq, new Random().nextInt(65535) + 1, Byte.valueOf(power.key));
                    IoBuffer buffer = CodecManager.getManager().confParamReqEncode((byte) 0, (short) 460, Byte.valueOf(mnc.key), (short) intFreq, (short) 0, new Random().nextInt(65535) + 1, Byte.valueOf(power.key));
                    buffer.flip();
                    byte[] out = new byte[buffer.limit()];
                    buffer.get(out);
                    try {
                        anInterface.sendRequest(out);
                        DialogUtils.showProgressDialog(getActivity(), "参数设置中，请稍后...");
                        ((BaseActivity) getActivity()).startLoadingTimer(setConfMessageId);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        ((BaseActivity) getActivity()).removeTimer(setConfMessageId);
                        DialogUtils.dismissProgressDialog();
                        ToastUtils.show(getActivity(), "参数设置失败");
                    }
                } else {
//                    IoBuffer buffer = CodecManager.getManager().taskCfgReqEncode((byte) 0, (byte) 0, null, (byte) 1,
//                            (short) 460, Byte.valueOf(mnc.key), Byte.valueOf(density.key), (short) intFreq
//                            , Byte.valueOf(power.key), (short) intNFreq, (byte) 1, (byte) 1);
                    IoBuffer buffer = CodecManager.getManager().taskCfgReqEncode((byte) 0, (byte) 0, null, (byte) 1,
                            (short) 460, Byte.valueOf(mnc.key), Byte.valueOf(density.key), (short) intFreq
                            , Byte.valueOf(power.key), (short) 0, (byte) 1, (byte) 1);
                    buffer.flip();
                    byte[] out = new byte[buffer.limit()];
                    buffer.get(out);
                    try {
                        anInterface.sendRequest(out);
                        DialogUtils.showProgressDialog(getActivity(), "启动中，请稍后...");
                        ((BaseActivity) getActivity()).startLoadingTimer(startServerMessageId, 200);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        ((BaseActivity) getActivity()).removeTimer(startServerMessageId);
                        DialogUtils.dismissProgressDialog();
                        ToastUtils.show(getActivity(), "启动中失败");
                    }
                }
            }
        });
        Intent intent = new Intent(getActivity(), UdpDataSendService.class);
        getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ScanFreqResponseHandler.SCAN_FAILED);
        intentFilter.addAction(ScanFreqResponseHandler.SCAN_SUCCESS);
        intentFilter.addAction(ConfigParamResponseHandler.CONF_PARAM_SUCCESS);
        intentFilter.addAction(ConfigParamResponseHandler.CONF_PARAM_FAILED);
        intentFilter.addAction(MessageResponseHandler.BTS_STATE);
        getActivity().registerReceiver(receiver, intentFilter);
        return view;
    }

    private void readyToScan() {
        if (selectedItem == 0) {
            DialogUtils.showProgressDialog(getActivity(), "自动扫频中，请稍后...");
            KeyValuePair valuePair = (KeyValuePair) mOperator.getSelectedItem();
            IoBuffer buffer = CodecManager.getManager().scanFreqReqEncode((byte) 0, Byte.parseByte(valuePair.key));
            buffer.flip();
            byte[] out = new byte[buffer.limit()];
            buffer.get(out);
            try {
                anInterface.sendRequest(out);
                ((BaseActivity) getActivity()).startLoadingTimer(scanMessageId);
            } catch (RemoteException e) {
                e.printStackTrace();
                ((BaseActivity) getActivity()).removeTimer(scanMessageId);
                DialogUtils.dismissProgressDialog();
                ToastUtils.show(getActivity(), "自动扫频请求失败,请重试");

            }
        } else if (selectedItem == 1) {
            DialogUtils.showProgressDialog(getActivity(), "小区信息获取中，请稍后...");
            KeyValuePair valuePair = (KeyValuePair) mOperator.getSelectedItem();
            IoBuffer buffer = CodecManager.getManager().scanFreqReqEncode((byte) 1, Byte.parseByte(valuePair.key));
            buffer.flip();
            byte[] out = new byte[buffer.limit()];
            buffer.get(out);
            try {
                anInterface.sendRequest(out);
                ((BaseActivity) getActivity()).startLoadingTimer(scanMessageId);
            } catch (RemoteException e) {
                e.printStackTrace();
                ((BaseActivity) getActivity()).removeTimer(scanMessageId);
                DialogUtils.dismissProgressDialog();
                ToastUtils.show(getActivity(), "自动扫频请求失败,请重试");

            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(receiver);
        getActivity().unbindService(connection);
        ((BaseActivity) getActivity()).removeTimer(startServerMessageId);
        ((BaseActivity) getActivity()).removeTimer(setConfMessageId);
        ((BaseActivity) getActivity()).removeTimer(scanMessageId);
    }

    private void initPowerLevelSpinner() {
        List<KeyValuePair> levelList = new ArrayList<KeyValuePair>(5);
        for (int i = 5; i != 0; i--) {
            KeyValuePair keyValuePair = new KeyValuePair();
            keyValuePair.key = i + "";
            keyValuePair.value = i + "级";
            levelList.add(keyValuePair);
        }

        ArrayAdapter<KeyValuePair> levelAdapter = new ArrayAdapter<KeyValuePair>(getActivity(), R.layout.spinner_item,
                levelList);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPowerLevel.setAdapter(levelAdapter);
    }

    private void initUserDensitySpinner() {
        KeyValuePair intensive = new KeyValuePair();
        intensive.key = "1";
        intensive.value = "密集";

        KeyValuePair general = new KeyValuePair();
        general.key = "2";
        general.value = "一般";

        KeyValuePair rare = new KeyValuePair();
        rare.key = "3";
        rare.value = "稀少";

        List<KeyValuePair> densityList = new ArrayList<KeyValuePair>(3);
        densityList.add(intensive);
        densityList.add(general);
        densityList.add(rare);

        ArrayAdapter<KeyValuePair> densityAdapter = new ArrayAdapter<KeyValuePair>(getActivity(), R.layout.spinner_item,
                densityList);
        densityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mUserDensity.setAdapter(densityAdapter);
    }

    private void initOperatorSpinner() {
        KeyValuePair chinaMobile = new KeyValuePair();
        chinaMobile.key = "0";
        chinaMobile.value = "中国移动";

        KeyValuePair chinaUnicom = new KeyValuePair();
        chinaUnicom.key = "1";
        chinaUnicom.value = "中国联通";
        List<KeyValuePair> operatorList = new ArrayList<KeyValuePair>(2);
        operatorList.add(chinaMobile);
        operatorList.add(chinaUnicom);
        ArrayAdapter<KeyValuePair> operatorAdapter = new ArrayAdapter<KeyValuePair>(getActivity(), R.layout.spinner_item,
                operatorList);
        operatorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mOperator.setAdapter(operatorAdapter);
    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            anInterface = IUdpConnectionInterface.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            anInterface = null;
            DialogUtils.dismissProgressDialog();
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ScanFreqResponseHandler.SCAN_SUCCESS)) {
                ((BaseActivity) getActivity()).removeTimer(scanMessageId);
                DialogUtils.dismissProgressDialog();
                ScanFreqResponse response = (ScanFreqResponse) intent.getSerializableExtra(ScanFreqResponseHandler.DATA);
                if (response.action == 0) {
                    if (response.cellIdx != 0) {
                        mStationFrequency.setText(response.cellIdx + "");
                    }
//                    if (response.selectSCell != 0) {
//                        mNFrequency.setText(response.selectSCell + "");
//                    }
                    ToastUtils.show(getActivity(), "频点信息获取成功");
                } else if (response.action == 1) {
                    List<ScanFreqResponse.Entry2> resultList = new ArrayList<ScanFreqResponse.Entry2>();
                    for (ScanFreqResponse.Entry1 entry1 : response.entry1List) {
                        for (ScanFreqResponse.Entry2 entry2 : entry1.entry2List) {
                            if (!resultList.contains(entry2)) {
                                if (entry2.nCell != 0)
                                    resultList.add(entry2);
                            }
                        }
                    }
                    if (resultList.size() > 0) {
                        Intent startIntent = new Intent(getActivity(), ScanFrequencyResultActivity.class);
                        startIntent.putExtra("DATA", (java.io.Serializable) resultList);
                        startActivityForResult(startIntent, 1000);
                    } else {
                        ToastUtils.show(getActivity(), "无扫频结果");
                    }
                }
            } else if (action.equals(ScanFreqResponseHandler.SCAN_FAILED)) {
                ((BaseActivity) getActivity()).removeTimer(scanMessageId);
                ScanFreqResponse response = (ScanFreqResponse) intent.getSerializableExtra(ScanFreqResponseHandler.DATA);
                String cause = "";
                if (response.cause == 0) {
                    cause = "扫频设备初始化失败";
                } else if (response.cause == 2) {
                    cause = "扫频失败，请重试!";
                } else if (response.cause == -2) {
                    cause = "扫频失败";
                } else if (response.cause == -4) {
                    cause = "扫频设备出现故障，无法获取频率";
                } else if (response.cause == -3 || response.cause == -5) {
                    cause = "无法获取频率";
                } else if (response.cause == 3) {
                    cause = "正在获取中...";
                }
                if (!StringUtils.isBlank(cause)) {
                    ToastUtils.show(getActivity(), cause);
                }
                DialogUtils.dismissProgressDialog();
            } else if (action.equals(ConfigParamResponseHandler.CONF_PARAM_SUCCESS)) {
                ((BaseActivity) getActivity()).removeTimer(setConfMessageId);
                ToastUtils.show(getActivity(), "参数设置成功!");
                userDao.clear();
                saveOrUpdateSetting();
                DialogUtils.dismissProgressDialog();
                getActivity().finish();
            } else if (action.equals(ConfigParamResponseHandler.CONF_PARAM_FAILED)) {
                ((BaseActivity) getActivity()).removeTimer(setConfMessageId);
                ToastUtils.show(getActivity(), "参数设置失败!");
                DialogUtils.dismissProgressDialog();
            } else if (action.equals(TaskConfigResponseHandler.TASK_CFG_FAILED)) {
                ((BaseActivity) getActivity()).removeTimer(startServerMessageId);
                ToastUtils.show(getActivity(), "基站启动失败!");
                DialogUtils.dismissProgressDialog();
            } else if (action.equals(MessageResponseHandler.BTS_STATE)) {
                ((BaseActivity) getActivity()).removeTimer(startServerMessageId);
                MsgResponse response = (MsgResponse) intent.getSerializableExtra(MessageResponseHandler.BTS_DATA);
                String stationName = response.action == 0 ? "采集站" : "功能站";
                if (response.btsState == SystemConstants.BTS_STATUS.STATION_POWER_ON) {
                    ToastUtils.show(getActivity(), stationName + "基站上电成功");
                } else if (response.btsState == SystemConstants.BTS_STATUS.STATION_START_SUCCESS) {
                    ToastUtils.show(getActivity(), "基站启动成功，开始接管");
                    saveOrUpdateSetting();
                    DialogUtils.dismissProgressDialog();
                    getActivity().finish();
                } else if (response.btsState == SystemConstants.BTS_STATUS.STATION_DISCONNECTED) {
                    ((BaseActivity) getActivity()).removeTimer(startServerMessageId);
                    ToastUtils.show(getActivity(), stationName + "基站链路失败");
                    DialogUtils.dismissProgressDialog();
                } else if (response.btsState == SystemConstants.BTS_STATUS.STATION_START_FAILED) {
                    ((BaseActivity) getActivity()).removeTimer(startServerMessageId);
                    ToastUtils.show(getActivity(), stationName + "基站启动失败");
                    DialogUtils.dismissProgressDialog();
                }
            }
        }
    };

    private void saveOrUpdateSetting() {
        SettingModel settingModel = settingDao.getSetting();
        if (settingModel == null) {
            settingModel = new SettingModel();
        }
        settingModel.takeOverMode = takeOverMode;
        settingModel.cmccMonitorCount = Integer.valueOf(cmccMonitorLineCount.getText().toString());
        settingModel.cuccMonitorCount = Integer.valueOf(chinaUnicomMonitorCount.getText().toString());
        settingModel.stationFrequency = Integer.parseInt(mStationFrequency.getText().toString());
//        settingModel.regionFrequency = Integer.parseInt(mNFrequency.getText().toString());
        settingModel.regionFrequency = 0;
        settingModel.operator = ((KeyValuePair) mOperator.getSelectedItem()).key;
        settingModel.intensity = mUserDensity.getSelectedItemPosition();
        settingModel.power = mPowerLevel.getSelectedItemPosition();
        if (mAutoSendSMS.isChecked()) {
            settingModel.autoSendSms = SystemConstants.AUTO_SEND_SMS.AUTO_SEND;
        } else {
            settingModel.autoSendSms = SystemConstants.AUTO_SEND_SMS.DO_NOT_SEND;
        }
        if (StringUtils.isBlank(mAutoSendSMSInterval.getText().toString())) {
            settingModel.autoSendSmsInterval = 20;
        } else {
            settingModel.autoSendSmsInterval = Integer.valueOf(mAutoSendSMSInterval.getText().toString());
        }
        settingDao.saveOrUpdate(settingModel);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            ScanFreqResponse.Entry2 selected = (ScanFreqResponse.Entry2) data.getSerializableExtra("DATA");
            if (selected == null) {
                ToastUtils.show(getActivity(), "没有选择频点信息");
                return;
            }
            mStationFrequency.setText(selected.nCell + "");
//            mNFrequency.setText("1");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}