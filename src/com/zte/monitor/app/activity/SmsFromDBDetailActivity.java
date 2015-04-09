package com.zte.monitor.app.activity;

import android.os.Bundle;
import android.widget.TextView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.model.response.SmsInfoResponse;

/**
 * Created by Sylar on 15/4/9.
 */
public class SmsFromDBDetailActivity extends BaseActivity {

    private TextView imsiTextView;
    private TextView dateTimeTextView;
    private TextView smsTypeTextView;
    private TextView phoneTextView;
    private TextView modPhoneTextView;
    private TextView contentTextView;
    private TextView modContentTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_from_db_detail);

        imsiTextView = (TextView) findViewById(R.id.tv_sms_imsi);
        smsTypeTextView = (TextView) findViewById(R.id.tv_sms_direction);
        dateTimeTextView = (TextView) findViewById(R.id.tv_sms_date_time);
        phoneTextView = (TextView) findViewById(R.id.tv_sms_phone_num);
        modPhoneTextView = (TextView) findViewById(R.id.tv_sms_mod_phone_num);
        contentTextView = (TextView) findViewById(R.id.tv_sms_content);
        modContentTextView = (TextView) findViewById(R.id.tv_sms_mod_content);

        SmsInfoResponse response = (SmsInfoResponse) getIntent().getSerializableExtra("SmsModel");
        imsiTextView.setText(response.imsi);
        if (response.smsType == (byte) 1) {
            smsTypeTextView.setText("发送短信");
        } else if (response.smsType == (byte) 2) {
            smsTypeTextView.setText("接收短信");
        }
        dateTimeTextView.setText(response.datetime);
        phoneTextView.setText(response.smsNum);
        modPhoneTextView.setText(response.modSmsNum);
        contentTextView.setText(response.message);
        modContentTextView.setText(response.modMessage);
    }
}
