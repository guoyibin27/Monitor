package com.zte.monitor.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.adapter.SelectImsiAdapter;
import com.zte.monitor.app.database.dao.UserDao;
import com.zte.monitor.app.model.UserModel;
import com.zte.monitor.app.util.StringUtils;
import com.zte.monitor.app.util.ToastUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylar on 15/2/24.
 */
public class SelectedIMSIActivity extends BaseActivity {

    private ListView listView;
    private Button doneButton;
    private SelectImsiAdapter adapter;
    private UserDao userDao;
    private List<UserModel> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_imsi);

        listView = (ListView) findViewById(R.id.list_view);
        adapter = new SelectImsiAdapter(this, R.layout.imsi_list_item);
        listView.setAdapter(adapter);

        doneButton = (Button) findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userList != null && userList.size() > 0) {
                    List<UserModel> userModelList = new ArrayList<UserModel>();
                    for (UserModel userModel : userList) {
                        if (userModel.isChecked)
                            userModelList.add(userModel);
                    }

                    if (userModelList.size() > 0) {
                        Intent intent = new Intent();
                        intent.putExtra("USER_LIST", (Serializable) userModelList);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        ToastUtils.show(SelectedIMSIActivity.this, "没有选择发送IMSI");
                    }
                } else {
                    ToastUtils.show(SelectedIMSIActivity.this, "没有选择发送IMSI");
                }

            }
        });

        userDao = new UserDao(this);
        userList = userDao.getAllUserList();

        List<UserModel> selectedUserList = (List<UserModel>) getIntent().getSerializableExtra("USER_LIST");
        if (selectedUserList != null && selectedUserList.size() > 0) {
            for (UserModel selectedUser : selectedUserList) {
                for (UserModel userModel : userList) {
                    if (!StringUtils.isBlank(selectedUser.imsi)) {
                        userModel.isChecked = selectedUser.imsi.equals(userModel.imsi);
                    } else if (!StringUtils.isBlank(selectedUser.imei)) {
                        userModel.isChecked = selectedUser.imei.equals(userModel.imei);
                    }
                }
            }
        }

        adapter.getData().addAll(userList);
        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                UserModel userModel = (UserModel) adapterView.getItemAtPosition(i);
                userModel.isChecked = !userModel.isChecked;
                adapter.notifyDataSetChanged();
            }
        });
    }
}
