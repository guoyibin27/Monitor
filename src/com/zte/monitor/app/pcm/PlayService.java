package com.zte.monitor.app.pcm;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.os.IBinder;
import com.zte.monitor.app.MonitorApplication;
import com.zte.monitor.app.handler.MessageResponseHandler;
import com.zte.monitor.app.handler.MonitorPcmResponseHandler;
import com.zte.monitor.app.model.response.MonitorPcmResponse;
import com.zte.monitor.app.util.StringUtils;

/**
 * Created by Sylar on 15/3/16.
 */
public class PlayService extends Service {

    public interface PlayStatus {
        int play = 0x01;
        int stop = 0x02;
    }

    public static final String PLAY_STATUS = "playStatus";
    private PcmAudioParam audioParam;
    private PcmPlayer pcmPlayer;
    private String tempImsi;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MonitorPcmResponseHandler.MONITOR_PCM_SUCCESS);
        registerReceiver(receiver, filter);
        initPcmPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int status = intent.getIntExtra(PLAY_STATUS, -1);
            switch (status) {
                case PlayStatus.play:
                    sendBroadcast(new Intent(MessageResponseHandler.ACTION_STOP_SPEAKING));
                    tempImsi = MonitorApplication.currentPlayMonitorPcmImsi;
                    MonitorApplication.currentPlayMonitorPcmImsi = "";

                    break;
                case PlayStatus.stop:
                    if (StringUtils.isBlank(tempImsi)) {
                        sendBroadcast(new Intent(MessageResponseHandler.ACTION_START_SPEAKING));
                    }
                    MonitorApplication.currentPlayMonitorPcmImsi = tempImsi;
                    tempImsi = "";
                    break;
            }
            pcmPlayer.stop();
            pcmPlayer.prepare();
            pcmPlayer.play();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void initPcmPlayer() {
        audioParam = new PcmAudioParam();
        audioParam.mFrequency = 8000;
        audioParam.mChannel = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        audioParam.mSampBit = AudioFormat.ENCODING_PCM_16BIT;
        pcmPlayer = new PcmPlayer(audioParam);
        pcmPlayer.prepare();
        pcmPlayer.play();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(MonitorPcmResponseHandler.MONITOR_PCM_SUCCESS)) {
                MonitorPcmResponse response = (MonitorPcmResponse) intent.getSerializableExtra(MonitorPcmResponseHandler.MONITOR_PCM_DATA);
                if (response.imsi.equals(MonitorApplication.playImsi)) {
                    pcmPlayer.setPcmData(response.pcm);
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        pcmPlayer.stop();
        pcmPlayer.release();
        stopSelf();
    }
}
