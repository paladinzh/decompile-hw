package com.huawei.systemmanager.netassistant.traffic.appdetail;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo.AppDetailInfo.BaseInfo;
import java.util.LinkedList;
import java.util.List;

public class AppDetailAdapter extends BaseAdapter {
    private OnClickListener mClickListener;
    private LayoutInflater mLayoutInflater;
    private List<BaseInfo> mList;

    public static class NormalHolder {
        public TextView title;
    }

    public static class SwitchHolder {
        public View divider;
        public TextView summary;
        public Switch sw;
        public TextView title;
    }

    public static class TitleHolder {
        public TextView title;
    }

    public AppDetailAdapter(Activity ac) {
        this.mList = new LinkedList();
        this.mLayoutInflater = ac.getLayoutInflater();
    }

    public AppDetailAdapter(Activity ac, OnClickListener l) {
        this(ac);
        this.mClickListener = l;
    }

    public int getCount() {
        return this.mList.size();
    }

    public BaseInfo getItem(int i) {
        return (BaseInfo) this.mList.get(i);
    }

    public long getItemId(int i) {
        return (long) i;
    }

    public int getItemViewType(int position) {
        BaseInfo info = getItem(position);
        return info == null ? 0 : info.getType();
    }

    public int getViewTypeCount() {
        return 3;
    }

    public void swapData(List<BaseInfo> list) {
        if (list != null) {
            this.mList.clear();
            this.mList.addAll(list);
            notifyDataSetChanged();
        }
    }

    public View getView(int pos, View converView, ViewGroup viewGroup) {
        int type = getItemViewType(pos);
        if (type == 0) {
            converView = initTitleView(converView, viewGroup);
            bindTitleView(pos, converView);
            return converView;
        } else if (type == 1) {
            converView = initSwitchView(converView, viewGroup);
            bindSwitchView(pos, converView);
            return converView;
        } else if (type != 2) {
            return converView;
        } else {
            converView = initNormalView(converView, viewGroup);
            bindNormalView(pos, converView);
            return converView;
        }
    }

    private View initTitleView(View convertView, ViewGroup viewGroup) {
        if (convertView != null) {
            return convertView;
        }
        convertView = this.mLayoutInflater.inflate(R.layout.permission_list_tab_item_tag, viewGroup, false);
        TitleHolder holder = new TitleHolder();
        holder.title = (TextView) convertView.findViewById(R.id.tvTagName);
        convertView.setTag(holder);
        return convertView;
    }

    private void bindTitleView(int pos, View convertView) {
        ((TitleHolder) convertView.getTag()).title.setText(getItem(pos).getTitle());
    }

    private View initSwitchView(View convertView, ViewGroup viewGroup) {
        if (convertView != null) {
            return convertView;
        }
        convertView = this.mLayoutInflater.inflate(R.layout.netassistant_app_detail_switch, null);
        SwitchHolder holder = new SwitchHolder();
        holder.title = (TextView) convertView.findViewById(R.id.title);
        holder.summary = (TextView) convertView.findViewById(R.id.summary);
        holder.sw = (Switch) convertView.findViewById(R.id.app_policy_switch);
        holder.divider = convertView.findViewById(R.id.divider);
        holder.sw.setOnClickListener(this.mClickListener);
        convertView.setTag(holder);
        return convertView;
    }

    private void bindSwitchView(int pos, View convertView) {
        BaseInfo info = getItem(pos);
        SwitchHolder holder = (SwitchHolder) convertView.getTag();
        holder.title.setText(info.getTitle());
        holder.summary.setText(info.getSubTitle());
        holder.sw.setChecked(info.isChecked());
        holder.sw.setEnabled(info.isEnable());
        holder.sw.setTag(info.getTask());
        if (pos + 1 >= getCount()) {
            return;
        }
        if (getItemViewType(pos + 1) == 0) {
            holder.divider.setVisibility(8);
        } else {
            holder.divider.setVisibility(0);
        }
    }

    private View initNormalView(View convertView, ViewGroup viewGroup) {
        if (convertView != null) {
            return convertView;
        }
        convertView = this.mLayoutInflater.inflate(R.layout.app_detail_list_view_item, viewGroup, false);
        NormalHolder holder = new NormalHolder();
        holder.title = (TextView) convertView.findViewById(R.id.content);
        convertView.setTag(holder);
        return convertView;
    }

    private void bindNormalView(int pos, View convertView) {
        ((NormalHolder) convertView.getTag()).title.setText(getItem(pos).getSubTitle());
    }
}
