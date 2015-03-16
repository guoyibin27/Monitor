package com.zte.monitor.app.activity.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.zte.monitor.app.R;

/**
 * Created by Sylar on 14-9-13.
 */
public class CDMASettingFragment extends Fragment {

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.cdma_setting_fragment, null);
        return view;
    }
}
