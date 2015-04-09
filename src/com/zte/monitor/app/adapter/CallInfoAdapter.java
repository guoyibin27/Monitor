package com.zte.monitor.app.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.model.response.CallInfoResponse;

/**
 * Created by Sylar on 15/4/9.
 */
public class CallInfoAdapter extends AbstractListViewAdapter<CallInfoResponse> {
    public CallInfoAdapter(Activity activity, int layoutResId) {
        super(activity, layoutResId);
    }

    @Override
    protected IViewHolder<CallInfoResponse> getViewHolder() {
        return new ViewHolder();
    }

    private class ViewHolder implements IViewHolder<CallInfoResponse> {

        private TextView imsiTextView;
        private TextView callTypeTextView;
        private TextView dateTimeTextView;
        private TextView phoneTextView;

        @Override
        public void init(View view, int position) {
            imsiTextView = (TextView) view.findViewById(R.id.call_imsi);
            callTypeTextView = (TextView) view.findViewById(R.id.call_type);
            dateTimeTextView = (TextView) view.findViewById(R.id.date_time);
            phoneTextView = (TextView) view.findViewById(R.id.phone_num);
        }

        @Override
        public void update(CallInfoResponse callInfoResponse, int position) {
            imsiTextView.setText(callInfoResponse.imsi);
            if (callInfoResponse.callType == (byte) 1) {
                callTypeTextView.setText("主叫");
            } else if (callInfoResponse.callType == (byte) 2) {
                callTypeTextView.setText("被叫");
            }
            dateTimeTextView.setText(callInfoResponse.callTime);
            phoneTextView.setText(callInfoResponse.phNum);
        }
    }
}
