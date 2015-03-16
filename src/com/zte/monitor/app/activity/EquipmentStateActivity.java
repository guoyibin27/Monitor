package com.zte.monitor.app.activity;

import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.database.dao.BtsStateDao;
import com.zte.monitor.app.database.dao.UserDao;
import com.zte.monitor.app.model.StateModel;
import com.zte.monitor.app.udp.UdpDataSendService;
import com.zte.monitor.app.udp.aidl.IUdpConnectionInterface;
import com.zte.monitor.app.util.DialogUtils;
import com.zte.monitor.app.util.PreferencesUtils;
import com.zte.monitor.app.util.ToastUtils;
import org.apache.mina.core.buffer.IoBuffer;

public class EquipmentStateActivity extends BaseActivity {

    private Button changeToCollectStationButton;
    private BtsStateDao btsStateDao;
    private IUdpConnectionInterface anInterface;
    private UserDao userDao;

    //GSM
    private LinearLayout gsmLayout;
    private TextView gsmBtsStateTextView;
    private Button gsmPauseStationButton;
    private TextView connStateTextView;
    private ImageView gsmProxyStateImage;
    private ImageView gsmGetPhoneNumStateImage;
//    private TextView gsmSystemModeTextView;
//    private TextView gpsStateTextView;

    //CDMA
    private LinearLayout cdmaLayout;

    //WCDMA
    private LinearLayout wcdmaLayout;

    //TD
    private LinearLayout tdLayout;

    //LTE
    private LinearLayout lteLayout;

    //WIFI
    private LinearLayout wifiLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_state);
        btsStateDao = new BtsStateDao(this);
        userDao = new UserDao(this);
        changeToCollectStationButton = (Button) findViewById(R.id.change_to_collect_station);
        connStateTextView = (TextView) findViewById(R.id.conn_state_text_view);

        gsmLayout = (LinearLayout) findViewById(R.id.gsm_layout);
        cdmaLayout = (LinearLayout) findViewById(R.id.cdma_layout);
        wcdmaLayout = (LinearLayout) findViewById(R.id.wcdma_layout);
        tdLayout = (LinearLayout) findViewById(R.id.td_layout);
        lteLayout = (LinearLayout) findViewById(R.id.lte_layout);
        wifiLayout = (LinearLayout) findViewById(R.id.wifi_layout);

        String title = PreferencesUtils.getString(this, "WORK_MODE");
        String[] titles;
        if (title.contains(";")) {
            titles = title.split(";");
        } else {
            titles = new String[]{title};
        }

        showContentIf(titles);

        if (titles.length > 1) {
            changeToCollectStationButton.setVisibility(View.GONE);
        } else {
            changeToCollectStationButton.setVisibility(View.VISIBLE);
        }


        gsmPauseStationButton = (Button) findViewById(R.id.gsm_pause_station);
        gsmBtsStateTextView = (TextView) findViewById(R.id.gsm_bts_state_text_view);
        gsmProxyStateImage = (ImageView) findViewById(R.id.gsm_proxy_state_image);
        gsmGetPhoneNumStateImage = (ImageView) findViewById(R.id.gsm_get_phone_num_state_image);
//        gsmSystemModeTextView = (TextView) findViewById(R.id.gsm_sys_mode_text_view);
//        gpsStateTextView = (TextView) findViewById(R.id.gsm_gps_state_text_view);

        Intent intent = new Intent(this, UdpDataSendService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.zte.monitor.action.ACTION_CHECK_CONNECTION");
        registerReceiver(receiver, intentFilter);
    }

    private void showContentIf(String[] titles) {
        gsmLayout.setVisibility(View.GONE);
        cdmaLayout.setVisibility(View.GONE);
        wcdmaLayout.setVisibility(View.GONE);
        tdLayout.setVisibility(View.GONE);
        lteLayout.setVisibility(View.GONE);
        wifiLayout.setVisibility(View.GONE);
        if (titles.length > 0) {
            for (String title : titles) {
                if (title.equals(SystemConstants.NETWORK_SYSTEM.GSM)) {
                    gsmLayout.setVisibility(View.VISIBLE);
                } else if (title.equals(SystemConstants.NETWORK_SYSTEM.CDMA)) {
                    cdmaLayout.setVisibility(View.VISIBLE);
                } else if (title.equals(SystemConstants.NETWORK_SYSTEM.WCDMA)) {
                    wcdmaLayout.setVisibility(View.VISIBLE);
                } else if (title.equals(SystemConstants.NETWORK_SYSTEM.TD)) {
                    tdLayout.setVisibility(View.VISIBLE);
                } else if (title.equals(SystemConstants.NETWORK_SYSTEM.LTE)) {
                    lteLayout.setVisibility(View.VISIBLE);
                } else if (title.equals(SystemConstants.NETWORK_SYSTEM.WIFI)) {
                    wifiLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadData();
    }

    private void reloadData() {
        final StateModel stateModel = btsStateDao.get();
        if (stateModel != null) {
            if (stateModel.sysMode == SystemConstants.SYSTEM_MODE.MODE_FUNCTION) {
                changeToCollectStationButton.setVisibility(View.VISIBLE);
                changeToCollectStationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(EquipmentStateActivity.this)
                                .setTitle("提示").setMessage("切换采集站将会终端监听，是否确认切换?")
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                IoBuffer buffer = CodecManager.getManager().collectUserReqEncode((byte) 0);
                                buffer.flip();
                                byte[] request = new byte[buffer.limit()];
                                buffer.get(request);
                                try {
                                    anInterface.sendRequest(request);
                                    StateModel stateModel = btsStateDao.get();
                                    if (stateModel == null) {
                                        stateModel = new StateModel();
                                    }
                                    stateModel.sysMode = SystemConstants.SYSTEM_MODE.MODE_COLLECT;
                                    btsStateDao.saveOrUpdate(stateModel);
                                    userDao.clear();
                                    reloadData();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                    ToastUtils.show(EquipmentStateActivity.this, "采集用户请求发送失败!");
                                }
                            }
                        }).create().show();
                    }
                });
            } else {
                changeToCollectStationButton.setVisibility(View.GONE);
            }

            if (stateModel.btsState == SystemConstants.BTS_STATUS.STATION_START_SUCCESS) {
                gsmPauseStationButton.setVisibility(View.VISIBLE);
                gsmPauseStationButton.setText("暂停基站");
                gsmPauseStationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(EquipmentStateActivity.this)
                                .setTitle("提示").setMessage("确认暂停基站吗?")
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                IoBuffer buffer = CodecManager.getManager().taskCfgReqEncode((byte) 2, (byte) 0, null, (byte) 2,
                                        (short) 460, (byte) 0, (byte) 0, (short) 0
                                        , (byte) 0, (short) 0, (byte) 1, (byte) 1);
                                buffer.flip();
                                byte[] out = new byte[buffer.limit()];
                                buffer.get(out);
                                try {
                                    anInterface.sendRequest(out);
                                    stateModel.btsState = SystemConstants.BTS_STATUS.STATION_PAUSE;
                                    btsStateDao.saveOrUpdate(stateModel);
                                    gsmPauseStationButton.setText("恢复基站");
                                    reloadData();
//                                    DialogUtils.showProgressDialog(EquipmentStateActivity.this, "正在暂停基站，请稍后...");
                                } catch (RemoteException e) {
                                    e.printStackTrace();
//                                    DialogUtils.dismissProgressDialog();
                                    ToastUtils.show(EquipmentStateActivity.this, "暂停基站失败");
                                }
                            }
                        }).create().show();
                    }
                });
            } else if (stateModel.btsState == SystemConstants.BTS_STATUS.STATION_PAUSE) {
                gsmPauseStationButton.setVisibility(View.VISIBLE);
                gsmPauseStationButton.setText("恢复基站");
                gsmPauseStationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(EquipmentStateActivity.this)
                                .setTitle("提示").setMessage("确认恢复基站吗?")
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                IoBuffer pauseStationBuffer = CodecManager.getManager().taskCfgReqEncode((byte) 2, (byte) 0, null, (byte) 2,
                                        (short) 460, (byte) 0, (byte) 0, (short) 0
                                        , (byte) 0, (short) 0, (byte) 1, (byte) 1);
                                pauseStationBuffer.flip();
                                byte[] out = new byte[pauseStationBuffer.limit()];
                                pauseStationBuffer.get(out);
                                try {
                                    anInterface.sendRequest(out);
                                    stateModel.btsState = SystemConstants.BTS_STATUS.STATION_START_SUCCESS;
                                    btsStateDao.saveOrUpdate(stateModel);
                                    gsmPauseStationButton.setText("暂停基站");
                                    reloadData();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
//                                    DialogUtils.dismissProgressDialog();
                                    ToastUtils.show(EquipmentStateActivity.this, "基站恢复失败");
                                }

                                if (stateModel.sysMode == SystemConstants.SYSTEM_MODE.MODE_FUNCTION) {
                                    IoBuffer buffer = CodecManager.getManager().collectUserReqEncode((byte) 0);
                                    buffer.flip();
                                    byte[] request = new byte[buffer.limit()];
                                    buffer.get(request);
                                    try {
                                        anInterface.sendRequest(request);
                                        StateModel stateModel = btsStateDao.get();
                                        if (stateModel == null) {
                                            stateModel = new StateModel();
                                        }
                                        stateModel.sysMode = SystemConstants.SYSTEM_MODE.MODE_COLLECT;
                                        btsStateDao.saveOrUpdate(stateModel);
                                        userDao.clear();
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                        ToastUtils.show(EquipmentStateActivity.this, "采集用户请求发送失败!");
                                    }
                                }


                            }
                        }).create().show();
                    }
                });
            }

            fillData(stateModel);
        } else {
            StateModel temp = new StateModel();
            fillData(temp);
            gsmPauseStationButton.setVisibility(View.GONE);
            gsmPauseStationButton.setOnClickListener(null);

            changeToCollectStationButton.setVisibility(View.GONE);
            changeToCollectStationButton.setOnClickListener(null);
        }
    }

    private void fillData(StateModel stateModel) {
        gsmBtsStateTextView.setText(SystemConstants.BTS_STATE.get(stateModel.btsState));
        connStateTextView.setText(SystemConstants.CONN_STATE.get(stateModel.connState));
//        gsmSystemModeTextView.setText(SystemConstants.SYSTEM_MODE_DATA.get(stateModel.sysMode));
//        gpsStateTextView.setText();
        switch (stateModel.proxyCardState) {
            case SystemConstants.CARD_STATE.STATE_RED:
                gsmProxyStateImage.setBackgroundResource(R.drawable.red_icon);
                break;
            case SystemConstants.CARD_STATE.STATE_GREEN:
                gsmProxyStateImage.setBackgroundResource(R.drawable.green_icon);
                break;
            case SystemConstants.CARD_STATE.STATE_ERROR:
                gsmProxyStateImage.setBackgroundResource(R.drawable.gray_icon);
                break;
            default:
                gsmProxyStateImage.setBackgroundResource(R.drawable.gray_icon);
                break;
        }

        switch (stateModel.getPhNumCardState) {
            case SystemConstants.CARD_STATE.STATE_RED:
                gsmGetPhoneNumStateImage.setBackgroundResource(R.drawable.red_icon);
                break;
            case SystemConstants.CARD_STATE.STATE_GREEN:
                gsmGetPhoneNumStateImage.setBackgroundResource(R.drawable.green_icon);
                break;
            case SystemConstants.CARD_STATE.STATE_ERROR:
                gsmGetPhoneNumStateImage.setBackgroundResource(R.drawable.gray_icon);
                break;
            default:
                gsmGetPhoneNumStateImage.setBackgroundResource(R.drawable.gray_icon);
                break;

        }
    }

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
            Log.e("Tag", action);
            if (action.equals("com.zte.monitor.action.ACTION_CHECK_CONNECTION")) {
                reloadData();
            }
        }
    };
}
