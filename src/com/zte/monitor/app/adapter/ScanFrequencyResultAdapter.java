package com.zte.monitor.app.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.model.response.ScanFreqResponse;

/**
 * Created by Sylar on 14-10-14.
 */
public class ScanFrequencyResultAdapter extends AbstractListViewAdapter<ScanFreqResponse.Entry2> {
    public ScanFrequencyResultAdapter(Activity activity, int layoutResId) {
        super(activity, layoutResId);
    }

    @Override
    protected IViewHolder<ScanFreqResponse.Entry2> getViewHolder() {
        return new ViewHolder();
    }

    private class ViewHolder implements IViewHolder<ScanFreqResponse.Entry2> {
        private TextView c1;
        private TextView c2;
        private TextView rxlev;
        private TextView bsic;
        private TextView nCell;
        private TextView nLac;
        private TextView nCellId;
        private RadioButton checkButton;

        @Override
        public void init(View view, int position) {
            c1 = (TextView) view.findViewById(R.id.c1_text_view);
            c2 = (TextView) view.findViewById(R.id.c2_text_view);
            rxlev = (TextView) view.findViewById(R.id.rxlev_text_view);
            bsic = (TextView) view.findViewById(R.id.bsic_text_view);
            nCell = (TextView) view.findViewById(R.id.ncell_text_view);
            nLac = (TextView) view.findViewById(R.id.nlac_text_view);
            nCellId = (TextView) view.findViewById(R.id.ncell_id_text_view);
            checkButton = (RadioButton) view.findViewById(R.id.check_radio_button);
        }

        @Override
        public void update(ScanFreqResponse.Entry2 entry2, int position) {
            c1.setText(entry2.c1 + "");
            c2.setText(entry2.c2 + "");
            rxlev.setText(entry2.rxlev + "");
            bsic.setText(entry2.bsic + "");
            nCell.setText(entry2.nCell + "");
            nLac.setText(entry2.nLac + "");
            nCellId.setText(entry2.nCellId + "");
            checkButton.setChecked(entry2.isChecked);
        }
    }
}
