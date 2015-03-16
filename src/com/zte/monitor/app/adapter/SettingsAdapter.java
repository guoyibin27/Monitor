package com.zte.monitor.app.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.activity.fragment.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylar on 14-9-13.
 */
public class SettingsAdapter extends FragmentPagerAdapter {
    private Context mContext;
    private String[] titles;
    private List<Fragment> fragmentList;

    public SettingsAdapter(Context context, FragmentManager fm, String[] titles) {
        super(fm);
        this.mContext = context;
        this.titles = titles;
        fragmentList = new ArrayList<Fragment>(titles.length);
        for (String title : titles) {
            if (title.equals(SystemConstants.NETWORK_SYSTEM.GSM)) {
                fragmentList.add(new GSMSettingFragment());
            }
            if (title.equals(SystemConstants.NETWORK_SYSTEM.CDMA)) {
                fragmentList.add(new CDMASettingFragment());
            }
            if (title.equals(SystemConstants.NETWORK_SYSTEM.WCDMA)) {
                fragmentList.add(new WCDMASettingFragment());
            }
            if (title.equals(SystemConstants.NETWORK_SYSTEM.TD)) {
                fragmentList.add(new TDSettingFragment());
            }
            if (title.equals(SystemConstants.NETWORK_SYSTEM.LTE)) {
                fragmentList.add(new LTESettingFragment());
            }
            if (title.equals(SystemConstants.NETWORK_SYSTEM.WIFI)) {
                fragmentList.add(new WiFiSettingFragment());
            }
        }
    }

    @Override
    public Fragment getItem(int i) {
        return fragmentList.get(i);
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

}
