package com.zte.monitor.app.activity;

import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.adapter.MainPagerAdapter;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.database.SQLiteManager;
import com.zte.monitor.app.database.dao.*;
import com.zte.monitor.app.pcm.PlayService;
import com.zte.monitor.app.udp.UdpDataSendService;
import com.zte.monitor.app.udp.aidl.IUdpConnectionInterface;
import com.zte.monitor.app.util.NotificationUtils;
import com.zte.monitor.app.util.PreferencesUtils;
import com.zte.monitor.app.util.TextToSpeechManager;
import com.zte.monitor.app.util.ToastUtils;
import org.apache.mina.core.buffer.IoBuffer;

import java.lang.reflect.Field;

/**
 * Created by Sylar on 8/26/14.
 */
public class MainActivity extends BaseActivity {
    private Button blackListButton;
    private Button unknownListButton;
    private ViewPager pager;
    private MainPagerAdapter mAdapter;
    private ListView listView;
    private IUdpConnectionInterface anInterface;
    private BtsStateDao btsStateDao;
    private MonitorLineDao monitorLineDao;
    private SensitiveWordDao sensitiveWordDao;
    private SensitivePhoneNumberDao sensitivePhoneNumberDao;
    private SettingDao settingDao;
    private UserDao userDao;
    private SmsDao smsDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btsStateDao = new BtsStateDao(this);
        monitorLineDao = new MonitorLineDao(this);
        sensitiveWordDao = new SensitiveWordDao(this);
        sensitivePhoneNumberDao = new SensitivePhoneNumberDao(this);
        settingDao = new SettingDao(this);
        userDao = new UserDao(this);
        smsDao = new SmsDao(this);
        blackListButton = (Button) findViewById(R.id.blackListButton);
        blackListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blackListButton.setBackgroundResource(R.drawable.btn_left_pressed);
                pager.setCurrentItem(0);
//                startActivity(new Intent(MainActivity.this, UserDetailActivity.class));

            }
        });


        unknownListButton = (Button) findViewById(R.id.unknownListButton);
        unknownListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unknownListButton.setBackgroundResource(R.drawable.btn_right_pressed);
                pager.setCurrentItem(1);
            }
        });

        pager = (ViewPager) findViewById(R.id.view_pager);
        mAdapter = new MainPagerAdapter(this, getSupportFragmentManager());
        pager.setAdapter(mAdapter);
        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (i == 0) {//黑白名单
                    blackListButton.setBackgroundResource(R.drawable.btn_left_pressed);
                    unknownListButton.setBackgroundResource(R.drawable.btn_right_normal);
                } else if (i == 1) {//未知用户
                    blackListButton.setBackgroundResource(R.drawable.btn_left_normal);
                    unknownListButton.setBackgroundResource(R.drawable.btn_right_pressed);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        createWorkModeView();
//        createProxyPhoneNumberDialog();
        Intent intent = new Intent(this, UdpDataSendService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        startService(new Intent(this, PlayService.class));
    }

    private void createWorkModeView() {
        final AlertDialog builder = new AlertDialog.Builder(this)
                .setTitle("请选择工作制式")
                .setMultiChoiceItems(new String[]{SystemConstants.NETWORK_SYSTEM.GSM, SystemConstants.NETWORK_SYSTEM.CDMA,
                        SystemConstants.NETWORK_SYSTEM.WCDMA, SystemConstants.NETWORK_SYSTEM.TD, SystemConstants.NETWORK_SYSTEM.LTE,
                        SystemConstants.NETWORK_SYSTEM.WIFI}, new boolean[]{false, false, false, false, false, false}, null)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (listView.getCheckedItemCount() == 0) {
                            try {
                                Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialogInterface, false);
                            } catch (NoSuchFieldException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            ToastUtils.show(MainActivity.this, "必须选择一种以上的工作制式");
                        } else {
                            try {
                                Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialogInterface, true);
                            } catch (NoSuchFieldException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            StringBuffer buffer = new StringBuffer();
                            for (int j = 0; j < listView.getCount(); j++) {
                                if (listView.getCheckedItemPositions().get(j)) {
                                    buffer.append(listView.getAdapter().getItem(j).toString()).append(";");
                                }
                            }
                            buffer = buffer.replace(buffer.length() - 1, buffer.length(), "");
                            PreferencesUtils.putString(MainActivity.this, "WORK_MODE", buffer.toString());
                            NotificationUtils.getInstance(getApplicationContext()).createCustomNotification();
                        }
                    }
                }).create();
        listView = builder.getListView();
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_add:
                startActivity(new Intent(MainActivity.this, AddUserActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle("提示").setMessage("确认退出程序?").setCancelable(false)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
//                System.exit(0);
            }
        }).create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextToSpeechManager.getManager(getApplicationContext()).initialize();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IoBuffer buffer = CodecManager.getManager().shutServerReqEncode((byte) 0);
        buffer.flip();
        byte[] request = new byte[buffer.limit()];
        buffer.get(request);
        try {
            anInterface.sendRequest(request);
            btsStateDao.clear();
            monitorLineDao.clear();
//            sensitivePhoneNumberDao.clear();
//            sensitiveWordDao.clear();
            smsDao.clear();
            userDao.clear();
//            settingDao.clear();
            unbindService(connection);
            stopService(new Intent(MainActivity.this, UdpDataSendService.class));
            stopService(new Intent(MainActivity.this, PlayService.class));
            SQLiteManager.getInstance(MainActivity.this).release();
            TextToSpeechManager.getManager(getApplicationContext()).release();
            NotificationUtils.getInstance(getApplicationContext()).cancelCustomNotification();
        } catch (RemoteException e) {
            e.printStackTrace();
//            ToastUtils.show(MainActivity.this, "关闭请求发送失败");
        }
    }

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
}
