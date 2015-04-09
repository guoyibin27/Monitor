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
 * Created by Sylar on 14-9-5.
 */
public class SearchActionProvider extends ActionProvider {
    private Context mContext;

    public SearchActionProvider(Context context) {
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
        subMenu.add(0, 0, 0, "上报用户搜索").setIcon(R.drawable.icon_user).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mContext.startActivity(new Intent(mContext, SearchUserActivity.class));
                return true;
            }
        });

        subMenu.add(0, 1, 1, "上报短信搜索").setIcon(R.drawable.message).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mContext.startActivity(new Intent(mContext, SearchSmsActivity.class));
                return true;
            }
        });

        subMenu.add(0, 2, 2, "用户基本信息查询").setIcon(R.drawable.icon_user).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mContext.startActivity(new Intent(mContext, SearchBaseInfoActivity.class));
                return true;
            }
        });

        subMenu.add(0, 3, 3, "特殊用户信息查询").setIcon(R.drawable.icon_user).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mContext.startActivity(new Intent(mContext, SearchSpcInfoActivity.class));
                return true;
            }
        });

        subMenu.add(0, 4, 4, "短信查询").setIcon(R.drawable.icon_user).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mContext.startActivity(new Intent(mContext, SearchSmsFromDBActivity.class));
                return true;
            }
        });

        subMenu.add(0, 5, 5, "呼叫信息查询").setIcon(R.drawable.icon_user).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mContext.startActivity(new Intent(mContext, SearchCallInfoActivity.class));
                return true;
            }
        });
    }

}
