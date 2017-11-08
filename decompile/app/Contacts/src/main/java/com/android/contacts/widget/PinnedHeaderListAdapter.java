package com.android.contacts.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.android.common.widget.CompositeCursorAdapter;
import com.android.contacts.widget.PinnedHeaderListView.PinnedHeaderAdapter;

public abstract class PinnedHeaderListAdapter extends CompositeCursorAdapter implements PinnedHeaderAdapter {
    private boolean[] mHeaderVisibility;
    private boolean mPinnedPartitionHeadersEnabled;

    public PinnedHeaderListAdapter(Context context) {
        super(context);
    }

    public void setPinnedPartitionHeadersEnabled(boolean flag) {
        this.mPinnedPartitionHeadersEnabled = flag;
    }

    public int getPinnedHeaderCount() {
        if (this.mPinnedPartitionHeadersEnabled) {
            return getPartitionCount();
        }
        return 0;
    }

    protected boolean isPinnedPartitionHeaderVisible(int partition) {
        if (this.mPinnedPartitionHeadersEnabled && hasHeader(partition) && !isPartitionEmpty(partition)) {
            return true;
        }
        return false;
    }

    public View getPinnedHeaderView(int partition, View convertView, ViewGroup parent) {
        if (!hasHeader(partition)) {
            return null;
        }
        View view = null;
        if (convertView != null) {
            Integer headerType = (Integer) convertView.getTag();
            if (headerType != null && headerType.intValue() == 0) {
                view = convertView;
            }
        }
        if (view == null) {
            view = newHeaderView(getContext(), partition, null, parent);
            if (view == null) {
                return null;
            }
            view.setTag(Integer.valueOf(0));
            view.setFocusable(false);
            view.setEnabled(false);
        }
        bindHeaderView(view, partition, getCursor(partition));
        return view;
    }

    public void configurePinnedHeaders(PinnedHeaderListView listView) {
        if (this.mPinnedPartitionHeadersEnabled) {
            int i;
            int size = getPartitionCount();
            if (this.mHeaderVisibility == null || this.mHeaderVisibility.length != size) {
                this.mHeaderVisibility = new boolean[size];
            }
            for (i = 0; i < size; i++) {
                boolean visible = isPinnedPartitionHeaderVisible(i);
                this.mHeaderVisibility[i] = visible;
                if (!visible) {
                    listView.setHeaderInvisible(i, true);
                }
            }
            int headerViewsCount = listView.getHeaderViewsCount();
            int maxTopHeader = -1;
            int topHeaderHeight = 0;
            for (i = 0; i < size; i++) {
                if (this.mHeaderVisibility[i]) {
                    if (i > getPartitionForPosition(listView.getPositionAt(topHeaderHeight) - headerViewsCount)) {
                        break;
                    }
                    listView.setHeaderPinnedAtTop(i, topHeaderHeight, false);
                    topHeaderHeight += listView.getPinnedHeaderHeight(i);
                    maxTopHeader = i;
                }
            }
            int maxBottomHeader = size;
            int bottomHeaderHeight = 0;
            int listHeight = listView.getHeight();
            i = size;
            while (true) {
                i--;
                if (i <= maxTopHeader) {
                    break;
                } else if (this.mHeaderVisibility[i]) {
                    int position = listView.getPositionAt(listHeight - bottomHeaderHeight) - headerViewsCount;
                    if (position >= 0) {
                        int partition = getPartitionForPosition(position - 1);
                        if (partition == -1 || i <= partition) {
                            break;
                        }
                        bottomHeaderHeight += listView.getPinnedHeaderHeight(i);
                        listView.setHeaderPinnedAtBottom(i, listHeight - bottomHeaderHeight, position < getPositionForPartition(i));
                        maxBottomHeader = i;
                    } else {
                        break;
                    }
                }
            }
            for (i = maxTopHeader + 1; i < maxBottomHeader; i++) {
                if (this.mHeaderVisibility[i]) {
                    listView.setHeaderInvisible(i, isPartitionEmpty(i));
                }
            }
        }
    }

    public int getScrollPositionForHeader(int viewIndex) {
        return getPositionForPartition(viewIndex);
    }
}
