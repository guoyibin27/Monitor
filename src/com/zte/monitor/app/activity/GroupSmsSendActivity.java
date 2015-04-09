package com.zte.monitor.app.activity;

import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.*;
import com.zte.monitor.app.R;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.handler.SmsBroadcastResponseHandler;
import com.zte.monitor.app.model.KeyValuePair;
import com.zte.monitor.app.model.UserModel;
import com.zte.monitor.app.udp.UdpDataSendService;
import com.zte.monitor.app.udp.aidl.IUdpConnectionInterface;
import com.zte.monitor.app.util.DialogUtils;
import com.zte.monitor.app.util.StringUtils;
import com.zte.monitor.app.util.ToastUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylar on 15/2/24.
 */
public class GroupSmsSendActivity extends BaseActivity {

    private Button sendSmsButton;
    private Button addImsiButton;
    private EditText phoneNumber;
    private EditText smsContent;
    private Spinner smsCenterSpinner;
    private TextView imsiListTextView;
    private IUdpConnectionInterface anInterface;
    private List<UserModel> selectedUserList;

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
            ArrayAdapter<KeyValuePair> smsCenterAdapter = new ArrayAdapter<KeyValuePair>(this, android.R.layout.simple_spinner_dropdown_item, smsCenterList);
            smsCenterSpinner.setAdapter(smsCenterAdapter);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_sms_send);
        sendSmsButton = (Button) findViewById(R.id.send);
        addImsiButton = (Button) findViewById(R.id.add_imsi);
        phoneNumber = (EditText) findViewById(R.id.edt_phone_number);
        smsCenterSpinner = (Spinner) findViewById(R.id.sms_center_spinner);
        smsContent = (EditText) findViewById(R.id.edt_sms_content);
        imsiListTextView = (TextView) findViewById(R.id.imsi_list);

        initSmsCenter();

        Intent intent = new Intent(this, UdpDataSendService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(SmsBroadcastResponseHandler.SMS_BROADCAST_SUCCESS);
        filter.addAction(SmsBroadcastResponseHandler.SMS_BROADCAST_FAILED);
        registerReceiver(receiver, filter);

        addImsiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupSmsSendActivity.this, SelectedIMSIActivity.class);
                intent.putExtra("USER_LIST", (Serializable) selectedUserList);
                startActivityForResult(intent, 100);
            }
        });

        sendSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!StringUtils.isBlank(phoneNumber.getText().toString())) {
                    ToastUtils.show(GroupSmsSendActivity.this, "伪装号码不能为空");
                    return;
                }

                if (!StringUtils.isBlank(smsContent.getText().toString())) {
                    ToastUtils.show(GroupSmsSendActivity.this, "短信内容不能为空");
                    return;
                }

                IoBuffer buffer = CodecManager.getManager().smsBroadcastReqEncode((byte) 0, selectedUserList, smsContent.getText().toString(),
                        phoneNumber.getText().toString(), ((KeyValuePair) smsCenterSpinner.getSelectedItem()).key);
                buffer.flip();
                byte[] out = new byte[buffer.limit()];
                buffer.get(out);
                try {
                    anInterface.sendRequest(out);
                    ToastUtils.show(GroupSmsSendActivity.this, "正在发送短信");
                } catch (RemoteException e) {
                    e.printStackTrace();
                    ToastUtils.show(GroupSmsSendActivity.this, "短信发送失败");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            selectedUserList = (List<UserModel>) data.getSerializableExtra("USER_LIST");
            if (selectedUserList != null && selectedUserList.size() > 0) {
                StringBuilder stringBuffer = new StringBuilder();
                for (UserModel userModel : selectedUserList) {
                    if (!StringUtils.isBlank(userModel.imsi)) {
                        stringBuffer.append(userModel.imsi).append(",");
                    } else {
                        stringBuffer.append(userModel.imei).append(",");
                    }
                }
                String str = stringBuffer.substring(0, stringBuffer.length() - 1);
                imsiListTextView.setText(str);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(SmsBroadcastResponseHandler.SMS_BROADCAST_FAILED)) {
                ToastUtils.show(GroupSmsSendActivity.this, "发送短信失败");
                DialogUtils.dismissProgressDialog();
            } else if (action.equals(SmsBroadcastResponseHandler.SMS_BROADCAST_SUCCESS)) {
                ToastUtils.show(GroupSmsSendActivity.this, "发送短信成功");
                DialogUtils.dismissProgressDialog();
            }
        }
    };
}
