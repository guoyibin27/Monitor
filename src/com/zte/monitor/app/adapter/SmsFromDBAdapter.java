package com.zte.monitor.app.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.model.response.SmsInfoResponse;
import com.zte.monitor.app.util.StringUtils;

/**
 * Created by Sylar on 15/4/9.
 */
public class SmsFromDBAdapter extends AbstractListViewAdapter<SmsInfoResponse> {

    public SmsFromDBAdapter(Activity activity, int layoutResId) {
        super(activity, layoutResId);
    }

    @Override
    protected IViewHolder<SmsInfoResponse> getViewHolder() {
        return new ViewHolder();
    }

    private class ViewHolder implements IViewHolder<SmsInfoResponse> {

        private TextView imsiTextView;
        private TextView smsTypeTextView;
        private TextView timeTextView;
        private TextView smsContentTextView;

        @Override
        public void init(View view, int position) {
            imsiTextView = (TextView) view.findViewById(R.id.sms_imsi);
            smsTypeTextView = (TextView) view.findViewById(R.id.sms_type);
            timeTextView = (TextView) view.findViewById(R.id.sms_send_time);
            smsContentTextView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public void update(SmsInfoResponse smsModel, int position) {
            imsiTextView.setText(smsModel.imsi);
            if (smsModel.smsType == (byte) 1) {
                smsTypeTextView.setText("发送短信");
            } else if (smsModel.smsType == (byte) 2) {
                smsTypeTextView.setText("接收短信");
            }

            if (!StringUtils.isBlank(smsModel.modMessage)) {
                smsContentTextView.setText(smsModel.modMessage);
                smsContentTextView.setTextColor(Color.RED);
            } else {
                smsContentTextView.setText(smsModel.message);
                smsContentTextView.setTextColor(Color.WHITE);
            }
        }
    }
}
