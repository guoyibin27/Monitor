package com.zte.monitor.app.activity;

import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.database.dao.SettingDao;
import com.zte.monitor.app.database.dao.SmsDao;
import com.zte.monitor.app.database.dao.UserDao;
import com.zte.monitor.app.handler.SmsBroadcastResponseHandler;
import com.zte.monitor.app.model.SettingModel;
import com.zte.monitor.app.model.SmsModel;
import com.zte.monitor.app.model.UserModel;
import com.zte.monitor.app.udp.UdpDataSendService;
import com.zte.monitor.app.udp.aidl.IUdpConnectionInterface;
import com.zte.monitor.app.util.DialogUtils;
import com.zte.monitor.app.util.StringUtils;
import com.zte.monitor.app.util.ToastUtils;
import org.apache.mina.core.buffer.IoBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylar on 14-9-23.
 */
public class SmsDetailActivity extends BaseActivity implements View.OnClickListener {
    public static final String DATA = "com.zte.monitor.app.SMS_DATA";
    private static final int STATUS_PREVIEW = 10001;
    private static final int STATUS_MODIFY = 10002;

    private TextView directionTextView;
    private TextView statusTextView;
    private TextView phoneNumTextView;
    private TextView countDownTextView;
    private TextView contentTextView;
    private TextView newContentTextView;
    private TextView scTextView;
    private TextView upTimeTextView;
    private TextView userNameTextView;
    private TextView imsiTextView;
    private EditText newContentEditText;
    private Button ignoreButton;
    private Button modifyButton;
    private Button closeButton;
    private SmsModel smsModel;
    private int status = STATUS_PREVIEW;
    private IUdpConnectionInterface anInterface;
    private SettingDao settingDao;
    private SmsDao smsDao;
    private UserDao userDao;
    private int messageId = (int) System.currentTimeMillis();

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
    private SettingModel settingModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_detail);
        settingDao = new SettingDao(this);
        userDao = new UserDao(this);
        smsDao = new SmsDao(this);
        smsModel = (SmsModel) getIntent().getSerializableExtra(DATA);
        initViews();
        initData();
        Intent intent = new Intent(this, UdpDataSendService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SmsBroadcastResponseHandler.SMS_BROADCAST_SUCCESS);
        intentFilter.addAction(SmsBroadcastResponseHandler.SMS_BROADCAST_FAILED);
        registerReceiver(receiver, intentFilter);
        settingModel = settingDao.getSetting();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        unregisterReceiver(receiver);
        removeTimer(messageId);
        smsModel = null;
    }

    private void initData() {
        if (smsModel != null) {
            if (smsModel.direction == 1) {
                directionTextView.setText("发送短信");
            } else if (smsModel.direction == 2) {
                directionTextView.setText("接收短信");
            }
            if (smsModel.status == SystemConstants.SMS_STATUS.IGNORE) {
                statusTextView.setText("丢弃");
                setButtonsVisibility(View.GONE);
            } else if (smsModel.status == SystemConstants.SMS_STATUS.UNHANDLED) {
                statusTextView.setText("未处理");
                setButtonsVisibility(View.VISIBLE);
            } else if (smsModel.status == SystemConstants.SMS_STATUS.MODIFIED_AND_SENT) {
                statusTextView.setText("修改已发送");
                setButtonsVisibility(View.GONE);
            } else if (smsModel.status == SystemConstants.SMS_STATUS.SENT) {
                statusTextView.setText("已发送");
                setButtonsVisibility(View.GONE);
            }
            UserModel userModel = userDao.getByImsi(smsModel.imsi);
            userNameTextView.setText(userModel.username);
            imsiTextView.setText(userModel.imsi);
            phoneNumTextView.setText(smsModel.phNum);
            countDownTextView.setText(smsModel.countdownNum + "");
            contentTextView.setText(smsModel.content);
            newContentTextView.setText(smsModel.newContent);
            upTimeTextView.setText(smsModel.upTime);
            scTextView.setText(smsModel.sc);

            if (userModel.isSmsAudit == 1 && smsModel.status == SystemConstants.SMS_STATUS.UNHANDLED) {
                setButtonsVisibility(View.VISIBLE);
            } else {
                setButtonsVisibility(View.GONE);
            }
        }
    }

    private void setButtonsVisibility(int visibility) {
        ignoreButton.setVisibility(visibility);
        closeButton.setVisibility(visibility);
        modifyButton.setVisibility(visibility);
    }

    private void initViews() {
        directionTextView = (TextView) findViewById(R.id.tv_sms_direction);
        statusTextView = (TextView) findViewById(R.id.tv_status);
        phoneNumTextView = (TextView) findViewById(R.id.tv_sms_phone_num);
        countDownTextView = (TextView) findViewById(R.id.tv_send_countdown_num);
        contentTextView = (TextView) findViewById(R.id.tv_sms_content);
        newContentTextView = (TextView) findViewById(R.id.tv_new_sms_content);
        scTextView = (TextView) findViewById(R.id.tv_sc);
        upTimeTextView = (TextView) findViewById(R.id.tv_up_time);
        newContentEditText = (EditText) findViewById(R.id.edit_new_sms_content);
        ignoreButton = (Button) findViewById(R.id.ignore_button);
        modifyButton = (Button) findViewById(R.id.modify_button);
        closeButton = (Button) findViewById(R.id.close_button);
        userNameTextView = (TextView) findViewById(R.id.tv_sms_username);
        imsiTextView = (TextView) findViewById(R.id.tv_sms_imsi);
        ignoreButton.setOnClickListener(this);
        modifyButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);
        resetButtonStatus();
    }

    private void resetButtonStatus() {
        if (status == STATUS_PREVIEW) {
            modifyButton.setText("修改");
            newContentTextView.setVisibility(View.VISIBLE);
            newContentEditText.setVisibility(View.GONE);
        } else if (status == STATUS_MODIFY) {
            modifyButton.setText("确定");
            newContentTextView.setVisibility(View.GONE);
            newContentEditText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ignore_button:
                smsModel.countdownNum = 0;
                smsModel.status = SystemConstants.SMS_STATUS.IGNORE;
                smsDao.update(smsModel);
                finish();
                break;
            case R.id.modify_button:
                if (status == STATUS_PREVIEW) {
                    status = STATUS_MODIFY;
                    modifyButton.setText("确定");
                    newContentEditText.setVisibility(View.VISIBLE);
                    newContentEditText.setText(contentTextView.getText());
                    newContentTextView.setVisibility(View.GONE);
//                    smsModel.status = status;
                    smsDao.update(smsModel);
                } else if (status == STATUS_MODIFY) {
                    if (StringUtils.isBlank(newContentEditText.getText().toString())) {
                        ToastUtils.show(SmsDetailActivity.this, "修改后的短信不能为空");
                        return;
                    }
                    sendSMS(newContentEditText.getText().toString());
                }
                break;
            case R.id.close_button:
                smsModel.status = SystemConstants.SMS_STATUS.UNHANDLED;
                smsModel.countdownNum = 0;
                smsDao.update(smsModel);
                countDownTextView.setText("0");
                finish();
                break;
        }
    }

    private void sendSMS(String content) {
        countDownTextView.setText("0");
        smsModel.countdownNum = 0;
        smsDao.update(smsModel);
        DialogUtils.showProgressDialog(SmsDetailActivity.this, "正在发送短息，请稍等...");
        List<UserModel> userModelList = new ArrayList<UserModel>(1);
        UserModel userModel = new UserModel();
        userModel.imsi = smsModel.imsi;
        userModelList.add(userModel);
        IoBuffer buffer;
        if (smsModel.direction == 1) {
            buffer = CodecManager.getManager().smsBroadcastReqEncode((byte) 1, userModelList, content,
                    smsModel.phNum, smsModel.sc);
        } else {
            buffer = CodecManager.getManager().smsBroadcastReqEncode((byte) 0, userModelList, content,
                    smsModel.phNum, smsModel.sc);
        }
        buffer.flip();
        byte[] out = new byte[buffer.limit()];
        buffer.get(out);
        try {
            anInterface.sendRequest(out);
            startLoadingTimer(messageId);
        } catch (RemoteException e) {
            e.printStackTrace();
            removeTimer(messageId);
            DialogUtils.dismissProgressDialog();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            removeTimer(messageId);
            DialogUtils.dismissProgressDialog();
            if (action.equals(SmsBroadcastResponseHandler.SMS_BROADCAST_SUCCESS)) {
                ToastUtils.show(context, "短信发送成功");
                smsModel.newContent = newContentEditText.getText().toString();
                smsModel.status = SystemConstants.SMS_STATUS.MODIFIED_AND_SENT;
                smsDao.update(smsModel);
                initData();
                status = STATUS_PREVIEW;
                resetButtonStatus();
            } else if (action.equals(SmsBroadcastResponseHandler.SMS_BROADCAST_FAILED)) {
                String response = (String) intent.getSerializableExtra(SmsBroadcastResponseHandler.CAUSE);
                ToastUtils.show(context, "短信发送失败," + response);
                smsModel.status = SystemConstants.SMS_STATUS.UNHANDLED;
                smsDao.update(smsModel);
            }
        }
    };

}
