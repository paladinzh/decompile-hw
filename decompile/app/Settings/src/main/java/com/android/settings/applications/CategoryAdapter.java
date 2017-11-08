package com.android.settings.applications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import java.util.Iterator;
import java.util.List;

public class CategoryAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<Category> mListData;

    private static class ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView appSummary;
        Switch appSwitch;

        private ViewHolder() {
        }
    }

    public CategoryAdapter(Context context, List<Category> pData) {
        this.mContext = context;
        this.mListData = pData;
        this.mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        int count = 0;
        if (this.mListData == null || this.mListData.size() != 1) {
            if (this.mListData != null) {
                for (Category category : this.mListData) {
                    count += category.getItemCount();
                }
            }
            return count;
        }
        for (Category category2 : this.mListData) {
            count = category2.getOneTypeItemCount();
        }
        return count;
    }

    public Object getItem(int position) {
        Iterator category$iterator;
        if (this.mListData != null && this.mListData.size() == 1) {
            category$iterator = this.mListData.iterator();
            if (category$iterator.hasNext()) {
                return ((Category) category$iterator.next()).getOneTypeItem(position);
            }
        }
        if (this.mListData == null || position < 0 || position > getCount()) {
            return null;
        }
        int categroyFirstIndex = 0;
        for (Category category : this.mListData) {
            int size = category.getItemCount();
            int categoryIndex = position - categroyFirstIndex;
            if (categoryIndex < size) {
                return category.getItem(categoryIndex);
            }
            categroyFirstIndex += size;
        }
        return null;
    }

    public int getItemViewType(int position) {
        if ((this.mListData != null && this.mListData.size() == 1) || this.mListData == null || position < 0 || position > getCount()) {
            return 1;
        }
        int categroyFirstIndex = 0;
        for (Category category : this.mListData) {
            int size = category.getItemCount();
            if (position - categroyFirstIndex == 0) {
                return 0;
            }
            categroyFirstIndex += size;
        }
        return 1;
    }

    public int getViewTypeCount() {
        return 2;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)) {
            case 0:
                if (convertView == null) {
                    convertView = this.mInflater.inflate(2130968677, null);
                }
                ((TextView) convertView.findViewById(16908310)).setText((String) getItem(position));
                break;
            case 1:
                ViewHolder viewHolder;
                if (convertView == null) {
                    convertView = this.mInflater.inflate(2130968668, null);
                    viewHolder = new ViewHolder();
                    viewHolder.appIcon = (ImageView) convertView.findViewById(2131886348);
                    viewHolder.appName = (TextView) convertView.findViewById(2131886349);
                    viewHolder.appSummary = (TextView) convertView.findViewById(2131886350);
                    viewHolder.appSwitch = (Switch) convertView.findViewById(2131886351);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                AppInfo appInfo = (AppInfo) getItem(position);
                viewHolder.appName.setText(appInfo.getmLabel());
                viewHolder.appIcon.setImageDrawable(appInfo.getmIcon());
                if (appInfo.getChecked()) {
                    appInfo.setSummary(this.mContext.getResources().getString(2131628559));
                } else {
                    appInfo.setSummary(this.mContext.getResources().getString(2131628558));
                }
                viewHolder.appSummary.setText(appInfo.getSummary());
                viewHolder.appSwitch.setChecked(appInfo.getChecked());
                break;
        }
        return convertView;
    }

    public boolean areAllItemsEnabled() {
        return false;
    }

    public boolean isEnabled(int position) {
        return getItemViewType(position) != 0;
    }
}
