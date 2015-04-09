package com.zte.monitor.app.activity;

import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.adapter.SmsFromDBAdapter;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.handler.FindSMSInfoResponseHandler;
import com.zte.monitor.app.model.response.SmsInfoResponse;
import com.zte.monitor.app.udp.UdpDataSendService;
import com.zte.monitor.app.udp.aidl.IUdpConnectionInterface;
import com.zte.monitor.app.util.DialogUtils;
import com.zte.monitor.app.util.ToastUtils;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 15/4/9.
 */
public class SmsListFromDBActivity extends BaseActivity {

    private ListView listView;
    private SmsFromDBAdapter adapter;
    private IUdpConnectionInterface anInterface;
    private int searchSmsMessageId = 60001;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            anInterface = IUdpConnectionInterface.Stub.asInterface(iBinder);
            searchData();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            anInterface = null;
        }
    };

    private void searchData() {
        Intent intent = getIntent();
        IoBuffer buffer = CodecManager.getManager().findSMSInfoReqEncode((byte) 1, intent.getStringExtra("IMSI"),
                intent.getStringExtra("DATE_FROM"), intent.getStringExtra("DATE_TO"), Byte.parseByte(intent.getStringExtra("SmsType")));
        buffer.flip();
        byte[] out = new byte[buffer.limit()];
        buffer.get(out);
        try {
            anInterface.sendRequest(out);
            DialogUtils.showProgressDialog(SmsListFromDBActivity.this, "查询中，请稍后...");
            startLoadingTimer(searchSmsMessageId);
        } catch (RemoteException e) {
            e.printStackTrace();
            removeTimer(searchSmsMessageId);
            DialogUtils.dismissProgressDialog();
            ToastUtils.show(SmsListFromDBActivity.this, "查询失败");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_list_from_db);
        listView = (ListView) findViewById(R.id.list_view);
        adapter = new SmsFromDBAdapter(this, R.layout.sms_from_db_list_item);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SmsInfoResponse smsModel = (SmsInfoResponse) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(SmsListFromDBActivity.this, SmsFromDBDetailActivity.class);
                intent.putExtra("SmsModel", smsModel);
                startActivity(intent);
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FindSMSInfoResponseHandler.FIND_SMS_INFO_SUCCESS);
        intentFilter.addAction(FindSMSInfoResponseHandler.FIND_SMS_INFO_FAILED);
        registerReceiver(receiver, intentFilter);

        Intent intent = new Intent(this, UdpDataSendService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(FindSMSInfoResponseHandler.FIND_SMS_INFO_SUCCESS)) {
                SmsInfoResponse response = (SmsInfoResponse) intent.getSerializableExtra(FindSMSInfoResponseHandler.FIND_SMS_INFO_DATA);
                adapter.getData().add(response);
                adapter.notifyDataSetChanged();
            } else if (action.equals(FindSMSInfoResponseHandler.FIND_SMS_INFO_FAILED)) {
                ToastUtils.show(context, "数据库查询失败");
                finish();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unbindService(connection);
        removeTimer(searchSmsMessageId);
    }
}
