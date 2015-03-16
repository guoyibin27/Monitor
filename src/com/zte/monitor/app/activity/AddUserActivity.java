package com.zte.monitor.app.activity;

import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import com.zte.monitor.app.R;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.handler.AddUserResponseHandler;
import com.zte.monitor.app.model.KeyValuePair;
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
 * @author Sylar @
 */
public class AddUserActivity extends BaseActivity {
    public static final String DATA = "AddUserActivity.DATA";

    private EditText mUserName;
    private EditText mPhoneNumber;
    private EditText mImsi;
    private EditText mImei;
    private Spinner mUserTypeSpinner;
    private IUdpConnectionInterface anInterface;
    private UserModel userModel;
    private int messageId = (int) System.currentTimeMillis();

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
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        UserModel userModel = (UserModel) getIntent().getSerializableExtra(DATA);
        initViews();
        initData(userModel);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AddUserResponseHandler.ADD_USER_FAILED);
        intentFilter.addAction(AddUserResponseHandler.ADD_USER_SUCCESS);
        registerReceiver(receiver, intentFilter);
        Intent intent = new Intent(this, UdpDataSendService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private void initData(UserModel userModel) {
        if (userModel != null) {
            mUserName.setText(userModel.username);
            mImei.setText(userModel.imei);
            mImsi.setText(userModel.imsi);
            mPhoneNumber.setText(userModel.phoneNumber);
            if (!StringUtils.isBlank(userModel.property)) {
                mUserTypeSpinner.setSelection(Integer.valueOf(userModel.property));
            }
        }
    }

    private void initViews() {
        mUserName = (EditText) findViewById(R.id.edt_username);
        mPhoneNumber = (EditText) findViewById(R.id.edt_phone_number);
        mImsi = (EditText) findViewById(R.id.edt_imsi);
        mImei = (EditText) findViewById(R.id.edt_imei);
        mUserTypeSpinner = (Spinner) findViewById(R.id.sp_user_type);
        KeyValuePair white = new KeyValuePair();
        white.key = SystemConstants.USER_PROPERTY.BLACK_LIST;
        white.value = "严控用户";

        final KeyValuePair black = new KeyValuePair();
        black.key = SystemConstants.USER_PROPERTY.WHITE_LIST;
        black.value = "友好用户";

        List<KeyValuePair> userTypeList = new ArrayList<KeyValuePair>(2);
        userTypeList.add(white);
        userTypeList.add(black);
        final ArrayAdapter<KeyValuePair> userTypeAdapter = new ArrayAdapter<KeyValuePair>(AddUserActivity.this, R.layout.spinner_item,
                userTypeList);
        userTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mUserTypeSpinner.setAdapter(userTypeAdapter);
        saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (StringUtils.isBlank(mImsi.getText().toString()) && StringUtils.isBlank(mImei.getText().toString())) {
                    ToastUtils.show(AddUserActivity.this, "IMSI或IMEI不能同时为空！");
                    return;
                }

                if (!StringUtils.isBlank(mImsi.getText().toString()) && mImsi.getText().toString().length() != 15) {
                    ToastUtils.show(AddUserActivity.this, "IMSI长度必须为15位");
                    return;
                }

                if (!StringUtils.isBlank(mImei.getText().toString()) && mImei.getText().toString().length() != 15) {
                    ToastUtils.show(AddUserActivity.this, "IMEI必须为15位！");
                    return;
                }

                DialogUtils.showProgressDialog(AddUserActivity.this, "正在保存用户，请稍等...");
                KeyValuePair userProper = (KeyValuePair) mUserTypeSpinner.getSelectedItem();
                userModel = new UserModel();
                userModel.username = mUserName.getText().toString();
                userModel.imsi = mImsi.getText().toString();
                userModel.imei = mImei.getText().toString();
                userModel.phoneNumber = mPhoneNumber.getText().toString();
                userModel.tmsi = "00000000";
                userModel.property = userProper.key;
                IoBuffer bu = CodecManager.getManager().spcUserReqEncode((byte) 0, userModel);
                byte[] out = new byte[bu.limit()];
                bu.flip();
                bu.get(out);
                try {
                    anInterface.sendRequest(out);
                    startLoadingTimer(messageId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    removeTimer(messageId);
                    DialogUtils.dismissProgressDialog();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unbindService(connection);
        removeTimer(messageId);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            DialogUtils.dismissProgressDialog();
            removeTimer(messageId);
            if (action.equals(AddUserResponseHandler.ADD_USER_FAILED)) {
                ToastUtils.show(AddUserActivity.this, "添加失败");
            } else {
                ToastUtils.show(AddUserActivity.this, "添加成功");
                finish();
            }
        }
    };
}
