package com.zte.monitor.app.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.adapter.SmsListAdapter;
import com.zte.monitor.app.database.dao.SmsDao;
import com.zte.monitor.app.handler.MessageResponseHandler;
import com.zte.monitor.app.model.SmsModel;
import com.zte.monitor.app.udp.UdpDataSendService;

import java.util.List;

/**
 * Created by Sylar on 14-9-13.
 * 短信列表
 */
public class SmsListActivity extends BaseActivity {

    private ListView mListView;
    private SmsListAdapter mAdapter;
    private SmsDao smsDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_list);
        smsDao = new SmsDao(this);
        mListView = (ListView) findViewById(R.id.list_view);
        mAdapter = new SmsListAdapter(this, R.layout.sms_list_item);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SmsModel smsModel = (SmsModel) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(SmsListActivity.this, SmsDetailActivity.class);
                intent.putExtra(SmsDetailActivity.DATA, smsModel);
                if (smsModel.status == SystemConstants.SMS_STATUS.NEW_ARRIVE) {
                    smsModel.status = SystemConstants.SMS_STATUS.UNHANDLED;
                }
                smsDao.update(smsModel);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MessageResponseHandler.SMS_INFO);
        filter.addAction(UdpDataSendService.UPDATE_SMS_INFO);
        registerReceiver(receiver, filter);
        loadData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void loadData() {
        mAdapter.getData().clear();
        List<SmsModel> smsModelList = smsDao.getSmsList();
        mAdapter.getData().addAll(smsModelList);
        mAdapter.notifyDataSetInvalidated();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadData();
        }
    };
}
