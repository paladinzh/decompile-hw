package com.huawei.systemmanager.power.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import java.util.List;

public class ApplicationInfoAdapter extends BaseAdapter {
    LayoutInflater infater = null;
    private List<AppInfo> mlistAppInfo = null;

    static class ViewHolder {
        ImageView appIcon;
        TextView tvPkgName;

        public ViewHolder(View view) {
            this.appIcon = (ImageView) view.findViewById(R.id.appGridIcon);
            this.tvPkgName = (TextView) view.findViewById(R.id.appGridName);
        }
    }

    public ApplicationInfoAdapter(Context context, List<AppInfo> apps) {
        this.infater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mlistAppInfo = apps;
    }

    public int getCount() {
        return this.mlistAppInfo.size();
    }

    public Object getItem(int position) {
        return this.mlistAppInfo.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertview, ViewGroup arg2) {
        View view;
        ViewHolder holder;
        if (convertview == null || convertview.getTag() == null) {
            view = this.infater.inflate(R.layout.app_gridview_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertview;
            holder = (ViewHolder) convertview.getTag();
        }
        AppInfo appInfo = (AppInfo) getItem(position);
        holder.appIcon.setImageDrawable(appInfo.getAppIcon());
        holder.tvPkgName.setText(appInfo.getPackageName());
        return view;
    }
}
