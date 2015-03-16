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
import com.zte.monitor.app.database.dao.SensitivePhoneNumberDao;

import java.util.List;

/**
 * Created by Sylar on 14-10-8.
 */
public class SensitivePhoneNumberActivity extends BaseActivity {

    private ListView listView;
    private SensitiveListAdapter adapter;
    private int currentMode = SystemConstants.MODE.PREVIEW;
    private SensitivePhoneNumberDao sensitivePhoneNumberDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensitive_phone_number);
        sensitivePhoneNumberDao = new SensitivePhoneNumberDao(SensitivePhoneNumberActivity.this);
        listView = (ListView) findViewById(R.id.sensitive_number_list_view);
        adapter = new SensitiveListAdapter(SensitivePhoneNumberActivity.this, R.layout.sensitive_list_item, new SensitiveListAdapter.Callback() {
            @Override
            public void onDelete(String s) {
                sensitivePhoneNumberDao.delete(s);
                loadData();
            }
        });
        currentMode = SystemConstants.MODE.PREVIEW;
        adapter.setMode(SystemConstants.MODE.PREVIEW);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                adapter.setMode(SystemConstants.MODE.EDIT);
                currentMode = SystemConstants.MODE.EDIT;
                adapter.notifyDataSetChanged();
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
        adapter.getData().clear();
        List<String> sensitiveWordList = sensitivePhoneNumberDao.getList();
        adapter.getData().addAll(sensitiveWordList);
        adapter.notifyDataSetInvalidated();
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
                startActivityForResult(new Intent(SensitivePhoneNumberActivity.this, AddSensitiveNumberActivity.class), 100);
            }
        } else if (currentMode == SystemConstants.MODE.EDIT) {
            if (item.getItemId() == 0) {
                adapter.setMode(SystemConstants.MODE.PREVIEW);
                currentMode = SystemConstants.MODE.PREVIEW;
                adapter.notifyDataSetChanged();
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
