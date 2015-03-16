package com.zte.monitor.app.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.model.RcdFileModel;

/**
 * Created by Sylar on 14-9-17.
 */
public class RcdListAdapter extends AbstractListViewAdapter<RcdFileModel> {
    public RcdListAdapter(Activity activity, int layoutResId) {
        super(activity, layoutResId);
    }

    @Override
    protected IViewHolder<RcdFileModel> getViewHolder() {
        return new ViewHolder();
    }

    private class ViewHolder implements IViewHolder<RcdFileModel> {
        private TextView fileName;
        private RadioButton radioButton;

        @Override
        public void init(View view, int position) {
            fileName = (TextView) view.findViewById(R.id.file_name);
            radioButton = (RadioButton) view.findViewById(R.id.radio_button);
        }

        @Override
        public void update(RcdFileModel rcdFileModel, int position) {
            fileName.setText(rcdFileModel.fileName);

        }
    }
}
