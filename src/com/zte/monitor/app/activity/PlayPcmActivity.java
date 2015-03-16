package com.zte.monitor.app.activity;

import android.app.AlertDialog;
import android.content.*;
import android.media.AudioFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.zte.monitor.app.MonitorApplication;
import com.zte.monitor.app.R;
import com.zte.monitor.app.adapter.PlayPcmAdapter;
import com.zte.monitor.app.database.dao.MonitorLineDao;
import com.zte.monitor.app.handler.MessageResponseHandler;
import com.zte.monitor.app.handler.MonitorPcmResponseHandler;
import com.zte.monitor.app.handler.MonitorResponseHandler;
import com.zte.monitor.app.model.MonitorLineModel;
import com.zte.monitor.app.model.response.MonitorPcmResponse;
import com.zte.monitor.app.pcm.PcmAudioParam;
import com.zte.monitor.app.pcm.PcmPlayer;
import com.zte.monitor.app.util.ToastUtils;

import java.lang.reflect.Field;
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
    private PcmAudioParam audioParam;
    private PcmPlayer pcmPlayer;
    private MonitorLineModel selectedModel;

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
                    if (i == j) {
                        selectedModel.isChecked = true;
                    } else {
                        monitorLineModel.isChecked = false;
                    }
                }
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
                MonitorApplication.currentPlayMonitorPcmImsi = selectedModel.imsi;
                showPlayDialog(selectedModel.imsi);
            }
        });
        initPcmPlayer();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MonitorResponseHandler.MONITOR_SUCCESS);
        filter.addAction(MonitorPcmResponseHandler.MONITOR_PCM_SUCCESS);
        filter.addAction(MonitorResponseHandler.MONITOR_SUCCESS);
        registerReceiver(receiver, filter);
    }

    private void initPcmPlayer() {
        audioParam = new PcmAudioParam();
        audioParam.mFrequency = 8000;
        audioParam.mChannel = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        audioParam.mSampBit = AudioFormat.ENCODING_PCM_16BIT;
        pcmPlayer = new PcmPlayer(audioParam);
        pcmPlayer.prepare();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(MonitorResponseHandler.MONITOR_SUCCESS)) {
                loadData();
            } else if (action.equals(MonitorPcmResponseHandler.MONITOR_PCM_SUCCESS)) {
                MonitorPcmResponse response = (MonitorPcmResponse) intent.getSerializableExtra(MonitorPcmResponseHandler.MONITOR_PCM_DATA);
                pcmPlayer.setPcmData(response.pcm);
            }
        }
    };

    private void showPlayDialog(String imsi) {
        final AlertDialog playDialog = new AlertDialog.Builder(PlayPcmActivity.this).setMessage("播放 " + imsi).create();
        playDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "播放", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialogInterface, false);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                sendBroadcast(new Intent(MessageResponseHandler.ACTION_STOP_SPEAKING));
                pcmPlayer.play();
                Button playButton = playDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                Button stopButton = playDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
                playButton.setEnabled(false);
                stopButton.setEnabled(true);
                playButton.invalidate();
                stopButton.invalidate();
            }
        });
        playDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "停止", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialogInterface, false);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                MonitorApplication.currentPlayMonitorPcmImsi = "";
                pcmPlayer.stop();
                Button playButton = playDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                Button stopButton = playDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
                playButton.setEnabled(true);
                stopButton.setEnabled(false);
                playButton.invalidate();
                stopButton.invalidate();
            }
        });
        playDialog.setButton(DialogInterface.BUTTON_POSITIVE, "关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialogInterface, true);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                sendBroadcast(new Intent(MessageResponseHandler.ACTION_START_SPEAKING));
                MonitorApplication.currentPlayMonitorPcmImsi = "";
                dialogInterface.dismiss();
            }
        });
        playDialog.setCancelable(false);
        playDialog.setCanceledOnTouchOutside(false);
        playDialog.show();
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
        adapter.notifyDataSetInvalidated();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pcmPlayer.release();
    }
}
