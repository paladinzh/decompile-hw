package com.huawei.systemmanager.addviewmonitor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import java.util.ArrayList;
import java.util.List;

public class AddViewMonitorAdapter extends BaseAdapter {
    private static final String TAG = "AddViewMonitorAdapter";
    private List<AddViewAppInfo> mDataList = new ArrayList();
    private LayoutInflater mInflater;

    public AddViewMonitorAdapter(Context context) {
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
    }

    public void setData(List<AddViewAppInfo> newItems) {
        this.mDataList.clear();
        this.mDataList.addAll(newItems);
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.mDataList.size();
    }

    public Object getItem(int position) {
        return this.mDataList.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.addview_app_fragment_item, null);
            AddViewHolder holder = new AddViewHolder();
            holder.mIcon = (ImageView) convertView.findViewById(R.id.addview_app_item_icon);
            holder.mLabel = (TextView) convertView.findViewById(R.id.addview_app_item_title);
            holder.mStatus = (TextView) convertView.findViewById(R.id.addview_app_item_sub_title);
            holder.mSwitch = (Switch) convertView.findViewById(R.id.addview_app_item_button);
            convertView.setTag(holder);
        }
        if (this.mDataList == null || this.mDataList.size() == 0) {
            HwLog.w(TAG, "the data list is empty.");
            return convertView;
        }
        int subTextId;
        holder = (AddViewHolder) convertView.getTag();
        AddViewAppInfo item = (AddViewAppInfo) getItem(position);
        holder.mIcon.setImageDrawable(HsmPackageManager.getInstance().getIcon(item.mPkgName));
        holder.mLabel.setText(item.mLabel);
        holder.mSwitch.setChecked(item.mAddViewAllow);
        holder.mSwitch.setImportantForAccessibility(2);
        if (item.mAddViewAllow) {
            subTextId = R.string.addview_allow_explain;
        } else {
            subTextId = R.string.addview_refuse_explain;
        }
        holder.mStatus.setText(subTextId);
        return convertView;
    }
}
