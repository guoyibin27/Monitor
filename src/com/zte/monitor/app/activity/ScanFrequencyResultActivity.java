package com.zte.monitor.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.adapter.ScanFrequencyResultAdapter;
import com.zte.monitor.app.model.response.ScanFreqResponse;
import com.zte.monitor.app.util.ToastUtils;

import java.util.List;

/**
 * Created by Sylar on 14-10-14.
 * 小区扫频结果列表
 */
public class ScanFrequencyResultActivity extends BaseActivity {

    private ListView mResultListView;
    private ScanFrequencyResultAdapter adapter;
    private Button doneButton;
    private ScanFreqResponse.Entry2 selectedEntry;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_frequency_result);
        mResultListView = (ListView) findViewById(R.id.result_list_view);
        mResultListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        adapter = new ScanFrequencyResultAdapter(this, R.layout.scan_frequency_result_list_item);
        List<ScanFreqResponse.Entry2> data = (List<ScanFreqResponse.Entry2>) getIntent().getSerializableExtra("DATA");
        mResultListView.setAdapter(adapter);
        adapter.getData().addAll(data);
        adapter.notifyDataSetChanged();
        mResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedEntry = (ScanFreqResponse.Entry2) adapterView.getItemAtPosition(i);
                for (int j = 0; j < adapter.getData().size(); j++) {
                    ScanFreqResponse.Entry2 entry = adapter.getData().get(j);
                    if (i == j) {
                        entry.isChecked = true;
                    } else {
                        entry.isChecked = false;
                    }
                }
                adapter.notifyDataSetInvalidated();
            }
        });
        doneButton = (Button) findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedEntry == null) {
                    ToastUtils.show(ScanFrequencyResultActivity.this, "请选择频点信息");
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("DATA", selectedEntry);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
