package com.zte.monitor.app.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.zte.monitor.app.R;
import com.zte.monitor.app.model.KeyValuePair;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Sylar on 15/4/9.
 */
public class SearchCallInfoActivity extends BaseActivity implements View.OnClickListener {

    private EditText imsiEditText;
    private Spinner callTypeSpinner;
    private EditText dateFromEditText;
    private EditText dateToEditText;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_call_info);
        setBorderIfActivityAsDialog();

        initViews();
    }

    private void initViews() {
        imsiEditText = (EditText) findViewById(R.id.search_sms_imsi);
        callTypeSpinner = (Spinner) findViewById(R.id.search_call_type);
        dateFromEditText = (EditText) findViewById(R.id.search_date_from);
        dateToEditText = (EditText) findViewById(R.id.search_date_to);
        searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(this);
        initDatePicker();
        fillSpinner();
    }

    private void initDatePicker() {
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int monthOfYear = calendar.get(Calendar.MONTH);
        final int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        dateFromEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    DatePickerDialog dpd = new DatePickerDialog(SearchCallInfoActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                            dateFromEditText.setText(i + "-" + month + "-" + day);
                        }
                    }, year, monthOfYear, dayOfMonth);
                    dpd.show();
                }
                return true;
            }
        });

        dateToEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    DatePickerDialog dpd = new DatePickerDialog(SearchCallInfoActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                            dateToEditText.setText(i + "-" + month + "-" + day);
                        }
                    }, year, monthOfYear, dayOfMonth);
                    dpd.show();
                }
                return true;
            }
        });
    }


    private void fillSpinner() {
        List<KeyValuePair> smsType = new ArrayList<KeyValuePair>(2);
        KeyValuePair pair = new KeyValuePair();
        pair.key = "1";
        pair.value = "主叫";
        smsType.add(pair);

        KeyValuePair rec = new KeyValuePair();
        rec.key = "2";
        rec.value = "被叫";
        smsType.add(rec);

        ArrayAdapter smsCenterAdapter = new ArrayAdapter<KeyValuePair>(this, android.R.layout.simple_spinner_dropdown_item, smsType);
        callTypeSpinner.setAdapter(smsCenterAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_button:
                Intent intent = new Intent(SearchCallInfoActivity.this, CallInfoListActivity.class);
                intent.putExtra("IMSI", imsiEditText.getText().toString());
                intent.putExtra("CallType", ((KeyValuePair) callTypeSpinner.getSelectedItem()).key);
                intent.putExtra("DATE_FROM", dateFromEditText.getText().toString());
                intent.putExtra("DATE_TO", dateToEditText.getText().toString());
                startActivity(intent);
                break;
        }
    }
}
