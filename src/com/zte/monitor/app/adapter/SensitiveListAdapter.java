package com.zte.monitor.app.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.SystemConstants;

/**
 * Created by Sylar on 14/11/13.
 */
public class SensitiveListAdapter extends AbstractListViewAdapter<String> {
    private int mode;
    private Callback callback;

    public SensitiveListAdapter(Activity activity, int layoutResId, Callback callback) {
        super(activity, layoutResId);
        this.callback = callback;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    protected IViewHolder<String> getViewHolder() {
        return new SensitiveViewHolder();
    }

    private class SensitiveViewHolder implements IViewHolder<String> {
        private ImageView deleteButton;
        private TextView textView;

        @Override
        public void init(View view, int position) {
            deleteButton = (ImageView) view.findViewById(R.id.delete_button);
            textView = (TextView) view.findViewById(R.id.text_view);
        }

        @Override
        public void update(final String s, int position) {
            textView.setText(s);
            if (mode == SystemConstants.MODE.PREVIEW) {
                deleteButton.setVisibility(View.GONE);
                deleteButton.setOnClickListener(null);
            } else {
                deleteButton.setVisibility(View.VISIBLE);
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onDelete(s);
                    }
                });
            }
        }
    }

    public static interface Callback {
        public void onDelete(String s);
    }
}
