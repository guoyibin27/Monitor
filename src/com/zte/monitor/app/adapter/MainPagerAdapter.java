package com.zte.monitor.app.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.zte.monitor.app.R;
import com.zte.monitor.app.activity.fragment.BlackWhiteUserListFragment;
import com.zte.monitor.app.activity.fragment.UnknownUserListFragment;

/**
 * Created by Sylar on 8/26/14.
 */
public class MainPagerAdapter extends FragmentPagerAdapter {
    private Context mContext;
    private String[] titles;

    public MainPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.mContext = context;
        titles = mContext.getResources().getStringArray(R.array.main_section_title);
    }

    @Override
    public Fragment getItem(int i) {
        if (i == 0) {
            return BlackWhiteUserListFragment.newInstance(null);
        } else if (i == 1) {
            return UnknownUserListFragment.newInstance(null);
        } else {
            return null;
        }
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
