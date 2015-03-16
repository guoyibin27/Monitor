package com.zte.monitor.app.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import com.zte.monitor.app.R;
import com.zte.monitor.app.database.dao.UserDao;
import com.zte.monitor.app.model.UserModel;
import com.zte.monitor.app.udp.aidl.IUdpConnectionInterface;
import com.zte.monitor.app.util.ToastUtils;

import java.util.List;

/**
 * Created by Sylar on 14-9-15.
 */
public class SearchUserActivity extends BaseActivity {

    private EditText mUserName;
    private EditText mImsi;
    private EditText mImei;
    private EditText mPhNum;
    private EditText mArea;

    private UserDao userDao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        setBorderIfActivityAsDialog();

        userDao = new UserDao(this);
        mUserName = (EditText) findViewById(R.id.search_user_name);
        mImsi = (EditText) findViewById(R.id.search_imsi);
        mImei = (EditText) findViewById(R.id.search_imei);
        mPhNum = (EditText) findViewById(R.id.search_ph_num);
        mArea = (EditText) findViewById(R.id.search_area);
        findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<UserModel> userModelList = userDao.getUserListBySearch(mUserName.getText().toString(), mImsi.getText().toString(), mImei.getText().toString(), mPhNum.getText().toString(), mArea.getText().toString());
                if (userModelList != null && userModelList.size() > 0) {
                    Intent intent = new Intent(SearchUserActivity.this, SearchUserResultActivity.class);
                    intent.putExtra("DATA", (java.io.Serializable) userModelList);
                    startActivity(intent);
                } else {
                    ToastUtils.show(SearchUserActivity.this, "无相关记录");
                }
            }
        });
//        Intent intent = new Intent(SearchUserActivity.this, UdpDataSendService.class);
//        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unbindService(connection);
    }
}
