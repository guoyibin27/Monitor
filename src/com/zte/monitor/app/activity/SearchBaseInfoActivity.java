package com.zte.monitor.app.activity;

import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import com.zte.monitor.app.R;
import com.zte.monitor.app.codec.CodecManager;
import com.zte.monitor.app.udp.UdpDataSendService;
import com.zte.monitor.app.udp.aidl.IUdpConnectionInterface;
import com.zte.monitor.app.util.ToastUtils;
import org.apache.mina.core.buffer.IoBuffer;

import java.util.Calendar;

/**
 * Created by Sylar on 15/2/2.
 */
public class SearchBaseInfoActivity extends BaseActivity {

    private EditText mImsi;
    private EditText mImei;
    private EditText dateFrom;
    private EditText dateTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_base_info);
        setBorderIfActivityAsDialog();

        mImsi = (EditText) findViewById(R.id.search_imsi);
        mImei = (EditText) findViewById(R.id.search_imei);
        dateFrom = (EditText) findViewById(R.id.search_date_from);
        dateTo = (EditText) findViewById(R.id.search_date_to);

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int monthOfYear = calendar.get(Calendar.MONTH);
        final int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        dateFrom.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    DatePickerDialog dpd = new DatePickerDialog(SearchBaseInfoActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                            String month = i1 + "";
                            if (i1 + 1 < 10) {
                                month = 0 + "" + (i1 + 1);
                            }

                            String day = i2 + "";
                            if (i2 + 1 < 10) {
                                day = 0 + "" + i2;
                            }
                            dateFrom.setText(i + "-" + month + "-" + day);
                        }
                    }, year, monthOfYear, dayOfMonth);
                    dpd.show();
                }
                return true;
            }
        });

        dateTo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    DatePickerDialog dpd = new DatePickerDialog(SearchBaseInfoActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                            String month = i1 + "";
                            if (i1 + 1 < 10) {
                                month = 0 + "" + (i1 + 1);
                            }

                            String day = i2 + "";
                            if (i2 + 1 < 10) {
                                day = 0 + "" + i2;
                            }
                            dateTo.setText(i + "-" + month + "-" + day);
                        }
                    }, year, monthOfYear, dayOfMonth);
                    dpd.show();
                }
                return true;
            }
        });

        findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchBaseInfoActivity.this, SearchedUserListActivity.class);
                intent.putExtra("SearchType", "BaseInfo");
                intent.putExtra("IMSI", mImsi.getText().toString());
                intent.putExtra("IMEI", mImei.getText().toString());
                intent.putExtra("DATE_FROM", dateFrom.getText().toString());
                intent.putExtra("DATE_TO", dateTo.getText().toString());
                startActivity(intent);
//                IoBuffer buffer = CodecManager.getManager().findBaseInfoReqEncode((byte) 1, mImsi.getText().toString(), mImei.getText().toString(),
//                        dateFrom.getText().toString(), dateTo.getText().toString());
//                buffer.flip();
//                byte[] out = new byte[buffer.limit()];
//                buffer.get(out);
//                try {
//                    anInterface.sendRequest(out);
////                    DialogUtils.showProgressDialog(SearchBaseInfoActivity.this, "查询中，请稍后...");
////                    startLoadingTimer(searchSpcMessageId);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
////                    removeTimer(searchSpcMessageId);
////                    DialogUtils.dismissProgressDialog();
//                    ToastUtils.show(SearchBaseInfoActivity.this, "查询失败");
//                }
            }
        });
    }
}
