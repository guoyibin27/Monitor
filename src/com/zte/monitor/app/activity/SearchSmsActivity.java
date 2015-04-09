package com.zte.monitor.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.adapter.SmsListAdapter;
import com.zte.monitor.app.database.dao.SmsDao;
import com.zte.monitor.app.model.SmsModel;
import com.zte.monitor.app.util.ScreenUtils;
import com.zte.monitor.app.util.StringUtils;
import com.zte.monitor.app.util.ToastUtils;

import java.util.List;

public class SearchSmsActivity extends BaseActivity implements SearchView.OnQueryTextListener {

    private ListView mListView;
    private SmsListAdapter mAdapter;
    private SmsDao smsDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_sms);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setTitle("");

        smsDao = new SmsDao(this);
        mListView = (ListView) findViewById(R.id.list_view);
        RelativeLayout mLayout = (RelativeLayout) findViewById(R.id.layout);
        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mAdapter = new SmsListAdapter(this, R.layout.sms_list_item);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SmsModel smsModel = (SmsModel) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(SearchSmsActivity.this, SmsDetailActivity.class);
                intent.putExtra(SmsDetailActivity.DATA, smsModel);
                smsModel.status = SystemConstants.SMS_STATUS.UNHANDLED;
                smsDao.update(smsModel);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem searchViewItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchViewItem.getActionView();
        searchView.onActionViewExpanded();
        ViewGroup.LayoutParams layoutParams = searchView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        layoutParams.width = ScreenUtils.getScreenResolution(this).getWidth();
        searchView.setLayoutParams(layoutParams);
        searchView.setQueryHint("搜索短信");
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(Animation.INFINITE, Animation.INFINITE);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        if (!StringUtils.isBlank(s)) {
            mAdapter.getData().clear();
            List<SmsModel> smsModelList = smsDao.searchSms(s);
            if (smsModelList != null && smsModelList.size() > 0) {
                mAdapter.getData().addAll(smsModelList);
            } else {
                ToastUtils.show(SearchSmsActivity.this, "无搜索结果");
            }
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter.getData().clear();
            List<SmsModel> smsModelList = smsDao.getSmsList();
            if (smsModelList != null && smsModelList.size() > 0) {
                mAdapter.getData().addAll(smsModelList);
            } else {
                ToastUtils.show(SearchSmsActivity.this, "无搜索结果");
            }
            mAdapter.notifyDataSetChanged();
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
//        if (!StringUtils.isBlank(s)) {
//            mListView.setVisibility(View.VISIBLE);
//        } else {
//            mListView.setVisibility(View.GONE);
//        }
        return false;
    }
}
