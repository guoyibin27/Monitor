package com.zte.monitor.app.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.Gravity;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import com.zte.monitor.app.udp.UdpConstants;
import com.zte.monitor.app.util.DialogUtils;
import com.zte.monitor.app.util.StringUtils;
import com.zte.monitor.app.util.ToastUtils;

import java.lang.reflect.Field;

/**
 * Created by Sylar on 14-9-1.
 */
public class BaseActivity extends FragmentActivity {

    private Context mContext;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (StringUtils.isBlank(action)) {
                return;
            }
            if (UdpConstants.ACTION_CONNECT_ERROR.equals(action)) {
                ToastUtils.show(BaseActivity.this, "Upd server connect error");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forceShowActionBarOverflowMenu(this);
        mContext = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UdpConstants.ACTION_CONNECT_ERROR);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public static void forceShowActionBarOverflowMenu(Context context) {
        try {
            ViewConfiguration config = ViewConfiguration.get(context);
            Field[] fields = ViewConfiguration.class.getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals("sHasPermanentMenuKey")) {
                    field.setAccessible(true);
                    field.setBoolean(config, false);
                    break;
                }
            }
//            Log.e("TAG", fields.toString());
//            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DialogUtils.dismissProgressDialog();
            ToastUtils.show(mContext, "服务器无响应,请检查网络");
        }
    };

    public void startLoadingTimer(int messageId) {
        mHandler.sendEmptyMessageDelayed(messageId, 10 * 1000);
    }

    public void startLoadingTimer(int messageId, int loadingTimerSecond) {
        mHandler.sendEmptyMessageDelayed(messageId, loadingTimerSecond * 1000);
    }

    public void removeTimer(int messageId) {
        mHandler.removeMessages(messageId);
    }

    protected void setBorderIfActivityAsDialog() {
        setFinishOnTouchOutside(false);
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay();  //为获取屏幕宽、高

        WindowManager.LayoutParams p = getWindow().getAttributes();  //获取对话框当前的参数值
        p.width = (int) (d.getWidth() * 0.9);    //宽度设置为屏幕的0.9
        p.alpha = 1.0f;      //设置本身透明度
        p.dimAmount = 0.5f;      //设置黑暗度
        getWindow().setAttributes(p);     //设置生效
        getWindow().setGravity(Gravity.CENTER);       //设置靠右对齐
    }
}
