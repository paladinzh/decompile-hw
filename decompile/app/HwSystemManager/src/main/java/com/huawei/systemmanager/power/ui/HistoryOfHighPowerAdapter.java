package com.huawei.systemmanager.power.ui;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import java.util.Date;

public class HistoryOfHighPowerAdapter extends CommonAdapter<HistoryAppInfo> {

    private static class ViewHolder4History {
        ImageView mIcon;
        TextView mIsShareUidApp;
        TextView mLabel;
        TextView mTime;

        private ViewHolder4History() {
        }
    }

    public HistoryOfHighPowerAdapter(Context ctx) {
        super(ctx);
    }

    protected View newView(int position, ViewGroup parent, HistoryAppInfo item) {
        View convertView = this.mInflater.inflate(R.layout.history_high_power_adapter, null);
        ViewHolder4History holder = new ViewHolder4History();
        holder.mIcon = (ImageView) convertView.findViewById(R.id.history_high_power_app_icon);
        holder.mLabel = (TextView) convertView.findViewById(R.id.history_high_power_app_name);
        holder.mTime = (TextView) convertView.findViewById(R.id.history_high_power_app_time);
        holder.mIsShareUidApp = (TextView) convertView.findViewById(R.id.isShareUidApp);
        convertView.setTag(holder);
        return convertView;
    }

    protected void bindView(int position, View view, HistoryAppInfo item) {
        ViewHolder4History holder = (ViewHolder4History) view.getTag();
        String timeFormat = displayTime(item.getmTime().longValue());
        holder.mIcon.setImageDrawable(item.getmIcon());
        holder.mLabel.setText(item.getmLabel());
        holder.mTime.setText(timeFormat);
        if (item.isShareUidApps()) {
            holder.mIsShareUidApp.setVisibility(0);
        } else {
            holder.mIsShareUidApp.setVisibility(4);
        }
    }

    private String displayTime(long timestamp) {
        String str = "";
        Date d = new Date(timestamp);
        return DateUtils.isToday(timestamp) ? DateFormat.getTimeFormat(getContext()).format(d) : DateFormat.getDateFormat(getContext()).format(d);
    }
}
