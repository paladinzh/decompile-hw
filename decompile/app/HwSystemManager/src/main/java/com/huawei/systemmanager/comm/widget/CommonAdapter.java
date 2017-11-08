package com.huawei.systemmanager.comm.widget;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class CommonAdapter<T> extends BaseAdapter {
    protected final Context mContex;
    protected final LayoutInflater mInflater;
    protected final List<T> mList = new ArrayList();

    protected abstract void bindView(int i, View view, T t);

    protected abstract View newView(int i, ViewGroup viewGroup, T t);

    public CommonAdapter(Context context) {
        this.mContex = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public CommonAdapter(Context context, LayoutInflater inflater) {
        this.mContex = context;
        if (inflater == null) {
            this.mInflater = LayoutInflater.from(this.mContex);
        } else {
            this.mInflater = inflater;
        }
    }

    public int getCount() {
        return this.mList.size();
    }

    public T getItem(int position) {
        return this.mList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        T item = this.mList.get(position);
        if (convertView == null) {
            convertView = newView(position, parent, item);
        }
        bindView(position, convertView, item);
        return convertView;
    }

    public boolean swapData(List<? extends T> list) {
        this.mList.clear();
        if (list != null) {
            this.mList.addAll(list);
        }
        notifyDataSetChanged();
        return true;
    }

    public boolean deleteItem(T item) {
        boolean res = this.mList.remove(item);
        if (res) {
            notifyDataSetChanged();
        }
        return res;
    }

    public void deleteItem(Collection<T> items) {
        this.mList.removeAll(items);
        notifyDataSetChanged();
    }

    public List<T> getData() {
        return Collections.unmodifiableList(this.mList);
    }

    public LayoutInflater getInflater() {
        return this.mInflater;
    }

    public Context getContext() {
        return this.mContex;
    }

    public Resources getResources() {
        return this.mContex.getResources();
    }

    public String getString(int resId) {
        return this.mContex.getString(resId);
    }

    public String getString(int resId, Object... formatArgs) {
        return this.mContex.getString(resId, formatArgs);
    }

    public void clear() {
        swapData(null);
    }
}
