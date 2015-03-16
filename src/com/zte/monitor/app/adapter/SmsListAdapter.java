package com.zte.monitor.app.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.model.SmsModel;
import com.zte.monitor.app.util.StringUtils;

/**
 * Created by Sylar on 14-9-3.
 */
public class SmsListAdapter extends AbstractListViewAdapter<SmsModel> {
    public SmsListAdapter(Activity activity, int layoutResId) {
        super(activity, layoutResId);
    }

    @Override
    protected IViewHolder<SmsModel> getViewHolder() {
        return new ViewHolder();
    }

    private class ViewHolder implements IViewHolder<SmsModel> {
        private TextView sendDate;
        private TextView direction;
        private TextView phoneNumber;
        private TextView content;
        private TextView newContent;
        private TextView status;

        @Override
        public void init(View view, int position) {
            sendDate = (TextView) view.findViewById(R.id.sms_send_time);
            direction = (TextView) view.findViewById(R.id.direction);
            phoneNumber = (TextView) view.findViewById(R.id.phone_number);
            content = (TextView) view.findViewById(R.id.content);
            newContent = (TextView) view.findViewById(R.id.new_content);
            status = (TextView) view.findViewById(R.id.sms_status);
        }

        @Override
        public void update(SmsModel smsModel, int position) {
            sendDate.setText(smsModel.upTime);
            phoneNumber.setText(smsModel.phNum);
            if (smsModel.direction == 1) {
                direction.setText("发送短信");
            } else if (smsModel.direction == 2) {
                direction.setText("接收短信");
            }
            content.setText(smsModel.content);
            if (!StringUtils.isBlank(smsModel.newContent)) {
                newContent.setVisibility(View.VISIBLE);
                newContent.setText(smsModel.newContent);
            } else {
                newContent.setVisibility(View.GONE);
                newContent.setText("");
            }
            
            status.setText(SystemConstants.SMS_STATUS_DATA.get(smsModel.status));
        }
    }
}
