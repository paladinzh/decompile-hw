package com.android.common.widget;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;

public abstract class CompositeCursorAdapter extends BaseAdapter {
    private boolean mCacheValid;
    private final Context mContext;
    private int mCount;
    private boolean mNotificationNeeded;
    private boolean mNotificationsEnabled;
    private ArrayList<Partition> mPartitions;

    public static class Partition {
        int count;
        Cursor cursor;
        boolean hasHeader;
        int idColumnIndex;
        boolean showIfEmpty;

        public Partition(boolean showIfEmpty, boolean hasHeader) {
            this.showIfEmpty = showIfEmpty;
            this.hasHeader = hasHeader;
        }
    }

    protected abstract void bindView(View view, int i, Cursor cursor, int i2);

    protected abstract View newView(Context context, int i, Cursor cursor, int i2, ViewGroup viewGroup);

    public CompositeCursorAdapter(Context context) {
        this(context, 2);
    }

    public CompositeCursorAdapter(Context context, int initialCapacity) {
        this.mCount = 0;
        this.mCacheValid = true;
        this.mNotificationsEnabled = true;
        this.mContext = context;
        this.mPartitions = new ArrayList();
    }

    public Context getContext() {
        return this.mContext;
    }

    public void addPartition(boolean showIfEmpty, boolean hasHeader) {
        addPartition(new Partition(showIfEmpty, hasHeader));
    }

    public void addPartition(Partition partition) {
        this.mPartitions.add(partition);
        invalidate();
        notifyDataSetChanged();
    }

    public void removePartition(int partitionIndex) {
        Cursor cursor = ((Partition) this.mPartitions.get(partitionIndex)).cursor;
        if (!(cursor == null || cursor.isClosed())) {
            cursor.close();
        }
        this.mPartitions.remove(partitionIndex);
        invalidate();
        notifyDataSetChanged();
    }

    public void clearPartitions() {
        for (Partition partition : this.mPartitions) {
            partition.cursor = null;
        }
        invalidate();
        notifyDataSetChanged();
    }

    public void setHasHeader(int partitionIndex, boolean flag) {
        ((Partition) this.mPartitions.get(partitionIndex)).hasHeader = flag;
        invalidate();
    }

    public void setShowIfEmpty(int partitionIndex, boolean flag) {
        ((Partition) this.mPartitions.get(partitionIndex)).showIfEmpty = flag;
        invalidate();
    }

    public Partition getPartition(int partitionIndex) {
        return (Partition) this.mPartitions.get(partitionIndex);
    }

    protected void invalidate() {
        this.mCacheValid = false;
    }

    public int getPartitionCount() {
        return this.mPartitions.size();
    }

    protected void ensureCacheValid() {
        if (!this.mCacheValid) {
            this.mCount = 0;
            for (Partition partition : this.mPartitions) {
                int count;
                Cursor cursor = partition.cursor;
                if (cursor == null || cursor.isClosed()) {
                    count = 0;
                } else {
                    count = cursor.getCount();
                }
                if (partition.hasHeader && (count != 0 || partition.showIfEmpty)) {
                    count++;
                }
                partition.count = count;
                this.mCount += count;
            }
            this.mCacheValid = true;
        }
    }

    public boolean hasHeader(int partition) {
        return ((Partition) this.mPartitions.get(partition)).hasHeader;
    }

    public int getCount() {
        ensureCacheValid();
        return this.mCount;
    }

    public Cursor getCursor(int partition) {
        return ((Partition) this.mPartitions.get(partition)).cursor;
    }

    public void changeCursor(int partition, Cursor cursor) {
        Cursor prevCursor = ((Partition) this.mPartitions.get(partition)).cursor;
        if (prevCursor != cursor) {
            if (!(prevCursor == null || prevCursor.isClosed())) {
                prevCursor.close();
            }
            ((Partition) this.mPartitions.get(partition)).cursor = cursor;
            if (!(cursor == null || cursor.isClosed())) {
                ((Partition) this.mPartitions.get(partition)).idColumnIndex = cursor.getColumnIndex("_id");
            }
            invalidate();
            notifyDataSetChanged();
        }
    }

    public boolean isPartitionEmpty(int partition) {
        Cursor cursor = ((Partition) this.mPartitions.get(partition)).cursor;
        return cursor == null || cursor.isClosed() || cursor.getCount() == 0;
    }

    public int getPartitionForPosition(int position) {
        ensureCacheValid();
        int start = 0;
        int n = this.mPartitions.size();
        for (int i = 0; i < n; i++) {
            int end = start + ((Partition) this.mPartitions.get(i)).count;
            if (position >= start && position < end) {
                return i;
            }
            start = end;
        }
        return -1;
    }

    public int getOffsetInPartition(int position) {
        ensureCacheValid();
        int start = 0;
        for (Partition partition : this.mPartitions) {
            int end = start + partition.count;
            if (position < start || position >= end) {
                start = end;
            } else {
                int offset = position - start;
                if (partition.hasHeader) {
                    offset--;
                }
                return offset;
            }
        }
        return -1;
    }

    public int getPositionForPartition(int partition) {
        ensureCacheValid();
        int position = 0;
        for (int i = 0; i < partition; i++) {
            position += ((Partition) this.mPartitions.get(i)).count;
        }
        return position;
    }

    public int getViewTypeCount() {
        return getItemViewTypeCount() + 1;
    }

    public int getItemViewTypeCount() {
        return 1;
    }

    protected int getItemViewType(int partition, int position) {
        return 1;
    }

    public int getItemViewType(int position) {
        ensureCacheValid();
        int start = 0;
        int i = 0;
        int n = this.mPartitions.size();
        while (i < n) {
            int end = start + ((Partition) this.mPartitions.get(i)).count;
            if (position < start || position >= end) {
                start = end;
                i++;
            } else {
                int offset = position - start;
                if (((Partition) this.mPartitions.get(i)).hasHeader) {
                    offset--;
                }
                if (offset == -1) {
                    return -1;
                }
                return getItemViewType(i, offset);
            }
        }
        throw new ArrayIndexOutOfBoundsException(position);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ensureCacheValid();
        int start = 0;
        int i = 0;
        int n = this.mPartitions.size();
        while (i < n) {
            int end = start + ((Partition) this.mPartitions.get(i)).count;
            if (position < start || position >= end) {
                start = end;
                i++;
            } else {
                View view;
                int offset = position - start;
                if (((Partition) this.mPartitions.get(i)).hasHeader) {
                    offset--;
                }
                if (offset == -1) {
                    view = getHeaderView(i, ((Partition) this.mPartitions.get(i)).cursor, convertView, parent);
                } else if (((Partition) this.mPartitions.get(i)).cursor.moveToPosition(offset)) {
                    view = getView(i, ((Partition) this.mPartitions.get(i)).cursor, offset, convertView, parent);
                } else {
                    throw new IllegalStateException("Couldn't move cursor to position " + offset);
                }
                if (view != null) {
                    return view;
                }
                throw new NullPointerException("View should not be null, partition: " + i + " position: " + offset);
            }
        }
        throw new ArrayIndexOutOfBoundsException(position);
    }

    protected View getHeaderView(int partition, Cursor cursor, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = newHeaderView(this.mContext, partition, cursor, parent);
        }
        bindHeaderView(view, partition, cursor);
        return view;
    }

    protected View newHeaderView(Context context, int partition, Cursor cursor, ViewGroup parent) {
        return null;
    }

    protected void bindHeaderView(View view, int partition, Cursor cursor) {
    }

    protected View getView(int partition, Cursor cursor, int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = newView(this.mContext, partition, cursor, position, parent);
        }
        bindView(view, partition, cursor, position);
        return view;
    }

    public Object getItem(int position) {
        ensureCacheValid();
        int start = 0;
        for (Partition mPartition : this.mPartitions) {
            int end = start + mPartition.count;
            if (position < start || position >= end) {
                start = end;
            } else {
                int offset = position - start;
                if (mPartition.hasHeader) {
                    offset--;
                }
                if (offset == -1) {
                    return null;
                }
                Cursor cursor = mPartition.cursor;
                if (cursor == null || cursor.isClosed() || !cursor.moveToPosition(offset)) {
                    return null;
                }
                return cursor;
            }
        }
        return null;
    }

    public long getItemId(int position) {
        ensureCacheValid();
        int start = 0;
        for (Partition mPartition : this.mPartitions) {
            int end = start + mPartition.count;
            if (position < start || position >= end) {
                start = end;
            } else {
                int offset = position - start;
                if (mPartition.hasHeader) {
                    offset--;
                }
                if (offset == -1 || mPartition.idColumnIndex == -1) {
                    return 0;
                }
                Cursor cursor = mPartition.cursor;
                if (cursor == null || cursor.isClosed() || !cursor.moveToPosition(offset)) {
                    return 0;
                }
                return cursor.getLong(mPartition.idColumnIndex);
            }
        }
        return 0;
    }

    public boolean areAllItemsEnabled() {
        for (Partition mPartition : this.mPartitions) {
            if (mPartition.hasHeader) {
                return false;
            }
        }
        return true;
    }

    public boolean isEnabled(int position) {
        ensureCacheValid();
        int start = 0;
        int i = 0;
        int n = this.mPartitions.size();
        while (i < n) {
            int end = start + ((Partition) this.mPartitions.get(i)).count;
            if (position < start || position >= end) {
                start = end;
                i++;
            } else {
                int offset = position - start;
                if (((Partition) this.mPartitions.get(i)).hasHeader && offset == 0) {
                    return false;
                }
                return isEnabled(i, offset);
            }
        }
        return false;
    }

    protected boolean isEnabled(int partition, int position) {
        return true;
    }

    public void notifyDataSetChanged() {
        if (this.mNotificationsEnabled) {
            this.mNotificationNeeded = false;
            super.notifyDataSetChanged();
            return;
        }
        this.mNotificationNeeded = true;
    }
}
