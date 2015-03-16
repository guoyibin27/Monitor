package com.zte.monitor.app.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import com.zte.monitor.app.R;
import com.zte.monitor.app.activity.widget.PagerSlidingTab;
import com.zte.monitor.app.adapter.SettingsAdapter;
import com.zte.monitor.app.util.PreferencesUtils;

/**
 * Created by Sylar on 14-9-13.
 */
public class SettingsActivity extends BaseActivity {

    private PagerSlidingTab tabs;
    private ViewPager pager;
    private SettingsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        tabs = (PagerSlidingTab) findViewById(R.id.tabs);
        pager = (ViewPager) findViewById(R.id.view_pager);
        String title = PreferencesUtils.getString(this, "WORK_MODE");
        String[] titles;
        if (title.contains(";")) {
            titles = title.split(";");
        } else {
            titles = new String[]{title};
        }
        mAdapter = new SettingsAdapter(this, getSupportFragmentManager(), titles);
        pager.setAdapter(mAdapter);
        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);
        tabs.setViewPager(pager);
    }
}
