package com.zte.monitor.app.activity.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.zte.monitor.app.R;
import com.zte.monitor.app.SystemConstants;
import com.zte.monitor.app.activity.UserDetailActivity;
import com.zte.monitor.app.activity.widget.PullToRefreshView;
import com.zte.monitor.app.adapter.UserListAdapter;
import com.zte.monitor.app.database.dao.UserDao;
import com.zte.monitor.app.handler.MessageResponseHandler;
import com.zte.monitor.app.model.UserModel;

import java.util.List;

/**
 * Created by Sylar on 8/25/14.
 */
public class UnknownUserListFragment extends Fragment implements PullToRefreshView.OnFooterRefreshListener, PullToRefreshView.OnHeaderRefreshListener {
    private final static int PAGE_SIZE = 20;
    private static final String[] userProperties = new String[]{SystemConstants.USER_PROPERTY.UNKNOWN_LIST};
    private static final int LOADING_CURRENT_PAGE_DATA = 0;
    private static final int LOADING_PREVIOUS_PAGE_DATA = 1;
    private static final int LOADING_NEXT_PAGE_DATA = 2;

    private UserListAdapter mAdapter;
    private PullToRefreshView pullToRefreshView;
    private UserDao userDao;
    private long pageIndex = 0;
    private long totalCount;

    public static UnknownUserListFragment newInstance(Bundle args) {
        UnknownUserListFragment fragment = new UnknownUserListFragment();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        userDao = new UserDao(getActivity());
        View rootView = inflater.inflate(R.layout.fragment_unknow_user_list, container, false);
        ListView mListView = (ListView) rootView.findViewById(R.id.list_view);
        pullToRefreshView = (PullToRefreshView) rootView.findViewById(R.id.main_pull_refresh_view);
        pullToRefreshView.setOnHeaderRefreshListener(this);
        pullToRefreshView.setOnFooterRefreshListener(this);

        mAdapter = new UserListAdapter(getActivity(), R.layout.user_list_item);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                UserModel user = (UserModel) adapterView.getItemAtPosition(i);
                user.isStatusChanged = 0;
                userDao.updateUserStatusChanged(user);
                Intent intent = new Intent(getActivity(), UserDetailActivity.class);
                intent.putExtra(UserDetailActivity.DATA, user);
                startActivity(intent);
            }
        });

        IntentFilter intentFilter = new IntentFilter(MessageResponseHandler.USER_INFO);
        getActivity().registerReceiver(receiver, intentFilter);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(LOADING_CURRENT_PAGE_DATA);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void loadData(int flag) {
        long userTotalCount = userDao.getUserCount(userProperties);
        totalCount = ((userTotalCount - 1) / PAGE_SIZE) + 1;
        mAdapter.getData().clear();
        List<UserModel> userModelList = userDao.getUserListByProperty(SystemConstants.USER_PROPERTY.UNKNOWN_LIST, pageIndex, PAGE_SIZE);
        if (userModelList != null && userModelList.size() > 0) {
            mAdapter.getData().addAll(userModelList);
        }
        mAdapter.notifyDataSetChanged();
        if (flag == LOADING_PREVIOUS_PAGE_DATA) {
            pullToRefreshView.onHeaderRefreshComplete();
        } else if (flag == LOADING_NEXT_PAGE_DATA) {
            pullToRefreshView.onFooterRefreshComplete();
        }
    }

    private boolean hasNextPage() {
        if (pageIndex >= totalCount) {
            pageIndex = totalCount - 1;
            return false;
        }
        return true;
    }

    private boolean hasPreviousPage() {
        if (pageIndex <= 0) {
            pageIndex = 0;
            return false;
        }
        return true;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(MessageResponseHandler.USER_INFO)) {
                loadData(LOADING_CURRENT_PAGE_DATA);
            }
        }
    };

    @Override
    public void onFooterRefresh(PullToRefreshView view) {
        pageIndex++;
        if (hasNextPage()) {
            loadData(LOADING_NEXT_PAGE_DATA);
        }else{
            pullToRefreshView.onFooterRefreshComplete();
        }
    }

    @Override
    public void onHeaderRefresh(PullToRefreshView view) {
        pageIndex--;
        if (hasPreviousPage()) {
            loadData(LOADING_PREVIOUS_PAGE_DATA);
        } else {
            pullToRefreshView.onHeaderRefreshComplete();
        }
    }
}
