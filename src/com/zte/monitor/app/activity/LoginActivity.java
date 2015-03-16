package com.zte.monitor.app.activity;

import android.app.AlertDialog;
import android.content.*;
import android.os.*;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.zte.monitor.app.MonitorApplication;
import com.zte.monitor.app.R;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.database.dao.*;
import com.zte.monitor.app.udp.UdpDataSendService;
import com.zte.monitor.app.udp.aidl.IUdpConnectionInterface;
import com.zte.monitor.app.util.DialogUtils;
import com.zte.monitor.app.util.StringUtils;
import com.zte.monitor.app.util.TextToSpeechManager;
import com.zte.monitor.app.util.ToastUtils;
import org.apache.mina.core.buffer.IoBuffer;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final int PORT = 20001;
    private EditText mUserNameEdt;
    private EditText mPasswordEdt;
    private Button mLoginBtn;
    private IUdpConnectionInterface anInterface;
    private ServerInfoDao serverInfoDao;
    private BtsStateDao btsStateDao;
    private MonitorLineDao monitorLineDao;
    private SensitiveWordDao sensitiveWordDao;
    private SensitivePhoneNumberDao sensitivePhoneNumberDao;
    private SettingDao settingDao;
    private UserDao userDao;
    private SmsDao smsDao;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (StringUtils.isBlank(action)) {
                return;
            }
            if ("com.zte.monitor.action.ACTION_HEART_BEAT_SUCCESS".equals(action)) {
                DialogUtils.dismissProgressDialog();
                handler.removeMessages(1);
                monitorLineDao.clear();
                smsDao.clear();
                userDao.clear();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                DialogUtils.dismissProgressDialog();
                ToastUtils.show(LoginActivity.this, "登录失败");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        serverInfoDao = new ServerInfoDao(this);
        btsStateDao = new BtsStateDao(this);
        monitorLineDao = new MonitorLineDao(this);
        sensitiveWordDao = new SensitiveWordDao(this);
        sensitivePhoneNumberDao = new SensitivePhoneNumberDao(this);
        settingDao = new SettingDao(this);
        userDao = new UserDao(this);
        smsDao = new SmsDao(this);

        mUserNameEdt = (EditText) findViewById(R.id.user_name);
        mPasswordEdt = (EditText) findViewById(R.id.user_password);
        mLoginBtn = (Button) findViewById(R.id.button_login);
        mLoginBtn.setOnClickListener(this);

        Intent intent = new Intent(LoginActivity.this, UdpDataSendService.class);
        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.zte.monitor.action.ACTION_HEART_BEAT_SUCCESS");
        filter.addAction("com.zte.monitor.action.ACTION_HEART_BEAT_FAILED");
        registerReceiver(receiver, filter);
        MonitorApplication.clientId = (byte) new Random(256).nextInt();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final TextToSpeechManager manager = TextToSpeechManager.getManager(getApplicationContext());
        if (!manager.checkSpeechServiceInstall()) {
            new AlertDialog.Builder(this).setTitle("提示")
                    .setMessage("缺少语音包，请点击确认安装").setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String assetsApk = "SpeechService.apk";
                                    manager.processInstall(LoginActivity.this, assetsApk);
                                }
                            });
                        }
                    }).create().show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_login:
                String userName = mUserNameEdt.getText().toString();
                String password = mPasswordEdt.getText().toString();

                if (StringUtils.isBlank(userName)) {
                    ToastUtils.show(LoginActivity.this, R.string.user_name_not_empty);
                    return;
                }

                if (StringUtils.isBlank(password)) {
                    ToastUtils.show(LoginActivity.this, R.string.password_not_empty);
                    return;
                }

                if (!(userName.equalsIgnoreCase("admin") && password.equalsIgnoreCase("123456"))) {
                    ToastUtils.show(LoginActivity.this, "用户名密码错误");
                    return;
                }
                final CodecManager manager = CodecManager.getManager();
                IoBuffer heatBeatBuff = manager.heartBeatReqEncode((byte) 1, MonitorApplication.clientId, (byte) 10);
                heatBeatBuff.flip();
                byte[] request = new byte[heatBeatBuff.limit()];
                heatBeatBuff.get(request);
                try {
                    anInterface.sendRequest(request);
                    DialogUtils.showProgressDialog(LoginActivity.this, "正在登录...");
                    handler.sendEmptyMessageDelayed(1, 5 * 1000);
                } catch (RemoteException e) {
                    DialogUtils.dismissProgressDialog();
                    ToastUtils.show(LoginActivity.this, "登录请求发送失败!");
                    e.printStackTrace();
                }
//                startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                finish();
                break;
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DialogUtils.dismissProgressDialog();
            DialogUtils.showAlertDialog(LoginActivity.this, "服务器无响应,是否切换服务器?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    showInputServerAddressDialog();
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    startService(new Intent(LoginActivity.this, UdpDataSendService.class));
                }
            });
        }
    };

    private void showInputServerAddressDialog() {
        final EditText serverAddress = new EditText(this);
        serverAddress.setHint("服务器");
        serverAddress.setSingleLine(true);
        serverAddress.setKeyListener(new NumberKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                return new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.'};
            }

            @Override
            public int getInputType() {
                return InputType.TYPE_NUMBER_FLAG_DECIMAL;
            }
        });
        serverAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        new AlertDialog.Builder(this).setTitle("请输入服务器地址")
                .setView(serverAddress).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String address = serverAddress.getText().toString();
                Pattern pattern = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
                Matcher matcher = pattern.matcher(address);
                if (!matcher.matches()) {
                    ToastUtils.show(LoginActivity.this, "服务器地址输入格式不正确");

                    try {
                        Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialogInterface, false);
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
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
                    serverInfoDao.update(address, PORT);
                    startService(new Intent(LoginActivity.this, UdpDataSendService.class));
                }
            }
        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
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
                dialogInterface.dismiss();
            }
        }).create().show();
    }

    ServiceConnection connection = new ServiceConnection() {
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
    protected void onDestroy() {
        super.onDestroy();
        DialogUtils.dismissProgressDialog();
        unbindService(connection);
        unregisterReceiver(receiver);
    }
}
