package com.zte.monitor.app.activity;

import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.zte.monitor.app.MonitorApplication;
import com.zte.monitor.app.R;
import com.zte.monitor.app.adapter.PlayPcmAdapter;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.database.dao.MonitorLineDao;
import com.zte.monitor.app.handler.MonitorResponseHandler;
import com.zte.monitor.app.model.MonitorLineModel;
import com.zte.monitor.app.model.UserModel;
import com.zte.monitor.app.model.response.MonitorResponse;
import com.zte.monitor.app.pcm.PlayService;
import com.zte.monitor.app.udp.aidl.IUdpConnectionInterface;
import com.zte.monitor.app.util.DialogUtils;
import com.zte.monitor.app.util.ToastUtils;
import org.apache.mina.core.buffer.IoBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylar on 14-9-19.
 */
public class PlayPcmActivity extends BaseActivity {
    private static final int STATUS_PLAY = 0;
    private static final int STATUS_STOP = 1;
    private static final int STATUS_PAUSE = 3;
    private int status = STATUS_PLAY;

    private Button playButton;
    private ListView listView;
    private MonitorLineDao monitorLineDao;
    private PlayPcmAdapter adapter;
    private MonitorLineModel selectedModel;
    private int startMonitor = 10001;
    private int stopMonitor = 10002;
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
        setContentView(R.layout.activity_play_pcm);
        monitorLineDao = new MonitorLineDao(this);
        listView = (ListView) findViewById(R.id.list_view);
        playButton = (Button) findViewById(R.id.play_button);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        adapter = new PlayPcmAdapter(this, R.layout.play_pcm_item);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedModel = (MonitorLineModel) adapterView.getItemAtPosition(i);
                for (int j = 0; j < adapter.getData().size(); j++) {
                    MonitorLineModel monitorLineModel = adapter.getData().get(j);
                    monitorLineModel.isChecked = false;
                }

                Intent intent = new Intent(PlayPcmActivity.this, PlayService.class);
                if (selectedModel.isChecked) {
                    MonitorApplication.playImsi = "";
                    selectedModel.isChecked = false;
                    intent.putExtra(PlayService.PLAY_STATUS, PlayService.PlayStatus.stop);
                } else {
                    MonitorApplication.playImsi = selectedModel.imsi;
                    selectedModel.isChecked = true;
                    intent.putExtra(PlayService.PLAY_STATUS, PlayService.PlayStatus.play);
                }

                if (selectedModel.imsi.equals(MonitorApplication.playImsi)) {
                    playButton.setText("停止监听");
                    MonitorApplication.playerStatus = PlayService.PlayStatus.stop;
                } else {
                    playButton.setText("监听");
                    MonitorApplication.playerStatus = PlayService.PlayStatus.play;
                }

                startService(intent);
                adapter.notifyDataSetInvalidated();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedModel == null) {
                    ToastUtils.show(PlayPcmActivity.this, "请选择监听");
                    return;
                }
                DialogUtils.showProgressDialog(PlayPcmActivity.this, "正在发送请求,请稍等...");
                if (MonitorApplication.playerStatus == PlayService.PlayStatus.play) {
                    MonitorApplication.currentPlayMonitorPcmImsi = selectedModel.imsi;
                    List<UserModel> userModelList = new ArrayList<UserModel>();
                    UserModel userModel = new UserModel();
                    userModel.imsi = selectedModel.imsi;
                    userModel.imei = selectedModel.imei;
                    userModelList.add(userModel);
                    IoBuffer buffer = CodecManager.getManager().monitorReqEncode((byte) 0, userModelList);
                    buffer.flip();
                    byte[] playListReq = new byte[buffer.limit()];
                    buffer.get(playListReq);
                    try {
                        anInterface.sendRequest(playListReq);
                        startLoadingTimer(startMonitor);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        removeTimer(startMonitor);
                        DialogUtils.dismissProgressDialog();
                        ToastUtils.show(PlayPcmActivity.this, "请求发送失败");
                    }
                } else if (MonitorApplication.playerStatus == PlayService.PlayStatus.stop) {
                    MonitorApplication.currentPlayMonitorPcmImsi = "";
                    List<UserModel> userModelList = new ArrayList<UserModel>();
                    UserModel userModel = new UserModel();
                    userModel.imsi = selectedModel.imsi;
                    userModel.imei = selectedModel.imei;
                    userModelList.add(userModel);
                    IoBuffer buffer = CodecManager.getManager().monitorReqEncode((byte) 1, userModelList);
                    buffer.flip();
                    byte[] playListReq = new byte[buffer.limit()];
                    buffer.get(playListReq);
                    try {
                        anInterface.sendRequest(playListReq);
                        startLoadingTimer(stopMonitor);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        removeTimer(stopMonitor);
                        DialogUtils.dismissProgressDialog();
                        ToastUtils.show(PlayPcmActivity.this, "请求发送失败");
                    }
                }

            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction(MonitorResponseHandler.MONITOR_SUCCESS);
        filter.addAction(MonitorResponseHandler.MONITOR_FAILED);
        registerReceiver(receiver, filter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            DialogUtils.dismissProgressDialog();
            if (action.equals(MonitorResponseHandler.MONITOR_SUCCESS)) {
                removeTimer(startMonitor);
                MonitorResponse response = (MonitorResponse) intent.getSerializableExtra(MonitorResponseHandler.MONITOR_DATA);
                if (response.entryList.size() > 0) {
                    MonitorResponse.Entry entry = response.entryList.get(0);
                    MonitorApplication.currentPlayMonitorPcmImsi = entry.userModel.imsi;
                    ToastUtils.show(context, "监听成功!");
                } else {
                    ToastUtils.show(context, "监听失败,服务端发生错误!");
                }
            } else if (action.equals(MonitorResponseHandler.MONITOR_FAILED)) {
                ToastUtils.show(context, "监听失败!");
                removeTimer(stopMonitor);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        adapter.getData().clear();
        List<MonitorLineModel> monitorLineModelList = monitorLineDao.getList();
        adapter.getData().addAll(monitorLineModelList);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeTimer(stopMonitor);
        removeTimer(startMonitor);
    }
}
