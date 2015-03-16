package com.zte.monitor.app.activity;

import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.*;
import com.zte.monitor.app.R;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.handler.AddUserResponseHandler;
import com.zte.monitor.app.model.KeyValuePair;
import com.zte.monitor.app.model.UserModel;
import com.zte.monitor.app.model.response.UdpResponse;
import com.zte.monitor.app.udp.UdpDataSendService;
import com.zte.monitor.app.udp.aidl.IUdpConnectionInterface;
import com.zte.monitor.app.util.DialogUtils;
import com.zte.monitor.app.util.StringUtils;
import com.zte.monitor.app.util.ToastUtils;
import org.apache.mina.core.buffer.IoBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylar on 15/2/2.
 */
public class UserDetailAndEditActivity extends BaseActivity {

    private int messageId = (int) System.currentTimeMillis();
    private TextView userNameView;
    private TextView imsiView;
    private TextView imeiView;
    private TextView phoneNumView;
    private TextView userProView;
    private TextView upTimeView;

    private EditText userNameEdit;
    private TextView imsiViewEdit;
    private TextView imeiViewEdit;
    private EditText phoneNumEdit;
    private Spinner userProSpinner;
    private TextView upTimeViewEdit;

    private LinearLayout viewLayout;
    private LinearLayout editLayout;

    private Button deleteButton;
    private Button editButton;
    private Button confirmEditButton;
    private Button cancelEditButton;

    private IUdpConnectionInterface anInterface;

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
        setContentView(R.layout.activity_user_detail_and_edit);
        viewLayout = (LinearLayout) findViewById(R.id.view_layout);
        editLayout = (LinearLayout) findViewById(R.id.edit_layout);

        deleteButton = (Button) findViewById(R.id.delete_button);
        editButton = (Button) findViewById(R.id.edit_button);
        confirmEditButton = (Button) findViewById(R.id.confirm_edit_button);
        cancelEditButton = (Button) findViewById(R.id.cancel_edit_button);

        userNameView = (TextView) findViewById(R.id.tv_username);
        imsiView = (TextView) findViewById(R.id.tv_imsi);
        imeiView = (TextView) findViewById(R.id.tv_imei);
        phoneNumView = (TextView) findViewById(R.id.tv_phone_number);
        userProView = (TextView) findViewById(R.id.tv_user_pro);
        upTimeView = (TextView) findViewById(R.id.tv_last_updated);

        imsiViewEdit = (TextView) findViewById(R.id.tv_imsi_edit);
        imeiViewEdit = (TextView) findViewById(R.id.tv_imei_edit);
        userNameEdit = (EditText) findViewById(R.id.edt_username);
        phoneNumEdit = (EditText) findViewById(R.id.edt_phone_number);
        userProSpinner = (Spinner) findViewById(R.id.spinner_user_pro);
        upTimeViewEdit = (TextView) findViewById(R.id.tv_last_updated_edit);

        initUserProSpinner();

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(UserDetailAndEditActivity.this).setMessage("确认删除")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                DialogUtils.showProgressDialog(UserDetailAndEditActivity.this, "删除用户");
                                KeyValuePair userProper = (KeyValuePair) userProSpinner.getSelectedItem();
                                UserModel userModel = new UserModel();
                                userModel.username = userNameEdit.getText().toString();
                                userModel.imsi = imsiViewEdit.getText().toString();
                                userModel.imei = imeiViewEdit.getText().toString();
                                userModel.phoneNumber = phoneNumEdit.getText().toString();
                                userModel.tmsi = "00000000";
                                userModel.property = userProper.key;
                                IoBuffer bu = CodecManager.getManager().spcUserReqEncode((byte) 1, userModel);
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
                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setCancelable(false).create().show();
            }
        });

        confirmEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DialogUtils.showProgressDialog(UserDetailAndEditActivity.this, "正在保存用户，请稍等...");
                KeyValuePair userProper = (KeyValuePair) userProSpinner.getSelectedItem();
                UserModel userModel = new UserModel();
                userModel.username = userNameEdit.getText().toString();
                userModel.imsi = imsiViewEdit.getText().toString();
                userModel.imei = imeiViewEdit.getText().toString();
                userModel.phoneNumber = phoneNumEdit.getText().toString();
                userModel.tmsi = "00000000";
                userModel.property = userProper.key;
                IoBuffer bu = CodecManager.getManager().spcUserReqEncode((byte) 2, userModel);
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


        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmEditButton.setVisibility(View.VISIBLE);
                cancelEditButton.setVisibility(View.VISIBLE);
                editLayout.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.GONE);
                editButton.setVisibility(View.GONE);
                viewLayout.setVisibility(View.GONE);
            }
        });

        cancelEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmEditButton.setVisibility(View.GONE);
                cancelEditButton.setVisibility(View.GONE);
                editLayout.setVisibility(View.GONE);
                deleteButton.setVisibility(View.VISIBLE);
                editButton.setVisibility(View.VISIBLE);
                viewLayout.setVisibility(View.VISIBLE);
            }
        });

        UserModel userModel = (UserModel) getIntent().getSerializableExtra("UserInfo");
        if (userModel != null) {
            if (!StringUtils.isBlank(userModel.username)) {
                userNameView.setText(userModel.username);
            } else {
                userNameView.setText("未知");
            }

            imsiView.setText(userModel.imsi);
            imeiView.setText(userModel.imei);
            phoneNumView.setText(userModel.phoneNumber);
            if (userModel.property.equals(SystemConstants.USER_PROPERTY.UNKNOWN_LIST)) {
                userProView.setText("未知用户");
            } else if (userModel.property.equals(SystemConstants.USER_PROPERTY.BLACK_LIST)) {
                userProView.setText("严控用户");
            } else if (userModel.property.equals(SystemConstants.USER_PROPERTY.WHITE_LIST)) {
                userProView.setText("友好用户");
            }
            upTimeView.setText(userModel.lastUpdated);


            userNameEdit.setText(userModel.username);
            imsiViewEdit.setText(userModel.imsi);
            imeiViewEdit.setText(userModel.imei);
            phoneNumEdit.setText(userModel.phoneNumber);
            upTimeViewEdit.setText(userModel.lastUpdated);
            if (userModel.property.equals(SystemConstants.USER_PROPERTY.UNKNOWN_LIST)) {
                userProSpinner.setSelection(0);
            } else if (userModel.property.equals(SystemConstants.USER_PROPERTY.BLACK_LIST)) {
                userProSpinner.setSelection(2);
            } else if (userModel.property.equals(SystemConstants.USER_PROPERTY.WHITE_LIST)) {
                userProSpinner.setSelection(1);
            }
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AddUserResponseHandler.ADD_USER_FAILED);
        intentFilter.addAction(AddUserResponseHandler.ADD_USER_SUCCESS);
        registerReceiver(receiver, intentFilter);

        Intent intent = new Intent(this, UdpDataSendService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private void initUserProSpinner() {
        KeyValuePair unknown = new KeyValuePair();
        unknown.key = "0";
        unknown.value = "未知";

        KeyValuePair friendly = new KeyValuePair();
        friendly.key = "1";
        friendly.value = "友好";

        KeyValuePair control = new KeyValuePair();
        control.key = "2";
        control.value = "严控";

        List<KeyValuePair> spinnerList = new ArrayList<KeyValuePair>(3);
        spinnerList.add(unknown);
        spinnerList.add(friendly);
        spinnerList.add(control);

        ArrayAdapter<KeyValuePair> densityAdapter = new ArrayAdapter<KeyValuePair>(this, R.layout.spinner_item, spinnerList);
        densityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userProSpinner.setAdapter(densityAdapter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            DialogUtils.dismissProgressDialog();
            removeTimer(messageId);
            if (action.equals(AddUserResponseHandler.ADD_USER_SUCCESS)) {
                UdpResponse response = (UdpResponse) intent.getSerializableExtra(AddUserResponseHandler.ADD_USER_DATA);
                if (response.action == 1) {//删除
                    ToastUtils.show(context, "用户删除成功");
                    DialogUtils.dismissProgressDialog();
                    finish();
                } else if (response.action == 2) {//添加
                    ToastUtils.show(UserDetailAndEditActivity.this, "修改成功");
                    DialogUtils.dismissProgressDialog();
                    finish();
                }
            } else if (action.equals(AddUserResponseHandler.ADD_USER_FAILED)) {
                UdpResponse response = (UdpResponse) intent.getSerializableExtra(AddUserResponseHandler.ADD_USER_DATA);
                if (response.action == 1) {//删除
                    ToastUtils.show(context, "用户删除失败");
                    DialogUtils.dismissProgressDialog();
                } else if (response.action == 2) {//添加
                    ToastUtils.show(UserDetailAndEditActivity.this, "修改失败");
                    DialogUtils.dismissProgressDialog();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unbindService(connection);
        removeTimer(messageId);
    }
}
