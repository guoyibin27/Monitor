package com.zte.monitor.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.adapter.SensitiveListAdapter;
import com.zte.monitor.app.database.dao.SensitiveWordDao;

import java.util.List;

/**
 * Created by Sylar on 14-10-8.
 */
public class SensitiveWordActivity extends BaseActivity {
    private ListView sensitiveWordListView;
    private SensitiveListAdapter mAdapter;
    private SensitiveWordDao sensitiveWordDao;
    private int currentMode = SystemConstants.MODE.PREVIEW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensitive_word);
        sensitiveWordDao = new SensitiveWordDao(SensitiveWordActivity.this);
        sensitiveWordListView = (ListView) findViewById(R.id.sensitive_word_list_view);
        mAdapter = new SensitiveListAdapter(SensitiveWordActivity.this, R.layout.sensitive_list_item, new SensitiveListAdapter.Callback() {
            @Override
            public void onDelete(String s) {
                sensitiveWordDao.delete(s);
                loadData();
            }
        });
        currentMode = SystemConstants.MODE.PREVIEW;
        mAdapter.setMode(SystemConstants.MODE.PREVIEW);
        sensitiveWordListView.setAdapter(mAdapter);
        sensitiveWordListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                mAdapter.setMode(SystemConstants.MODE.EDIT);
                currentMode = SystemConstants.MODE.EDIT;
                mAdapter.notifyDataSetChanged();
                invalidateOptionsMenu();
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        mAdapter.getData().clear();
        List<String> sensitiveWordList = sensitiveWordDao.getList();
        mAdapter.getData().addAll(sensitiveWordList);
        mAdapter.notifyDataSetInvalidated();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (currentMode == SystemConstants.MODE.PREVIEW) {
            menu.add(0, 0, 0, "添加").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else if (currentMode == SystemConstants.MODE.EDIT) {
            menu.add(0, 0, 0, "完成").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (currentMode == SystemConstants.MODE.PREVIEW) {
            if (item.getItemId() == 0) {
                startActivityForResult(new Intent(SensitiveWordActivity.this, AddSensitiveWordActivity.class), 100);
            }
        } else if (currentMode == SystemConstants.MODE.EDIT) {
            if (item.getItemId() == 0) {
                mAdapter.setMode(SystemConstants.MODE.PREVIEW);
                currentMode = SystemConstants.MODE.PREVIEW;
                mAdapter.notifyDataSetChanged();
                invalidateOptionsMenu();
            }
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            loadData();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
