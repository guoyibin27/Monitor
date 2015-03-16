package com.zte.monitor.app.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylar on 8/25/14.
 */
public abstract class AbstractListViewAdapter<T> extends BaseAdapter {

    protected Activity mActivity;
    private List<T> data;
    private int mLayoutResId;

    public AbstractListViewAdapter(Activity activity, int layoutResId) {
        this.mActivity = activity;
        this.mLayoutResId = layoutResId;
        data = new ArrayList<T>();
    }

    public List<T> getData() {
        return data;
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public T getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        IViewHolder<T> holder;
        if (view == null) {
            holder = getViewHolder();
            view = LayoutInflater.from(mActivity).inflate(mLayoutResId, null);
            holder.init(view, i);
            view.setTag(holder);
        } else {
            holder = (IViewHolder<T>) view.getTag();
        }
        holder.update(getItem(i), i);
        return view;
    }

    protected abstract IViewHolder<T> getViewHolder();

    public static interface IViewHolder<T> {
        public void init(View view, int position);

        public void update(T t, int position);
    }
}
