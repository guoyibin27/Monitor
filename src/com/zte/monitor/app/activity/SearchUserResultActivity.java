package com.zte.monitor.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.adapter.UserListAdapter;
import com.zte.monitor.app.model.UserModel;

import java.util.List;

/**
 * Created by Sylar on 14-9-15.
 */
public class SearchUserResultActivity extends BaseActivity {
    private ListView mListView;
    private UserListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user_result);
        mListView = (ListView) findViewById(R.id.list_view);
        mAdapter = new UserListAdapter(SearchUserResultActivity.this, R.layout.user_list_item);
        mListView.setAdapter(mAdapter);
        List<UserModel> userModelList = (List<UserModel>) getIntent().getSerializableExtra("DATA");
        mAdapter.getData().addAll(userModelList);
        mAdapter.notifyDataSetChanged();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                UserModel user = (UserModel) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(SearchUserResultActivity.this, UserDetailActivity.class);
                intent.putExtra(UserDetailActivity.DATA, user);
                startActivity(intent);
            }
        });
    }
}
