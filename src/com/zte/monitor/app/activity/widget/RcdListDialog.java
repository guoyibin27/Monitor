package com.zte.monitor.app.activity.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.model.RcdFileModel;

import java.util.List;

/**
 * Created by Sylar on 14-9-17.
 */
public class RcdListDialog extends AlertDialog {
    private Context mContext;
    private ArrayAdapter mAdapter;

    public RcdListDialog(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    private void init() {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(mContext).inflate(R.layout.rcd_list_diaolog_view, null);
        ListView listView = (ListView) view.findViewById(R.id.file_name_list_view);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//        mAdapter = new RcdListAdapter((Activity) mContext, R.layout.rcd_list_dialog_item);
        mAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_single_choice);
        listView.setAdapter(mAdapter);
        Button play = (Button) view.findViewById(R.id.play);
        Button stop = (Button) view.findViewById(R.id.stop);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setView(view);
//        addContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }


    public void fillData(List<String> rcdFileModelList) {
        mAdapter.addAll(rcdFileModelList);
        mAdapter.notifyDataSetInvalidated();
    }

}
