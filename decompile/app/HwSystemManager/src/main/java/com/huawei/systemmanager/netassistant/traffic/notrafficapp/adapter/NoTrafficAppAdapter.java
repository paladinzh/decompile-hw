package com.huawei.systemmanager.netassistant.traffic.notrafficapp.adapter;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.netassistant.netapp.bean.NoTrafficAppInfo;

public class NoTrafficAppAdapter extends CommonAdapter<NoTrafficAppInfo> {
    private OnClickListener mOnClickListener;

    public static class ViewHolder {
        CheckBox checkBox;
        ImageView iconView;
        TextView nameTextView;
        TextView summaryTextView;
    }

    public NoTrafficAppAdapter(Activity ac, OnClickListener listener) {
        super(ac);
        this.mOnClickListener = listener;
    }

    public NoTrafficAppInfo getItem(int position) {
        return (NoTrafficAppInfo) super.getItem(position);
    }

    protected View newView(int position, ViewGroup parent, NoTrafficAppInfo item) {
        View convertView = this.mInflater.inflate(R.layout.no_traffic_app_list_item, null);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.iconView = (ImageView) convertView.findViewById(R.id.app_icon);
        viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.app_name);
        viewHolder.summaryTextView = (TextView) convertView.findViewById(R.id.summary);
        viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
        convertView.setTag(viewHolder);
        return convertView;
    }

    protected void bindView(int position, View convertView, NoTrafficAppInfo info) {
        if (info != null) {
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            if (viewHolder.iconView != null) {
                viewHolder.iconView.setImageDrawable(info.getIcon());
            }
            if (viewHolder.nameTextView != null) {
                viewHolder.nameTextView.setText(info.getAppLabel());
            }
            if (viewHolder.checkBox != null) {
                viewHolder.checkBox.setChecked(info.isChecked());
                viewHolder.checkBox.setOnClickListener(this.mOnClickListener);
                viewHolder.checkBox.setTag(Integer.valueOf(position));
            }
            if (viewHolder.summaryTextView != null) {
                if (info.isMultiApp()) {
                    viewHolder.summaryTextView.setVisibility(0);
                    viewHolder.summaryTextView.setText(R.string.net_assistant_more_application);
                } else {
                    viewHolder.summaryTextView.setVisibility(8);
                }
            }
        }
    }

    public boolean isAllChecked() {
        int count = 0;
        for (NoTrafficAppInfo noTrafficAppInfo : this.mList) {
            count += noTrafficAppInfo.isChecked() ? 1 : 0;
        }
        if (count == getCount()) {
            return true;
        }
        return false;
    }
}
