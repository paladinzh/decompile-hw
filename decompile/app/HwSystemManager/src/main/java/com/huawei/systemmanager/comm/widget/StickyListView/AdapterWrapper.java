package com.huawei.systemmanager.comm.widget.StickyListView;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import java.util.LinkedList;
import java.util.List;

class AdapterWrapper extends BaseAdapter implements StickyListHeadersAdapter {
    private final Context mContext;
    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        public void onInvalidated() {
            AdapterWrapper.this.mHeaderCache.clear();
            super.notifyDataSetInvalidated();
        }

        public void onChanged() {
            super.notifyDataSetChanged();
        }
    };
    final StickyListHeadersAdapter mDelegate;
    private Drawable mDivider;
    private int mDividerHeight;
    private final List<View> mHeaderCache = new LinkedList();
    private OnHeaderClickListener mOnHeaderClickListener;

    public interface OnHeaderClickListener {
        void onHeaderClick(View view, int i, long j);
    }

    AdapterWrapper(Context context, StickyListHeadersAdapter delegate) {
        this.mContext = context;
        this.mDelegate = delegate;
        delegate.registerDataSetObserver(this.mDataSetObserver);
    }

    void setDivider(Drawable divider) {
        this.mDivider = divider;
    }

    void setDividerHeight(int dividerHeight) {
        this.mDividerHeight = dividerHeight;
    }

    public boolean areAllItemsEnabled() {
        return this.mDelegate.areAllItemsEnabled();
    }

    public boolean isEnabled(int position) {
        return this.mDelegate.isEnabled(position);
    }

    public int getCount() {
        return this.mDelegate.getCount();
    }

    public Object getItem(int position) {
        return this.mDelegate.getItem(position);
    }

    public long getItemId(int position) {
        return this.mDelegate.getItemId(position);
    }

    public boolean hasStableIds() {
        return this.mDelegate.hasStableIds();
    }

    public int getItemViewType(int position) {
        return this.mDelegate.getItemViewType(position);
    }

    public int getViewTypeCount() {
        return this.mDelegate.getViewTypeCount();
    }

    public boolean isEmpty() {
        return this.mDelegate.isEmpty();
    }

    private void recycleHeaderIfExists(WrapperView wv) {
        View header = wv.mHeader;
        if (header != null) {
            this.mHeaderCache.add(header);
        }
    }

    private View configureHeader(WrapperView wv, final int position) {
        View header = this.mDelegate.getHeaderView(position, wv.mHeader == null ? popHeader() : wv.mHeader, wv);
        if (header == null) {
            throw new NullPointerException("Header view must not be null.");
        }
        header.setClickable(true);
        header.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (AdapterWrapper.this.mOnHeaderClickListener != null) {
                    AdapterWrapper.this.mOnHeaderClickListener.onHeaderClick(v, position, AdapterWrapper.this.mDelegate.getHeaderId(position));
                }
            }
        });
        return header;
    }

    private View popHeader() {
        if (this.mHeaderCache.size() > 0) {
            return (View) this.mHeaderCache.remove(0);
        }
        return null;
    }

    private boolean previousPositionHasSameHeader(int position) {
        if (position == 0 || this.mDelegate.getHeaderId(position) != this.mDelegate.getHeaderId(position - 1)) {
            return false;
        }
        return true;
    }

    public WrapperView getView(int position, View convertView, ViewGroup parent) {
        WrapperView wv = convertView == null ? new WrapperView(this.mContext) : (WrapperView) convertView;
        View item = this.mDelegate.getView(position, wv.mItem, wv);
        View header = null;
        if (previousPositionHasSameHeader(position)) {
            recycleHeaderIfExists(wv);
        } else {
            header = configureHeader(wv, position);
        }
        if ((item instanceof Checkable) && !(wv instanceof CheckableWrapperView)) {
            wv = new CheckableWrapperView(this.mContext);
        } else if (!(item instanceof Checkable) && (wv instanceof CheckableWrapperView)) {
            wv = new WrapperView(this.mContext);
        }
        wv.update(item, header, this.mDivider, this.mDividerHeight);
        return wv;
    }

    public void setOnHeaderClickListener(OnHeaderClickListener onHeaderClickListener) {
        this.mOnHeaderClickListener = onHeaderClickListener;
    }

    public boolean equals(Object o) {
        return this.mDelegate.equals(o);
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return ((BaseAdapter) this.mDelegate).getDropDownView(position, convertView, parent);
    }

    public int hashCode() {
        return this.mDelegate.hashCode();
    }

    public void notifyDataSetChanged() {
        ((BaseAdapter) this.mDelegate).notifyDataSetChanged();
    }

    public void notifyDataSetInvalidated() {
        ((BaseAdapter) this.mDelegate).notifyDataSetInvalidated();
    }

    public String toString() {
        return this.mDelegate.toString();
    }

    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        return this.mDelegate.getHeaderView(position, convertView, parent);
    }

    public long getHeaderId(int position) {
        return this.mDelegate.getHeaderId(position);
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        this.mDelegate.registerDataSetObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        this.mDelegate.unregisterDataSetObserver(observer);
    }
}
