package com.android.contacts.hap.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.google.android.gms.R;

public class FavoriteGroupMergeAdapter extends BaseAdapter {
    private final Context mContext;
    private final FavoritesFrequentAdapter mFrequentAdapter;
    private final FavoritesStarredAdapter mStarredAdapter;

    private static class DividerViewHolder {
        TextView title;

        private DividerViewHolder() {
        }
    }

    public FavoriteGroupMergeAdapter(Context aContext, FavoritesStarredAdapter starredAdapter, FavoritesFrequentAdapter frequentAdapter) {
        this.mStarredAdapter = starredAdapter;
        this.mFrequentAdapter = frequentAdapter;
        this.mContext = aContext;
    }

    public int getCount() {
        return getFavoritesListItemCountWithHeader() + getFrequentListItemCountWithHeader();
    }

    private int getFavoritesListItemCountWithHeader() {
        int dataCount = this.mStarredAdapter.getCount();
        if (dataCount > 0) {
            return dataCount + 1;
        }
        return 0;
    }

    private int getFrequentListItemCountWithHeader() {
        int dataCount = this.mFrequentAdapter.getCount();
        if (dataCount > 0) {
            return dataCount + 1;
        }
        return 0;
    }

    public Object getItem(int aposition) {
        return null;
    }

    public long getItemId(int aPosition) {
        return (long) aPosition;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);
        if (viewType == 0) {
            return getDivider(position, convertView, parent);
        }
        if (viewType != 1) {
            return this.mFrequentAdapter.getView((position - getFavoritesListItemCountWithHeader()) - 1, convertView, parent);
        }
        if (this.mFrequentAdapter != null) {
            this.mStarredAdapter.setIfHasFrequentItem(this.mFrequentAdapter.getCount());
        }
        return this.mStarredAdapter.getView(position - 1, convertView, parent);
    }

    private View getDivider(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.list_separator, parent, false);
            DividerViewHolder viewHolder = new DividerViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(viewHolder);
        }
        DividerViewHolder holder = (DividerViewHolder) convertView.getTag();
        if (getFavoritesListItemCountWithHeader() <= 0 || position != 0) {
            holder.title.setText(this.mContext.getString(R.string.contact_favorites_frequent_label).toUpperCase());
        } else {
            holder.title.setText(this.mContext.getString(R.string.contacts_section_header_starred).toUpperCase());
        }
        return convertView;
    }

    public int getViewTypeCount() {
        return 3;
    }

    public int getItemViewType(int position) {
        int starredCount = getFavoritesListItemCountWithHeader();
        if (position == 0 || position == starredCount) {
            return 0;
        }
        if (position < starredCount) {
            return 1;
        }
        return 2;
    }
}
