package com.android.gallery3d.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.android.gallery3d.common.Utils;
import java.util.ArrayList;
import java.util.List;

public class SharedItemAdapter extends BaseAdapter {
    private List<Integer> mIndexHolder = new ArrayList(2);
    private ViewProvider mViewProvider;

    public interface ViewProvider {
        View getItemView(int i, View view, ViewGroup viewGroup);
    }

    public SharedItemAdapter(ViewProvider viewProvider) {
        Utils.assertTrue(viewProvider != null);
        this.mViewProvider = viewProvider;
    }

    public void clear() {
        this.mIndexHolder.clear();
        notifyDataSetChanged();
    }

    public void addIndex(int index) {
        this.mIndexHolder.add(Integer.valueOf(index));
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.mIndexHolder.size();
    }

    public Object getItem(int position) {
        return this.mIndexHolder.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public int getItemViewType(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (position > this.mIndexHolder.size()) {
            return null;
        }
        return this.mViewProvider.getItemView(((Integer) this.mIndexHolder.get(position)).intValue(), convertView, parent);
    }

    public int getViewTypeCount() {
        return 1;
    }

    public boolean hasStableIds() {
        return false;
    }

    public boolean isEmpty() {
        return false;
    }
}
