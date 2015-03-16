package com.zte.monitor.app.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.model.UserModel;
import com.zte.monitor.app.util.StringUtils;

/**
 * Created by Sylar on 15/2/24.
 */
public class SelectImsiAdapter extends AbstractListViewAdapter<UserModel> {

    public SelectImsiAdapter(Activity activity, int layoutResId) {
        super(activity, layoutResId);
    }

    @Override
    protected IViewHolder<UserModel> getViewHolder() {
        return new ImsiViewHolder();
    }

    private class ImsiViewHolder implements IViewHolder<UserModel> {

        private CheckBox checkBox;
        private TextView imsi;

        @Override
        public void init(View view, int position) {
            checkBox = (CheckBox) view.findViewById(R.id.check_box);
            imsi = (TextView) view.findViewById(R.id.tv_imsi);
        }

        @Override
        public void update(UserModel userModel, int position) {
            checkBox.setChecked(userModel.isChecked);
            if (!StringUtils.isBlank(userModel.imsi)) {
                imsi.setText(userModel.imsi);
            } else if (!StringUtils.isBlank(userModel.imei)) {
                imsi.setText(userModel.imei);
            }
        }
    }
}
