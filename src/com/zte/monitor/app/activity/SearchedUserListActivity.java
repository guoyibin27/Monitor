package com.zte.monitor.app.activity;

import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.adapter.UserListAdapter;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.handler.FindBaseInfoResponseHandler;
import com.zte.monitor.app.handler.FindSpcInfoResponseHandler;
import com.zte.monitor.app.model.UserModel;
import com.zte.monitor.app.model.response.UserResponse;
import com.zte.monitor.app.udp.UdpDataSendService;
import com.zte.monitor.app.udp.aidl.IUdpConnectionInterface;
import com.zte.monitor.app.util.DialogUtils;
import com.zte.monitor.app.util.StringUtils;
import com.zte.monitor.app.util.ToastUtils;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Sylar on 15/2/2.
 */
public class SearchedUserListActivity extends BaseActivity {

    private UserListAdapter mAdapter;
    private ListView listView;

    private int searchSpcMessageId = 50001;
    private IUdpConnectionInterface anInterface;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searched_user_list);
        listView = (ListView) findViewById(R.id.list_view);
        mAdapter = new UserListAdapter(this, R.layout.user_list_item);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                UserModel userModel = (UserModel) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(SearchedUserListActivity.this, UserDetailAndEditActivity.class);
                intent.putExtra("UserInfo", userModel);
                startActivity(intent);
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FindBaseInfoResponseHandler.FIND_BASE_INFO_SUCCESS);
        intentFilter.addAction(FindBaseInfoResponseHandler.FIND_BASE_INFO_FAILED);
        intentFilter.addAction(FindSpcInfoResponseHandler.FIND_SPC_INFO_SUCCESS);
        intentFilter.addAction(FindSpcInfoResponseHandler.FIND_SPC_INFO_FAILED);
        registerReceiver(receiver, intentFilter);

        Intent intent = new Intent(this, UdpDataSendService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

    }

    private void searchData() {
        Intent intent = getIntent();
        String searchType = intent.getStringExtra("SearchType");
        if (!StringUtils.isBlank(searchType)) {
            if ("BaseInfo".equals(searchType)) {
                intent.putExtra("SearchType", "BaseInfo");

                IoBuffer buffer = CodecManager.getManager().findBaseInfoReqEncode((byte) 1, intent.getStringExtra("IMSI"),
                        intent.getStringExtra("IMEI"),
                        intent.getStringExtra("DATE_FROM"), intent.getStringExtra("DATE_TO"));
                buffer.flip();
                byte[] out = new byte[buffer.limit()];
                buffer.get(out);
                try {
                    anInterface.sendRequest(out);
                    DialogUtils.showProgressDialog(SearchedUserListActivity.this, "查询中，请稍后...");
                    startLoadingTimer(searchSpcMessageId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    removeTimer(searchSpcMessageId);
                    DialogUtils.dismissProgressDialog();
                    ToastUtils.show(SearchedUserListActivity.this, "查询失败");
                }
            } else if ("SpcInfo".equals(searchType)) {
                UserModel userModel = (UserModel) intent.getSerializableExtra("UserModel");
                String userPro = intent.getStringExtra("UserPro");

                IoBuffer buffer = CodecManager.getManager().findSpcInfoReqEncode((byte) 1, userModel, userPro);
                buffer.flip();
                byte[] out = new byte[buffer.limit()];
                buffer.get(out);
                try {
                    anInterface.sendRequest(out);
                    DialogUtils.showProgressDialog(SearchedUserListActivity.this, "查询中，请稍后...");
                    startLoadingTimer(searchSpcMessageId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    removeTimer(searchSpcMessageId);
                    DialogUtils.dismissProgressDialog();
                    ToastUtils.show(SearchedUserListActivity.this, "查询失败");
                }
            }
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(FindBaseInfoResponseHandler.FIND_BASE_INFO_SUCCESS)) {
                UserResponse response = (UserResponse) intent.getSerializableExtra(FindBaseInfoResponseHandler.FIND_BASE_INFO_DATA);
                mAdapter.getData().add(response.userModel);
                mAdapter.notifyDataSetChanged();
            } else if (action.equals(FindBaseInfoResponseHandler.FIND_BASE_INFO_FAILED)) {
                ToastUtils.show(context, "数据库查询失败");
                finish();
            } else if (action.equals(FindSpcInfoResponseHandler.FIND_SPC_INFO_SUCCESS)) {
                UserResponse response = (UserResponse) intent.getSerializableExtra(FindSpcInfoResponseHandler.FIND_SPC_INFO_DATA);
                mAdapter.getData().add(response.userModel);
                mAdapter.notifyDataSetChanged();
            } else if (action.equals(FindSpcInfoResponseHandler.FIND_SPC_INFO_FAILED)) {
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
        removeTimer(searchSpcMessageId);
    }
}
