package com.zte.monitor.app.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import com.zte.monitor.app.R;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.model.KeyValuePair;
import com.zte.monitor.app.model.UserModel;
import com.zte.monitor.app.udp.UdpDataSendService;
import com.zte.monitor.app.udp.aidl.IUdpConnectionInterface;
import com.zte.monitor.app.util.ToastUtils;
import org.apache.mina.core.buffer.IoBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylar on 15/2/2.
 */
public class SearchSpcInfoActivity extends BaseActivity {

    private EditText mUserName;
    private EditText mImsi;
    private EditText mImei;
    private EditText mPhNum;
    private Spinner mUserPro;
    private int searchSpcMessageId = 50001;

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
        setContentView(R.layout.activity_search_spc_info);

        setBorderIfActivityAsDialog();

        mUserName = (EditText) findViewById(R.id.search_user_name);
        mImsi = (EditText) findViewById(R.id.search_imsi);
        mImei = (EditText) findViewById(R.id.search_imei);
        mPhNum = (EditText) findViewById(R.id.search_ph_num);
        mUserPro = (Spinner) findViewById(R.id.search_user_pro);

        initSpinner();

        findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserModel userModel = new UserModel();
                userModel.imsi = mImsi.getText().toString();
                userModel.imei = mImei.getText().toString();
                userModel.phoneNumber = mPhNum.getText().toString();
                userModel.username = mUserName.getText().toString();
                String userPro = ((KeyValuePair) mUserPro.getSelectedItem()).key;

                Intent intent = new Intent(SearchSpcInfoActivity.this, SearchedUserListActivity.class);
                intent.putExtra("SearchType", "SpcInfo");
                intent.putExtra("UserModel", userModel);
                intent.putExtra("UserPro", userPro);
                startActivity(intent);

//                IoBuffer buffer = CodecManager.getManager().findSpcInfoReqEncode((byte) 1, userModel, userPro);
//                buffer.flip();
//                byte[] out = new byte[buffer.limit()];
//                buffer.get(out);
//                try {
//                    anInterface.sendRequest(out);
////                    DialogUtils.showProgressDialog(SearchSpcInfoActivity.this, "查询中，请稍后...");
////                    startLoadingTimer(searchSpcMessageId);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
////                    removeTimer(searchSpcMessageId);
////                    DialogUtils.dismissProgressDialog();
//                    ToastUtils.show(SearchSpcInfoActivity.this, "查询失败");
//                }
            }
        });

        Intent intent = new Intent(this, UdpDataSendService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private void initSpinner() {
        KeyValuePair unknown = new KeyValuePair();
        unknown.key = "0";
        unknown.value = "未知";

        KeyValuePair friendly = new KeyValuePair();
        friendly.key = "1";
        friendly.value = "友好";

        KeyValuePair control = new KeyValuePair();
        control.key = "2";
        control.value = "严控";

        KeyValuePair spc = new KeyValuePair();
        spc.key = "3";
        spc.value = "特殊";

        KeyValuePair all = new KeyValuePair();
        all.key = "4";
        all.value = "全部";

        List<KeyValuePair> spinnerList = new ArrayList<KeyValuePair>(3);
        spinnerList.add(unknown);
        spinnerList.add(friendly);
        spinnerList.add(control);
        spinnerList.add(spc);
        spinnerList.add(all);

        ArrayAdapter<KeyValuePair> densityAdapter = new ArrayAdapter<KeyValuePair>(this, android.R.layout.simple_spinner_item, spinnerList);
        densityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mUserPro.setAdapter(densityAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        removeTimer(searchSpcMessageId);
    }
}
