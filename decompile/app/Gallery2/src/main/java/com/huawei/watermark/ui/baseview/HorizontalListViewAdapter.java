package com.huawei.watermark.ui.baseview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class HorizontalListViewAdapter extends BaseAdapter {
    public Context mContext;
    public LayoutInflater mInflater;
    public int selectIndex = -1;

    public HorizontalListViewAdapter(Context context) {
        this.mContext = context;
        this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
    }

    public int getCount() {
        return 0;
    }

    public Object getItem(int position) {
        return Integer.valueOf(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return convertView;
    }

    public void setSelectIndex(int i) {
        this.selectIndex = i;
    }
}
