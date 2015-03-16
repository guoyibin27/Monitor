package com.zte.monitor.app.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.zte.monitor.app.R;
import com.zte.monitor.app.database.dao.SensitiveWordDao;
import com.zte.monitor.app.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylar on 14/11/13.
 */
public class AddSensitiveWordActivity extends BaseActivity {
    private LinearLayout newSensitiveWordLayout;
    private LinearLayout addSensitiveWordLayout;
    private List<String> sensitiveWordList;
    private SensitiveWordDao sensitiveWordDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sensitive_word);

        setBorderIfActivityAsDialog();

        sensitiveWordList = new ArrayList<String>();
        sensitiveWordDao = new SensitiveWordDao(this);
        addSensitiveWordLayout = (LinearLayout) findViewById(R.id.add_sensitive_word_layout);
        newSensitiveWordLayout = (LinearLayout) findViewById(R.id.new_sensitive_layout);
        addSensitiveWordLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newSensitiveWordLayout.getChildCount() > 4) {
                    return;
                }
                final View newItemView = LayoutInflater.from(AddSensitiveWordActivity.this).inflate(R.layout.add_sensitive_word_item, null);
                newItemView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        newSensitiveWordLayout.removeView(newItemView);
                        newSensitiveWordLayout.invalidate();
                    }
                });
                EditText editText = (EditText) newItemView.findViewById(R.id.sensitive_word);
                editText.requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager) editText.getContext().getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(editText, InputMethodManager.RESULT_SHOWN);
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                newSensitiveWordLayout.addView(newItemView);
            }
        });

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int count = newSensitiveWordLayout.getChildCount();
                for (int i = 0; i < count; i++) {
                    View childView = newSensitiveWordLayout.getChildAt(i);
                    if (childView instanceof LinearLayout) {
                        View wordView = childView.findViewById(R.id.sensitive_word);
                        if (wordView instanceof EditText) {
                            EditText wordEditView = (EditText) wordView;
                            final String sensitiveWord = wordEditView.getText().toString();
                            if (!StringUtils.isBlank(sensitiveWord)) {
                                sensitiveWordList.add(sensitiveWord);
                            }
                        }
                    }
                }
                if (sensitiveWordList != null && sensitiveWordList.size() > 0) {
                    sensitiveWordDao.batchSave(sensitiveWordList);
                    setResult(RESULT_OK);
                } else {
                    setResult(RESULT_CANCELED);
                }
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}