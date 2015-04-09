package com.zte.monitor.app.activity.widget;

import android.content.Context;
import android.content.Intent;
import android.view.ActionProvider;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import com.zte.monitor.app.R;
import com.zte.monitor.app.activity.*;

/**
 * Created by Sylar on 14/11/13.
 */
public class SystemMenuActionProvider extends ActionProvider {
    private Context mContext;

    public SystemMenuActionProvider(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public View onCreateActionView() {
        return null;
    }

    @Override
    public boolean hasSubMenu() {
        return true;
    }

    @Override
    public void onPrepareSubMenu(SubMenu subMenu) {
        subMenu.clear();
        subMenu.add(0, 0, 0, R.string.action_server_config).setIcon(R.drawable.setting).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mContext.startActivity(new Intent(mContext, SettingsActivity.class));
                return true;
            }
        });
        subMenu.add(0, 1, 1, R.string.action_equipment_state).setIcon(R.drawable.equipment_state).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mContext.startActivity(new Intent(mContext, EquipmentStateActivity.class));
                return true;
            }
        });
        subMenu.add(0, 2, 2, R.string.action_sms_list).setIcon(R.drawable.sms_list).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mContext.startActivity(new Intent(mContext, SmsListActivity.class));
                return true;
            }
        });
        subMenu.add(0, 3, 3, R.string.action_play_pcm).setIcon(R.drawable.shishiyuyin).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mContext.startActivity(new Intent(mContext, PlayPcmActivity.class));
                return true;
            }
        });
        subMenu.add(0, 4, 4, R.string.action_sensitive_word).setIcon(R.drawable.add_mingan).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mContext.startActivity(new Intent(mContext, SensitiveWordActivity.class));
                return true;
            }
        });
        subMenu.add(0, 5, 5, R.string.action_sensitive_number).setIcon(R.drawable.add_mingan_haoma).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mContext.startActivity(new Intent(mContext, SensitivePhoneNumberActivity.class));
                return true;
            }
        });
        subMenu.add(0, 6, 6, R.string.action_sms_group_send).setIcon(R.drawable.send_message).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mContext.startActivity(new Intent(mContext, GroupSmsSendActivity.class));
                return true;
            }
        });

    }
}
