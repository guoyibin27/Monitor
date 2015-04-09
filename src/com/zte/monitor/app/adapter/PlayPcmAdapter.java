package com.zte.monitor.app.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.model.MonitorLineModel;

/**
 * Created by Sylar on 14-9-19.
 */
public class PlayPcmAdapter extends AbstractListViewAdapter<MonitorLineModel> {
    public PlayPcmAdapter(Activity activity, int layoutResId) {
        super(activity, layoutResId);
    }

    @Override
    protected IViewHolder<MonitorLineModel> getViewHolder() {
        return new ViewHolder();
    }

    private static final class ViewHolder implements IViewHolder<MonitorLineModel> {
        private TextView networkSystem;
        private TextView carrier;
        private LinearLayout carrierLayout;
        private TextView imsi;
        private CheckBox radioButton;

        @Override
        public void init(View view, int position) {
            networkSystem = (TextView) view.findViewById(R.id.network_system);
            carrier = (TextView) view.findViewById(R.id.carrier_label);
            carrierLayout = (LinearLayout) view.findViewById(R.id.carrier_layout);
            imsi = (TextView) view.findViewById(R.id.tv_imsi);
            radioButton = (CheckBox) view.findViewById(R.id.radio_button);
        }

        @Override
        public void update(MonitorLineModel monitorLineModel, int position) {
            networkSystem.setText(monitorLineModel.networkSystems);
            if (monitorLineModel.networkSystems.equals(SystemConstants.NETWORK_SYSTEM.GSM)) {
                carrierLayout.setVisibility(View.VISIBLE);
                carrier.setText(monitorLineModel.carrier);
            } else {
                carrierLayout.setVisibility(View.GONE);
                carrier.setText("");
            }
            imsi.setText(monitorLineModel.imsi);
            radioButton.setChecked(monitorLineModel.isChecked);
        }
    }
}
